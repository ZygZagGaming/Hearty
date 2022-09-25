package com.zygzag.hearty;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = HeartyMain.MODID)
public class ClientEventHandler {
    public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        //System.out.println("ics: " + event.player.level.isClientSide + ", " + event.player.getAttributeValue(Registry.BROKEN_HEARTS_ATTRIBUTE.get()));
    }

    @SubscribeEvent
    public static void preOverlayRenderEvent(RenderGuiOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.getOverlay().id().getPath().equals("player_health") && !mc.gui.getCameraPlayer().getAbilities().instabuild && !mc.gui.getCameraPlayer().isSpectator()) {
            renderPlayerHealth(mc.gui, mc, event.getPoseStack());
            event.setCanceled(true);
        }
    }

    public static void renderPlayerHealth(Gui gui, Minecraft minecraft, PoseStack stack) {
        Player player = gui.getCameraPlayer();
        int ceilHealth = Mth.ceil(player.getHealth());
        boolean isHealthBlinking = gui.healthBlinkTime > (long)gui.tickCount && (gui.healthBlinkTime - (long)gui.tickCount) / 3L % 2L == 1L;
        long t = Util.getMillis();
        //System.out.println(player.invulnerableTime);
        if (player.invulnerableTime > 0 && gui.healthBlinkTime <= gui.tickCount) {
            gui.lastHealthTime = t;
            if (ceilHealth > gui.lastHealth) gui.healthBlinkTime = gui.tickCount + 10;
            else gui.healthBlinkTime = gui.tickCount + 20;
        }

        if (t - gui.lastHealthTime > 1000L) {
            gui.displayHealth = ceilHealth;
            gui.lastHealthTime = t;
        }

        gui.lastHealth = ceilHealth;
        int displayHealth = gui.displayHealth;
        gui.random.setSeed(gui.tickCount * 312871L);
        FoodData fooddata = player.getFoodData();
        int food = fooddata.getFoodLevel();
        int xMin = gui.screenWidth / 2 - 91;
        int xMax = gui.screenWidth / 2 + 91;
        int yMin = gui.screenHeight - 39;
        float maxHealth = Math.max((float)player.getAttributeValue(Attributes.MAX_HEALTH), (float)Math.max(displayHealth, ceilHealth));
        int absorptionHearts = Mth.ceil(player.getAbsorptionAmount());
        int heartLayersTotal = Mth.ceil((maxHealth + (float)absorptionHearts) / 2.0F / 10.0F);
        int j2 = Math.max(10 - (heartLayersTotal - 2), 3);
        int k2 = yMin - (heartLayersTotal - 1) * j2 - 10;
        int yMax = yMin - 10;
        int armor = player.getArmorValue();
        int regenHeartWiggle = -1;
        if (player.hasEffect(MobEffects.REGENERATION)) {
            regenHeartWiggle = gui.tickCount % Mth.ceil(maxHealth + 5.0F);
        }

        minecraft.getProfiler().push("armor"); //region armor

        int l3;
        for(int k3 = 0; k3 < 10; ++k3) {
            if (armor > 0) {
                l3 = xMin + k3 * 8;
                if (k3 * 2 + 1 < armor) {
                    gui.blit(stack, l3, k2, 34, 9, 9, 9);
                }

                if (k3 * 2 + 1 == armor) {
                    gui.blit(stack, l3, k2, 25, 9, 9, 9);
                }

                if (k3 * 2 + 1 > armor) {
                    gui.blit(stack, l3, k2, 16, 9, 9, 9);
                }
            }
        }
        // endregion

        minecraft.getProfiler().popPush("health"); // region health
        renderHearts(gui, stack, player, xMin, yMin, j2, regenHeartWiggle, maxHealth, ceilHealth, displayHealth, absorptionHearts, isHealthBlinking);
        // endregion

        LivingEntity vehicle = gui.getPlayerVehicleWithHealth();
        l3 = gui.getVehicleMaxHearts(vehicle);
        int i4;
        int j4;
        int k4;
        int l4;
        int i5;
        if (l3 == 0) {
            minecraft.getProfiler().popPush("food"); // region food

            for(i4 = 0; i4 < 10; ++i4) {
                j4 = yMin;
                k4 = 16;
                l4 = 0;
                if (player.hasEffect(MobEffects.HUNGER)) {
                    k4 += 36;
                    l4 = 13;
                }

                if (player.getFoodData().getSaturationLevel() <= 0.0F && gui.tickCount % (food * 3 + 1) == 0) {
                    j4 = yMin + (gui.random.nextInt(3) - 1);
                }

                i5 = xMax - i4 * 8 - 9;
                gui.blit(stack, i5, j4, 16 + l4 * 9, 27, 9, 9);
                if (i4 * 2 + 1 < food) {
                    gui.blit(stack, i5, j4, k4 + 36, 27, 9, 9);
                }

                if (i4 * 2 + 1 == food) {
                    gui.blit(stack, i5, j4, k4 + 45, 27, 9, 9);
                }
            }

            yMax -= 10; // endregion
        }

        minecraft.getProfiler().popPush("air"); // region air
        i4 = player.getMaxAirSupply();
        j4 = Math.min(player.getAirSupply(), i4);
        if (player.isEyeInFluid(FluidTags.WATER) || j4 < i4) {
            k4 = gui.getVisibleVehicleHeartRows(l3) - 1;
            yMax -= k4 * 10;
            l4 = Mth.ceil((double)(j4 - 2) * 10.0 / (double)i4);
            i5 = Mth.ceil((double)j4 * 10.0 / (double)i4) - l4;

            for(int j5 = 0; j5 < l4 + i5; ++j5) {
                if (j5 < l4) {
                    gui.blit(stack, xMax - j5 * 8 - 9, yMax, 16, 18, 9, 9);
                } else {
                    gui.blit(stack, xMax - j5 * 8 - 9, yMax, 25, 18, 9, 9);
                }
            }
        } // endregion

        minecraft.getProfiler().pop();

    }

    public static void renderHearts(Gui gui, PoseStack stack, Player player, int xMin, int yMin, int c, int regenHeartWiggle, float maxHealth, int ceilHealth, int displayHealth, int absorptionHearts, boolean isHealthBlinking) {
        int i = 0;
        boolean left = true;
        for (HeartType type : HeartyMain.REGISTERED_HEART_TYPES) {
            int amt = type.getNumber(player, player.level, gui);
            ResourceLocation texture = type.texture(player, player.level, gui);
            for (int k = i; i < k + amt; i++) {
                int row = i / (HeartyConfig.RENDER_DOUBLE_HEARTS.get() ? 10 : 20);
                int column = (i / (HeartyConfig.RENDER_DOUBLE_HEARTS.get() ? 1 : 2)) % 10;
                int x = xMin + column * 8;
                int y = yMin - row * c;

                //System.out.println("x: " + x + ", y: " + y);

                if (type == HeartType.HEALTH) {
                    if (amt <= 4) y += gui.random.nextInt(2);
                    if (i == regenHeartWiggle || i == regenHeartWiggle - 1) y -= 2;
                }

                if (HeartyConfig.RENDER_DOUBLE_HEARTS.get()) renderHeart(stack, texture, x, y);
                else if (left) renderLeftHalfHeart(stack, texture, x, y);
                else renderRightHalfHeart(stack, texture, x, y);
                left = !left;
            }
        }
    }

    public static void renderHeart(PoseStack stack, ResourceLocation texture, int x, int y) {
        RenderSystem.setShaderTexture(0, texture);
        Gui.blit(stack, x, y, 0, 0, 0, 9, 9, 16, 16);
        RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
    }

    public static void renderLeftHalfHeart(PoseStack stack, ResourceLocation texture, int x, int y) {
        RenderSystem.setShaderTexture(0, texture);
        Gui.blit(stack, x, y, 0, 0, 0, 5, 9, 16, 16);
        RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
    }

    public static void renderRightHalfHeart(PoseStack stack, ResourceLocation texture, int x, int y) {
        RenderSystem.setShaderTexture(0, texture);
        Gui.blit(stack, x + 5, y, 0, 5, 0, 9, 9, 16, 16);
        RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
    }
}
