package com.example.nodelay.mixin;

import com.example.nodelay.NoDelayConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
 * Выборочно убирает ванильную задержку правого клика.
 *
 * <p>В 26.2 (как и в 1.21.x) {@code Minecraft#startUseItem()} начинается так:
 * <pre>
 * if (this.gameMode.isDestroying()) return;
 * this.rightClickDelay = 4;   // &lt;- единственная запись этого поля в игре
 * if (this.player.isHandsBusy()) return;
 * ... // свитч по this.hitResult.getType(): ENTITY / BLOCK / MISS
 * </pre>
 * а {@code Minecraft#tick()} уменьшает поле на 1 каждый тик, и ПКМ обрабатывается заново только
 * когда поле равно 0. Хук ставится на саму запись поля ({@code PUTFIELD rightClickDelay:I}, сразу
 * после неё), а не на {@code @At("TAIL")}: у {@code startUseItem()} несколько ранних
 * {@code return}, и так хук срабатывает при любом пути выполнения метода.
 *
 * <p>Категория действия определяется по предмету в главной руке (кристалл, ведро, элитра) и по
 * {@code Minecraft#hitResult}: блок — установка блоков, сущность — житель или «другая сущность»,
 * пустота — использование предмета. Задержка каждой категории берётся из {@link NoDelayConfig};
 * выключенная категория возвращает ванильные 4 тика.
 *
 * <p>Класс назван по моду специально: в нескольких модулях Fabric API тоже есть миксин
 * {@code MinecraftMixin}, а отдельное имя делает лог Mixin читаемым.
 */
@Mixin(Minecraft.class)
public class NoDelayMixin {
	// 26.2 не обфусцирован: официальные имена уже являются рантайм-именами, рефмапа нет.
	// remap = false запрещает Mixin искать члены в "обфусцированных" именах.
	@Shadow(remap = false)
	private int rightClickDelay;

	@Shadow(remap = false)
	public HitResult hitResult;

	@Shadow(remap = false)
	public LocalPlayer player;

	@Inject(method = "startUseItem", remap = false,
			at = @At(value = "FIELD",
					target = "Lnet/minecraft/client/Minecraft;rightClickDelay:I",
					opcode = Opcodes.PUTFIELD,
					shift = At.Shift.AFTER))
	private void nodelay$overrideDelay(CallbackInfo ci) {
		NoDelayConfig config = NoDelayConfig.get();
		if (!config.enabled || this.player == null) {
			return;
		}

		int delay = NoDelayConfig.VANILLA_DELAY;
		ItemStack stack = this.player.getMainHandItem();
		if (stack.is(Items.END_CRYSTAL)) {
			delay = config.delayFor(config.crystals);
		} else if (stack.is(Items.WATER_BUCKET) || stack.is(Items.LAVA_BUCKET)) {
			delay = config.delayFor(config.buckets);
		} else if (stack.is(Items.ELYTRA)) {
			delay = config.delayFor(config.elytra);
		} else if (this.hitResult instanceof BlockHitResult blockHit) {
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

		// 0 = следующее использование разрешено уже на следующем тике.
		this.rightClickDelay = delay;
	}
}