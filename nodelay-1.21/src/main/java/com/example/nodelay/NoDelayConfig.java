package com.example.nodelay;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Конфиг NoDelay, хранится в {@code config/nodelay.json}. Перечитать в игре:
 * {@code /nodelay reload} (клиентская команда).
 *
 * <p>Мод трогает одно ванильное поле: {@code Minecraft#rightClickDelay}. Ваниль ставит его в 4 при
 * каждом использовании предмета ({@code Minecraft#startUseItem()}) и уменьшает на 1 каждый тик,
 * а ПКМ обрабатывается заново только когда поле станет 0. Это и есть видимая «задержка между
 * двумя действиями», если держать правую кнопку мыши зажатой.
 *
 * <p>Задержка снимается выборочно, по категориям действия. Категория определяется по тому, на что
 * смотрит прицел ({@code Minecraft#hitResult}): блок — {@link #blocks}, сущность — житель или
 * другая — {@link #villagers}/{@link #entities}, «в пустоту» — {@link #items}.
 */
public final class NoDelayConfig {
	/** Ванильное значение из {@code Minecraft#startUseItem()}: 4 тика между двумя действиями. */
	public static final int VANILLA_DELAY = 4;
	/** Верхняя граница, принимаемая из конфига (20 тиков = 1 секунда). */
	public static final int MAX_DELAY = 20;
	/** Все категории в порядке вывода в {@code /nodelay}. */
	public static final String[] CATEGORIES = { "blocks", "villagers", "entities", "items" };

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "nodelay.json";
	private static NoDelayConfig instance;

	/** Настройки одной категории действий. */
	public static final class Category {
		/** Выключенная категория откатывается на ванильные {@link #VANILLA_DELAY} тика. */
		public boolean enabled = true;
		/** Задержка в тиках вместо ванильных 4. 0 = действие каждый тик. */
		public int delay = 0;

		/** Значение, зажатое в разумный диапазон: правка файла руками не сломает клиент. */
		public int effectiveDelay() {
			return Math.max(0, Math.min(delay, MAX_DELAY));
		}
	}

	// --- поля JSON-конфига ------------------------------------------------
	/** Главный выключатель. При false ванильная задержка в 4 тика не трогается вовсе. */
	public boolean enabled = true;
	/** Установка блоков и использование предметов по блоку ({@code BlockHitResult}). */
	public Category blocks = new Category();
	/** Торговля с жителями ({@code EntityHitResult} + {@code Villager}). */
	public Category villagers = new Category();
	/** Взаимодействие с прочими существами ({@code EntityHitResult}). */
	public Category entities = disabled();
	/** Использование предмета «в воздухе» (прицел {code MISS}): еда, стрельба, бросание. */
	public Category items = disabled();
	// -----------------------------------------------------------------------

	private static Category disabled() {
		Category category = new Category();
		category.enabled = false;
		return category;
	}

	public static NoDelayConfig get() {
		if (instance == null) {
			load();
		}
		return instance;
	}

	public static void load() {
		Path path = configPath();
		NoDelayConfig config = new NoDelayConfig();
		try {
			if (Files.exists(path)) {
				NoDelayConfig parsed = GSON.fromJson(Files.readString(path), NoDelayConfig.class);
				if (parsed != null) {
					config = parsed;
				}
			} else {
				Files.writeString(path, GSON.toJson(config));
			}
		} catch (IOException | RuntimeException e) {
			// Битый конфиг не должен ронять клиент: откатываемся на значения по умолчанию
			// в памяти и НЕ трогаем файл, чтобы пользователь не потерял свои данные.
			NoDelayClient.LOGGER.warn("Could not read {}, using defaults: {}", FILE_NAME, e.toString());
		}
		instance = config;
	}

	public static void save() {
		Path path = configPath();
		try {
			Files.writeString(path, GSON.toJson(get()));
		} catch (IOException e) {
			NoDelayClient.LOGGER.warn("Could not save {}: {}", FILE_NAME, e.toString());
		}
	}

	private static Path configPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
	}

	/** Категория по имени (для команды {@code /nodelay}). */
	public Category category(String name) {
		for (String candidate : CATEGORIES) {
			if (candidate.equals(name)) {
				switch (candidate) {
					case "blocks":
						return blocks;
					case "villagers":
						return villagers;
					case "entities":
						return entities;
					default:
						return items;
				}
			}
		}
		return null;
	}

	/** Итоговая задержка категории: выключена = ванильные тики, включена = её настройка. */
	public int delayFor(Category category) {
		if (category == null || !category.enabled) {
			return VANILLA_DELAY;
		}
		return category.effectiveDelay();
	}
}