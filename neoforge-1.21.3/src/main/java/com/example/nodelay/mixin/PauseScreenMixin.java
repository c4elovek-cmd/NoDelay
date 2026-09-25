package com.example.nodelay.mixin;

import com.example.nodelay.gui.NoDelayScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Добавляет кнопку «NoDelay» в правый нижний угол экрана паузы. Вход в нативный экран настроек
 * без внешних зависимостей; при наличии Mod Menu туда же добавляется кнопка «Config».
 */
@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin {
	@Shadow protected Minecraft minecraft;
	@Shadow public int width;
	@Shadow public int height;

	@Shadow
	protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget);

	@Inject(method = "init", at = @At("TAIL"))
	private void nodelay$addButton(CallbackInfo ci) {
		int buttonWidth = 90;
		int buttonHeight = 20;
		this.addRenderableWidget(Button.builder(
				Component.literal("NoDelay"),
				button -> this.minecraft.setScreen(new NoDelayScreen()))
				.bounds(this.width - buttonWidth - 4, this.height - buttonHeight - 4,
						buttonWidth, buttonHeight)
				.build());
	}
}