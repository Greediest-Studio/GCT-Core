package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import com.gmm.gctold.common.events.EarthboundAltarClick;
import com.gmm.gctold.common.tile.TileEntityEarthboundAltar;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

  public class BlockEarthboundAltar extends Block implements ITileEntityProvider {
  public static final Block block = new BlockEarthboundAltar();
public BlockEarthboundAltar() {
    super(Material.ROCK);
    setTranslationKey("earthbound_altar");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 5);
    setHardness(10.0F);
    setResistance(10.0F);
    setLightLevel(0.0F);
    setLightOpacity(0);
    setCreativeTab(GctAllCreativeTab.TAB);
  }

  @SideOnly(Side.CLIENT)
  public BlockRenderLayer getRenderLayer() {
    return BlockRenderLayer.CUTOUT;
  }

  public boolean isFullCube(IBlockState state) {
    return false;
  }

  public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
    return (new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D)).union(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.7D, 1.0D));
  }

  public boolean isOpaqueCube(IBlockState state) {
    return false;
  }

  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return new TileEntityEarthboundAltar();
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
    if (tileentity instanceof TileEntityEarthboundAltar)
      InventoryHelper.dropInventoryItems(world, pos, (IInventory)tileentity);
    world.removeTileEntity(pos);
    super.breakBlock(world, pos, state);
  }

  public boolean hasComparatorInputOverride(IBlockState state) {
    return true;
  }

  public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
    TileEntity tileentity = worldIn.getTileEntity(pos);
    if (tileentity instanceof TileEntityEarthboundAltar)
      return Container.calcRedstoneFromInventory((IInventory)tileentity);
    return 0;
  }

  public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, EnumHand hand, EnumFacing direction, float hitX, float hitY, float hitZ) {
    super.onBlockActivated(world, pos, state, entity, hand, direction, hitX, hitY, hitZ);
    int x = pos.getX();
    int y = pos.getY();
    int z = pos.getZ();
      EarthboundAltarClick.run(entity, world, x, y, z);
    return true;
  }
}
