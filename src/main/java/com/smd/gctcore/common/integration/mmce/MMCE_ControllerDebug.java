package com.smd.gctcore.common.integration.mmce;

import com.smd.gctcore.gctcore;
import com.smd.gctcore.common.config.GctCoreConfig;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftingStatus;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class MMCE_ControllerDebug {

    private static final Map<ControllerKey, ControllerSnapshot> SNAPSHOTS = new HashMap<>();

    private MMCE_ControllerDebug() {
    }

    public static void traceControllerBlockStateChange(World world, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
        if (!GctCoreConfig.mmceDebug.traceControllerBlockStateChanges || world == null || pos == null || oldState == null || newState == null) {
            return;
        }
        if (!(oldState.getBlock() instanceof BlockController) && !(newState.getBlock() instanceof BlockController)) {
            return;
        }

        EnumFacing oldFacing = getFacing(oldState);
        EnumFacing newFacing = getFacing(newState);
        Boolean oldFormed = getFormed(oldState);
        Boolean newFormed = getFormed(newState);
        if (oldFacing == newFacing && equalsNullable(oldFormed, newFormed)) {
            return;
        }

        gctcore.LOGGER.warn("[MMCE-DEBUG] Controller block state changed at dim={} pos={} flags={} old={} new={}",
                world.provider.getDimension(), formatPos(pos), flags, describeBlockState(oldState), describeBlockState(newState));
        logTileSnapshot(world, pos);
        logStackTrace();
    }

    public static void checkTickState(World world, BlockPos pos, TileMultiblockMachineController controller) {
        if (!GctCoreConfig.mmceDebug.logControllerTickStateChanges || world == null || pos == null || controller == null) {
            return;
        }

        ControllerKey key = new ControllerKey(world.provider.getDimension(), pos);
        ControllerSnapshot current = ControllerSnapshot.capture(world, pos, controller);
        ControllerSnapshot previous = SNAPSHOTS.put(key, current);
        if (previous != null && !previous.equals(current)) {
            gctcore.LOGGER.warn("[MMCE-DEBUG] Controller tick state changed at dim={} pos={} old={} new={}",
                    key.dimension, formatPos(pos), previous, current);
        }
        pruneSnapshots(world);
    }

    private static void logTileSnapshot(World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileMultiblockMachineController) {
            gctcore.LOGGER.warn("[MMCE-DEBUG] Controller tile snapshot: {}",
                    ControllerSnapshot.capture(world, pos, (TileMultiblockMachineController) tile));
        } else {
            gctcore.LOGGER.warn("[MMCE-DEBUG] Tile at controller position is {}", tile == null ? "null" : tile.getClass().getName());
        }
    }

    private static void logStackTrace() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        int printed = 0;
        int maxDepth = GctCoreConfig.mmceDebug.traceStackDepth;
        gctcore.LOGGER.warn("[MMCE-DEBUG] setBlockState caller stack:");
        for (StackTraceElement element : stack) {
            String className = element.getClassName();
            if (className.equals(Thread.class.getName()) || className.equals(MMCE_ControllerDebug.class.getName())) {
                continue;
            }
            gctcore.LOGGER.warn("[MMCE-DEBUG]   at {}", element);
            printed++;
            if (printed >= maxDepth) {
                break;
            }
        }
    }

    private static void pruneSnapshots(World world) {
        if (SNAPSHOTS.size() < 4096) {
            return;
        }
        int dimension = world.provider.getDimension();
        Iterator<ControllerKey> iterator = SNAPSHOTS.keySet().iterator();
        while (iterator.hasNext()) {
            ControllerKey key = iterator.next();
            if (key.dimension != dimension) {
                iterator.remove();
            }
        }
    }

    private static String describeBlockState(IBlockState state) {
        return state.getBlock().getRegistryName() + "{facing=" + getFacing(state) + ", formed=" + getFormed(state) + "}";
    }

    private static EnumFacing getFacing(IBlockState state) {
        return state.getPropertyKeys().contains(BlockController.FACING) ? state.getValue(BlockController.FACING) : null;
    }

    private static Boolean getFormed(IBlockState state) {
        return state.getPropertyKeys().contains(BlockController.FORMED) ? state.getValue(BlockController.FORMED) : null;
    }

    private static String formatPos(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private static boolean equalsNullable(Object first, Object second) {
        return first == null ? second == null : first.equals(second);
    }

    private static final class ControllerKey {
        private final int dimension;
        private final BlockPos pos;

        private ControllerKey(int dimension, BlockPos pos) {
            this.dimension = dimension;
            this.pos = pos.toImmutable();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ControllerKey)) {
                return false;
            }
            ControllerKey other = (ControllerKey) obj;
            return dimension == other.dimension && pos.equals(other.pos);
        }

        @Override
        public int hashCode() {
            return 31 * dimension + pos.hashCode();
        }
    }

    private static final class ControllerSnapshot {
        private final EnumFacing blockFacing;
        private final Boolean blockFormed;
        private final EnumFacing controllerRotation;
        private final boolean structureFormed;
        private final String status;
        private final String machine;

        private ControllerSnapshot(EnumFacing blockFacing, Boolean blockFormed, EnumFacing controllerRotation, boolean structureFormed, String status, String machine) {
            this.blockFacing = blockFacing;
            this.blockFormed = blockFormed;
            this.controllerRotation = controllerRotation;
            this.structureFormed = structureFormed;
            this.status = status;
            this.machine = machine;
        }

        private static ControllerSnapshot capture(World world, BlockPos pos, TileMultiblockMachineController controller) {
            IBlockState state = world.getBlockState(pos);
            CraftingStatus status = controller.getControllerStatus();
            DynamicMachine foundMachine = controller.getFoundMachine();
            return new ControllerSnapshot(
                    getFacing(state),
                    getFormed(state),
                    controller.getControllerRotation(),
                    controller.isStructureFormed(),
                    status == null ? "null" : status.toString(),
                    foundMachine == null || foundMachine.getRegistryName() == null ? "null" : foundMachine.getRegistryName().toString()
            );
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ControllerSnapshot)) {
                return false;
            }
            ControllerSnapshot other = (ControllerSnapshot) obj;
            return structureFormed == other.structureFormed
                    && blockFacing == other.blockFacing
                    && equalsNullable(blockFormed, other.blockFormed)
                    && controllerRotation == other.controllerRotation
                    && equalsNullable(status, other.status)
                    && equalsNullable(machine, other.machine);
        }

        @Override
        public int hashCode() {
            int result = blockFacing == null ? 0 : blockFacing.hashCode();
            result = 31 * result + (blockFormed == null ? 0 : blockFormed.hashCode());
            result = 31 * result + (controllerRotation == null ? 0 : controllerRotation.hashCode());
            result = 31 * result + (structureFormed ? 1 : 0);
            result = 31 * result + (status == null ? 0 : status.hashCode());
            result = 31 * result + (machine == null ? 0 : machine.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "{blockFacing=" + blockFacing
                    + ", blockFormed=" + blockFormed
                    + ", controllerRotation=" + controllerRotation
                    + ", structureFormed=" + structureFormed
                    + ", status=" + status
                    + ", machine=" + machine
                    + '}';
        }
    }
}
