package com.smd.gctcore.common.items.botania;

import com.smd.gctcore.common.entity.botania.EntityCustomSpark;
import com.smd.gctcore.common.entity.botania.EntityVanaheimSpark;
import net.minecraft.world.World;

public class ItemVanaheimSpark extends ItemTieredSpark {

    public ItemVanaheimSpark() {
        super("vanaheim_spark", "gctcore.vanaheim_spark");
    }

    @Override
    protected EntityCustomSpark createSpark(World world) {
        return new EntityVanaheimSpark(world);
    }

    @Override
    protected String getTooltipKey() {
        return "greedycraft.tooltip.botania.spark.vanaheim";
    }
}
