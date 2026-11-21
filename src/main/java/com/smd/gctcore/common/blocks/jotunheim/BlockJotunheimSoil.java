package com.smd.gctcore.common.blocks.jotunheim;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

public class BlockJotunheimSoil extends BlockJotunheimNatural {
    public BlockJotunheimSoil(String name, boolean grass) {
        super(name, grass ? Material.GRASS : Material.GROUND, SoundType.GROUND,
                "shovel", 0.6F, BlockRenderLayer.SOLID, false);
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos,
                                   EnumFacing direction, IPlantable plant) {
        EnumPlantType type = plant.getPlantType(world, pos.up());
        return direction == EnumFacing.UP && (type == EnumPlantType.Plains
                || type == EnumPlantType.Cave || type == EnumPlantType.Beach);
    }
}
