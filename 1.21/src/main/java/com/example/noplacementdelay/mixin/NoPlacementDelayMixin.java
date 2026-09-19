package com.example.noplacementdelay.mixin;

import com.example.noplacementdelay.NoPlacementDelayConfig;

import net.minecraft.client.Minecraft;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Убирает ванильную задержку правого клика.
 *
 * <p>В 26.2 {@code Minecraft#startUseItem()} начинается так:
 * <pre>
 * if (this.gameMode.isDestroying()) return;
 * this.rightClickDelay = 4;          // &lt;- единственная запись этого поля в игре
 * if (this.player.isHandsBusy()) return;
 * ... // использование предмета по hitResult (тоже есть ранние return)
 * </pre>
 * а {@code Minecraft#tick()} уменьшает поле на 1 каждый тик, и ПКМ обрабатывается заново только
 * когда поле равно 0. Хук ставится на саму запись поля ({@code PUTFIELD rightClickDelay:I}, сразу
 * после неё), а не на {@code @At("TAIL")}: у {@code startUseItem()} несколько ранних
 * {@code return}, и так хук срабатывает при любом пути выполнения метода.
 *
 * <p>Класс назван по моду специально: в нескольких модулях Fabric API тоже есть миксин
 * {@code MinecraftMixin}, а отдельное имя делает лог Mixin читаемым.
 */
@Mixin(Minecraft.class)
public class NoPlacementDelayMixin {
	@Shadow
	private int rightClickDelay;

	@Inject(method = "startUseItem",
			at = @At(value = "FIELD",
					target = "Lnet/minecraft/client/Minecraft;rightClickDelay:I",
					opcode = Opcodes.PUTFIELD,
					shift = At.Shift.AFTER))
	private void noplacementdelay$overrideDelay(CallbackInfo ci) {
		NoPlacementDelayConfig config = NoPlacementDelayConfig.get();
		if (config.enabled) {
			// 0 = следующее использование разрешено уже на следующем тике.
			this.rightClickDelay = config.effectiveDelay();
		}
	}
}