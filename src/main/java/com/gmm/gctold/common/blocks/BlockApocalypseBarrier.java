package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import com.gmm.gctold.common.events.ApocalypseBarrierClick;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

  public class BlockApocalypseBarrier extends Block {
  public static final Block block = new BlockApocalypseBarrier();

  public BlockApocalypseBarrier() {
    super(Material.ROCK);
    setTranslationKey("apocalypse_barrier");
    setSoundType(SoundType.METAL);
    setHardness(1.0F);
    setResistance(10.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
    setBlockUnbreakable();
  }

  @SideOnly(Side.CLIENT)
  public BlockRenderLayer getRenderLayer() {
    return BlockRenderLayer.CUTOUT;
  }

  public boolean isOpaqueCube(IBlockState state) {
    return false;
  }

  public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, EnumHand hand, EnumFacing direction, float hitX, float hitY, float hitZ) {
    super.onBlockActivated(world, pos, state, entity, hand, direction, hitX, hitY, hitZ);
    int x = pos.getX();
    int y = pos.getY();
    int z = pos.getZ();
      ApocalypseBarrierClick.run(entity, world, x, y, z);
    return true;
  }
}
