//? if neoforge {
/*package io.github.jumperonjava.jjelytraswap.platforms.neoforge;

import io.github.jumperonjava.jjelytraswap.JJElytraSwapInit;
import io.github.jumperonjava.jjelytraswap.ModPlatform;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
//? if <1.21 {
/^import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;

import java.util.function.Consumer;

^///?} else {
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

//?}
@Mod("jjelytraswap")
public class JJElytraSwapNeoForge {

    private static IEventBus MOD_EVENT_BUS;
    private static IEventBus GAME_EVENT_BUS;

    public JJElytraSwapNeoForge(IEventBus eventBus) {
        MOD_EVENT_BUS = eventBus;
        GAME_EVENT_BUS = NeoForge.EVENT_BUS;
        JJElytraSwapInit.entrypoint(new NeoForgePlatform());
    }

    public static class NeoForgePlatform implements ModPlatform {

        @Override
        public String getModloader() {
            return "NeoForge";
        }

        @Override
        public boolean isModLoaded(String modId) {
            return ModList.get().isLoaded(modId);
        }


        List<Consumer<MinecraftClient>> clientEvents = new ArrayList<>();

        @Override
        public void registerClientTickEvent(Consumer<MinecraftClient> o) {
            GAME_EVENT_BUS.addListener((Consumer<ClientTickEvent.Post>)event -> o.accept(MinecraftClient.getInstance()));
        }

        @Override
        public KeyBinding registerKeyBind(String translationKeyName, int defaultKeyId, String category) {
            var keyBinding = new KeyBinding(translationKeyName, defaultKeyId, category);
            MOD_EVENT_BUS.addListener((Consumer<RegisterKeyMappingsEvent>) event -> event.register(keyBinding));
            return keyBinding;
        }
    }
}
*///?}