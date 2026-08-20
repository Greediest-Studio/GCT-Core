package com.smd.gctcore.common.items.botania;

import com.smd.gctcore.common.entity.botania.EntityGaiaSpark;
import net.minecraft.world.World;

public class ItemGaiaSpark extends ItemTieredSpark {

    public ItemGaiaSpark() {
        super("gaia_spark", "gctcore.gaia_spark");
    }

    @Override
    protected EntityGaiaSpark createSpark(World world) {
        return new EntityGaiaSpark(world);
    }

    @Override
    protected String getTooltipKey() {
        return "greedycraft.tooltip.botania.spark.gaia";
    }
}
