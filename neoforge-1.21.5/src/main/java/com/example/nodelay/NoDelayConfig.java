package com.example.nodelay;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.neoforged.fml.loading.FMLPaths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NoDelay config for Forge/NeoForge, stored in {@code config/nodelay.json}
 * (same path as on Fabric/Quilt). There is no in-game command on these loaders -
 * edit the file and restart.
 *
 * <p>The mod touches one vanilla field: {@code Minecraft#rightClickDelay}. Vanilla sets it
 * to 4 on every item use ({@code Minecraft#startUseItem()}) and decrements it by 1 each
 * tick; right-click is re-processed only once the field reaches 0. That is the visible
 * "delay between two actions" while holding the right mouse button.
 *
 * <p>The delay is removed selectively, per action category. The category comes from what the
 * crosshair is on ({@code Minecraft#hitResult}): a block - {@link #blocks}, an entity -
 * villager or other - {@link #villagers}/{@link #entities}, empty - {@link #items}.
 */
public final class NoDelayConfig {
	/** Vanilla value from {@code Minecraft#startUseItem()}: 4 ticks between two actions. */
	public static final int VANILLA_DELAY = 4;
	/** Upper bound accepted from the config (20 ticks = 1 second). */
	public static final int MAX_DELAY = 20;
	/** All categories, in the order printed by the mod. */
	public static final String[] CATEGORIES = { "blocks", "villagers", "entities", "items" };

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Logger LOGGER = LoggerFactory.getLogger("nodelay-config");
	private static final String FILE_NAME = "nodelay.json";
	private static NoDelayConfig instance;

	/** Settings of a single action category. */
	public static final class Category {
		/** A disabled category falls back to the vanilla {@link #VANILLA_DELAY} ticks. */
		public boolean enabled = true;
		/** Delay in ticks instead of the vanilla 4. 0 = action every tick. */
		public int delay = 0;

		/** Value clamped into a sane range: hand-editing the file cannot break the client. */
		public int effectiveDelay() {
			return Math.max(0, Math.min(delay, MAX_DELAY));
		}
	}

	// --- JSON config fields ------------------------------------------------
	/** Master switch. When false, the vanilla 4 tick delay is left untouched. */
	public boolean enabled = true;
	/** Placing blocks and using items against a block ({@code BlockHitResult}). */
	public Category blocks = new Category();
	/** Trading with villagers ({@code EntityHitResult} + {@code Villager}). */
	public Category villagers = new Category();
	/** Interacting with other creatures ({@code EntityHitResult}). */
	public Category entities = disabled();
	/** Using an item "in the air" (crosshair MISS): eating, shooting, throwing. */
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
			// A broken config must not crash the client: fall back to in-memory
			// defaults and DO NOT touch the file so the user keeps their data.
			LOGGER.warn("Could not read {}, using defaults: {}", FILE_NAME, e.toString());
		}
		instance = config;
	}

	public static void save() {
		Path path = configPath();
		try {
			Files.writeString(path, GSON.toJson(get()));
		} catch (IOException e) {
			LOGGER.warn("Could not save {}: {}", FILE_NAME, e.toString());
		}
	}

	private static Path configPath() {
		return FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
	}

	/** Category by name. */
	public Category category(String name) {
		switch (name) {
			case "blocks":
				return blocks;
			case "villagers":
				return villagers;
			case "entities":
				return entities;
			case "items":
				return items;
			default:
				return null;
		}
	}

	/** Final delay of a category: disabled = vanilla ticks, enabled = its setting. */
	public int delayFor(Category category) {
		if (category == null || !category.enabled) {
			return VANILLA_DELAY;
		}
		return category.effectiveDelay();
	}
}