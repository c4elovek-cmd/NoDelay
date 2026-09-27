package com.example.nodelay.crystal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;

/**
 * «Быстрое разбивание» кристаллов: после удара, который должен пройти, клиент удерживает
 * кристалл скрытым, пока vanilla-последовательность предсказания блоков не подтвердит
 * обработку удара. На пинге больше нуля это убирает паузу между ударом и исчезновением.
 */
public final class CrystalBreaker {

	private final KeptCrystals keptCrystals;

	public CrystalBreaker(KeptCrystals keptCrystals) {
		this.keptCrystals = keptCrystals;
	}

	public void breakIfPossible(Minecraft client, EndCrystal crystal) {
		LocalPlayer player = client.player;
		ClientLevel level = client.level;
		if (player == null || level == null) {
			return;
		}

		// Сервер молча игнорирует удары по сущностям вне границы мира.
		if (!level.getWorldBorder().isWithinBounds(crystal.blockPosition()) || !AttackDamage.breaksCrystal(player)) {
			return;
		}

		keptCrystals.keep(crystal, ((SequencedLevel) level).nodelay$blockSequence());
		Crosshair.retargetPast(client, crystal);
	}
}