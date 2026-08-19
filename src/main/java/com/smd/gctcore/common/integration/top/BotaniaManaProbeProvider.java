package com.smd.gctcore.common.integration.top;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.NumberFormat;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import vazkii.botania.api.mana.IManaCollector;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.common.block.tile.mana.TilePool;

public final class BotaniaManaProbeProvider implements IProbeInfoProvider {

    private static final ResourceLocation BOTANIA_POOL = new ResourceLocation("botania", "pool");
    private static final ResourceLocation BOTANIA_SPREADER = new ResourceLocation("botania", "spreader");
    private static final ResourceLocation BOTANIVERSE_POOL = new ResourceLocation("botaniverse", "morepool");
    private static final ResourceLocation BOTANIVERSE_SPREADER = new ResourceLocation("botaniverse", "morespreader");
    private static final ResourceLocation DREAMING_POOL = new ResourceLocation("botanicadds", "pool_dreaming");
    private static final ResourceLocation GCT_POOL = new ResourceLocation("gctcore", "gct_mana_pool");
    private static final ResourceLocation GCT_SPREADER = new ResourceLocation("gctcore", "gct_mana_spreader");

    private static final int DREAMING_POOL_CAPACITY = 2_000_000;
    private static final int BAR_WIDTH = 110;
    private static final int BORDER_COLOR = 0xFFFFFFFF;
    private static final int BACKGROUND_COLOR = 0xFF808080;
    private static final int MANA_COLOR = 0xFF27FFF7;

    @Override
    public String getID() {
        return "gctcore:botania_mana";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world,
                             IBlockState blockState, IProbeHitData data) {
        ResourceLocation blockId = blockState.getBlock().getRegistryName();
        boolean spreaderBlock = isSpreader(blockId);
        boolean poolBlock = isPool(blockId);
        if (!spreaderBlock && !poolBlock) {
            return;
        }

        TileEntity tile = world.getTileEntity(data.getPos());
        int currentMana;
        int maxMana;

        if (spreaderBlock) {
            if (!(tile instanceof IManaCollector)) {
                return;
            }
            IManaCollector collector = (IManaCollector) tile;
            currentMana = collector.getCurrentMana();
            maxMana = collector.getMaxMana();
        } else {
            if (!(tile instanceof IManaPool)) {
                return;
            }
            currentMana = ((IManaPool) tile).getCurrentMana();
            maxMana = getPoolCapacity(blockId, tile);
        }

        if (maxMana <= 0) {
            return;
        }
        currentMana = Math.max(0, Math.min(currentMana, maxMana));

        probeInfo.progress(currentMana, maxMana,
                probeInfo.defaultProgressStyle()
                        .prefix("Mana:" + currentMana)
                        .suffix("/" + maxMana)
                        .width(BAR_WIDTH)
                        .showText(true)
                        .numberFormat(NumberFormat.NONE)
                        .borderColor(BORDER_COLOR)
                        .backgroundColor(BACKGROUND_COLOR)
                        .filledColor(MANA_COLOR)
                        .alternateFilledColor(MANA_COLOR));
    }

    private static boolean isSpreader(ResourceLocation blockId) {
        return BOTANIA_SPREADER.equals(blockId)
                || BOTANIVERSE_SPREADER.equals(blockId)
                || GCT_SPREADER.equals(blockId);
    }

    private static boolean isPool(ResourceLocation blockId) {
        return BOTANIA_POOL.equals(blockId)
                || BOTANIVERSE_POOL.equals(blockId)
                || DREAMING_POOL.equals(blockId)
                || GCT_POOL.equals(blockId);
    }

    private static int getPoolCapacity(ResourceLocation blockId, TileEntity tile) {
        if (tile instanceof TilePool) {
            return ((TilePool) tile).manaCap;
        }
        if (DREAMING_POOL.equals(blockId)) {
            return DREAMING_POOL_CAPACITY;
        }

        NBTTagCompound tag = tile.writeToNBT(new NBTTagCompound());
        return tag.getInteger("manaCap");
    }
}
