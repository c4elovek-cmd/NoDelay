package com.example.nodelay.crystal;

/**
 * Уровень, способный сообщить текущую последовательность предсказания состояния блоков
 * (vanilla API). Реализуется {@link com.example.nodelay.mixin.ClientLevelMixin}.
 */
public interface SequencedLevel {

	int nodelay$blockSequence();
}