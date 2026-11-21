package com.smd.gctcore.common.blocks.jotunheim;

import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimSlab;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/** The placed double slab drops two usable half slabs and has no separate item. */
public final class BlockJotunheimDoubleWoodSlab extends BlockNilfheimSlab.Double {
    private final BlockSlab halfSlab;

    public BlockJotunheimDoubleWoodSlab(String name, BlockSlab halfSlab) {
        super(name, Material.WOOD, SoundType.WOOD, "axe", 0);
        this.halfSlab = halfSlab;
        setCreativeTab(null);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(halfSlab);
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(halfSlab);
    }

    @Override
    public int quantityDropped(Random random) { return 2; }
}
