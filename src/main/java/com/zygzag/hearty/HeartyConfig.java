package com.zygzag.hearty;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class HeartyConfig {
    public static ForgeConfigSpec.BooleanValue RENDER_DOUBLE_HEARTS,
        WITHER_OVERRIDE_POISON;
    public static void register() {
        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
        RENDER_DOUBLE_HEARTS = clientBuilder.comment(" Whether or not hearts should store 1 hp instead of the vanilla 2").define("render_double_hearts", true);
        WITHER_OVERRIDE_POISON = clientBuilder.comment(" Whether or not withered black hearts should show instead of poisoned green hearts when the player has both effects at once. (very minor)").define("wither_override_poison", true);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientBuilder.build());
    }
}
