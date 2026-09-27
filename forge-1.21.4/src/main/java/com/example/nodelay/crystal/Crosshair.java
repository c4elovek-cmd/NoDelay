package com.example.nodelay.crystal;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.phys.EntityHitResult;

/** Перестрел прицела мимо спрятанного кристалла, чтобы следующая цель ловилась мгновенно. */
final class Crosshair {

	private Crosshair() {
	}

	static void retargetPast(Minecraft client, EndCrystal crystal) {
		if (!(client.hitResult instanceof EntityHitResult target) || target.getEntity() != crystal) {
			return;
		}

		client.gameRenderer.pick(1.0F);
	}
}