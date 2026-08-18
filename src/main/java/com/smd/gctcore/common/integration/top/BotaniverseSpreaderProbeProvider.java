package com.smd.gctcore.common.integration.top;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import vazkii.botania.api.mana.IManaCollector;

public final class BotaniverseSpreaderProbeProvider implements IProbeInfoProvider {

    private static final ResourceLocation MORE_SPREADER = new ResourceLocation("botaniverse", "morespreader");

    @Override
    public String getID() {
        return "gctcore:botaniverse_spreader";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world,
                             IBlockState blockState, IProbeHitData data) {
        if (!MORE_SPREADER.equals(blockState.getBlock().getRegistryName())) {
            return;
        }

        TileEntity tile = world.getTileEntity(data.getPos());
        if (!(tile instanceof IManaCollector)) {
            return;
        }

        IManaCollector spreader = (IManaCollector) tile;
        int maxMana = Math.max(1, spreader.getMaxMana());
        int currentMana = Math.max(0, Math.min(maxMana, spreader.getCurrentMana()));
        String label = "Mana:" + currentMana + "/" + maxMana;

        probeInfo.progress(currentMana, maxMana,
                probeInfo.defaultProgressStyle()
                        .prefix("Mana:")
                        .suffix("/" + maxMana));
        probeInfo.text(label);
    }
}
