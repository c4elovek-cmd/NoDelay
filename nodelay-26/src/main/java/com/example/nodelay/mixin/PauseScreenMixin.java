package com.example.nodelay.mixin;

import com.example.nodelay.gui.NoDelayScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Добавляет кнопку «NoDelay» в правый нижний угол экрана паузы. Кнопка открывает нативный экран
 * настроек мода {@link NoDelayScreen} (без внешних зависимостей; при наличии Mod Menu туда же
 * добавляется кнопка «Config»).
 *
 * <p><b>Почему миксин на {@link Screen}, а не на {@link PauseScreen}:</b> в рантайм-Mixin, на
 * котором работает Fabric Loader 0.19.x + MC 26.2 (sponge-mixin 0.17.4), {@code @Shadow}
 * резолвится ТОЛЬКО по членам, объявленным непосредственно в целевом классе
 * ({@code TargetClassContext.findAliasedMethod} перебирает лишь {@code classNode.methods}).
 * {@code addRenderableWidget}, {@code minecraft}, {@code width} и {@code height} объявлены в
 * {@code Screen} и лишь наследуются {@code PauseScreen} — поэтому при
 * {@code @Mixin(PauseScreen.class)} применение падало с
 * {@code @Shadow method addRenderableWidget(...) was not located in the target class PauseScreen.
 * No refMap loaded.} Все {@code @Shadow} здесь указывают на члены, объявленные прямо в
 * {@code Screen} (теперь это целевой класс), так что резолв проходит штатно.
 *
 * <p><b>Точка впрыска — {@code Screen#init(int, int)}</b>, финальный метод жизненного цикла,
 * который вызывается для любого экрана и который PauseScreen не переопределяет. Для экрана паузы
 * он исполняется сразу после {@code PauseScreen#init()} (строящего меню через
 * {@code createPauseMenu}), поэтому кнопка добавляется после всех ванильных виджетов. При resize
 * {@code init(int, int)} перестраивает виджеты через {@code repositionElements()} — кнопка
 * добавляется заново в нужной позиции, дубликатов не накапливается. Для всех остальных экранов
 * создание кнопки отсекается проверкой {@code instanceof PauseScreen}. Сигнатура {@code init(II)V}
 * указана явно, чтобы не задеть одноимённый {@code init()}.
 */
@Mixin(Screen.class)
public abstract class PauseScreenMixin {
	// 26.2 не обфусцирован: официальные имена уже являются рантайм-именами, рефмапа нет.
	// remap = false запрещает Mixin искать члены в "обфусцированных" именах.
	@Shadow(remap = false) protected Minecraft minecraft;
	@Shadow(remap = false) public int width;
	@Shadow(remap = false) public int height;

	@Shadow(remap = false)
	protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget);

	@Inject(method = "init(II)V", remap = false, at = @At("TAIL"))
	private void nodelay$addButton(CallbackInfo ci) {
		if (!((Object) this instanceof PauseScreen)) {
			return;
		}

		int buttonWidth = 90;
		int buttonHeight = 20;
		this.addRenderableWidget(Button.builder(
				Component.literal("NoDelay"),
				button -> this.minecraft.setScreenAndShow(new NoDelayScreen()))
				.bounds(this.width - buttonWidth - 4, this.height - buttonHeight - 4,
						buttonWidth, buttonHeight)
				.build());
	}
}