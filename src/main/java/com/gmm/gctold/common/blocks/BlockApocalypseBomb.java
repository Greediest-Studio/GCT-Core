package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

  public class BlockApocalypseBomb extends Block {
  public static final Block block = new BlockApocalypseBomb();

  public BlockApocalypseBomb() {
    super(Material.ROCK);
    setTranslationKey("apocalypse_bomb");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 1);
    setHardness(100.0F);
    setResistance(10.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
    setBlockUnbreakable();
  }

  public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
    super.onBlockAdded(world, pos, state);
    explode(world, pos);
  }

  public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos) {
    super.neighborChanged(state, world, pos, neighborBlock, fromPos);
    if (world.getRedstonePowerFromNeighbors(pos) > 0) {
      explode(world, pos);
    }
  }

  @SideOnly(Side.CLIENT)
  public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
    super.randomDisplayTick(state, world, pos, random);
    EntityPlayerSP entityPlayerSP = (Minecraft.getMinecraft()).player;
    int x = pos.getX();
    int y = pos.getY();
    int z = pos.getZ();
    int i = x;
    int j = y;
    int k = z;
    for (int l = 0; l < 1; l++) {
      double d0 = i + 0.5D + (random.nextFloat() - 0.5D) * 0.5D;
      double d1 = j + 0.7D + (random.nextFloat() - 0.5D) * 0.5D + 0.5D;
      double d2 = k + 0.5D + (random.nextFloat() - 0.5D) * 0.5D;
      world.spawnParticle(EnumParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D, new int[0]);
    }
  }

  public void onExplosionDestroy(World world, BlockPos pos, Explosion e) {
    super.onExplosionDestroy(world, pos, e);
    explode(world, pos);
  }

  public void onBlockClicked(World world, BlockPos pos, EntityPlayer entity) {
    super.onBlockClicked(world, pos, entity);
    explode(world, pos);
  }

  public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
    super.onEntityCollision(world, pos, state, entity);
    explode(world, pos);
  }

  public void onEntityWalk(World world, BlockPos pos, Entity entity) {
    super.onEntityWalk(world, pos, entity);
    explode(world, pos);
  }

  private void explode(World world, BlockPos pos) {
    if (!world.isRemote) {
      world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 3.0F, true);
    }
  }
}
