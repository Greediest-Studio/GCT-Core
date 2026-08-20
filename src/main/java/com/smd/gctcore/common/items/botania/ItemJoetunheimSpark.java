package com.smd.gctcore.common.items.botania;

import com.smd.gctcore.common.entity.botania.EntityCustomSpark;
import com.smd.gctcore.common.entity.botania.EntityJoetunheimSpark;
import net.minecraft.world.World;

public class ItemJoetunheimSpark extends ItemTieredSpark {

    public ItemJoetunheimSpark() {
        super("joetunheim_spark", "gctcore.joetunheim_spark");
    }

    @Override
    protected EntityCustomSpark createSpark(World world) {
        return new EntityJoetunheimSpark(world);
    }

    @Override
    protected String getTooltipKey() {
        return "greedycraft.tooltip.botania.spark.joetunheim";
    }
}
