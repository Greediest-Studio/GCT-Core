package com.gmm.gctold.common.blocks;

import net.minecraft.util.BlockRenderLayer;
import com.gmm.gctold.misc.GctAllCreativeTab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.Block;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockShalloiteStair extends BlockStairs {
    public static final Block block = new BlockShalloiteStair();

    public BlockShalloiteStair() {
    super(new Block(Material.ROCK).getDefaultState());
    this.setTranslationKey("shalloite_stair");
    this.setSoundType(SoundType.STONE);
    this.setHarvestLevel("pickaxe", 0);
    this.setHardness(2.0f);
    this.setResistance(20.0f);
    this.setLightLevel(0.0f);
    this.setLightOpacity(15);
    this.setCreativeTab(GctAllCreativeTab.TAB);

    }



    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
    return BlockRenderLayer.CUTOUT;
    }

}
