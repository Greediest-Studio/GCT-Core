package com.smd.gctcore.common.blocks.nilfheim;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockNilfheimSoil extends Block {
    public BlockNilfheimSoil(String name, Material material, float hardness) {
        super(material);
        setRegistryName(name);
        setTranslationKey("gctcore." + name);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHardness(hardness);
        setSoundType(soundFor(material));
        setHarvestLevel("shovel", 0);
    }

    private SoundType soundFor(Material material) {
        if (material == Material.GRASS || material == Material.GROUND) {
            return SoundType.GROUND;
        }
        if (material == Material.CRAFTED_SNOW) {
            return SoundType.SNOW;
        }
        if (material == Material.CLAY) {
            return SoundType.GROUND;
        }
        return SoundType.STONE;
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, net.minecraftforge.common.IPlantable plantable) {
        return true;
    }
}
