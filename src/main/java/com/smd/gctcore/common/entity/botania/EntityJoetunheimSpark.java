package com.smd.gctcore.common.entity.botania;

import com.smd.gctcore.misc.ItemRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Joetunheim spark: 400,000 Mana/tick and a 28-block scan radius. */
public class EntityJoetunheimSpark extends EntityCustomSpark {

    private static final int TRANSFER_RATE = 400_000;
    private static final int SCAN_RANGE = 28;

    public EntityJoetunheimSpark(World world) {
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
        return new ItemStack(ItemRegistry.JOETUNHEIM_SPARK);
    }
}
