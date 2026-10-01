package com.gmm.gctold.common.items;

import com.gmm.gctold.misc.GctAllCreativeTab;

import java.util.List;
import com.gmm.gctold.common.events.KeyOfDarkRightClickedOnBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemKeyOfDark extends Item {
  public static final Item block = new ItemKeyOfDark();
    public ItemKeyOfDark() {
      setMaxDamage(0);
      this.maxStackSize = 1;
      setTranslationKey("keyofdark");
      setRegistryName("keyofdark");
      setCreativeTab(GctAllCreativeTab.TAB);
    }
public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
      super.addInformation(itemstack, world, list, flag);
      list.add("来自黑暗的堕落");
    }

    public EnumActionResult onItemUseFirst(EntityPlayer entity, World world, BlockPos pos, EnumFacing direction,
        float hitX, float hitY, float hitZ, EnumHand hand) {
      return useKey(entity, world, pos, hand, direction);
    }

    public EnumActionResult onItemUse(EntityPlayer entity, World world, BlockPos pos, EnumHand hand,
        EnumFacing direction, float hitX, float hitY, float hitZ) {
      return useKey(entity, world, pos, hand, direction);
    }

    private EnumActionResult useKey(EntityPlayer entity, World world, BlockPos pos, EnumHand hand,
        EnumFacing direction) {
      ItemStack itemstack = entity.getHeldItem(hand);
      if (!entity.canPlayerEdit(pos, direction, itemstack))
        return EnumActionResult.FAIL;
      int x = pos.getX();
      int y = pos.getY();
      int z = pos.getZ();
      return KeyOfDarkRightClickedOnBlock.run(entity, world, x, y, z) ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
    }
  }


