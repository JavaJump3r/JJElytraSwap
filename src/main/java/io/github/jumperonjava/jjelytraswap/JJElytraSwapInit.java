package io.github.jumperonjava.jjelytraswap;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;
import me.lunaluna.fabric.elytrarecast.Startup;
import me.lunaluna.fabric.elytrarecast.config.Config;

public class JJElytraSwapInit
{
	public static final String MODID = "jjelytraswap";
	public static final Logger LOGGER = LoggerFactory.getLogger("JJElytraSwap");
	public static ModPlatform PLATFORM = null;

	public static void entrypoint(ModPlatform platform) {
		JJElytraSwapInit.PLATFORM = platform;
		onInitializeClient();

	}
	public static boolean enabled = true;
	static MinecraftClient client = MinecraftClient.getInstance();

	public static void tryWearChestplate(MinecraftClient client) {
		if (client.world == null || client.player == null) {
			return;
		}

		if ( isSlotChestplate(38)) {
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

		//? if fabric {
		if(PLATFORM.isModLoaded("elytra-recast")){
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
		//?}


		//i don't know slot order lol
		if(!(client.player.getInventory().armor.get(2).getItem() == Items.ELYTRA ||
				client.player.getInventory().armor.get(1).getItem() == Items.ELYTRA))
			return;

		if (!chestplateSlots.isEmpty()) {
			int bestSlot = chestplateSlots.get(0);
			swap(bestSlot, client);
		}
	}

	public static void tryWearElytra() {
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
			wearElytra(bestSlot);
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
			if (isSlotChestplate(slot)) {
				chestplateSlots.add(slot);
			}
		}

		return chestplateSlots;
	}
    private static Registry<Enchantment> getEnchantmentRegistry() {
        return MinecraftClient.getInstance().world.getRegistryManager()
        .getOrThrow(RegistryKeys.ENCHANTMENT);
	}

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
		logInChat("1");


        var ARMOR = EntityAttributes.ARMOR;
        var TOUGHNESS = EntityAttributes.ARMOR_TOUGHNESS;

		if(chestplateItem.getItem() instanceof ArmorItem armorItem){
			logInChat("2");
			var component = armorItem.getComponents().get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
			logInChat("3");
			for (AttributeModifiersComponent.Entry entry : component.modifiers()) {
				logInChat("4");
				RegistryEntry<EntityAttribute> attribute = entry.attribute();
				logInChat("5");
				if(attribute.value() == ARMOR) {
					score += entry.modifier().value();
                    logInChat("armor: ",entry.modifier().value());
				}
				if(attribute.value() == TOUGHNESS) {
					score += entry.modifier().value();
                    logInChat("toughness: ",entry.modifier().value());
				}
			}
			score += getLevel(Enchantments.PROTECTION,chestplateItem)*2;
			score += getLevel(Enchantments.MENDING,chestplateItem)*0.5;
			score += chestplateItem.contains(DataComponentTypes.CUSTOM_NAME)?0.25:0;
			score += getLevel(Enchantments.UNBREAKING,chestplateItem)*0.24/3;
		}

		return (int) (score*1000);
	}

	private static void wearElytra(int slotId) {
		swap(slotId, client);
		try {
			//? if fabric {
			client.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(client.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
			//?} else {
			/*client.getNetworkHandler().send(new ClientCommandC2SPacket(client.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
			*///?}

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
	public static boolean isSlotChestplate(int slotId) {

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

	public static boolean shouldWearChestplatePrevTick =true;
	public static void onInitializeClient() {
		var bind = PLATFORM.registerKeyBind("jjelytraswap.keybind",-1,"JJElytraSwap");
		PLATFORM.registerClientTickEvent(client->{
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
			boolean isInAir = !client.player.isOnGround() && !client.player.isInFluid();
			boolean shouldWearChestplate = !isInAir;
			if(shouldWearChestplate && !shouldWearChestplatePrevTick)
				tryWearChestplate(client);
			shouldWearChestplatePrevTick = shouldWearChestplate;
		});

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