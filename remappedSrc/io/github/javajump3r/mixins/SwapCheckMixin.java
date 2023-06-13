package io.github.javajump3r.mixins;

import io.github.javajump3r.ElytraSwapInit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class SwapCheckMixin {

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getEquippedStack(Lnet/minecraft/entity/EquipmentSlot;)Lnet/minecraft/item/ItemStack;", shift = At.Shift.BEFORE))
    public void swapToElytra(CallbackInfo callbackInfo) {
        var target = ((ClientPlayerEntity) (Object) this);
        if (!target.isOnGround() && !target.isFallFlying() && !target.isTouchingWater() && !target.hasStatusEffect(StatusEffects.LEVITATION)) {
            ElytraSwapInit.tryWearElytra(MinecraftClient.getInstance());
        }
    }
}
