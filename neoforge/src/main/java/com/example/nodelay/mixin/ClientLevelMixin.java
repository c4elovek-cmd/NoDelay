package com.example.nodelay.mixin;

import com.example.nodelay.crystal.CrystalOptimizer;
import com.example.nodelay.crystal.SequencedLevel;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Даёт {@link ClientLevel} последовательность предсказания блоков, прячет удерживаемые
 * кристаллы из рендера и освобождает их по подтверждению сервера.
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin implements SequencedLevel {

	@Shadow
	@Final
	private BlockStatePredictionHandler blockStatePredictionHandler;

	@Override
	public int nodelay$blockSequence() {
		return blockStatePredictionHandler.currentSequence();
	}

	// Кристалл прячется, а не удаляется: отклонённый сервером удар вернёт его после освобождения.
	@Inject(method = "entitiesForRendering", at = @At("RETURN"), cancellable = true)
	private void nodelay$hideKeptCrystals(CallbackInfoReturnable<Iterable<Entity>> cir) {
		cir.setReturnValue(CrystalOptimizer.get().keptCrystals().hide(cir.getReturnValue()));
	}

	@Inject(method = "handleBlockChangedAck", at = @At("HEAD"))
	private void nodelay$releaseKeptCrystals(int sequence, CallbackInfo ci) {
		CrystalOptimizer.get().keptCrystals().acknowledged(sequence);
	}
}