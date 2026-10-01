
package com.gmm.gctold.common.items;

import java.util.List;

import com.gmm.gctold.misc.GctAllCreativeTab;
import com.gmm.gctold.common.events.ModeChanger;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemGreedycraftModChanger extends Item {
    public static final Item block = new ItemGreedycraftModChanger();

    public ItemGreedycraftModChanger() {
        this.setMaxDamage(0);
        this.maxStackSize = 1;
        this.setTranslationKey("greedycraft_mod_changer");
        this.setRegistryName("greedycraft_mod_changer");
        this.setCreativeTab(GctAllCreativeTab.TAB);

    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add("右键在生存和创造之间进行切换");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ModeChanger.run(player, world, (int) player.posX, (int) player.posY, (int) player.posZ);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
}
