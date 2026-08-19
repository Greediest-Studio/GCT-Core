package com.smd.gctcore.common.integration.top;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import vazkii.botania.api.mana.IManaPool;
import com.smd.gctcore.misc.Mods;

public final class ModdedManaPoolTextProbeProvider implements IProbeInfoProvider {

    private static final ResourceLocation BOTANIVERSE_POOL = new ResourceLocation("botaniverse", "morepool");
    private static final ResourceLocation DREAMING_POOL = new ResourceLocation("botanicadds", "pool_dreaming");
    private static final ResourceLocation GCT_MANA_POOL = new ResourceLocation("gctcore", "gct_mana_pool");
    private static final int DREAMING_POOL_CAPACITY = 2_000_000;

    @Override
    public String getID() {
        return "gctcore:modded_mana_pool_text";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world,
                             IBlockState blockState, IProbeHitData data) {
        ResourceLocation blockId = blockState.getBlock().getRegistryName();
        // ExtraBotany already supplies this exact text for every TilePool. Avoid
        // registering a second line for GCT's TilePool subclass when it is present.
        if (Mods.EXTRABOTANY.isLoading() && GCT_MANA_POOL.equals(blockId)) {
            return;
        }
        if (!BOTANIVERSE_POOL.equals(blockId) && !DREAMING_POOL.equals(blockId) && !GCT_MANA_POOL.equals(blockId)) {
            return;
        }

        TileEntity tile = world.getTileEntity(data.getPos());
        if (!(tile instanceof IManaPool)) {
            return;
        }

        int currentMana = Math.max(0, ((IManaPool) tile).getCurrentMana());
        int maxMana = getMaxMana(blockId, tile);
        if (maxMana <= 0) {
            return;
        }

        currentMana = Math.min(currentMana, maxMana);
        probeInfo.text("Mana:" + currentMana + "/" + maxMana);
    }

    private static int getMaxMana(ResourceLocation blockId, TileEntity tile) {
        if (DREAMING_POOL.equals(blockId)) {
            return DREAMING_POOL_CAPACITY;
        }

        NBTTagCompound tag = tile.writeToNBT(new NBTTagCompound());
        return tag.getInteger("manaCap");
    }
}
