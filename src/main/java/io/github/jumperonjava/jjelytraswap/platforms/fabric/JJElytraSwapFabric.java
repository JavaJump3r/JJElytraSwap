//? if fabric {
package io.github.jumperonjava.jjelytraswap.platforms.fabric;

import io.github.jumperonjava.jjelytraswap.ModPlatform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import io.github.jumperonjava.jjelytraswap.JJElytraSwapInit;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

import java.util.function.Consumer;

public class JJElytraSwapFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		JJElytraSwapInit.entrypoint(new FabricPlatform());
	}
	public static class FabricPlatform implements ModPlatform{

		@Override
		public String getModloader() {
			return "Fabric";
		}

		@Override
		public boolean isModLoaded(String modloader) {
			return FabricLoader.getInstance().isModLoaded(modloader);
		}

		@Override
		public void registerClientTickEvent(Consumer<MinecraftClient> o) {
			ClientTickEvents.END_CLIENT_TICK.register(o::accept);
		}

		@Override
		public KeyBinding registerKeyBind(String translationKeyName, int defaultKeyId, String category) {
			var bind = new KeyBinding(translationKeyName,defaultKeyId,category);
			KeyBindingHelper.registerKeyBinding(bind);
			return bind;
		}
	}
}
//?}