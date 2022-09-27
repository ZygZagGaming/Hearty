package com.zygzag.hearty;

import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.function.TriFunction;

import java.util.List;

public interface HeartType {
    HeartType HEALTH = basicSuppliers(
            1.0,
            (player, world, gui) -> Mth.ceil(player.getHealth()),
            (player, world, gui) -> {
                boolean isHealthBlinking = gui.healthBlinkTime > (long)gui.tickCount && (gui.healthBlinkTime - (long)gui.tickCount) / 3L % 2L == 1L;
                return new ResourceLocation("hearty:textures/hearts/vanilla/" + (
                        player.hasEffect(MobEffects.POISON) && !(player.hasEffect(MobEffects.WITHER) && HeartyConfig.WITHER_OVERRIDE_POISON.get()) ? "poisoned" :
                        player.hasEffect(MobEffects.WITHER) ? "withered" :
                        player.isFullyFrozen() ? "frozen" :
                        "full"
                ) + (
                        world.getLevelData().isHardcore() ? "_hardcore" : ""
                ) + (
                        isHealthBlinking ? "_blinking" : ""
                ) + ".png");
            }
    );
    HeartType EMPTY = basicWithBlink(
            2.0,
            (player, world, gui) -> Mth.ceil(player.getMaxHealth()) - Mth.ceil(player.getHealth()),
            new ResourceLocation("hearty:textures/hearts/vanilla/empty.png"),
            new ResourceLocation("hearty:textures/hearts/vanilla/empty_blinking.png")
    );
    HeartType ABSORPTION = basicHardcoreWithBlink(
            3.0,
            (player, world, gui) -> Mth.ceil(player.getAbsorptionAmount()),
            new ResourceLocation("hearty:textures/hearts/vanilla/absorption.png"),
            new ResourceLocation("hearty:textures/hearts/vanilla/absorption_hardcore.png"),
            new ResourceLocation("hearty:textures/hearts/vanilla/absorption_blinking.png"),
            new ResourceLocation("hearty:textures/hearts/vanilla/absorption_hardcore_blinking.png")
    );
    int getNumber(Player player, Level world, Gui gui);
    ResourceLocation getTexture(Player player, Level world, Gui gui);
    default double getPriority() {
        return 1.0;
    }

    static HeartType basicSuppliers(double priority, TriFunction<Player, Level, Gui, Integer> getNumber, TriFunction<Player, Level, Gui, ResourceLocation> getTexture) {
        return new HeartType() {
            @Override
            public int getNumber(Player player, Level world, Gui gui) {
                return getNumber.apply(player, world, gui);
            }

            @Override
            public ResourceLocation getTexture(Player player, Level world, Gui gui) {
                return getTexture.apply(player, world, gui);
            }

            @Override
            public double getPriority() {
                return priority;
            }
        };
    }
    static HeartType basic(double priority, TriFunction<Player, Level, Gui, Integer> getNumber, ResourceLocation texture) {
        return basicSuppliers(priority, getNumber, (a, b, c) -> texture);
    }

    static HeartType basicWithBlink(double priority, TriFunction<Player, Level, Gui, Integer> getNumber, ResourceLocation normal, ResourceLocation blinking) {
        return basicSuppliers(priority, getNumber, (player, world, gui) -> {
            boolean isHealthBlinking = gui.healthBlinkTime > (long)gui.tickCount && (gui.healthBlinkTime - (long)gui.tickCount) / 3L % 2L == 1L;
            return isHealthBlinking ? blinking : normal;
        });
    }

    static HeartType basicHardcore(double priority, TriFunction<Player, Level, Gui, Integer> getNumber, ResourceLocation normal, ResourceLocation hardcore) {
        return basicSuppliers(priority, getNumber, (player, world, gui) -> world.getLevelData().isHardcore() ? hardcore : normal);
    }

    static HeartType basicHardcoreWithBlink(double priority, TriFunction<Player, Level, Gui, Integer> getNumber, ResourceLocation normal, ResourceLocation hardcore, ResourceLocation blinking, ResourceLocation hardcoreBlinking) {
        return basicSuppliers(priority, getNumber, (player, world, gui) -> {
            boolean isHealthBlinking = gui.healthBlinkTime > (long)gui.tickCount && (gui.healthBlinkTime - (long)gui.tickCount) / 3L % 2L == 1L;
            return isHealthBlinking ? (world.getLevelData().isHardcore() ? hardcoreBlinking : blinking) : (world.getLevelData().isHardcore() ? hardcore : normal);
        });
    }

    static HeartType basicSuppliersOverrides(double priority, TriFunction<Player, Level, Gui, Integer> getNumber, TriFunction<Player, Level, Gui, ResourceLocation> getTexture, List<TriFunction<Player, Level, Gui, Integer>> numberOverrides, List<TriFunction<Player, Level, Gui, ResourceLocation>> texOverrides) {
        return basicSuppliers(priority, (p, w, g) -> {
            int original = getNumber.apply(p, w, g);
            return HeartyUtil.first(HeartyUtil.map(numberOverrides, (k) -> k.apply(p, w, g)), original, (it) -> it != original);
        }, (p, w, g) -> {
            ResourceLocation original = getTexture.apply(p, w, g);
            return HeartyUtil.first(HeartyUtil.map(texOverrides, (k) -> k.apply(p, w, g)), original, (it) -> it != original);
        });
    }
}
