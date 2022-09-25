package com.zygzag.hearty;

import com.mojang.logging.LogUtils;
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
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.function.TriFunction;
import org.slf4j.Logger;
import oshi.util.tuples.Quartet;
import oshi.util.tuples.Triplet;

import java.util.List;
import java.util.function.BiFunction;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(HeartyMain.MODID)
public class HeartyMain {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "hearty";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public static List<HeartType> REGISTERED_HEART_TYPES = HeartyUtil.sorted(List.of(HeartType.HEALTH, HeartType.EMPTY, HeartType.ABSORPTION), (a, b) -> (int) Math.signum(a.priority() - b.priority()));


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

    private void processIMCs(final InterModProcessEvent event) {
        for (InterModComms.IMCMessage message : event.getIMCStream().toList()) {
            if (message.method().equals("register_heart")) {
                /* what a type */
                Triplet<Double, TriFunction<Player, Level, Gui, Integer>, TriFunction<Player, Level, Gui, ResourceLocation>> triplet = (Triplet<Double, TriFunction<Player, Level, Gui, Integer>, TriFunction<Player, Level, Gui, ResourceLocation>>) message.messageSupplier().get();
                addNewHeartType(HeartType.basicSuppliers(triplet.getA(), triplet.getB(), triplet.getC()));
            }
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) { }

    public static void addNewHeartType(HeartType type) {
        REGISTERED_HEART_TYPES = HeartyUtil.insertSorted(REGISTERED_HEART_TYPES, type, (a, b) -> (int) Math.signum(a.priority() - b.priority()));
    }
}
