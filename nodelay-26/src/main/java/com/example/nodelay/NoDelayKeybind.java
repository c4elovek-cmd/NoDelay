package com.example.nodelay;

import com.example.nodelay.gui.NoDelayScreen;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import org.lwjgl.glfw.GLFW;

/**
 * Хоткей для открытия экрана настроек NoDelay.
 *
 * <p>Клавиша по умолчанию — {@code N} (от NoDelay), переназначается в «Управление → Кнопки»,
 * отдельная категория «NoDelay». Регистрация идёт через Fabric API
 * ({@code KeyMappingHelper}), поэтому хоткей доступен только при установленном Fabric API — так
 * же, как и команда {@code /nodelay}; без него настройки открываются кнопкой на экране паузы
 * или правкой {@code config/nodelay.json}.
 *
 * <p>Нажатия снимаются в конце клиентского тика ({@code END_CLIENT_TICK}): ваниль сама дренирует
 * клики только собственных {@code KeyMapping}, модовый нужно снимать самостоятельно. Экран
 * открывается только когда нет открытого GUI (ваниль не ставит клик keyбинда при открытом
 * экране, но guard защищает от накопления и открытия поверх чужого экрана).
 */
public final class NoDelayKeybind {
	/** Отдельная категория раздела в «Управление → Кнопки». */
	private static final KeyMapping.Category CATEGORY =
			KeyMapping.Category.register(Identifier.fromNamespaceAndPath("nodelay", "settings"));

	/** Зарегистрированный keybind; null, пока хоткей не подключён (нет Fabric API). */
	private static KeyMapping openConfig;

	private NoDelayKeybind() {
	}

	/** Регистрирует хоткей и подписывает его дренирование на конец клиентского тика. */
	public static void register() {
		if (openConfig != null) {
			return;
		}
		openConfig = KeyMappingHelper.registerKeyMapping(
				new KeyMapping("key.nodelay.open", GLFW.GLFW_KEY_N, CATEGORY));
		ClientTickEvents.END_CLIENT_TICK.register(NoDelayKeybind::onEndTick);
	}

	private static void onEndTick(Minecraft minecraft) {
		while (openConfig.consumeClick()) {
			if (minecraft.gui.screen() == null) {
				minecraft.setScreenAndShow(new NoDelayScreen());
			}
		}
	}
}