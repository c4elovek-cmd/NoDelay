package com.example.nodelay;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * РљР»РёРµРЅС‚СЃРєР°СЏ РєРѕРјР°РЅРґР° {@code /nodelay}. Р—Р°РіСЂСѓР¶Р°РµС‚СЃСЏ С‚РѕР»СЊРєРѕ РїСЂРё РЅР°Р»РёС‡РёРё Fabric API
 * (СЃРј. {@link NoDelayClient#onInitializeClient()}).
 *
 * <p>РЈРїСЂР°РІР»РµРЅРёРµ РїСЂРѕРёСЃС…РѕРґРёС‚ РїРѕ РєР°С‚РµРіРѕСЂРёСЏРј ({@code blocks}, {@code villagers}, {@code entities},
 * {@code items}): РєР°Р¶РґР°СЏ РІРєР»СЋС‡Р°РµС‚СЃСЏ/РІС‹РєР»СЋС‡Р°РµС‚СЃСЏ Рё РїРѕР»СѓС‡Р°РµС‚ СЃРІРѕСЋ Р·Р°РґРµСЂР¶РєСѓ.
 * <pre>
 * /nodelay                      СЃС‚Р°С‚СѓСЃ
 * /nodelay on | off             РіР»Р°РІРЅС‹Р№ РІС‹РєР»СЋС‡Р°С‚РµР»СЊ
 * /nodelay blocks on | off      РєР°С‚РµРіРѕСЂРёСЏ
 * /nodelay villagers delay 0    Р·Р°РґРµСЂР¶РєР° РєР°С‚РµРіРѕСЂРёРё (0..20 С‚РёРєРѕРІ)
 * /nodelay reload               РїРµСЂРµС‡РёС‚Р°С‚СЊ config/nodelay.json
 * </pre>
 *
 * <p>Р’СЃРµ СЃРѕРѕР±С‰РµРЅРёСЏ Р±РµСЂСѓС‚СЃСЏ РёР· СЏР·С‹РєРѕРІС‹С… С„Р°Р№Р»РѕРІ {@code assets/nodelay/lang/*.json} С‡РµСЂРµР·
 * {@link Component#translatable(String, Object...)}, РїРѕСЌС‚РѕРјСѓ С‚РµРєСЃС‚ РїРѕРґСЃС‚СЂР°РёРІР°РµС‚СЃСЏ РїРѕРґ СЏР·С‹Рє РёРіСЂС‹.
 */
public final class NoDelayCommands {
	private NoDelayCommands() {
	}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			LiteralArgumentBuilder<FabricClientCommandSource> root = ClientCommandManager.literal("nodelay")
					.executes(context -> {
						context.getSource().sendFeedback(status());
						return Command.SINGLE_SUCCESS;
					})
					.then(ClientCommandManager.literal("on").executes(context -> {
						NoDelayConfig config = NoDelayConfig.get();
						config.enabled = true;
						NoDelayConfig.save();
						feedback(context.getSource(), "nodelay.message.enabled", ChatFormatting.GREEN);
						return Command.SINGLE_SUCCESS;
					}))
					.then(ClientCommandManager.literal("off").executes(context -> {
						NoDelayConfig config = NoDelayConfig.get();
						config.enabled = false;
						NoDelayConfig.save();
						feedback(context.getSource(), "nodelay.message.disabled", ChatFormatting.GRAY,
								NoDelayConfig.VANILLA_DELAY);
						return Command.SINGLE_SUCCESS;
					}))
					.then(ClientCommandManager.literal("reload").executes(context -> {
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

	/** РџРѕРґРєРѕРјР°РЅРґС‹ РѕРґРЅРѕР№ РєР°С‚РµРіРѕСЂРёРё: {@code on}, {@code off}, {@code delay <ticks>}, СЃС‚Р°С‚СѓСЃ. */
	private static LiteralArgumentBuilder<FabricClientCommandSource> category(String name) {
		return ClientCommandManager.literal(name)
				.executes(context -> {
					context.getSource().sendFeedback(categoryStatus(name));
					return Command.SINGLE_SUCCESS;
				})
				.then(ClientCommandManager.literal("on").executes(context -> {
					NoDelayConfig config = NoDelayConfig.get();
					NoDelayConfig.Category category = config.category(name);
					category.enabled = true;
					NoDelayConfig.save();
					feedback(context.getSource(), "nodelay.message.cat_on", ChatFormatting.GREEN,
							label(name), category.effectiveDelay());
					return Command.SINGLE_SUCCESS;
				}))
				.then(ClientCommandManager.literal("off").executes(context -> {
					NoDelayConfig config = NoDelayConfig.get();
					NoDelayConfig.Category category = config.category(name);
					category.enabled = false;
					NoDelayConfig.save();
					feedback(context.getSource(), "nodelay.message.cat_off", ChatFormatting.GRAY,
							label(name), NoDelayConfig.VANILLA_DELAY);
					return Command.SINGLE_SUCCESS;
				}))
				.then(ClientCommandManager.literal("delay")
						.then(ClientCommandManager.argument("ticks", IntegerArgumentType.integer(0, NoDelayConfig.MAX_DELAY))
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

	/** РЎС‚Р°С‚СѓСЃ РєРѕРјР°РЅРґС‹: РіР»Р°РІРЅС‹Р№ РІС‹РєР»СЋС‡Р°С‚РµР»СЊ + РІСЃРµ РєР°С‚РµРіРѕСЂРёРё. */
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
