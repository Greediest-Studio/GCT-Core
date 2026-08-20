package com.smd.gctcore.common.entity.botania;

import com.smd.gctcore.misc.ItemRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Vanaheim spark: 25,000,000 Mana/tick and a 40-block scan radius. */
public class EntityVanaheimSpark extends EntityCustomSpark {

    private static final int TRANSFER_RATE = 25_000_000;
    private static final int SCAN_RANGE = 40;

    public EntityVanaheimSpark(World world) {
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
        return new ItemStack(ItemRegistry.VANAHEIM_SPARK);
    }
}
