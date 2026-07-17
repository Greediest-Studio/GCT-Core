package com.smd.gctcore.common.mixin.mekanism;

import com.smd.gctcore.common.config.GCTCompatConfig;
import com.smd.gctcore.common.integration.mekanism.DigitalMinerHarvestAccess;
import com.smd.gctcore.common.integration.mekanism.GctMekanismUpgrades;
import com.smd.gctcore.common.integration.mekanism.MekanismHarvestHelper;
import io.netty.buffer.ByteBuf;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Digital Miner harvest-level limit driven by installed mining-level upgrade NBT
 * (stored on the tile when the upgrade is installed; synced to client via TE packets).
 */
@Mixin(value = TileEntityDigitalMiner.class, remap = false)
public abstract class MixinTileEntityDigitalMiner implements DigitalMinerHarvestAccess {

    @Unique
    private int gct$miningLevel;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gct$supportMiningLevelUpgrade(CallbackInfo ci) {
        if (GctMekanismUpgrades.MINING_LEVEL != null) {
            ((TileEntityDigitalMiner) (Object) this).setSupportedUpgrade(GctMekanismUpgrades.MINING_LEVEL);
        }
    }

    @Inject(method = "canMine", at = @At("HEAD"), cancellable = true)
    private void gct$limitByMiningLevel(Coord4D coord, CallbackInfoReturnable<Boolean> cir) {
        if (!GCTCompatConfig.mekanismIntegration.enableDigitalMinerHarvestLimit) {
            return;
        }
        try {
            World world = ((TileEntity) (Object) this).getWorld();
            if (world == null) {
                return;
            }
            IBlockState state = coord.getBlockState(world);
            if (!MekanismHarvestHelper.canDigitalMinerMine(state, gct$getMiningLevel())) {
                cir.setReturnValue(false);
            }
        } catch (Throwable ignored) {
            // Be permissive for modded blocks that throw from harvest APIs.
        }
    }

    @Inject(method = "writeCustomNBT", at = @At("RETURN"))
    private void gct$writeMiningLevel(NBTTagCompound nbtTags, CallbackInfo ci) {
        nbtTags.setInteger(MekanismHarvestHelper.TILE_MINING_LEVEL_KEY, this.gct$miningLevel);
    }

    @Inject(method = "readCustomNBT", at = @At("RETURN"))
    private void gct$readMiningLevel(NBTTagCompound nbtTags, CallbackInfo ci) {
        if (nbtTags.hasKey(MekanismHarvestHelper.TILE_MINING_LEVEL_KEY)) {
            this.gct$miningLevel = Math.max(0, nbtTags.getInteger(MekanismHarvestHelper.TILE_MINING_LEVEL_KEY));
        } else {
            this.gct$miningLevel = 0;
        }
    }

    /** Sync mining level with full/generic GUI packets (type 0 / type 1). */
    @Inject(method = "addBasicData", at = @At("RETURN"))
    private void gct$writeMiningLevelPacket(TileNetworkList data, CallbackInfo ci) {
        data.add(this.gct$miningLevel);
    }

    @Inject(method = "readBasicData", at = @At("RETURN"))
    private void gct$readMiningLevelPacket(ByteBuf dataStream, CallbackInfo ci) {
        if (dataStream.isReadable()) {
            this.gct$miningLevel = Math.max(0, dataStream.readInt());
        }
    }

    @Override
    public int gct$getMiningLevel() {
        return this.gct$miningLevel;
    }

    @Override
    public void gct$setMiningLevel(int level) {
        this.gct$miningLevel = Math.max(0, level);
    }
}
