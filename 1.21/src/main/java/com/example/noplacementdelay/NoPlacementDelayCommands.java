package com.example.noplacementdelay;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Клиентская команда {@code /noplacementdelay}. Загружается только при наличии Fabric API
 * (см. {@link NoPlacementDelayClient#onInitializeClient()}).
 *
 * <p>Все сообщения берутся из языковых файлов {@code assets/noplacementdelay/lang/*.json} через
 * {@link Component#translatable(String, Object...)}, поэтому текст подстраивается под язык игры
 * (русский интерфейс — русский текст, любой другой — английский или язык перевода).
 */
public final class NoPlacementDelayCommands {
	private NoPlacementDelayCommands() {
	}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal("noplacementdelay")
						.executes(context -> {
							context.getSource().sendFeedback(status());
							return Command.SINGLE_SUCCESS;
						})
						.then(ClientCommandManager.literal("on").executes(context -> {
							NoPlacementDelayConfig config = NoPlacementDelayConfig.get();
							config.enabled = true;
							NoPlacementDelayConfig.save();
							context.getSource().sendFeedback(msg("noplacementdelay.message.enabled",
									config.effectiveDelay()).withStyle(ChatFormatting.GREEN));
							return Command.SINGLE_SUCCESS;
						}))
						.then(ClientCommandManager.literal("off").executes(context -> {
							NoPlacementDelayConfig config = NoPlacementDelayConfig.get();
							config.enabled = false;
							NoPlacementDelayConfig.save();
							context.getSource().sendFeedback(msg("noplacementdelay.message.disabled",
									NoPlacementDelayConfig.VANILLA_DELAY).withStyle(ChatFormatting.GRAY));
							return Command.SINGLE_SUCCESS;
						}))
						.then(ClientCommandManager.literal("delay")
								.then(ClientCommandManager.argument("ticks", IntegerArgumentType.integer(0, NoPlacementDelayConfig.MAX_DELAY))
										.executes(context -> {
											NoPlacementDelayConfig config = NoPlacementDelayConfig.get();
											config.delay = IntegerArgumentType.getInteger(context, "ticks");
											config.enabled = true;
											NoPlacementDelayConfig.save();
											context.getSource().sendFeedback(msg("noplacementdelay.message.delay_set",
													config.effectiveDelay()).withStyle(ChatFormatting.GREEN));
											return Command.SINGLE_SUCCESS;
										})))
						.then(ClientCommandManager.literal("reload").executes(context -> {
							NoPlacementDelayConfig.load();
							context.getSource().sendFeedback(msg("noplacementdelay.message.reloaded")
									.withStyle(ChatFormatting.GREEN));
							return Command.SINGLE_SUCCESS;
						}))
		));
	}

	/** Текущее состояние: включён/выключен и задержка. */
	private static Component status() {
		NoPlacementDelayConfig config = NoPlacementDelayConfig.get();
		return msg("noplacementdelay.message.status",
				translatable(config.enabled ? "noplacementdelay.state.on" : "noplacementdelay.state.off"),
				config.effectiveDelay(),
				NoPlacementDelayConfig.VANILLA_DELAY)
				.withStyle(config.enabled ? ChatFormatting.GREEN : ChatFormatting.GRAY);
	}

	private static Component translatable(String key) {
		return Component.translatable(key);
	}

	private static MutableComponent msg(String key, Object... args) {
		return Component.translatable(key, args);
	}
}