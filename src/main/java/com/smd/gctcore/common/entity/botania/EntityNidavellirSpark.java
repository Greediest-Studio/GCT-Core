package com.smd.gctcore.common.entity.botania;

import com.smd.gctcore.misc.ItemRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Nidavellir spark: 1,600,000 Mana/tick and a 32-block scan radius. */
public class EntityNidavellirSpark extends EntityCustomSpark {

    private static final int TRANSFER_RATE = 1_600_000;
    private static final int SCAN_RANGE = 32;

    public EntityNidavellirSpark(World world) {
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
        return new ItemStack(ItemRegistry.NIDAVELLIR_SPARK);
    }
}
