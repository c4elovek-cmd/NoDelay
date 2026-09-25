package com.example.nodelay;

import net.fabricmc.api.ClientModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NoDelay entry point (Quilt, client). Quilt Loader runs client entrypoints from
 * {@code fabric.mod.json}; {@code quilt.mod.json} declares the same class via
 * {@code client_init}.
 *
 * <p>The code is shared with Fabric: Quilt Loader ships the fabric-loader API, so the
 * vanilla-only mixin and config work natively. Fabric API is not needed - there is no
 * in-game command on Quilt, edit {@code config/nodelay.json} instead.
 */
public class NoDelayQuilt implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("nodelay-quilt");

	@Override
	public void onInitializeClient() {
		NoDelayConfig config = NoDelayConfig.get();
		LOGGER.info("NoDelay (Quilt) loaded (enabled: {}, blocks: {}t, villagers: {}t, entities: {}t, items: {}t)",
				config.enabled,
				config.delayFor(config.blocks),
				config.delayFor(config.villagers),
				config.delayFor(config.entities),
				config.delayFor(config.items));
	}
}