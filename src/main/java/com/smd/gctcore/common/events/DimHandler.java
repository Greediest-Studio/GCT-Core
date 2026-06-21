package com.smd.gctcore.common.events;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.world.biome.nilfheim.BiomeNilfheimBase;
import com.smd.gctcore.common.world.biome.nilfheim.NilfheimBiomes;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Tags.MOD_ID)
public class DimHandler {
    @SubscribeEvent
    public static void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        if (event.getEntity().world.provider.getDimension() == 103) {
            event.setDensity(0.0F);
            event.setCanceled(true);
            return;
        }

        if (event.getEntity().world.provider.getDimension() == NilfheimBiomes.DIMENSION_ID) {
            Biome biome = event.getEntity().world.getBiome(event.getEntity().getPosition());
            if (biome == NilfheimBiomes.SUNKEN_TEAR) {
                event.setDensity(0.022F);
            } else if (biome == NilfheimBiomes.HOWLING_ICEFIELD || biome == NilfheimBiomes.MISTVEIL_DEPTHS) {
                event.setDensity(0.135F);
            } else {
                event.setDensity(0.110F);
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onFogColors(EntityViewRenderEvent.FogColors event) {
        if (event.getEntity().world.provider.getDimension() == 103) {
            event.setRed(0.0F);
            event.setGreen(0.0F);
            event.setBlue(0.0F);
            return;
        }

        if (event.getEntity().world.provider.getDimension() == NilfheimBiomes.DIMENSION_ID) {
            Biome biome = event.getEntity().world.getBiome(event.getEntity().getPosition());
            int color = biome instanceof BiomeNilfheimBase
                    ? ((BiomeNilfheimBase) biome).getFogColor()
                    : 0x142A46;
            if (biome != NilfheimBiomes.SUNKEN_TEAR) {
                color = blend(color, 0x112B4A, 0.60F);
            }

            event.setRed(((color >> 16) & 255) / 255.0F);
            event.setGreen(((color >> 8) & 255) / 255.0F);
            event.setBlue((color & 255) / 255.0F);
        }
    }

    private static int blend(int first, int second, float amount) {
        int r = (int) (((first >> 16) & 255) * (1.0F - amount) + ((second >> 16) & 255) * amount);
        int g = (int) (((first >> 8) & 255) * (1.0F - amount) + ((second >> 8) & 255) * amount);
        int b = (int) ((first & 255) * (1.0F - amount) + (second & 255) * amount);
        return (r << 16) | (g << 8) | b;
    }
}
