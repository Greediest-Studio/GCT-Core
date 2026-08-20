package com.smd.gctcore.common.items.botania;

import com.smd.gctcore.common.entity.botania.EntityCustomSpark;
import com.smd.gctcore.common.entity.botania.EntityNidavellirSpark;
import net.minecraft.world.World;

public class ItemNidavellirSpark extends ItemTieredSpark {

    public ItemNidavellirSpark() {
        super("nidavellir_spark", "gctcore.nidavellir_spark");
    }

    @Override
    protected EntityCustomSpark createSpark(World world) {
        return new EntityNidavellirSpark(world);
    }

    @Override
    protected String getTooltipKey() {
        return "greedycraft.tooltip.botania.spark.nidavellir";
    }
}
