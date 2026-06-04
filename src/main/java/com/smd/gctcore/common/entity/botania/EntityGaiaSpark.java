package com.smd.gctcore.common.entity.botania;

import com.smd.gctcore.misc.ItemRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityGaiaSpark extends EntityCustomSpark {

    private static final int TRANSFER_RATE = 25000;
    private static final int SCAN_RANGE = 20;

    public EntityGaiaSpark(World world) {
        super(world);
    }

    @Override
    protected int getTransferRate() {
        return TRANSFER_RATE;
    }

    @Override
    protected int getScanRange() {
        return SCAN_RANGE;
    }

    @Override
    protected ItemStack createSparkItem() {
        return new ItemStack(ItemRegistry.GAIA_SPARK);
    }
}
