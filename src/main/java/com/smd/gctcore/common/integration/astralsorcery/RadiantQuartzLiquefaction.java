package com.smd.gctcore.common.integration.astralsorcery;

import com.smd.gctcore.misc.ItemRegistry;
import hellfirepvp.astralsorcery.common.base.WellLiquefaction;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import net.minecraft.item.ItemStack;

import java.awt.Color;

public class RadiantQuartzLiquefaction {

    public static void init() {
        WellLiquefaction.registerLiquefaction(new ItemStack(ItemRegistry.RAW_QUARTZ), BlocksAS.fluidLiquidStarlight, 0.07F, 80, new Color(0xDD, 0xEE, 0xFF));
        WellLiquefaction.registerLiquefaction(new ItemStack(ItemRegistry.SHAPED_QUARTZ), BlocksAS.fluidLiquidStarlight, 0.12F, 72, new Color(0xDD, 0xEE, 0xFF));
    }
}
