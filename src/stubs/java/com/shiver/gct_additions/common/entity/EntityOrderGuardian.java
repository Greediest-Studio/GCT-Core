package com.shiver.gct_additions.common.entity;

import com.brandon3055.draconicevolution.entity.EntityChaosGuardian;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

// Compile-time stub only - excluded from packaged jar by build.gradle
public class EntityOrderGuardian extends EntityChaosGuardian {

    public EntityOrderGuardian(World world) {
        super(world);
    }

    public boolean destroyOrderBlocks(AxisAlignedBB bounds) {
        return false;
    }
}

