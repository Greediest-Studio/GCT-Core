package com.smd.gctcore.common.integration.extendedcrafting;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import com.smd.gctcore.Tags;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemEncodedExtendedPattern extends Item implements ICraftingPatternItem {
    private final ExtendedCraftingTier tier;

    public ItemEncodedExtendedPattern(ExtendedCraftingTier tier) {
        this.tier = tier;
        setRegistryName(Tags.MOD_ID, tier.id() + "_encoded_extended_pattern");
        setTranslationKey(Tags.MOD_ID + "." + tier.id() + "_encoded_extended_pattern");
        setCreativeTab(CreativeTabs.MISC);
        setMaxStackSize(64);
    }

    public ExtendedCraftingTier tier() {
        return tier;
    }

    @Override
    public ICraftingPatternDetails getPatternForItem(ItemStack stack, World world) {
        return ExtendedPatternData.isValid(stack, world) ? new ExtendedCraftingPatternDetails(stack, world) : null;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (!player.isSneaking() || !ExtendedPatternData.isEncoded(held)) {
            return new ActionResult<>(EnumActionResult.PASS, held);
        }
        if (!world.isRemote) {
            ItemStack blank = new ItemStack(ExtendedCraftingAutomation.blankPattern(tier), held.getCount());
            player.setHeldItem(hand, blank);
            held = blank;
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, held);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (!ExtendedPatternData.isEncoded(stack)) {
            return;
        }
        if (world != null && !ExtendedPatternData.isValid(stack, world)) {
            tooltip.add(TextFormatting.RED + net.minecraft.util.text.translation.I18n.translateToLocal(
                    "tooltip.gctcore.extended_pattern.invalid"));
        }
        ItemStack output = ExtendedPatternData.readOutput(stack);
        if (!output.isEmpty()) {
            tooltip.add(TextFormatting.GOLD + net.minecraft.util.text.translation.I18n.translateToLocalFormatted(
                    "tooltip.gctcore.extended_pattern.crafts", output.getCount(), output.getDisplayName()));
        }
        for (ItemStack input : ExtendedPatternData.readInputs(stack)) {
            tooltip.add(TextFormatting.GRAY + "  " + input.getCount() + " x " + input.getDisplayName());
        }
        tooltip.add(TextFormatting.DARK_GRAY + net.minecraft.util.text.translation.I18n.translateToLocal(
                "tooltip.gctcore.extended_pattern.clear"));
    }

    public ItemStack getOutput(ItemStack stack) {
        return ExtendedPatternData.readOutput(stack);
    }
}
