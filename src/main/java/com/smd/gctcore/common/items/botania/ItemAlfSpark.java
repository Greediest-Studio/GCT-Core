package com.smd.gctcore.common.items.botania;

import com.smd.gctcore.common.entity.botania.EntityAlfSpark;
import net.minecraft.world.World;

public class ItemAlfSpark extends ItemTieredSpark {

    public ItemAlfSpark() {
        super("alf_spark", "gctcore.alf_spark");
    }

    @Override
    protected EntityAlfSpark createSpark(World world) {
        return new EntityAlfSpark(world);
    }

    @Override
    protected String getTooltipKey() {
        return "greedycraft.tooltip.botania.spark.dream";
    }
}
