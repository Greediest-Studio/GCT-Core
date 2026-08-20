package com.smd.gctcore.common.integration.top;

import com.smd.gctcore.common.entity.botania.EntityAlfSpark;
import com.smd.gctcore.common.entity.botania.EntityGaiaSpark;
import com.smd.gctcore.common.entity.botania.EntityJoetunheimSpark;
import com.smd.gctcore.common.entity.botania.EntityNidavellirSpark;
import com.smd.gctcore.common.entity.botania.EntityVanaheimSpark;
import mcjty.theoneprobe.api.IProbeHitEntityData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoEntityProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/** TOPCE entity information for the additional GCT-Core spark tiers. */
public final class GctSparkProbeProvider implements IProbeInfoEntityProvider {

    @Override
    public String getID() {
        return "gctcore.spark";
    }

    @Override
    public void addProbeEntityInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player,
                                   World world, Entity entity, IProbeHitEntityData data) {
        int transferRate = getTransferRate(entity);
        if (transferRate > 0) {
            // Match TopAllDependents' Botania spark provider format.
            probeInfo.text(transferRate + "Mana/s");
        }
    }

    private static int getTransferRate(Entity entity) {
        if (entity instanceof EntityAlfSpark) {
            return 6_000;
        }
        if (entity instanceof EntityGaiaSpark) {
            return 25_000;
        }
        if (entity instanceof EntityJoetunheimSpark) {
            return 400_000;
        }
        if (entity instanceof EntityNidavellirSpark) {
            return 1_600_000;
        }
        if (entity instanceof EntityVanaheimSpark) {
            return 25_000_000;
        }
        return 0;
    }
}
