package com.example.nodelay.mixin;

import com.example.nodelay.crystal.CrystalOptimizer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Запускает быстрое разбивание кристаллов сразу после удара по кристаллу Энда. */
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

	@Inject(
			method = "attack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Player;attack(Lnet/minecraft/world/entity/Entity;)V",
					shift = At.Shift.AFTER))
	private void nodelay$afterAttack(Player player, Entity entity, CallbackInfo ci) {
		if (!(entity instanceof EndCrystal crystal)) {
			return;
		}

		if (CrystalOptimizer.get().enabled()) {
			CrystalOptimizer.get().breaker().breakIfPossible(Minecraft.getInstance(), crystal);
		}
	}
}