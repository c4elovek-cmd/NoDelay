package com.example.nodelay.crystal;

import com.google.common.collect.Iterables;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Список «удерживаемых» кристаллов. Когда игрок бьёт кристалл и удар должен пройти, клиент
 * прячет кристалл сразу, не дожидаясь пакета удаления от сервера: на пинге больше нуля это
 * убирает «залипание» разбитого кристалла и позволяет мгновенно навестись на следующую цель.
 * Если сервер удар отклонил, кристалл через {@value RELEASE_AFTER_MS} мс снова появляется.
 */
public final class KeptCrystals {

	private static final long RELEASE_AFTER_MS = 1500;
	// Достигается только когда за ударом не последовало блок-подтверждение: ничто не отличит
	// отклонённый удар от принятого, поэтому просто вернём кристалл обратно.
	private static final long RELEASE_AFTER = TimeUnit.MILLISECONDS.toNanos(RELEASE_AFTER_MS);

	private final List<EndCrystal> kept = new ArrayList<>();

	private long lastKeptAt;

	private static boolean isVisible(Entity entity, long keptSince) {
		return !(entity instanceof KeptCrystal crystal)
				|| !crystal.nodelay$isKept()
				|| crystal.nodelay$keptAt() - keptSince <= 0;
	}

	public void keep(EndCrystal crystal, int sequence) {
		long now = System.nanoTime();
		forgetSettled(now - RELEASE_AFTER);
		((KeptCrystal) crystal).nodelay$keep(now, sequence);
		kept.add(crystal);
		lastKeptAt = now;
	}

	// Сервер присылает удаление, пока обрабатывает удар, и подтверждение — только после каждого
	// пакета до него; по подтверждённой последовательности удержанное освобождается.
	public void acknowledged(int sequence) {
		forgetSettled(System.nanoTime() - RELEASE_AFTER);
		kept.removeIf(crystal -> {
			KeptCrystal keptCrystal = (KeptCrystal) crystal;
			if (keptCrystal.nodelay$sequence() >= sequence) {
				return false;
			}

			keptCrystal.nodelay$release();
			return true;
		});
	}

	public Predicate<? super Entity> hide(Predicate<? super Entity> predicate) {
		long keptSince = System.nanoTime() - RELEASE_AFTER;
		if (nothingKept(keptSince)) {
			return predicate;
		}

		return entity -> isVisible(entity, keptSince) && predicate.test(entity);
	}

	public Iterable<Entity> hide(Iterable<Entity> entities) {
		long keptSince = System.nanoTime() - RELEASE_AFTER;
		if (nothingKept(keptSince)) {
			return entities;
		}

		return Iterables.filter(entities, entity -> isVisible(entity, keptSince));
	}

	private void forgetSettled(long keptSince) {
		kept.removeIf(crystal -> crystal.isRemoved() || isVisible(crystal, keptSince));
	}

	// Очищается, как только всё истекло, чтобы покинутый уровень не держался в памяти.
	private boolean nothingKept(long keptSince) {
		if (kept.isEmpty()) {
			return true;
		}

		if (lastKeptAt - keptSince <= 0) {
			kept.clear();
			return true;
		}

		return false;
	}
}