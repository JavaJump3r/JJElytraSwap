package io.github.jumperonjava.jjelytraswap.mixin;

import io.github.jumperonjava.jjelytraswap.JJElytraSwapInit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ClientPlayerEntity.class)
public class SwapCheckMixin {

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;checkGliding()Z", shift = At.Shift.AFTER))
    public void swapToElytra(CallbackInfo callbackInfo) {
        if (!JJElytraSwapInit.enabled)
            return;
        var target = ((ClientPlayerEntity) (Object) this);
        if (!target.isOnGround() &&
                !target.isGliding()
                && !target.isTouchingWater() && !target.hasStatusEffect(StatusEffects.LEVITATION)) {
            JJElytraSwapInit.tryWearElytra();
        }
    }
}
