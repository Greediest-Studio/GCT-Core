package com.smd.gctcore.common.entity.botania;

import com.smd.gctcore.misc.ItemRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityAlfSpark extends EntityCustomSpark {

    private static final int TRANSFER_RATE = 6000;
    private static final int SCAN_RANGE = 16;

    public EntityAlfSpark(World world) {
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
        return new ItemStack(ItemRegistry.ALF_SPARK);
    }
}
