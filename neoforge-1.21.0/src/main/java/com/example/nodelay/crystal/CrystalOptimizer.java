package com.example.nodelay.crystal;

import com.example.nodelay.NoDelayConfig;

/**
 * Клиентский кристальный оптимизатор — функция Marlow's Crystal Optimizer (MIT,
 * Deathmotion/HypherionSA), встроенная в NoDelay. Кристаллы Энда исчезают в момент удара,
 * не дожидаясь пакета удаления от сервера, а прицел сразу переходит на следующую цель.
 *
 * <p>Модуль считается частью категории {@code crystals}: активен, пока категория включена
 * в {@link NoDelayConfig}. Серверный мод не нужен — используется ванильный механизм
 * предсказания состояния блоков, а отклонённый сервером удар возвращает кристалл через 1.5 c.
 */
public final class CrystalOptimizer {

	private static final CrystalOptimizer INSTANCE = new CrystalOptimizer();

	private final KeptCrystals keptCrystals = new KeptCrystals();
	private final CrystalBreaker breaker = new CrystalBreaker(keptCrystals);

	private CrystalOptimizer() {
	}

	public static CrystalOptimizer get() {
		return INSTANCE;
	}

	/** Включён ли оптимизатор: вместе с категорией {@code crystals}. */
	public boolean enabled() {
		return NoDelayConfig.get().crystals.enabled;
	}

	public KeptCrystals keptCrystals() {
		return keptCrystals;
	}

	public CrystalBreaker breaker() {
		return breaker;
	}
}