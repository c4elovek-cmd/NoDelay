package com.example.noplacementdelay;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Конфиг No Placement Delay, хранится в {@code config/noplacementdelay.json}.
 * Перечитать в игре: {@code /noplacementdelay reload} (клиентская команда).
 *
 * <p>Весь мод — про одно ванильное поле: {@code Minecraft#rightClickDelay}. Ваниль ставит его в 4
 * при каждом использовании предмета ({@code Minecraft#startUseItem()}) и уменьшает на 1 каждый
 * тик, а ПКМ обрабатывается заново только когда поле станет 0. Это и есть видимая «задержка между
 * двумя установками блоков», если держать правую кнопку мыши зажатой.
 */
public final class NoPlacementDelayConfig {
	/** Ванильное значение из {@code Minecraft#startUseItem()}: 4 тика между двумя действиями. */
	public static final int VANILLA_DELAY = 4;
	/** Верхняя граница, принимаемая из конфига (20 тиков = 1 секунда). */
	public static final int MAX_DELAY = 20;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "noplacementdelay.json";
	private static NoPlacementDelayConfig instance;

	// --- поля JSON-конфига ------------------------------------------------
	/** Главный выключатель. При false ванильная задержка в 4 тика не трогается. */
	public boolean enabled = true;
	/** Задержка в тиках вместо ванильных 4. 0 = действие каждый тик. */
	public int delay = 0;
	// -----------------------------------------------------------------------

	public static NoPlacementDelayConfig get() {
		if (instance == null) {
			load();
		}
		return instance;
	}

	public static void load() {
		Path path = configPath();
		NoPlacementDelayConfig config = new NoPlacementDelayConfig();
		try {
			if (Files.exists(path)) {
				NoPlacementDelayConfig parsed = GSON.fromJson(Files.readString(path), NoPlacementDelayConfig.class);
				if (parsed != null) {
					config = parsed;
				}
			} else {
				Files.writeString(path, GSON.toJson(config));
			}
		} catch (IOException | RuntimeException e) {
			// Битый конфиг не должен ронять клиент: откатываемся на значения по умолчанию
			// в памяти и НЕ трогаем файл, чтобы пользователь не потерял свои данные.
			NoPlacementDelayClient.LOGGER.warn("Could not read {}, using defaults: {}", FILE_NAME, e.toString());
		}
		instance = config;
	}

	public static void save() {
		Path path = configPath();
		try {
			Files.writeString(path, GSON.toJson(get()));
		} catch (IOException e) {
			NoPlacementDelayClient.LOGGER.warn("Could not save {}: {}", FILE_NAME, e.toString());
		}
	}

	private static Path configPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
	}

	/** Значение из конфига, зажатое в разумный диапазон: правка файла руками не сломает клиент. */
	public int effectiveDelay() {
		return Math.max(0, Math.min(delay, MAX_DELAY));
	}
}
