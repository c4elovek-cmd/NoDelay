package com.example.nodelay.mixin;

import com.example.nodelay.NoDelayConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Selectively removes the vanilla right-click delay.
 *
 * <p>{@code Minecraft#startUseItem()} starts like this:
 * <pre>
 * if (this.gameMode.isDestroying()) return;
 * this.rightClickDelay = 4;   // &lt;- the only write to this field in the game
 * if (this.player.isHandsBusy()) return;
 * ... // switch on this.hitResult.getType(): ENTITY / BLOCK / MISS
 * </pre>
 * and {@code Minecraft#tick()} decrements the field every tick, re-processing right-click
 * only when it reaches 0. The hook attaches to the field write itself ({@code PUTFIELD
 * rightClickDelay:I}, right after it), not {@code @At("TAIL")}: {@code startUseItem()} has
 * several early {@code return}s, so this fires on every code path.
 *
 * <p>The action category comes from {@code Minecraft#hitResult}: block - placing blocks,
 * entity - villager or "other entity", empty - using an item. Each category delay comes
 * from {@link NoDelayConfig}; a disabled category keeps the vanilla 4 ticks.
 *
 * <p>Named after the mod on purpose: several mods also have a {@code MinecraftMixin} mixin,
 * and a distinct name keeps the Mixin log readable.
 */
@Mixin(Minecraft.class)
public class NoDelayMixin {
	@Shadow
	private int rightClickDelay;

	@Shadow
	public HitResult hitResult;

	@Inject(method = "startUseItem",
			at = @At(value = "FIELD",
					target = "Lnet/minecraft/client/Minecraft;rightClickDelay:I",
					opcode = Opcodes.PUTFIELD,
					shift = At.Shift.AFTER))
	private void nodelay$overrideDelay(CallbackInfo ci) {
		NoDelayConfig config = NoDelayConfig.get();
		if (!config.enabled) {
			return;
		}

		int delay = NoDelayConfig.VANILLA_DELAY;
		if (this.hitResult instanceof BlockHitResult) {
			delay = config.delayFor(config.blocks);
		} else if (this.hitResult instanceof EntityHitResult entityHit) {
			if (entityHit.getEntity() instanceof Villager) {
				delay = config.delayFor(config.villagers);
			} else {
				delay = config.delayFor(config.entities);
			}
		} else {
			delay = config.delayFor(config.items);
		}

		// 0 = next use allowed as early as the next tick.
		this.rightClickDelay = delay;
	}
}