package com.smd.gctcore.common.items;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.world.TimeLockedDimensions;
import com.smd.gctcore.common.world.WorldProviderLockedTime;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 时间调节器：右键让时间锁定维度（停机坪 / 虚无）的日期推进一天，昼夜相位保持不变。
 */
public class TimeAdjusterItem extends Item {

    public TimeAdjusterItem() {
        setRegistryName("time_adjuster");
        setTranslationKey(Tags.MOD_ID + ".time_adjuster");
        setCreativeTab(CreativeTabs.MISC);
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }

        WorldProviderLockedTime provider = TimeLockedDimensions.getProvider(world);
        if (provider == null) {
            sendStatus(player, new TextComponentTranslation("message.gctcore.time_adjuster.invalid"));
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        // 潜行右键回退一天，普通右键推进一天。
        boolean backwards = player.isSneaking();
        if (!TimeLockedDimensions.shiftDays(world, backwards ? -1 : 1)) {
            sendStatus(player, new TextComponentTranslation("message.gctcore.time_adjuster.earliest"));
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        sendStatus(player, new TextComponentTranslation(
                backwards ? "message.gctcore.time_adjuster.reverted" : "message.gctcore.time_adjuster.advanced",
                provider.getDayNumber()));
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_NOTE_PLING,
                SoundCategory.PLAYERS, 0.7F, backwards ? 0.8F : 1.5F);

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    private static void sendStatus(EntityPlayer player, ITextComponent message) {
        player.sendStatusMessage(message, true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.AQUA + I18n.format("tooltip.gctcore.time_adjuster.1"));
        tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.gctcore.time_adjuster.2"));
    }
}
