package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import com.gmm.gctold.common.events.ApocalypseAltarClick;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

  public class BlockApocalypseAltar extends Block {
  public static final Block block = new BlockApocalypseAltar();

  public BlockApocalypseAltar() {
    super(Material.ROCK);
    setTranslationKey("apocalypse_altar");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 25);
    setHardness(200.0F);
    setResistance(10000.0F);
    setLightLevel(0.0F);
    setLightOpacity(0);
    setCreativeTab(GctAllCreativeTab.TAB);
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

  public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, EnumHand hand, EnumFacing direction, float hitX, float hitY, float hitZ) {
    super.onBlockActivated(world, pos, state, entity, hand, direction, hitX, hitY, hitZ);
    int x = pos.getX();
    int y = pos.getY();
    int z = pos.getZ();
      ApocalypseAltarClick.run(entity, world, x, y, z);
    return true;
  }
}
