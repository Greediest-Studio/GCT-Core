package com.smd.gctcore.common.items.botania;

import com.smd.gctcore.common.entity.botania.EntityCustomSpark;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.IManaGivingItem;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.common.core.BotaniaCreativeTab;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/** Common item behaviour for the extra GCT-Core spark tiers. */
public abstract class ItemTieredSpark extends Item implements IManaGivingItem {

    protected ItemTieredSpark(String registryName, String translationKey) {
        setRegistryName(registryName);
        setTranslationKey(translationKey);
        setMaxStackSize(64);
        setCreativeTab(BotaniaCreativeTab.INSTANCE);
    }

    protected abstract EntityCustomSpark createSpark(World world);

    protected abstract String getTooltipKey();

    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing side,
                                      float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof ISparkAttachable) {
            ISparkAttachable attach = (ISparkAttachable) tile;
            ItemStack stack = player.getHeldItem(hand);
            if (attach.canAttachSpark(stack) && attach.getAttachedSpark() == null) {
                if (!world.isRemote) {
                    stack.shrink(1);
                    EntityCustomSpark spark = createSpark(world);
                    spark.setPosition(pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D);
                    world.spawnEntity(spark);
                    attach.attachSpark(spark);
                    VanillaPacketDispatcher.dispatchTEToNearbyPlayers(world, pos);
                }
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip,
                                ITooltipFlag flag) {
        String localized = I18n.translateToLocal(getTooltipKey());
        if (localized == null || localized.equals(getTooltipKey())) {
            return;
        }
        // GreedyCraft's language entries encode the line break as the two
        // characters "\\n"; accepting a real newline as well keeps the item
        // usable with ordinary language packs.
        for (String line : localized.replace("\\n", "\n").split("\\r?\\n", -1)) {
            tooltip.add(line);
        }
    }
}
