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
 * <p><b>Почему миксин на {@link Screen}, а не на {@link PauseScreen}:</b> {@code @Shadow}
 * резолвится только по членам, объявленным непосредственно в целевом классе, а
 * {@code addRenderableWidget}/{@code minecraft}/{@code width}/{@code height} объявлены в
 * {@code Screen} и лишь наследуются {@code PauseScreen} — поэтому при
 * {@code @Mixin(PauseScreen.class)} применение падало. Все {@code @Shadow} здесь указывают на
 * члены, объявленные прямо в {@code Screen}, так что резолв проходит штатно.
 *
 * <p><b>Точка впрыска — {@code Screen#init()}</b>, вызываемая при открытии любого экрана; для
 * экрана паузы она исполняется после построения меню, поэтому кнопка добавляется после всех
 * ванильных виджетов. Для всех остальных экранов создание кнопки отсекается проверкой
 * {@code instanceof PauseScreen}. Сигнатура {@code init()V} указана явно, чтобы не задеть
 * одноимённый приватный {@code init(Minecraft, int, int)}.
 */
@Mixin(Screen.class)
public abstract class PauseScreenMixin {
	@Shadow
	protected Minecraft minecraft;
	@Shadow
	public int width;
	@Shadow
	public int height;

	@Shadow
	protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget);

	@Inject(method = "init()V", at = @At("TAIL"))
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