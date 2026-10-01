
package com.gmm.gctold.common.items;

import java.util.List;

import com.gmm.gctold.misc.GctAllCreativeTab;
import com.gmm.gctold.common.events.DoorKeyOrderlandClick;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemDoorKeyOfOrderland extends Item {
    public static final Item block = new ItemDoorKeyOfOrderland();

    public ItemDoorKeyOfOrderland() {
        this.setMaxDamage(0);
        this.maxStackSize = 1;
        this.setTranslationKey("door_key_of_orderland");
        this.setRegistryName("door_key_of_orderland");
        this.setCreativeTab(GctAllCreativeTab.TAB);

    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add("右键生成一个秩序之地传送门！");
        tooltip.add("只有击败了凋灵风暴的人才有资格进入秩序之地……");
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing direction,
            float hitX, float hitY, float hitZ, EnumHand hand) {
        DoorKeyOrderlandClick.run(player, world, pos.getX(), pos.getY(), pos.getZ());
        return EnumActionResult.SUCCESS;
    }
}
