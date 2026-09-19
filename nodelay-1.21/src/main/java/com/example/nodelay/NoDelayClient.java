package com.example.nodelay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Точка входа NoDelay (только клиент, Minecraft 26.2 / Fabric).
 *
 * <p>Всю работу делает {@link com.example.nodelay.mixin.NoDelayMixin}: он перезаписывает
 * {@code Minecraft#rightClickDelay} сразу после того, как ваниль выставила его в
 * {@code Minecraft#startUseItem()}. В отличие от прежнего No Placement Delay задержка снимается
 * выборочно: одна настройка на установку блоков, отдельные — на жителей, других сущностей и
 * использование предметов в воздухе. Если категория выключена — остаётся ванильные 4 тика.
 *
 * <p>Minecraft 26.2 не обфусцирован, поэтому здесь используются ванильные имена.
 */
public class NoDelayClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("nodelay");

	@Override
	public void onInitializeClient() {
		NoDelayConfig config = NoDelayConfig.get();
		LOGGER.info("NoDelay loaded (enabled: {}, blocks: {}t, villagers: {}t, entities: {}t, items: {}t)",
				config.enabled,
				config.delayFor(config.blocks),
				config.delayFor(config.villagers),
				config.delayFor(config.entities),
				config.delayFor(config.items));

		// Команде /nodelay нужен Fabric API. На класс NoDelayCommands мы ссылаемся — и значит
		// загружаем его — только при наличии Fabric API, поэтому jar продолжает работать и без
		// него (тогда конфиг правится в config/nodelay.json).
		if (FabricLoader.getInstance().isModLoaded("fabric-api")) {
			NoDelayCommands.register();
		} else {
			LOGGER.info("Fabric API not found - /nodelay command disabled, edit config/nodelay.json instead");
		}
	}
}