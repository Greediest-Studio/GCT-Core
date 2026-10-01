package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import java.util.Random;
import javax.annotation.Nullable;
import com.gmm.gctold.common.events.BesideVoidPortalUp;
import com.gmm.gctold.common.tile.TileEntityBesideVoidPortal1;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

  public class BlockBesideVoidPortal1 extends Block implements ITileEntityProvider {
  public static final Block block = new BlockBesideVoidPortal1();
public BlockBesideVoidPortal1() {
    super(Material.ROCK);
    setTranslationKey("beside_void_portal_1");
    setSoundType(SoundType.GLASS);
    setHardness(1.0F);
    setResistance(10.0F);
    setLightLevel(0.33333334F);
    setLightOpacity(0);
    setCreativeTab(GctAllCreativeTab.TAB);
    setBlockUnbreakable();
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

  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return new TileEntityBesideVoidPortal1();
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
    if (tileentity instanceof TileEntityBesideVoidPortal1)
      InventoryHelper.dropInventoryItems(world, pos, (IInventory)tileentity);
    world.removeTileEntity(pos);
    super.breakBlock(world, pos, state);
  }

  public boolean hasComparatorInputOverride(IBlockState state) {
    return true;
  }

  public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
    TileEntity tileentity = worldIn.getTileEntity(pos);
    if (tileentity instanceof TileEntityBesideVoidPortal1)
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
      BesideVoidPortalUp.run(world, pos.getX(), pos.getY(), pos.getZ());
      if (world.getBlockState(pos).getBlock() == this) {
        world.scheduleUpdate(pos, this, tickRate(world));
      }
    }
  }

  public int tickRate(World worldIn) {
    return 20;
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
    for (int l = 0; l < 4; l++) {
      double d0 = i + 0.5D + (random.nextFloat() - 0.5D) * 0.5D * 20.0D;
      double d1 = j + 0.7D + (random.nextFloat() - 0.5D) * 0.5D + 0.5D;
      double d2 = k + 0.5D + (random.nextFloat() - 0.5D) * 0.5D * 20.0D;
      world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, 0.0D, 0.0D, 0.0D, new int[0]);
    }
  }
}
