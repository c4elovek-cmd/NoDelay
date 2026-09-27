package com.example.nodelay.crystal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
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

		LocalPlayer player = client.player;
		Entity camera = client.getCameraEntity();
		if (player == null || camera == null) {
			return;
		}

		client.hitResult = player.raycastHitResult(1.0F, camera);
		client.crosshairPickEntity = client.hitResult instanceof EntityHitResult hit ? hit.getEntity() : null;
	}
}