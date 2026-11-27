package com.smd.gctcore.common.tile;

import com.smd.gctcore.common.blocks.jotunheim.BlockJotunheimPortalCore;
import com.smd.gctcore.common.blocks.botania.BlockGctManaPool;
import com.smd.gctcore.common.blocks.botania.BlockGctManaPylon;
import com.smd.gctcore.common.blocks.botania.BlockGctManaWood;
import com.smd.gctcore.common.botania.GctManaPoolTier;
import com.smd.gctcore.common.botania.GctManaWoodVariant;
import com.smd.gctcore.common.world.jotunheim.YmirForceRules;
import com.smd.gctcore.misc.BlockRegistry;
import com.smd.gctcore.common.world.jotunheim.JotunheimTeleporter;
import com.smd.gctcore.common.world.jotunheim.JotunheimDimension;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import vazkii.botania.api.mana.IManaReceiver;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JotunheimPortalTileEntity extends TileEntity implements ITickable {
    private static final int MANA_COST = 80000000;
    private static final int REQUIRED_PYLONS = 4;
    private static final BlockPos[] LIVINGWOOD_POSITIONS = {
            new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0), new BlockPos(-2, 1, 0),
            new BlockPos(2, 1, 0), new BlockPos(-2, 3, 0), new BlockPos(2, 3, 0),
            new BlockPos(-1, 4, 0), new BlockPos(1, 4, 0)
    };

    private static final BlockPos[] GLIMMERING_LIVINGWOOD_POSITIONS = {
            new BlockPos(-2, 2, 0), new BlockPos(2, 2, 0), new BlockPos(0, 4, 0)
    };

    private static final BlockPos[] AIR_POSITIONS = {
            new BlockPos(-1, 1, 0), new BlockPos(0, 1, 0), new BlockPos(1, 1, 0),
            new BlockPos(-1, 2, 0), new BlockPos(0, 2, 0), new BlockPos(1, 2, 0),
            new BlockPos(-1, 3, 0), new BlockPos(0, 3, 0), new BlockPos(1, 3, 0)
    };

    private static final String TAG_AXIS = "axis";
    private int portalAxis;
    private int ticks;

    @Override
    public void update() {
        if (world == null || world.isRemote || !isActive()) {
            return;
        }
        // A neighbouring frame/pylon chunk may load after the core. Leave
        // the saved active state intact until the whole ritual is available.
        if (!isRitualAreaLoaded()) return;

        // Validate on the first tick after load, then once per second; the
        // ritual scans a volume of blocks and need not run every server tick.
        if (ticks % 20 == 0) {
            int validAxis = getValidAxis();
            if (validAxis == 0 || locatePylons().size() < REQUIRED_PYLONS) {
                setActive(false, 0);
                return;
            }
            if (portalAxis != validAxis) {
                portalAxis = validAxis;
                markDirty();
                sync();
            }
        }
        ticks++;
        if (ticks % 5 == 0) {
            teleportPlayers();
        }
    }

    public boolean tryActivate(EntityPlayer player) {
        if (world == null || world.isRemote || isActive()) {
            return false;
        }
        if (!isRitualAreaLoaded()) {
            sendStatus(player, "message.gctcore.jotunheim_portal.invalid_structure");
            return false;
        }

        int validAxis = getValidAxis();
        if (validAxis == 0) {
            sendStatus(player, "message.gctcore.jotunheim_portal.invalid_structure");
            return false;
        }

        List<TileEntity> pools = locatePylons();
        if (pools.size() < REQUIRED_PYLONS) {
            sendStatus(player, "message.gctcore.jotunheim_portal.missing_pylons");
            return false;
        }

        if (!consumeMana(pools, MANA_COST, world.isRemote)) {
            sendStatus(player, "message.gctcore.jotunheim_portal.not_enough_mana");
            return false;
        }

        setActive(true, validAxis);
        sendStatus(player, "message.gctcore.jotunheim_portal.activated");
        return true;
    }

    private void teleportPlayers() {
        AxisAlignedBB aabb = getPortalAABB();
        List<EntityPlayerMP> players = world.getEntitiesWithinAABB(EntityPlayerMP.class, aabb);
        for (EntityPlayerMP player : players) {
            if (player.dimension == JotunheimDimension.DIMENSION_ID
                    || player.isRiding()
                    || player.isBeingRidden()
                    || player.timeUntilPortal > 0) {
                continue;
            }

            WorldServer destination = DimensionManager.getWorld(JotunheimDimension.DIMENSION_ID);
            if (destination == null) {
                DimensionManager.initDimension(JotunheimDimension.DIMENSION_ID);
                destination = DimensionManager.getWorld(JotunheimDimension.DIMENSION_ID);
            }
            if (destination == null) {
                continue;
            }

            MinecraftServer server = player.getServer();
            if (server == null) {
                continue;
            }

            int layer = YmirForceRules.normalizeTalismans(player);
            if (layer < 0) layer = 3; // No talisman: enter the cloud realm.
            BlockPos target = JotunheimTeleporter.findSafeDestination(destination, player.posX, player.posZ, layer);
            player.timeUntilPortal = 100;
            server.getPlayerList().transferPlayerToDimension(
                    player,
                    JotunheimDimension.DIMENSION_ID,
                    new JotunheimTeleporter(destination, target)
            );
            player.timeUntilPortal = 100;
        }
    }

    private AxisAlignedBB getPortalAABB() {
        if (portalAxis == 2) {
            return new AxisAlignedBB(pos.add(0, 1, -1), pos.add(1, 4, 2));
        }
        return new AxisAlignedBB(pos.add(-1, 1, 0), pos.add(2, 4, 1));
    }

    public boolean isPortalActiveForRender() {
        return world != null && isActive();
    }

    public int getPortalAxisForRender() {
        return portalAxis != 0 ? portalAxis : getValidAxis();
    }

    private boolean consumeMana(List<TileEntity> pools, int amount, boolean simulate) {
        long available = 0;
        for (TileEntity pool : pools) {
            available += Math.max(0, getCurrentMana(pool));
            if (available >= amount) {
                break;
            }
        }

        if (available < amount) {
            return false;
        }

        if (!simulate) {
            int remaining = amount;
            for (TileEntity pool : pools) {
                int toDrain = Math.min(remaining, Math.max(0, getCurrentMana(pool)));
                if (toDrain > 0) {
                    drainMana(pool, toDrain);
                    remaining -= toDrain;
                }
                if (remaining <= 0) {
                    break;
                }
            }
        }

        return true;
    }

    private int getCurrentMana(TileEntity tile) {
        return tile instanceof IManaReceiver ? ((IManaReceiver) tile).getCurrentMana() : 0;
    }

    private void drainMana(TileEntity tile, int amount) {
        if (tile instanceof IManaReceiver) {
            ((IManaReceiver) tile).recieveMana(-amount);
        }
    }

    private List<TileEntity> locatePylons() {
        List<TileEntity> pools = new ArrayList<>();
        Set<BlockPos> poolPositions = new HashSet<>();
        int range = 5;
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pylonPos = pos.add(x, y, z);
                    if (!world.isBlockLoaded(pylonPos)) continue;
                    IBlockState pylonState = world.getBlockState(pylonPos);
                    if (pylonState.getBlock() != BlockRegistry.GCT_MANA_PYLON
                            || pylonState.getValue(BlockGctManaPylon.TIER) != GctManaPoolTier.JOETUNHEIM) {
                        continue;
                    }

                    BlockPos poolPos = pylonPos.down();
                    TileEntity pool = world.getTileEntity(poolPos);
                    if (isCompatibleManaPool(poolPos, pool) && poolPositions.add(pool.getPos())) {
                        pools.add(pool);
                    }
                }
            }
        }
        return pools;
    }

    private boolean isRitualAreaLoaded() {
        return world.isAreaLoaded(pos.add(-5, 0, -5), pos.add(5, 0, 5), false);
    }

    private int getValidAxis() {
        if (checkFrame(false, false) || checkFrame(false, true)) {
            return 1;
        }
        if (checkFrame(true, false) || checkFrame(true, true)) {
            return 2;
        }
        return 0;
    }

    private boolean checkFrame(boolean swapXZ, boolean mirror) {
        for (BlockPos offset : AIR_POSITIONS) {
            if (!isAir(offset, swapXZ, mirror)) {
                return false;
            }
        }

        for (BlockPos offset : LIVINGWOOD_POSITIONS) {
            if (!isManaWood(offset, GctManaWoodVariant.JOETUNHEIM, swapXZ, mirror)) {
                return false;
            }
        }

        for (BlockPos offset : GLIMMERING_LIVINGWOOD_POSITIONS) {
            if (!isManaWood(offset, GctManaWoodVariant.JOETUNHEIM_GLOWING, swapXZ, mirror)) {
                return false;
            }
        }

        return true;
    }

    private boolean isAir(BlockPos offset, boolean swapXZ, boolean mirror) {
        BlockPos checkPos = pos.add(convert(offset, swapXZ, mirror));
        if (!world.isBlockLoaded(checkPos)) return false;
        IBlockState state = world.getBlockState(checkPos);
        return state.getBlock().isAir(state, world, checkPos);
    }

    private boolean isManaWood(BlockPos offset, GctManaWoodVariant variant, boolean swapXZ, boolean mirror) {
        BlockPos checkPos = pos.add(convert(offset, swapXZ, mirror));
        if (!world.isBlockLoaded(checkPos)) return false;
        IBlockState state = world.getBlockState(checkPos);
        return state.getBlock() == BlockRegistry.GCT_MANA_WOOD
                && state.getValue(BlockGctManaWood.VARIANT) == variant;
    }

    private boolean isCompatibleManaPool(BlockPos poolPos, TileEntity pool) {
        if (!(pool instanceof IManaReceiver)) return false;
        IBlockState state = world.getBlockState(poolPos);
        return state.getBlock() == BlockRegistry.GCT_MANA_POOL
                && state.getValue(BlockGctManaPool.TIER) == GctManaPoolTier.JOETUNHEIM;
    }

    private BlockPos convert(BlockPos offset, boolean swapXZ, boolean mirror) {
        int x = offset.getX();
        int y = offset.getY();
        int z = mirror ? -offset.getZ() : offset.getZ();
        return swapXZ ? new BlockPos(z, y, x) : new BlockPos(x, y, z);
    }

    private boolean isActive() {
        return world.getBlockState(pos).getBlock() instanceof BlockJotunheimPortalCore
                && world.getBlockState(pos).getValue(BlockJotunheimPortalCore.ACTIVE);
    }

    private void setActive(boolean active, int axis) {
        portalAxis = axis;
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockJotunheimPortalCore) {
            world.setBlockState(pos, state.withProperty(BlockJotunheimPortalCore.ACTIVE, active), 1 | 2);
            markDirty();
            sync();
        }
    }

    private void sync() {
        if (world == null || world.isRemote) {
            return;
        }
        IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 8);
    }

    private void sendStatus(EntityPlayer player, String key) {
        if (player != null && !world.isRemote) {
            player.sendStatusMessage(new TextComponentTranslation(key), true);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        int axis = compound.getInteger(TAG_AXIS);
        portalAxis = axis == 1 || axis == 2 ? axis : 0;
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger(TAG_AXIS, portalAxis);
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

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    @Nonnull
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos.add(-2, 0, -2), pos.add(3, 5, 3));
    }
}
