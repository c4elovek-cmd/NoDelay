package com.example.nodelay.gui;

import com.example.nodelay.NoDelayConfig;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Нативный экран настроек NoDelay, построенный только из ванильных виджетов — без каких-либо
 * внешних зависимостей. Работает на любом лоадере с поддержкой Mixin (Fabric, Quilt, Forge,
 * NeoForge), поэтому является запасным вариантом, когда не установлен Mod Menu / Cloth Config.
 *
 * <p>Вертикально: заголовок, главный выключатель, затем по строке на категорию. У каждой
 * категории один широкий циклический переключатель (имя + состояние + задержка) и пара кнопок
 * {@code -}/{@code +}, меняющих задержку в диапазоне 0..{@value NoDelayConfig#MAX_DELAY} тиков.
 * Любое изменение сразу же сохраняется в {@code config/nodelay.json}.
 */
public class NoDelayScreen extends Screen {
	private final NoDelayConfig config = NoDelayConfig.get();

	/** Кнопка главного выключателя. */
	private Button masterButton;
	/** Кнопки категорий: одна широкая (вкл/выкл) плюс минус/плюс для задержки. */
	private final Button[] categoryButtons = new Button[NoDelayConfig.CATEGORIES.length];
	private final Button[] minusButtons = new Button[NoDelayConfig.CATEGORIES.length];
	private final Button[] plusButtons = new Button[NoDelayConfig.CATEGORIES.length];

	public NoDelayScreen() {
		super(Component.translatable("nodelay.gui.title"));
	}

	/**
	 * Статичный читаемый фон вместо дефолтной ванильной обработки: та, в главном меню (когда
	 * мира нет) рисует за экраном крутящуюся панораму, на которой текст почти не читается.
	 * Здесь — как у экрана паузы: размытие мира (если включено в настройках) + тёмная текстура
	 * меню. Панорама не рисуется вообще.
	 */
	@Override
	public void extractBackground(GuiGraphicsExtractor extractor, int width, int height, float partialTick) {
		this.extractBlurredBackground(extractor);
		this.extractMenuBackground(extractor);
	}

	@Override
	protected void init() {
		this.addRenderableWidget(new StringWidget(0, 12, this.width, this.font.lineHeight, this.title, this.font));

		int centerX = this.width / 2;
		int gap = 4;
		int toggleWidth = 150;
		int stepWidth = 20;
		int rowWidth = toggleWidth + gap + stepWidth + gap + stepWidth;
		int startX = centerX - rowWidth / 2;
		int y = 40;

		this.masterButton = Button.builder(this.masterLabel(), button -> {
			this.config.enabled = !this.config.enabled;
			NoDelayConfig.save();
			this.refresh();
		}).bounds(startX, y, rowWidth, 20).build();
		this.addRenderableWidget(this.masterButton);
		y += 24;

		for (int i = 0; i < NoDelayConfig.CATEGORIES.length; i++) {
			final int index = i;
			String name = NoDelayConfig.CATEGORIES[i];

			this.categoryButtons[i] = Button.builder(Component.empty(), button -> {
				NoDelayConfig.Category category = this.config.category(name);
				category.enabled = !category.enabled;
				NoDelayConfig.save();
				this.refresh();
			}).bounds(startX, y, toggleWidth, 20).build();
			this.minusButtons[i] = Button.builder(Component.literal("-"), button -> {
				NoDelayConfig.Category category = this.config.category(name);
				category.delay = Math.max(0, category.delay - 1);
				category.enabled = true;
				NoDelayConfig.save();
				this.refresh();
			}).bounds(startX + toggleWidth + gap, y, stepWidth, 20).build();
			this.plusButtons[i] = Button.builder(Component.literal("+"), button -> {
				NoDelayConfig.Category category = this.config.category(name);
				category.delay = Math.min(NoDelayConfig.MAX_DELAY, category.delay + 1);
				category.enabled = true;
				NoDelayConfig.save();
				this.refresh();
			}).bounds(startX + toggleWidth + gap + stepWidth + gap, y, stepWidth, 20).build();

			this.addRenderableWidget(this.categoryButtons[i]);
			this.addRenderableWidget(this.minusButtons[i]);
			this.addRenderableWidget(this.plusButtons[i]);
			y += 24;
		}

		this.refresh();
	}

	/** Обновляет подписи кнопок после любого изменения конфига. */
	private void refresh() {
		this.masterButton.setMessage(this.masterLabel());
		for (int i = 0; i < NoDelayConfig.CATEGORIES.length; i++) {
			String name = NoDelayConfig.CATEGORIES[i];
			NoDelayConfig.Category category = this.config.category(name);
			Component state = Component.translatable(category.enabled ? "nodelay.state.on" : "nodelay.state.off");
			Component label = Component.translatable("nodelay.cat." + name);
			this.categoryButtons[i].setMessage(Component.translatable("nodelay.message.cat_line",
					label, state, this.config.delayFor(category)));
		}
	}

	private Component masterLabel() {
		Component state = Component.translatable(this.config.enabled ? "nodelay.state.on" : "nodelay.state.off");
		return Component.translatable("nodelay.gui.master", state);
	}
}