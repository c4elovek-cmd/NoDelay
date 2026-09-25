package com.example.nodelay.integration;

import com.example.nodelay.gui.NoDelayScreen;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Интеграция с Mod Menu (ленивая): класс загружается только тогда, когда Mod Menu установлен
 * и читает entrypoint {@code modmenu} из {@code fabric.mod.json}. Без Mod Menu этот класс даже не
 * попадает в рантайм, поэтому jar свободен от жёстких зависимостей. Кнопка «Config» ведёт на наш
 * нативный экран {@link NoDelayScreen}.
 */
public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> new NoDelayScreen();
	}
}