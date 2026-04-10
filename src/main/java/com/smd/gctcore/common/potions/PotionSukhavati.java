package com.smd.gctcore.common.potions;

import com.smd.gctcore.gctcore;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

public class PotionSukhavati extends Potion {
    
    public PotionSukhavati() {
        super(false, 0x98FB98);
        setRegistryName(new ResourceLocation(gctcore.MODID, "sukhavati"));
        setPotionName("effect.sukhavati");
    }
}
