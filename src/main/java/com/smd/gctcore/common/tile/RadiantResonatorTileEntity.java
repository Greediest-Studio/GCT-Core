package com.smd.gctcore.common.tile;

import com.smd.gctcore.common.blocks.arcanearchives.RawQuartzClusterBlock;
import com.smd.gctcore.common.config.GctCoreConfig;
import com.smd.gctcore.misc.BlockRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class RadiantResonatorTileEntity extends TileEntity implements ITickable {
    private static final String TAG_GROWTH = "growth";
    private static final String TAG_OWNER = "owner";

    private UUID owner;
    private int growth;
    private int ticks;

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }

        ticks++;
        if (canTick() != TickResult.TICKING) {
            return;
        }

        int ticksRequired = Math.max(1, GctCoreConfig.radiantResonator.resonatorTickTime);
        if (growth < ticksRequired) {
            growth++;
            markDirty();
        }

        if (growth >= ticksRequired && world.isAirBlock(pos.up())) {
            growth = 0;
            world.setBlockState(pos.up(), BlockRegistry.RAW_QUARTZ_CLUSTER.getDefaultState());
            markDirty();
        }

        if (ticks % 50 == 0) {
            sync();
            world.notifyNeighborsOfStateChange(pos, getBlockType(), false);
        }
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        markDirty();
        sync();
    }

    @Nullable
    public UUID getOwner() {
        return owner;
    }

    public int getPercentageComplete() {
        int ticksRequired = Math.max(1, GctCoreConfig.radiantResonator.resonatorTickTime);
        return (int) Math.floor(growth / (double) ticksRequired * 100.0D);
    }

    public TickResult canTick() {
        if (world == null) {
            return TickResult.OFFLINE;
        }

        BlockPos up = pos.up();
        if (!world.isAirBlock(up)) {
            IBlockState upState = world.getBlockState(up);
            return upState.getBlock() instanceof RawQuartzClusterBlock ? TickResult.HARVEST_WAITING : TickResult.OBSTRUCTION;
        }

        return isOwnerOnline() ? TickResult.TICKING : TickResult.OFFLINE;
    }

    private boolean isOwnerOnline() {
        if (owner == null || world == null || world.getMinecraftServer() == null) {
            return false;
        }
        EntityPlayerMP player = world.getMinecraftServer().getPlayerList().getPlayerByUUID(owner);
        return player != null;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        growth = compound.getInteger(TAG_GROWTH);
        if (compound.hasKey(TAG_OWNER)) {
            owner = UUID.fromString(compound.getString(TAG_OWNER));
        }
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger(TAG_GROWTH, growth);
        if (owner != null) {
            compound.setString(TAG_OWNER, owner.toString());
        }
        return compound;
    }

    @Override
    @Nonnull
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    private void sync() {
        if (world == null || world.isRemote) {
            return;
        }
        IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 8);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public enum TickResult {
        OBSTRUCTION("obstruction", TextFormatting.RED),
        HARVEST_WAITING("harvestable", TextFormatting.GOLD),
        OFFLINE("offline", TextFormatting.DARK_RED),
        TICKING("resonating", TextFormatting.GREEN);

        private final String key;
        private final TextFormatting format;

        TickResult(String key, TextFormatting format) {
            this.key = key;
            this.format = format;
        }

        public String getTranslationKey() {
            return "top.gctcore.resonator.status." + key;
        }

        public TextFormatting getFormat() {
            return format;
        }
    }
}
