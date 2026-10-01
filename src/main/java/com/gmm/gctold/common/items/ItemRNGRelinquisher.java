
package com.gmm.gctold.common.items;

import java.util.List;

import com.gmm.gctold.misc.GctAllCreativeTab;
import com.gmm.gctold.common.events.RNGRelinquisherClick;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemRNGRelinquisher extends Item {
    public static final Item block = new ItemRNGRelinquisher();

    public ItemRNGRelinquisher() {
        this.setMaxDamage(0);
        this.maxStackSize = 1;
        this.setTranslationKey("rng_relinquisher");
        this.setRegistryName("rng_relinquisher");
        this.setCreativeTab(GctAllCreativeTab.TAB);

    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add("在末地原点右键传送至无名之地");
        tooltip.add("在无名之地任意地点右键传送回末地");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        RNGRelinquisherClick.run(player, world, (int) player.posX, (int) player.posY, (int) player.posZ);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
}
