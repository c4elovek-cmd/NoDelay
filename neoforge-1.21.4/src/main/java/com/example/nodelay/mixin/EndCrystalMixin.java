package com.example.nodelay.mixin;

import com.example.nodelay.crystal.KeptCrystal;

import net.minecraft.world.entity.boss.enderdragon.EndCrystal;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Навешивает на {@link EndCrystal} состояние «удерживается клиентом» (см. {@link KeptCrystal}). */
@Mixin(EndCrystal.class)
public class EndCrystalMixin implements KeptCrystal {

	@Unique
	private boolean nodelay$kept;

	@Unique
	private long nodelay$keptAt;

	@Unique
	private int nodelay$sequence;

	@Override
	public void nodelay$keep(long keptAt, int sequence) {
		nodelay$kept = true;
		nodelay$keptAt = keptAt;
		nodelay$sequence = sequence;
	}

	@Override
	public void nodelay$release() {
		nodelay$kept = false;
	}

	@Override
	public boolean nodelay$isKept() {
		return nodelay$kept;
	}

	@Override
	public long nodelay$keptAt() {
		return nodelay$keptAt;
	}

	@Override
	public int nodelay$sequence() {
		return nodelay$sequence;
	}
}