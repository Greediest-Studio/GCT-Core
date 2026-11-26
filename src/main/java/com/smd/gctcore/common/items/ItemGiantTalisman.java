package com.smd.gctcore.common.items;

import com.smd.gctcore.Tags;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A portable four-layer ward. Metadata zero is the cloud form, so ordinary
 * item creation starts in the cloud realm without requiring an NBT initializer.
 * The public layer API uses the terrain's bottom-to-top layer ordinals instead.
 */
public final class ItemGiantTalisman extends Item {
    public static final int VARIANT_COUNT = 4;
    private static final String[] VARIANT_NAMES = {"cloud", "surface", "deep", "lava"};

    public ItemGiantTalisman() {
        setRegistryName(Tags.MOD_ID, "giant_talisman");
        setTranslationKey(Tags.MOD_ID + ".giant_talisman");
        setCreativeTab(CreativeTabs.MISC);
        setMaxStackSize(1);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    /** Returns LAVA=0, DEEP=1, SURFACE=2, CLOUD=3, matching HeightLayer.ordinal(). */
    public int layerIndex(ItemStack stack) {
        return VARIANT_COUNT - 1 - variantMetadata(stack);
    }

    /** Updates the selected form using the terrain's bottom-to-top layer index. */
    public void setLayer(ItemStack stack, int ordinal) {
        if (!stack.isEmpty() && stack.getItem() == this) {
            stack.setItemDamage(metadataForLayer(ordinal));
        }
    }

    /** Converts LAVA..CLOUD ordinals to metadata CLOUD=0..LAVA=3. */
    public static int metadataForLayer(int ordinal) {
        return VARIANT_COUNT - 1 - Math.max(0, Math.min(VARIANT_COUNT - 1, ordinal));
    }

    /** Model suffix and translation suffix, in metadata order. */
    public static String variantNameForMetadata(int metadata) {
        return VARIANT_NAMES[normalizeMetadata(metadata)];
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return getTranslationKey() + "." + variantNameForMetadata(variantMetadata(stack));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) return;
        for (int metadata = 0; metadata < VARIANT_COUNT; metadata++) {
            items.add(new ItemStack(this, 1, metadata));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip,
                               ITooltipFlag flag) {
        String layer = I18n.format("tooltip.gctcore.giant_talisman.layer."
                + variantNameForMetadata(variantMetadata(stack)));
        tooltip.add(TextFormatting.AQUA + I18n.format("tooltip.gctcore.giant_talisman.layer", layer));
        tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.gctcore.giant_talisman.switch"));
        tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.gctcore.giant_talisman.use"));
    }

    private static int variantMetadata(ItemStack stack) {
        return stack.isEmpty() ? 0 : normalizeMetadata(stack.getMetadata());
    }

    private static int normalizeMetadata(int metadata) {
        return metadata >= 0 && metadata < VARIANT_COUNT ? metadata : 0;
    }
}
