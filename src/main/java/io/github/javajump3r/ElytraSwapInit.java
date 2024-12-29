package io.github.javajump3r;

import com.google.gson.Gson;
import me.lunaluna.fabric.elytrarecast.Startup;
import me.lunaluna.fabric.elytrarecast.config.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.*;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class ElytraSwapInit implements ClientModInitializer {
    public static boolean enabled = true;

    public static void tryWearChestplate(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return;
        }

        if ( isSlotChestplate(client, 38)) {
            return;
        }

        var chestplateSlots = getChestplateSlots();

        chestplateSlots = chestplateSlots
                .stream()
                .filter(slot->(getChestplateStat(client.player.getInventory().getStack(slot))>0f))
                .sorted(
                        Comparator.comparingInt(
                                        slot -> getChestplateStat(
                                                        client.player.getInventory().getStack(slot)
                                                )
                                )
                ).collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(chestplateSlots);

        if(FabricLoader.getInstance().isModLoaded("elytra-recast")){
            try {
            //nah i'm not gonna add clothconfig dependency
            //i'm going hard way
            //(Startup.INSTANCE.getConfig().getEnabled() requires clothconfig)
            Object configObject = Startup.INSTANCE.getConfig();
            Class configClass = Config.class;
            var method = configClass.getMethod("getEnabled");
            Boolean isEnabled = (Boolean) method.invoke(configObject);
            if(client.options.jumpKey.isPressed() && isEnabled)
                return;
            }
            catch (Exception ignored){
                ignored.printStackTrace();
            }
        }

        //i don't know slot order lol
        if(!(client.player.getInventory().armor.get(2).getItem() == Items.ELYTRA ||
           client.player.getInventory().armor.get(1).getItem() == Items.ELYTRA))
            return;

        if (!chestplateSlots.isEmpty()) {
            int bestSlot = chestplateSlots.get(0);
            swap(bestSlot, client);
        }
    }

    public static void tryWearElytra(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return;
        }

        if (client.player.getInventory().getStack(38).getItem() == Items.ELYTRA) {
            return;
        }

        var elytraSlots = getElytraSlots();

        elytraSlots.sort(Comparator.comparingInt(slot -> getElytraStat(client.player.getInventory().getStack(slot))));

        if (!elytraSlots.isEmpty()) {
            int bestSlot = elytraSlots.get(elytraSlots.size() - 1);
            wearElytra(bestSlot, client);
        }
    }

    public static List<Integer> getElytraSlots() {
        List<Integer> elytraSlots = new ArrayList<>();

        for (int slot : slotArray()) {
            if (MinecraftClient.getInstance().player.getInventory().getStack(slot).getItem().getComponents().contains(DataComponentTypes.GLIDER)) {
                elytraSlots.add(slot);
            }
        }
        return elytraSlots;
    }

    public static List<Integer> getChestplateSlots() {
        List<Integer> chestplateSlots = new ArrayList<>();
        var client = MinecraftClient.getInstance();

        for (int slot : slotArray()) {
            if (isSlotChestplate(client, slot)) {
                chestplateSlots.add(slot);
            }
        }

        return chestplateSlots;
    }
    private static Registry<Enchantment> getEnchantmentRegistry(){return MinecraftClient.getInstance().world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);}

    private static int getLevel(RegistryKey<Enchantment> key, ItemStack stack) {
        var enchant = getEnchantmentRegistry().get(key);
        RegistryEntry<Enchantment> enchantEntry = getEnchantmentRegistry().getEntry(enchant);
        return EnchantmentHelper.getLevel(enchantEntry,stack);
    }
    private static int getElytraStat(ItemStack elytraItem) {
        var stat = (getLevel(Enchantments.MENDING,elytraItem)*3+1)+getLevel(Enchantments.UNBREAKING,elytraItem);

        return stat;
    }

    private static int getChestplateStat(ItemStack chestplateItem) {
        float score = 1;
        if(chestplateItem.getItem() instanceof ArmorItem armorItem){
            var component = armorItem.getComponents().get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
            for (AttributeModifiersComponent.Entry entry : component.modifiers()) {
                RegistryEntry<EntityAttribute> attribute = entry.attribute();
                if(attribute.value() == EntityAttributes.ARMOR) {
                    score += entry.modifier().value();
                }
                if(attribute.value() == EntityAttributes.ARMOR_TOUGHNESS) {
                    score += entry.modifier().value();
                }
            }
            score += getLevel(Enchantments.PROTECTION,chestplateItem)*2;
            score += getLevel(Enchantments.MENDING,chestplateItem)*0.5;
            score += chestplateItem.contains(DataComponentTypes.CUSTOM_NAME)?0.25:0;
            score += getLevel(Enchantments.UNBREAKING,chestplateItem)*0.24/3;
        }

        return (int) (score*1000);
    }

    private static void wearElytra(int slotId, MinecraftClient client) {
        swap(slotId, client);
        try {
            client.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(client.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            client.player.startGliding();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }


    private static void swap(int slot, MinecraftClient client) {
        int slot2 = slot;
        if (slot2 == 40) slot2 = 45;
        if (slot2 < 9) slot2 += 36;

        try {
            client.interactionManager.clickSlot(0, slot2, 0, SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(0, 6, 0, SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(0, slot2, 0, SlotActionType.PICKUP, client.player);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }
    public static boolean isSlotChestplate(MinecraftClient client, int slotId) {

        if (client.player == null) {
            return false;
        }
        ItemStack chestSlot = client.player.getInventory().getStack(slotId);

        return !chestSlot.isEmpty() &&
                chestSlot.getItem() instanceof ArmorItem &&
                chestSlot.getItem().getComponents().get(DataComponentTypes.EQUIPPABLE).slot() == EquipmentSlot.CHEST &&
                getLevel(Enchantments.BINDING_CURSE,chestSlot) == 0;
    }

    private static int[] slotArray() {
        int[] range = new int[37];
        for (int i = 0; i < 9; i++) range[i] = 8 - i;
        for (int i = 9; i < 36; i++) range[i] = 35 - (i - 9);
        range[36] = 40;
        return range;
    }

    public static boolean prevTickIsOnGround=true;
    public void onInitializeClient() {
        var bind = new KeyBinding("jjelytraswap.keybind",-1,"LavaJumper");
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            if (client.world == null || client.player == null) {
                return;
            }

            if(bind.wasPressed())
            {
                enabled=!enabled;
                var ts = ("jjelytraswap."+(enabled?"enabled":"disabled"));
                client.inGameHud.getChatHud().addMessage(Text.translatable(ts));
            }
            if(!enabled)
                return;
            if(client.player.isOnGround()&&!prevTickIsOnGround)
                tryWearChestplate(client);
            prevTickIsOnGround=client.player.isOnGround();
        });
        KeyBindingHelper.registerKeyBinding(bind);
    }
    private static void logInChat(Object... objects){
        var chat = MinecraftClient.getInstance().inGameHud.getChatHud();
        if(chat==null)
            return;
        var msg = new StringBuilder();
        for(var object : objects){
            if(object instanceof Text text){
                msg.append(text.asTruncatedString(100000));
                continue;
            }
            msg.append(object);
        }

        chat.addMessage(Text.of(msg.toString()));
    }
}
