package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import java.util.Random;
import javax.annotation.Nullable;
import com.gmm.gctold.common.potions.PotionBluefire;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

  public class BlockBlueFire extends BlockFalling {
  public static final Block block = new BlockBlueFire();

  public BlockBlueFire() {
    super(Material.CLOTH);
    setTranslationKey("blue_fire");
    setSoundType(SoundType.CLOTH);
    setHardness(1.0F);
    setResistance(10.0F);
    setLightLevel(1.0F);
    setLightOpacity(0);
    setCreativeTab(GctAllCreativeTab.TAB);
  }

  @SideOnly(Side.CLIENT)
  public BlockRenderLayer getRenderLayer() {
    return BlockRenderLayer.CUTOUT;
  }

  @Nullable
  public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
    return NULL_AABB;
  }

  public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
    return true;
  }

  public boolean isFullCube(IBlockState state) {
    return false;
  }

  public boolean isOpaqueCube(IBlockState state) {
    return false;
  }

  public boolean isReplaceable(IBlockAccess blockAccess, BlockPos pos) {
    return true;
  }

  public int quantityDropped(Random random) {
    return 0;
  }

  public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
    super.onEntityCollision(world, pos, state, entity);
    if (!world.isRemote && entity instanceof EntityPlayer) {
      ((EntityPlayer)entity).addPotionEffect(new PotionEffect(PotionBluefire.potion, 100, 0, false, false));
    }
  }
}
