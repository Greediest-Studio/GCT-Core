package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import java.util.Random;
import com.gmm.gctold.common.events.SeekAltarOnBlockRightClicked;
import com.gmm.gctold.common.events.SeekAltarUpdateTick;
import com.gmm.gctold.common.tile.TileEntitySeekAltar;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

  public class BlockSeekAltar extends Block implements ITileEntityProvider {
  public static final Block block = new BlockSeekAltar();
public BlockSeekAltar() {
    super(Material.ROCK);
    setTranslationKey("seek_altar");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 20);
    setHardness(500.0F);
    setResistance(1.0E7F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }

  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return new TileEntitySeekAltar();
  }

  public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int eventID, int eventParam) {
    super.eventReceived(state, worldIn, pos, eventID, eventParam);
    TileEntity tileentity = worldIn.getTileEntity(pos);
    return (tileentity == null) ? false : tileentity.receiveClientEvent(eventID, eventParam);
  }

  public EnumBlockRenderType getRenderType(IBlockState state) {
    return EnumBlockRenderType.MODEL;
  }

  public void breakBlock(World world, BlockPos pos, IBlockState state) {
    TileEntity tileentity = world.getTileEntity(pos);
    if (tileentity instanceof TileEntitySeekAltar)
      InventoryHelper.dropInventoryItems(world, pos, (IInventory)tileentity);
    world.removeTileEntity(pos);
    super.breakBlock(world, pos, state);
  }

  public boolean hasComparatorInputOverride(IBlockState state) {
    return true;
  }

  public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
    TileEntity tileentity = worldIn.getTileEntity(pos);
    if (tileentity instanceof TileEntitySeekAltar)
      return Container.calcRedstoneFromInventory((IInventory)tileentity);
    return 0;
  }

  public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
    super.onBlockAdded(world, pos, state);
    if (!world.isRemote) {
      world.scheduleUpdate(pos, this, tickRate(world));
    }
  }

  public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
    super.updateTick(world, pos, state, random);
    if (!world.isRemote) {
      SeekAltarUpdateTick.run(world, pos.getX(), pos.getY(), pos.getZ());
      world.scheduleUpdate(pos, this, tickRate(world));
    }
  }

  public int tickRate(World worldIn) {
    return 10;
  }

  public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, EnumHand hand, EnumFacing direction, float hitX, float hitY, float hitZ) {
    super.onBlockActivated(world, pos, state, entity, hand, direction, hitX, hitY, hitZ);
    int x = pos.getX();
    int y = pos.getY();
    int z = pos.getZ();
      SeekAltarOnBlockRightClicked.run(entity, world, x, y, z);
    return true;
  }
}
