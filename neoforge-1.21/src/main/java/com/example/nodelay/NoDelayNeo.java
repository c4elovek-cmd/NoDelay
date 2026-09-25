package com.example.nodelay;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NoDelay entry point (NeoForge).
 *
 * <p>All the work is done by {@link com.example.nodelay.mixin.NoDelayMixin}: it overwrites
 * {@code Minecraft#rightClickDelay} right after Minecraft sets it in
 * {@code Minecraft#startUseItem()}. Unlike the old No Placement Delay the delay is removed
 * selectively - one setting for placing blocks, and separate ones for villagers, other
 * entities and using items in the air. A disabled category keeps the vanilla 4 ticks.
 *
 * <p>Marked {@code dist = Dist.CLIENT} so the mod is never loaded on a dedicated server.
 */
@Mod(value = NoDelayNeo.MODID, dist = Dist.CLIENT)
public class NoDelayNeo {
	public static final String MODID = "nodelay";
	public static final Logger LOGGER = LoggerFactory.getLogger("nodelay-neoforge");

	public NoDelayNeo() {
		NoDelayConfig config = NoDelayConfig.get();
		LOGGER.info("NoDelay loaded (enabled: {}, blocks: {}t, villagers: {}t, entities: {}t, items: {}t)",
				config.enabled,
				config.delayFor(config.blocks),
				config.delayFor(config.villagers),
				config.delayFor(config.entities),
				config.delayFor(config.items));
	}
}