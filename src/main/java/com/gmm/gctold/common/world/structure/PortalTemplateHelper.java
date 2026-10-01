package com.gmm.gctold.common.world.structure;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class PortalTemplateHelper {
  private PortalTemplateHelper() {}

  public static boolean placeFacingPortal(World world, Entity entity, BlockPos clickedPos,
      StructureTemplateId eastWestTemplate, StructureTemplateId northSouthTemplate) {
    if (world.isRemote) {
      return true;
    }

    EnumFacing facing = entity.getHorizontalFacing();
    StructureTemplateId templateId;
    BlockPos origin;
    switch (facing) {
      case EAST:
        templateId = eastWestTemplate;
        origin = clickedPos.add(0, 1, -1);
        break;
      case WEST:
        templateId = eastWestTemplate;
        origin = clickedPos.add(0, 1, -2);
        break;
      case NORTH:
        templateId = northSouthTemplate;
        origin = clickedPos.add(-1, 1, 0);
        break;
      case SOUTH:
        templateId = northSouthTemplate;
        origin = clickedPos.add(-2, 1, 0);
        break;
      default:
        return false;
    }

    return StructureGenerationHelper.placeTemplate(world, templateId, origin, Rotation.NONE, Mirror.NONE);
  }
}
