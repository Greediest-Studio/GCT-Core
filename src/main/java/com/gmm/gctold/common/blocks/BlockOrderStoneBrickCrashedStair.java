package com.gmm.gctold.common.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import com.gmm.gctold.misc.GctAllCreativeTab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.Block;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockOrderStoneBrickCrashedStair extends BlockStairs {
    public static final Block block = new BlockOrderStoneBrickCrashedStair();

    public BlockOrderStoneBrickCrashedStair() {
    super(new Block(Material.ROCK).getDefaultState());
    this.setTranslationKey("order_stone_brick_crashed_stair");
    this.setSoundType(SoundType.STONE);
    this.setHarvestLevel("pickaxe", 12);
    this.setHardness(50.0f);
    this.setResistance(4000.0f);
    this.setLightLevel(0.0f);
    this.setLightOpacity(15);
    this.setCreativeTab(GctAllCreativeTab.TAB);

    }



    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
    return BlockRenderLayer.CUTOUT;
    }

    public boolean isOpaqueCube(final IBlockState state) {
    return false;
    }

}
