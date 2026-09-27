package com.example.nodelay.crystal;

/**
 * Кристалл Энда, который клиент «удерживает» после удара, пока сервер не подтвердит обработку.
 * Реализуется {@link com.example.nodelay.mixin.EndCrystalMixin} поверх {@code EndCrystal}.
 */
public interface KeptCrystal {

	/** Пометить кристалл как удерживаемый: спрятанный, но ещё не подтверждённый сервером. */
	void nodelay$keep(long keptAt, int sequence);

	/** Вернуть кристалл: сервер подтвердил его судьбу. */
	void nodelay$release();

	/** Удерживается ли кристалл в данный момент. */
	boolean nodelay$isKept();

	/** Момент начала удержания (наносекунды {@code System.nanoTime()}). */
	long nodelay$keptAt();

	/** Последовательность предсказания блоков на момент удара. */
	int nodelay$sequence();
}