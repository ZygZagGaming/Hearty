package com.zygzag.hearty;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.function.TriFunction;
import org.slf4j.Logger;
import oshi.util.tuples.Pair;
import oshi.util.tuples.Quartet;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(HeartyMain.MODID)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HeartyMain {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "hearty";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final HashMap<String, ResourceLocation> CACHE = new HashMap<>();
    public static Map<ResourceLocation, HeartType> REGISTERED_HEART_TYPES = Map.of(loc("minecraft:health"), HeartType.HEALTH, loc("minecraft:empty"), HeartType.EMPTY, loc("minecraft:absorption"), HeartType.ABSORPTION);
    private static final Map<ResourceLocation, List<TriFunction<Player, Level, Gui, ResourceLocation>>> HEART_TEXTURE_OVERRIDES = new HashMap<>();
    private static final Map<ResourceLocation, List<TriFunction<Player, Level, Gui, Integer>>> HEART_NUMBER_OVERRIDES = new HashMap<>();

    public HeartyMain() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::enqueueIMCs);
        modEventBus.addListener(this::processIMCs);

        HeartyConfig.register();

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void enqueueIMCs(final InterModEnqueueEvent event) {

    }

    @SuppressWarnings("unchecked") // honestly who cares about unchecked casts
    private void processIMCs(final InterModProcessEvent event) {
        for (InterModComms.IMCMessage message : event.getIMCStream().toList()) {
            switch (message.method()) {
                case "register_heart":
                    try {
                        /* what a type */
                        Quartet<ResourceLocation, Double, TriFunction<Player, Level, Gui, Integer>, TriFunction<Player, Level, Gui, ResourceLocation>> quartet = (Quartet<ResourceLocation, Double, TriFunction<Player, Level, Gui, Integer>, TriFunction<Player, Level, Gui, ResourceLocation>>) message.messageSupplier().get();
                        HeartType type = HeartType.basicSuppliers(quartet.getB(), quartet.getC(), quartet.getD());
                        List<TriFunction<Player, Level, Gui, ResourceLocation>> texOverrides = HEART_TEXTURE_OVERRIDES.get(quartet.getA());
                        if (texOverrides != null && texOverrides.size() >= 1)
                            type = HeartType.basicSuppliers(type.getPriority(), type::getNumber, texOverrides.get(0));
                        List<TriFunction<Player, Level, Gui, Integer>> numOverrides = HEART_NUMBER_OVERRIDES.get(quartet.getA());
                        if (numOverrides != null && numOverrides.size() >= 1)
                            type = HeartType.basicSuppliers(type.getPriority(), numOverrides.get(0), type::getTexture);
                        addOrReplaceHeartType(quartet.getA(), type);
                    } catch (ClassCastException e) {
                        LOGGER.error("Mod " + message.senderModId() + " sent a register_heart message with an incorrect type: " + message.messageSupplier().get());
                    }
                    break;
                case "replace_number_provider":
                    try {
                        Pair<ResourceLocation, TriFunction<Player, Level, Gui, Integer>> pair = (Pair<ResourceLocation, TriFunction<Player, Level, Gui, Integer>>) message.messageSupplier().get();
                        HeartType a = REGISTERED_HEART_TYPES.get(pair.getA());
                        if (a != null)
                            addOrReplaceHeartType(pair.getA(), HeartType.basicSuppliers(a.getPriority(), pair.getB(), a::getTexture));
                        if (HEART_NUMBER_OVERRIDES.containsKey(pair.getA()))
                            HEART_NUMBER_OVERRIDES.get(pair.getA()).add(pair.getB());
                        else HEART_NUMBER_OVERRIDES.put(pair.getA(), new ArrayList<>(List.of(pair.getB())));
                    } catch (ClassCastException e) {
                        LOGGER.error("Mod " + message.senderModId() + " sent a replace_number_provider message with an incorrect type: " + message.messageSupplier().get());
                    }
                    break;
                case "replace_texture_provider":
                    try {
                        Pair<ResourceLocation, TriFunction<Player, Level, Gui, ResourceLocation>> pair = (Pair<ResourceLocation, TriFunction<Player, Level, Gui, ResourceLocation>>) message.messageSupplier().get();
                        HeartType a = REGISTERED_HEART_TYPES.get(pair.getA());
                        if (a != null)
                            addOrReplaceHeartType(pair.getA(), HeartType.basicSuppliers(a.getPriority(), a::getNumber, pair.getB()));
                        if (HEART_TEXTURE_OVERRIDES.containsKey(pair.getA()))
                            HEART_TEXTURE_OVERRIDES.get(pair.getA()).add(pair.getB());
                        else HEART_TEXTURE_OVERRIDES.put(pair.getA(), new ArrayList<>(List.of(pair.getB())));
                    } catch (ClassCastException e) {
                        LOGGER.error("Mod " + message.senderModId() + " sent a replace_texture_provider message with an incorrect type: " + message.messageSupplier().get());
                    }
                    break;
            }
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) { }

    public static void addOrReplaceHeartType(ResourceLocation id, HeartType type) {
        REGISTERED_HEART_TYPES.put(id, type);
    }

    public static ResourceLocation loc(String string) {
        if (!string.contains(":")) string = "glyphic:" + string;
        if (!CACHE.containsKey(string)) CACHE.put(string, new ResourceLocation(string));
        return CACHE.get(string);
    }
}
