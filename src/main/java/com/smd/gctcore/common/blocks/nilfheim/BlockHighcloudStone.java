package com.smd.gctcore.common.blocks.nilfheim;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class BlockHighcloudStone extends Block {
    private final boolean locked;
    private final Block unlockedVariant;

    public BlockHighcloudStone(String name, boolean locked, Block unlockedVariant) {
        super(Material.ROCK);
        this.locked = locked;
        this.unlockedVariant = unlockedVariant;

        setRegistryName(name);
        setTranslationKey("gctcore." + name);
        setSoundType(SoundType.STONE);

        if (locked) {
            setResistance(6000000.0F);
            setBlockUnbreakable();
            setHardness(-1.0F);
            setCreativeTab(null);
        } else {
            setHardness(0.5F);
            setResistance(15.0F);
            setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
            setHarvestLevel("pickaxe", 0);
        }
    }

    @Override
    public ItemStack getPickBlock(net.minecraft.block.state.IBlockState state, RayTraceResult target,
                                  World world, BlockPos pos, EntityPlayer player) {
        if (locked && unlockedVariant != null) {
            return new ItemStack(unlockedVariant);
        }
        return super.getPickBlock(state, target, world, pos, player);
    }
}
