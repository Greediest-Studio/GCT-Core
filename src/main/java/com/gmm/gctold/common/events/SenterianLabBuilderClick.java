
package com.gmm.gctold.common.events;

import com.gmm.gctold.common.world.structure.StructureGenerationHelper;
import com.gmm.gctold.common.world.structure.GctAllStructureTemplates;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public final class SenterianLabBuilderClick
{
    private SenterianLabBuilderClick() {
    }
    
    public static void run(World world, EntityPlayer player, int x, int y, int z) {
        if (world.provider.getDimension() == 829 && y == 21) {
            world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), 3);
            if (player != null) {
                player.sendMessage(new TextComponentString("黑岩实验室已经生成！可能会造成一定卡顿，请知悉"));
            }
            if (!world.isRemote) {
                StructureGenerationHelper.placeTemplate(world, GctAllStructureTemplates.SENTERIAN_LAB, new BlockPos(x, y, z), Rotation.NONE, Mirror.NONE);
            }
        }
        else {
            if (player != null) {
                player.sendMessage(new TextComponentString("维度或高度不正确！"));
            }
        }
    }
}



