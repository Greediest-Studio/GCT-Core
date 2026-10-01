
package com.gmm.gctold.common.events;

import com.gmm.gctold.common.world.structure.StructureGenerationHelper;
import com.gmm.gctold.common.world.structure.GctAllStructureTemplates;

import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
public final class DoorKeyOrderlandClick
{
    private DoorKeyOrderlandClick() {
    }
    
    public static void run(Entity entity, World world, int x, int y, int z) {
        if (entity.getHorizontalFacing() == EnumFacing.EAST && !world.isRemote) {
            StructureGenerationHelper.placeTemplate(world, GctAllStructureTemplates.ORDER_PORTAL, new BlockPos(x, y + 1, z - 1), Rotation.NONE, Mirror.NONE);
        }
        if (entity.getHorizontalFacing() == EnumFacing.WEST && !world.isRemote) {
            StructureGenerationHelper.placeTemplate(world, GctAllStructureTemplates.ORDER_PORTAL, new BlockPos(x, y + 1, z - 2), Rotation.NONE, Mirror.NONE);
        }
        if (entity.getHorizontalFacing() == EnumFacing.NORTH && !world.isRemote) {
            StructureGenerationHelper.placeTemplate(world, GctAllStructureTemplates.ORDER_PORTAL_2, new BlockPos(x - 1, y + 1, z), Rotation.NONE, Mirror.NONE);
        }
        if (entity.getHorizontalFacing() == EnumFacing.SOUTH && !world.isRemote) {
            StructureGenerationHelper.placeTemplate(world, GctAllStructureTemplates.ORDER_PORTAL_2, new BlockPos(x - 2, y + 1, z), Rotation.NONE, Mirror.NONE);
        }
    }
}



