package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.registry.GctAllItems;

import com.gmm.gctold.misc.GctAllCreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockOrderCrystalOre extends Block {
    public static final Block block = new BlockOrderCrystalOre();

    public BlockOrderCrystalOre() {
        super(Material.ROCK);
        setTranslationKey("order_crystal_ore");
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 12);
        setHardness(150.0F);
        setResistance(2000000.0F);
        setLightOpacity(15);
        setCreativeTab(GctAllCreativeTab.TAB);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        drops.add(new ItemStack(GctAllItems.ORDER_CRYSTAL));
    }
}
