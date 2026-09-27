package com.example.nodelay.gui;

import com.example.nodelay.NoDelayConfig;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Нативный экран настроек NoDelay, построенный только из ванильных виджетов — без каких-либо
 * внешних зависимостей. Работает на любом лоадере с поддержкой Mixin (Fabric, Quilt, Forge,
 * NeoForge), поэтому является запасным вариантом, когда не установлен Mod Menu / Cloth Config.
 *
 * <p><b>Всё видно сразу, без прокрутки.</b> Высота строк, отступы и наличие заголовка/кнопки
 * «Готово» подбираются под фактическую высоту окна ({@link #chooseMetrics()}), поэтому заголовок,
 * главный переключатель, все семь категорий и «Готово» всегда помещаются на экране целиком.
 *
 * <p><b>Текст не обрезается.</b> Название категории рисуется обычным текстом ({@link StringWidget})
 * шириной ровно под саму строку, а не кнопкой фиксированной ширины, поэтому длинные переводы вроде
 * русского «Размещение и использование блоков» читаются целиком. Ширина колонки кнопок считается от
 * реальной ширины подписей «вкл»/«выкл»; если окно всё же слишком узкое, название сокращается
 * многоточием, но никогда не наезжает на кнопки.
 *
 * <p>Вертикально: заголовок, главный переключатель во всю ширину, затем по строке на категорию
 * (название + переключатель вкл/выкл + кнопки {@code -}/{@code +} и текущая задержка), внизу —
 * «Готово». Любое изменение сразу же сохраняется в {@code config/nodelay.json}.
 */
public class NoDelayScreen extends Screen {
	/**
	 * Варианты вёрстки {@code {высота строки, вертикальный зазор, боковой отступ}} — от просторного
	 * к компактному. Первый, который целиком помещается в окне, и берётся.
	 */
	private static final int[][] LAYOUTS = { { 20, 4, 10 }, { 20, 3, 8 }, { 18, 3, 6 }, { 16, 2, 6 } };
	/** Высота и ширина нижней кнопки «Готово». */
	private static final int DONE_WIDTH = 100;
	private static final int DONE_HEIGHT = 20;
	/** Ширина кнопок {@code -}/{@code +}. */
	private static final int STEP_WIDTH = 20;
	/**
	 * Фон экрана: почти непрозрачный тёмный. Ванильная обработка в главном меню (когда мира нет)
	 * рисует за экраном крутящуюся панораму, на которой текст почти не читается; здесь фон
	 * статичный, поэтому подписи читаются при любой сцене.
	 */
	private static final int BACKGROUND = 0xF2101010;

	private final NoDelayConfig config = NoDelayConfig.get();

	/** Выбранная вёрстка (см. {@link #chooseMetrics()}). */
	private int rowHeight;
	private int rowGap;
	private int margin;
	private boolean showTitle;
	private boolean showDone;

	/** Кнопка главного выключателя. */
	private Button masterButton;
	/** По кнопке на категорию: переключатель вкл/выкл и пара {@code -}/{@code +} для задержки. */
	private final Button[] stateButtons = new Button[NoDelayConfig.CATEGORIES.length];
	private final Button[] minusButtons = new Button[NoDelayConfig.CATEGORIES.length];
	private final Button[] plusButtons = new Button[NoDelayConfig.CATEGORIES.length];
	/** Название категории и её текущая задержка — обычный текст, а не кнопки. */
	private final StringWidget[] nameLabels = new StringWidget[NoDelayConfig.CATEGORIES.length];
	private final StringWidget[] delayLabels = new StringWidget[NoDelayConfig.CATEGORIES.length];

	public NoDelayScreen() {
		super(Component.translatable("nodelay.gui.title"));
	}

	/**
	 * Статичный тёмный фон вместо дефолтной ванильной обработки: та в главном меню (когда мира
	 * нет) рисует за экраном крутящуюся панораму, а размытие мира в игре местами оставляет
	 * светлые пятна под светлым текстом. Здесь — один ровный тёмный слой на весь экран.
	 */
	@Override
	public void extractBackground(GuiGraphicsExtractor extractor, int width, int height, float partialTick) {
		extractor.fill(0, 0, this.width, this.height, BACKGROUND);
	}

	@Override
	protected void init() {
		this.chooseMetrics();

		int contentWidth = Math.max(80, this.width - 2 * this.margin);
		int stepWidth = Math.min(STEP_WIDTH, Math.max(16, contentWidth / 10));
		int stateWidth = Math.max(28, this.measure(Component.translatable("nodelay.state.off")) + 10);
		int delayWidth = this.measure(Component.literal(String.valueOf(NoDelayConfig.MAX_DELAY))) + 4;
		int controlsWidth = stateWidth + 2 * this.rowGap + 2 * stepWidth + delayWidth;
		// Столько остаётся под название: при нехватке места название укорачивается, а не кнопки.
		int nameWidth = Math.max(40, contentWidth - controlsWidth - this.rowGap);

		int y = this.margin;
		if (this.showTitle) {
			int titleWidth = this.measure(this.title);
			this.addRenderableWidget(new StringWidget((this.width - titleWidth) / 2, y, titleWidth,
					this.font.lineHeight, this.title, this.font));
			y += this.font.lineHeight + 4;
		}

		this.masterButton = Button.builder(this.masterLabel(), button -> {
			this.config.enabled = !this.config.enabled;
			NoDelayConfig.save();
			this.refresh();
		}).bounds(this.margin, y, contentWidth, this.rowHeight).build();
		this.addRenderableWidget(this.masterButton);
		y += this.rowHeight + this.rowGap;

		int controlsX = this.margin + contentWidth - controlsWidth;
		for (int i = 0; i < NoDelayConfig.CATEGORIES.length; i++) {
			final String name = NoDelayConfig.CATEGORIES[i];

			int textY = y + (this.rowHeight - this.font.lineHeight) / 2;
			Component label = Component.translatable("nodelay.cat." + name);
			int labelWidth = Math.min(nameWidth, this.measure(label));
			if (labelWidth < this.measure(label)) {
				label = Component.literal(this.font.plainSubstrByWidth(label.getString(), labelWidth));
				labelWidth = this.measure(label);
			}
			this.nameLabels[i] = new StringWidget(this.margin, textY, labelWidth, this.font.lineHeight,
					label, this.font);

			int x = controlsX;
			this.stateButtons[i] = Button.builder(Component.empty(), button -> {
				NoDelayConfig.Category category = this.config.category(name);
				category.enabled = !category.enabled;
				NoDelayConfig.save();
				this.refresh();
			}).bounds(x, y, stateWidth, this.rowHeight).build();
			x += stateWidth + this.rowGap;
			this.minusButtons[i] = Button.builder(Component.literal("-"), button -> {
				NoDelayConfig.Category category = this.config.category(name);
				category.delay = Math.max(0, category.delay - 1);
				category.enabled = true;
				NoDelayConfig.save();
				this.refresh();
			}).bounds(x, y, stepWidth, this.rowHeight).build();
			x += stepWidth + this.rowGap;
			this.delayLabels[i] = new StringWidget(x, textY, delayWidth, this.font.lineHeight,
					Component.literal("0"), this.font);
			x += delayWidth + this.rowGap;
			this.plusButtons[i] = Button.builder(Component.literal("+"), button -> {
				NoDelayConfig.Category category = this.config.category(name);
				category.delay = Math.min(NoDelayConfig.MAX_DELAY, category.delay + 1);
				category.enabled = true;
				NoDelayConfig.save();
				this.refresh();
			}).bounds(x, y, stepWidth, this.rowHeight).build();

			this.addRenderableWidget(this.nameLabels[i]);
			this.addRenderableWidget(this.stateButtons[i]);
			this.addRenderableWidget(this.minusButtons[i]);
			this.addRenderableWidget(this.delayLabels[i]);
			this.addRenderableWidget(this.plusButtons[i]);
			y += this.rowHeight + this.rowGap;
		}

		if (this.showDone) {
			this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
					.bounds((this.width - DONE_WIDTH) / 2, y, DONE_WIDTH, DONE_HEIGHT).build());
		}

		this.refresh();
	}

	/**
	 * Подбирает вёрстку под высоту окна: перебирает {@link #LAYOUTS} от просторного к компактному и
	 * берёт первый, что помещается целиком. Если не помещается даже компактный (окно ниже ~150
	 * пикселей), последовательно убираются «Готово» и заголовок — так все семь категорий и главный
	 * переключатель остаются на экране. Прокрутки нет ни в одном случае.
	 */
	private void chooseMetrics() {
		for (int[] layout : LAYOUTS) {
			this.rowHeight = layout[0];
			this.rowGap = layout[1];
			this.margin = layout[2];
			this.showTitle = true;
			this.showDone = true;
			if (this.totalHeight(true, true) <= this.height) {
				return;
			}
		}

		int[] smallest = LAYOUTS[LAYOUTS.length - 1];
		this.rowHeight = smallest[0];
		this.rowGap = smallest[1];
		this.margin = smallest[2];
		// Решения принимаются по очереди, чтобы итоговая комбинация гарантированно влезала:
		// сначала жертвуем «Готово», затем заголовок, но уже с учётом того, что убрали.
		this.showDone = this.totalHeight(true, false) <= this.height;
		this.showTitle = this.totalHeight(this.showDone, true) <= this.height;
	}

	/** Высота всей вёрстки при заданных флагах заголовка и кнопки «Готово». */
	private int totalHeight(boolean title, boolean done) {
		int height = this.margin;
		if (title) {
			height += this.font.lineHeight + 4;
		}
		// Главный переключатель + по строке на каждую из категорий.
		height += (NoDelayConfig.CATEGORIES.length + 1) * (this.rowHeight + this.rowGap);
		if (done) {
			height += DONE_HEIGHT + this.rowGap;
		}
		return height + this.margin;
	}

	/** Ширина подписи в пикселях; меряется строка, чтобы не зависеть от версии {@code Component}. */
	private int measure(Component text) {
		return this.font.width(text.getString());
	}

	/** Обновляет подписи после любого изменения конфига. */
	private void refresh() {
		this.masterButton.setMessage(this.masterLabel());
		for (int i = 0; i < NoDelayConfig.CATEGORIES.length; i++) {
			NoDelayConfig.Category category = this.config.category(NoDelayConfig.CATEGORIES[i]);
			Component state = Component.translatable(category.enabled ? "nodelay.state.on" : "nodelay.state.off");
			this.stateButtons[i].setMessage(state);
			this.delayLabels[i].setMessage(Component.literal(String.valueOf(this.config.delayFor(category))));
		}
	}

	private Component masterLabel() {
		Component state = Component.translatable(this.config.enabled ? "nodelay.state.on" : "nodelay.state.off");
		return Component.translatable("nodelay.gui.master", state);
	}
}
