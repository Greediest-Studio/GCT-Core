package com.smd.gctcore.common.items.botania;

import com.smd.gctcore.common.entity.botania.EntityAlfSpark;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.IManaGivingItem;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;

import vazkii.botania.common.core.BotaniaCreativeTab;

import javax.annotation.Nonnull;

public class ItemAlfSpark extends Item implements IManaGivingItem {

    public ItemAlfSpark() {
        setRegistryName("alf_spark");
        setTranslationKey("gctcore.alf_spark");
        setMaxStackSize(64);
        setCreativeTab(BotaniaCreativeTab.INSTANCE);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing side,
                                      float xv, float yv, float zv) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof ISparkAttachable) {
            ISparkAttachable attach = (ISparkAttachable) tile;
            ItemStack stack = player.getHeldItem(hand);
            if (attach.canAttachSpark(stack) && attach.getAttachedSpark() == null) {
                if (!world.isRemote) {
                    stack.shrink(1);
                    EntityAlfSpark spark = new EntityAlfSpark(world);
                    spark.setPosition(pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D);
                    world.spawnEntity(spark);
                    attach.attachSpark((ISparkEntity) spark);
                    VanillaPacketDispatcher.dispatchTEToNearbyPlayers(world, pos);
                }
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }
}
