package com.example.nodelay;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Клиентская команда {@code /nodelay}. Загружается только при наличии Fabric API
 * (см. {@link NoDelayClient#onInitializeClient()}).
 *
 * <p>Управление происходит по категориям ({@code blocks}, {@code villagers}, {@code entities},
 * {@code items}): каждая включается/выключается и получает свою задержку.
 * <pre>
 * /nodelay                      статус
 * /nodelay on | off             главный выключатель
 * /nodelay blocks on | off      категория
 * /nodelay villagers delay 0    задержка категории (0..20 тиков)
 * /nodelay reload               перечитать config/nodelay.json
 * </pre>
 *
 * <p>Все сообщения берутся из языковых файлов {@code assets/nodelay/lang/*.json} через
 * {@link Component#translatable(String, Object...)}, поэтому текст подстраивается под язык игры.
 */
public final class NoDelayCommands {
	private NoDelayCommands() {
	}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			LiteralArgumentBuilder<FabricClientCommandSource> root = ClientCommands.literal("nodelay")
					.executes(context -> {
						context.getSource().sendFeedback(status());
						return Command.SINGLE_SUCCESS;
					})
					.then(ClientCommands.literal("on").executes(context -> {
						NoDelayConfig config = NoDelayConfig.get();
						config.enabled = true;
						NoDelayConfig.save();
						feedback(context.getSource(), "nodelay.message.enabled", ChatFormatting.GREEN);
						return Command.SINGLE_SUCCESS;
					}))
					.then(ClientCommands.literal("off").executes(context -> {
						NoDelayConfig config = NoDelayConfig.get();
						config.enabled = false;
						NoDelayConfig.save();
						feedback(context.getSource(), "nodelay.message.disabled", ChatFormatting.GRAY,
								NoDelayConfig.VANILLA_DELAY);
						return Command.SINGLE_SUCCESS;
					}))
					.then(ClientCommands.literal("reload").executes(context -> {
						NoDelayConfig.load();
						feedback(context.getSource(), "nodelay.message.reloaded", ChatFormatting.GREEN);
						return Command.SINGLE_SUCCESS;
					}));

			for (String name : NoDelayConfig.CATEGORIES) {
				root.then(category(name));
			}
			dispatcher.register(root);
		});
	}

	/** Подкоманды одной категории: {@code on}, {@code off}, {@code delay <ticks>}, статус. */
	private static LiteralArgumentBuilder<FabricClientCommandSource> category(String name) {
		return ClientCommands.literal(name)
				.executes(context -> {
					context.getSource().sendFeedback(categoryStatus(name));
					return Command.SINGLE_SUCCESS;
				})
				.then(ClientCommands.literal("on").executes(context -> {
					NoDelayConfig config = NoDelayConfig.get();
					NoDelayConfig.Category category = config.category(name);
					category.enabled = true;
					NoDelayConfig.save();
					feedback(context.getSource(), "nodelay.message.cat_on", ChatFormatting.GREEN,
							label(name), category.effectiveDelay());
					return Command.SINGLE_SUCCESS;
				}))
				.then(ClientCommands.literal("off").executes(context -> {
					NoDelayConfig config = NoDelayConfig.get();
					NoDelayConfig.Category category = config.category(name);
					category.enabled = false;
					NoDelayConfig.save();
					feedback(context.getSource(), "nodelay.message.cat_off", ChatFormatting.GRAY,
							label(name), NoDelayConfig.VANILLA_DELAY);
					return Command.SINGLE_SUCCESS;
				}))
				.then(ClientCommands.literal("delay")
						.then(ClientCommands.argument("ticks", IntegerArgumentType.integer(0, NoDelayConfig.MAX_DELAY))
								.executes(context -> {
									NoDelayConfig config = NoDelayConfig.get();
									NoDelayConfig.Category category = config.category(name);
									category.delay = IntegerArgumentType.getInteger(context, "ticks");
									category.enabled = true;
									NoDelayConfig.save();
									feedback(context.getSource(), "nodelay.message.cat_delay_set", ChatFormatting.GREEN,
											label(name), category.delay);
									return Command.SINGLE_SUCCESS;
								})));
	}

	/** Статус команды: главный выключатель + все категории. */
	private static Component status() {
		NoDelayConfig config = NoDelayConfig.get();
		MutableComponent status = msg("nodelay.message.status",
				translatable(config.enabled ? "nodelay.state.on" : "nodelay.state.off"))
				.withStyle(config.enabled ? ChatFormatting.GREEN : ChatFormatting.GRAY);
		for (String name : NoDelayConfig.CATEGORIES) {
			status.append("\n").append(categoryStatus(name));
		}
		return status;
	}

	private static Component categoryStatus(String name) {
		NoDelayConfig config = NoDelayConfig.get();
		NoDelayConfig.Category category = config.category(name);
		return msg("nodelay.message.cat_line",
				label(name),
				translatable(category.enabled ? "nodelay.state.on" : "nodelay.state.off"),
				config.delayFor(category))
				.withStyle(category.enabled ? ChatFormatting.GREEN : ChatFormatting.GRAY);
	}

	private static Component label(String name) {
		return translatable("nodelay.cat." + name);
	}

	private static void feedback(FabricClientCommandSource source, String key, ChatFormatting style, Object... args) {
		source.sendFeedback(msg(key, args).withStyle(style));
	}

	private static Component translatable(String key) {
		return Component.translatable(key);
	}

	private static MutableComponent msg(String key, Object... args) {
		return Component.translatable(key, args);
	}
}