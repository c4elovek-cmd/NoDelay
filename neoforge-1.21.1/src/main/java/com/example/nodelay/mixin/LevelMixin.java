package com.example.nodelay.mixin;

import com.example.nodelay.crystal.CrystalOptimizer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Predicate;

/**
 * Прячет удерживаемые кристаллы из выборки сущностей по предикату (вне зависимости от того,
 * откуда идёт выбор — список пуст, когда оптимизатор выключен).
 */
@Mixin(Level.class)
public abstract class LevelMixin {

	// Имя параметра меняется по версиям (`predicate` до 1.21.11, `selector` с 26.1), поэтому
	// без name= — только дескриптор метода и argsOnly.
	@SuppressWarnings({"ModifyVariableMayUseName", "NameDoesntMatchTargetClass"})
	@ModifyVariable(
			method = "getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;",
			at = @At("HEAD"),
			argsOnly = true)
	private Predicate<? super Entity> nodelay$hideKeptCrystals(Predicate<? super Entity> predicate) {
		if (!((Level) (Object) this).isClientSide()) {
			return predicate;
		}

		return CrystalOptimizer.get().keptCrystals().hide(predicate);
	}
}