package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.registry.GctAllItems;


import com.gmm.gctold.misc.GctAllCreativeTab;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

  public class BlockGravityDebris extends Block {
  public static final Block block = new BlockGravityDebris();

  public BlockGravityDebris() {
    super(Material.ROCK);
    setTranslationKey("gravity_debris");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 11);
    setHardness(100.0F);
    setResistance(30.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }

  public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
    drops.add(new ItemStack(GctAllItems.GRAVITY_SCRAP, 2));
  }

  public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
    super.onBlockAdded(world, pos, state);
    if (!world.isRemote) {
      world.scheduleUpdate(pos, this, tickRate(world));
    }
  }

  public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos) {
    super.neighborChanged(state, world, pos, neighborBlock, fromPos);
    if (world.getRedstonePowerFromNeighbors(pos) > 0) {
      moveUpIfPossible(world, pos);
    }
  }

  public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
    super.updateTick(world, pos, state, random);
    if (!world.isRemote) {
      moveUpIfPossible(world, pos);
      if (world.getBlockState(pos).getBlock() == this) {
        world.scheduleUpdate(pos, this, tickRate(world));
      }
    }
  }

  public void onBlockClicked(World world, BlockPos pos, EntityPlayer entity) {
    super.onBlockClicked(world, pos, entity);
    moveUpIfPossible(world, pos);
  }

  public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
    super.onEntityCollision(world, pos, state, entity);
    moveUpIfPossible(world, pos);
  }

  public void onEntityWalk(World world, BlockPos pos, Entity entity) {
    super.onEntityWalk(world, pos, entity);
    moveUpIfPossible(world, pos);
  }

  public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, EnumHand hand, EnumFacing direction, float hitX, float hitY, float hitZ) {
    super.onBlockActivated(world, pos, state, entity, hand, direction, hitX, hitY, hitZ);
    moveUpIfPossible(world, pos);
    return true;
  }

  private void moveUpIfPossible(World world, BlockPos pos) {
    if (world.isRemote || world.getBlockState(pos.up()).getBlock() != Blocks.AIR) {
      return;
    }
    world.setBlockToAir(pos);
    if (pos.getY() < 255) {
      world.setBlockState(pos.up(), getDefaultState(), 3);
    }
  }
}

