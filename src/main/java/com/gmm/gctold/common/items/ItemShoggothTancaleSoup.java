package com.gmm.gctold.common.items;

import com.smd.gctcore.Tags;
import com.gmm.gctold.misc.GctAllCreativeTab;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemShoggothTancaleSoup extends ItemFood {
  public static final Item block = new ItemShoggothTancaleSoup();
    private static final float SANITY_CHANGE = -20.0F;

    public ItemShoggothTancaleSoup() {
      super(6, 0.6F, false);
      setTranslationKey("shoggoth_tancale_soup");
      setRegistryName(new ResourceLocation(Tags.MOD_ID, "shoggoth_tancale_soup"));
      setCreativeTab(GctAllCreativeTab.TAB);
      setMaxStackSize(64);
      setAlwaysEdible();
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
      return 32;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
      return EnumAction.EAT;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
      ItemStack stack = player.getHeldItem(hand);
      player.setActiveHand(hand);
      return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {
      boolean creativePlayer = entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode;
      ItemStack originalStack = creativePlayer ? stack.copy() : ItemStack.EMPTY;
      ItemStack result = super.onItemUseFinish(stack, world, entity);
      if (creativePlayer) {
        originalStack.setCount(Math.max(1, originalStack.getCount()));
        return originalStack;
      }
      return result;
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
      super.onFoodEaten(stack, world, player);
      if (GameStageHelper.hasStage(player, "final")) {
        float sanity = player.getEntityData().getFloat("sanityAbyss") + SANITY_CHANGE;
        player.getEntityData().setFloat("sanityAbyss", MathHelper.clamp(sanity, -100.0F, 100.0F));
      }
    }
  }

