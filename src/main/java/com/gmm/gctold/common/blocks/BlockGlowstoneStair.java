package com.gmm.gctold.common.blocks;

import net.minecraft.init.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import com.gmm.gctold.misc.GctAllCreativeTab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.Block;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockGlowstoneStair extends BlockStairs {
    public static final Block block = new BlockGlowstoneStair();

    public BlockGlowstoneStair() {
    super(new Block(Material.GLASS).getDefaultState());
    this.setTranslationKey("glowstone_stair");
    this.setSoundType(SoundType.GLASS);
    this.setHardness(1.0f);
    this.setResistance(10.0f);
    this.setLightLevel(1.0f);
    this.setLightOpacity(0);
    this.setCreativeTab(GctAllCreativeTab.TAB);

    }



    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
    return BlockRenderLayer.CUTOUT;
    }

    public boolean isOpaqueCube(final IBlockState state) {
    return false;
    }

    public void getDrops(final NonNullList<ItemStack> drops, final IBlockAccess world, final BlockPos pos, final IBlockState state, final int fortune) {
    drops.add(new ItemStack(Items.GLOWSTONE_DUST, 5));
    }

}
