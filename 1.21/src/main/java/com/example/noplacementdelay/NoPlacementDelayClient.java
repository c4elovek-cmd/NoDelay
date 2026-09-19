package com.example.noplacementdelay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Точка входа No Placement Delay (только клиент, Minecraft 26.2 / Fabric).
 *
 * <p>Всю работу делает {@link com.example.noplacementdelay.mixin.NoPlacementDelayMixin}: он
 * перезаписывает {@code Minecraft#rightClickDelay} сразу после того, как ваниль выставила его в
 * {@code Minecraft#startUseItem()}. При задержке 0 из конфига ПКМ обрабатывается каждый тик вместо
 * каждого 4-го — именно это игрок и замечает как «задержку между установками блоков».
 *
 * <p>Minecraft 26.2 не обфусцирован, поэтому здесь используются ванильные имена.
 */
public class NoPlacementDelayClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("noplacementdelay");

	@Override
	public void onInitializeClient() {
		NoPlacementDelayConfig config = NoPlacementDelayConfig.get();
		LOGGER.info("No Placement Delay loaded (enabled: {}, delay: {} tick(s), vanilla delay: {})",
				config.enabled, config.effectiveDelay(), NoPlacementDelayConfig.VANILLA_DELAY);

		// Команде /noplacementdelay нужен Fabric API. На класс NoPlacementDelayCommands мы
		// ссылаемся — и значит загружаем его — только при наличии Fabric API, поэтому jar
		// продолжает работать и без него (тогда конфиг правится в config/noplacementdelay.json).
		if (FabricLoader.getInstance().isModLoaded("fabric-api")) {
			NoPlacementDelayCommands.register();
		} else {
			LOGGER.info("Fabric API not found - /noplacementdelay command disabled, edit config/noplacementdelay.json instead");
		}
	}
}
