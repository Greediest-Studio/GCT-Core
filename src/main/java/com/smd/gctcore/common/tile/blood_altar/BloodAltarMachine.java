package com.smd.gctcore.common.tile.blood_altar;

import WayofTime.bloodmagic.altar.BloodAltar;
import WayofTime.bloodmagic.tile.TileAltar;
import com.smd.gctcore.common.mixin.bloodmagic.TileAltarAccessor;
import crafttweaker.util.IEventHandler;
import github.kasuminova.mmce.common.event.Phase;
import github.kasuminova.mmce.common.event.machine.MachineStructureUpdateEvent;
import github.kasuminova.mmce.common.event.machine.MachineTickEvent;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.machine.factory.FactoryRecipeThread;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;

/**
 * Integrates the JSON-defined Blood Altar machine with its dedicated factory
 * controller.  The structure is intentionally not duplicated in Java: MMCE
 * loads it directly from {@code config/modularmachinery/machinery}.
 */
public final class BloodAltarMachine {
    public static final ResourceLocation MACHINE_ID =
            new ResourceLocation("modularmachinery", "blood_altar");

    public static final String DATA_LEVEL = "level";
    public static final String DATA_CAPACITY = "capacityLP";
    public static final String DATA_LIFE_ESSENCE = "LP";
    public static final String DATA_MODE = "mode";
    public static final String DATA_SPEED = "speed";

    /**
     * MMCE persists these core-thread identifiers and compares them verbatim
     * with {@code MachineRecipe#getThreadName()}.  They are identifiers, not
     * display text; the GUI resolves them through the language files.
     */
    public static final String THREAD_PURIFICATION =
            "gui.gctcore.blood_altar.thread.purification";
    public static final String THREAD_ORB =
            "gui.gctcore.blood_altar.thread.orb";
    public static final String THREAD_CRAFTING =
            "gui.gctcore.blood_altar.thread.crafting";

    private static final String LEGACY_THREAD_PURIFICATION = "源质净化模块";
    private static final String LEGACY_THREAD_ORB = "宝珠输出模块";
    private static final String LEGACY_THREAD_CRAFTING = "血之合成模块";

    public static final String RUNE_SPEED = "bloodmagic:blood_rune@1";
    public static final String RUNE_BASE = "bloodmagic:blood_rune@0";
    public static final String RUNE_EFFICIENCY = "bloodmagic:blood_rune@2";
    public static final String RUNE_SACRIFICE = "bloodmagic:blood_rune@3";
    public static final String RUNE_SELF_SACRIFICE = "bloodmagic:blood_rune@4";
    public static final String RUNE_DISLOCATION = "bloodmagic:blood_rune@5";
    public static final String RUNE_AUGMENTED_CAPACITY = "bloodmagic:blood_rune@6";
    public static final String RUNE_CAPACITY = "bloodmagic:blood_rune@7";
    public static final String RUNE_ORB = "bloodmagic:blood_rune@8";
    public static final String RUNE_ACCELERATION = "bloodmagic:blood_rune@9";
    public static final String RUNE_THREAD = "additions:blood_rune_thread";
    public static final String RUNE_ECONOMY = "additions:blood_rune_economy";
    public static final String RUNE_PURIFICATION = "additions:blood_rune_purify";
    public static final String RUNE_PERSONAL = "additions:blood_rune_personal";

    private static final int MAX_EXTRA_THREADS = 15;
    private static final IEventHandler<MachineStructureUpdateEvent> STRUCTURE_UPDATE_HANDLER =
            BloodAltarMachine::onStructureUpdated;
    private static final IEventHandler<MachineTickEvent> PRE_TICK_HANDLER =
            BloodAltarMachine::onMachinePreTick;

    private BloodAltarMachine() {
    }

    public static boolean isBloodAltar(final DynamicMachine machine) {
        return machine != null && MACHINE_ID.equals(machine.getRegistryName());
    }

    /** Resolves the machine which MMCE loaded from its JSON configuration. */
    public static DynamicMachine getRegisteredMachine() {
        return MachineRegistry.getRegistry().getMachine(MACHINE_ID);
    }

    /** Converts only known legacy blood-altar thread names to stable IDs. */
    public static String canonicalThreadName(final String threadName) {
        if (LEGACY_THREAD_PURIFICATION.equals(threadName)
                || "gctcore.gui.blood_altar.thread.purification".equals(threadName)) {
            return THREAD_PURIFICATION;
        }
        if (LEGACY_THREAD_ORB.equals(threadName)
                || "gctcore.gui.blood_altar.thread.orb".equals(threadName)) {
            return THREAD_ORB;
        }
        if (LEGACY_THREAD_CRAFTING.equals(threadName)
                || "gctcore.gui.blood_altar.thread.crafting".equals(threadName)) {
            return THREAD_CRAFTING;
        }
        return threadName == null ? "" : threadName;
    }

    /**
     * Applies the non-structural settings previously supplied by the script.
     * This is called after MMCE has parsed the JSON, so structural changes in
     * the JSON remain wholly under pack control.
     */
    public static void configure(final DynamicMachine machine) {
        if (!isBloodAltar(machine)) {
            return;
        }

        machine.setMaxThreads(1);
        machine.setInternalParallelism(Integer.MAX_VALUE);
        machine.setMaxParallelism(Integer.MAX_VALUE);

        machine.getCoreThreadPreset().clear();
        machine.addCoreThread(FactoryRecipeThread
                .createCoreThread(THREAD_PURIFICATION)
                .addRecipe("purify"));
        machine.addCoreThread(FactoryRecipeThread
                .createCoreThread(THREAD_ORB));
        machine.addCoreThread(FactoryRecipeThread
                .createCoreThread(THREAD_CRAFTING));

        final List<?> handlers = machine.getMachineEventHandlers(MachineStructureUpdateEvent.class);
        if (handlers == null || !handlers.contains(STRUCTURE_UPDATE_HANDLER)) {
            machine.addMachineEventHandler(MachineStructureUpdateEvent.class, STRUCTURE_UPDATE_HANDLER);
        }

        final List<?> tickHandlers = machine.getMachineEventHandlers(MachineTickEvent.class);
        if (tickHandlers == null || !tickHandlers.contains(PRE_TICK_HANDLER)) {
            machine.addMachineEventHandler(MachineTickEvent.class, PRE_TICK_HANDLER);
        }
    }

    /**
     * MMCE's native structure-update event is used to cache the values needed
     * by the GUI.  No CraftTweaker handler is involved.
     */
    private static void onStructureUpdated(final MachineStructureUpdateEvent event) {
        final TileMultiblockMachineController controller = event.getController();
        if (!(controller instanceof BloodAltarFactoryController)
                || controller.getWorld() == null || controller.getWorld().isRemote) {
            return;
        }

        final NBTTagCompound data = copyData(controller);
        final int runeCount = countAllRunes(controller);
        final int level = getAltarLevel(controller, runeCount);
        final int threadRunes = controller.getBlocksInPattern(RUNE_THREAD);
        final int speedRunes = controller.getBlocksInPattern(RUNE_SPEED);
        final int efficiencyRunes = controller.getBlocksInPattern(RUNE_EFFICIENCY);

        controller.setExtraThreadCount(Math.min(MAX_EXTRA_THREADS, Math.max(0, threadRunes)));
        data.setInteger(DATA_LEVEL, level);
        data.setString(DATA_CAPACITY, calculateCapacity(controller).toPlainString());
        data.setInteger(DATA_SPEED, calculateSpeed(speedRunes, efficiencyRunes));
        if (!data.hasKey(DATA_LIFE_ESSENCE)) {
            data.setString(DATA_LIFE_ESSENCE, "0");
        }
        if (!data.hasKey(DATA_MODE)) {
            data.setInteger(DATA_MODE, 0);
        }

        controller.setCustomDataTag(data);
        controller.markForUpdateSync();
    }

    /**
     * Java equivalent of the former {@code MMEvents.onMachinePreTick}
     * handler.  The script allowed mode 1 to remove more LP than the
     * controller held; this implementation caps every export at that balance.
     */
    private static void onMachinePreTick(final MachineTickEvent event) {
        if (event.phase != Phase.START) {
            return;
        }

        final TileMultiblockMachineController controller = event.getController();
        if (!(controller instanceof BloodAltarFactoryController)
                || controller.getWorld() == null || controller.getWorld().isRemote) {
            return;
        }

        final NBTTagCompound data = copyData(controller);
        boolean dataChanged = false;
        if (!data.hasKey(DATA_LIFE_ESSENCE)) {
            data.setString(DATA_LIFE_ESSENCE, "0");
            dataChanged = true;
        }

        BigInteger controllerLifeEssence = getBigInteger(data, DATA_LIFE_ESSENCE).max(BigInteger.ZERO);
        if (!controllerLifeEssence.toString().equals(data.getString(DATA_LIFE_ESSENCE))) {
            data.setString(DATA_LIFE_ESSENCE, controllerLifeEssence.toString());
            dataChanged = true;
        }

        final int accelerationRunes = controller.getBlocksInPattern(RUNE_ACCELERATION);
        final int interval = Math.max(1, 20 - accelerationRunes);
        if (controller.getWorld().getWorldTime() % interval != 0) {
            syncData(controller, data, dataChanged);
            return;
        }

        final TileEntity tile = controller.getWorld().getTileEntity(controller.getPos().down(4));
        if (!(tile instanceof TileAltar)) {
            syncData(controller, data, dataChanged);
            return;
        }

        final TileAltar altar = (TileAltar) tile;
        final int altarBlood = Math.max(0, altar.getCurrentBlood());
        final int mode = data.getInteger(DATA_MODE);

        if (mode == 0) {
            final BigInteger capacity = getBigInteger(data, DATA_CAPACITY).max(BigInteger.ZERO);
            final BigInteger availableSpace = capacity.subtract(controllerLifeEssence).max(BigInteger.ZERO);
            final BigInteger transferred = BigInteger.valueOf(altarBlood).min(availableSpace);
            if (transferred.signum() > 0 && setAltarBlood(altar, altarBlood - transferred.intValue())) {
                controllerLifeEssence = controllerLifeEssence.add(transferred);
                data.setString(DATA_LIFE_ESSENCE, controllerLifeEssence.toString());
                dataChanged = true;
            }
        } else if (mode == 1) {
            final long altarFreeSpace = Math.max(0L, (long) altar.getCapacity() - altarBlood);
            final BigInteger transferLimit = calculateTransferLimit(
                    controller.getBlocksInPattern(RUNE_DISLOCATION));
            // Include controllerLifeEssence here. The CraftTweaker script
            // omitted it, which made controller LP negative after export.
            final BigInteger transferred = controllerLifeEssence
                    .min(BigInteger.valueOf(altarFreeSpace))
                    .min(transferLimit);
            if (transferred.signum() > 0 && setAltarBlood(altar, altarBlood + transferred.intValue())) {
                controllerLifeEssence = controllerLifeEssence.subtract(transferred);
                data.setString(DATA_LIFE_ESSENCE, controllerLifeEssence.toString());
                dataChanged = true;
            }
        }

        syncData(controller, data, dataChanged);
    }

    private static BigInteger calculateTransferLimit(final int dislocationRunes) {
        return new BigDecimal("1.2").pow(Math.max(0, dislocationRunes))
                .multiply(BigDecimal.valueOf(20L))
                .setScale(0, RoundingMode.DOWN)
                .toBigInteger();
    }

    /** Writes the Blood Magic main tank, rather than its separate fluid I/O tanks. */
    private static boolean setAltarBlood(final TileAltar altar, final int amount) {
        final BloodAltar bloodAltar = ((TileAltarAccessor) (Object) altar).gctcore$getBloodAltar();
        final FluidStack currentFluid = bloodAltar == null ? null : bloodAltar.getFluid();
        if (currentFluid == null) {
            return false;
        }

        bloodAltar.setMainFluid(new FluidStack(currentFluid, amount));
        altar.markDirty();
        final IBlockState state = altar.getWorld().getBlockState(altar.getPos());
        altar.getWorld().notifyBlockUpdate(altar.getPos(), state, state, 3);
        return true;
    }

    private static void syncData(final TileMultiblockMachineController controller,
                                 final NBTTagCompound data, final boolean changed) {
        if (!changed) {
            return;
        }
        controller.setCustomDataTag(data);
        controller.markForUpdateSync();
    }

    /** Shared by the Java-defined factory recipes. */
    static BigInteger getStoredLifeEssence(final TileMultiblockMachineController controller) {
        return getBigInteger(controller.getCustomDataTag(), DATA_LIFE_ESSENCE).max(BigInteger.ZERO);
    }

    /** Shared by the Java-defined factory recipes. */
    static BigInteger getStoredCapacity(final TileMultiblockMachineController controller) {
        return getBigInteger(controller.getCustomDataTag(), DATA_CAPACITY).max(BigInteger.ZERO);
    }

    /** Shared by the Java-defined factory recipes. */
    static void setStoredLifeEssence(final TileMultiblockMachineController controller,
                                     final BigInteger amount) {
        final NBTTagCompound data = copyData(controller);
        data.setString(DATA_LIFE_ESSENCE, amount.max(BigInteger.ZERO).toString());
        controller.setCustomDataTag(data);
        controller.markForUpdateSync();
    }

    private static NBTTagCompound copyData(final TileMultiblockMachineController controller) {
        final NBTTagCompound current = controller.getCustomDataTag();
        return current == null ? new NBTTagCompound() : current.copy();
    }

    private static BigInteger getBigInteger(final NBTTagCompound data, final String key) {
        if (data == null || !data.hasKey(key)) {
            return BigInteger.ZERO;
        }
        try {
            return new BigInteger(data.getString(key));
        } catch (NumberFormatException ignored) {
            return BigInteger.ZERO;
        }
    }

    private static int getAltarLevel(final TileMultiblockMachineController controller, final int runeCount) {
        final TileEntity tile = controller.getWorld().getTileEntity(controller.getPos().down(4));
        if (!(tile instanceof TileAltar)) {
            return 0;
        }

        final int baseLevel = ((TileAltar) tile).getTier().toInt();
        if (baseLevel <= 5) {
            return baseLevel;
        }
        if (!hasBlockAtAllCorners(controller, 14, 0, "additions:crimsonite_block@0") || runeCount < 284) {
            return 6;
        }
        if (!hasBlockAtAllCorners(controller, 18, 1, "additions:darkest_stonebrick_large@0") || runeCount < 416) {
            return 7;
        }
        return hasBlockAtAllCorners(controller, 22, 2, "additions:murderite_block@0") && runeCount >= 580 ? 9 : 8;
    }

    /** Checks the four rotation-aware tier-marker positions. */
    private static boolean hasBlockAtAllCorners(final TileMultiblockMachineController controller,
                                                final int radius, final int y, final String descriptor) {
        final IBlockState expected = descriptorState(descriptor);
        if (expected == null) {
            return false;
        }
        final int[] signs = {-1, 1};
        for (int xSign : signs) {
            for (int zSign : signs) {
                BlockPos offset = new BlockPos(xSign * radius, y, zSign * radius);
                EnumFacing facing = controller.getControllerRotation();
                if (facing != null) {
                    offset = MiscUtils.rotateYCCWNorthUntil(offset, facing);
                }
                if (!expected.equals(controller.getWorld().getBlockState(controller.getPos().add(offset)))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static IBlockState descriptorState(final String descriptor) {
        final List<IBlockState> states =
                hellfirepvp.modularmachinery.common.util.BlockArray.BlockInformation
                        .getDescriptor(descriptor).getApplicable();
        return states.isEmpty() ? null : states.get(0);
    }

    private static int countAllRunes(final TileMultiblockMachineController controller) {
        return controller.getBlocksInPattern("bloodmagic:blood_rune@0")
                + controller.getBlocksInPattern(RUNE_SPEED)
                + controller.getBlocksInPattern(RUNE_EFFICIENCY)
                + controller.getBlocksInPattern(RUNE_SACRIFICE)
                + controller.getBlocksInPattern(RUNE_SELF_SACRIFICE)
                + controller.getBlocksInPattern(RUNE_DISLOCATION)
                + controller.getBlocksInPattern(RUNE_AUGMENTED_CAPACITY)
                + controller.getBlocksInPattern(RUNE_CAPACITY)
                + controller.getBlocksInPattern(RUNE_ORB)
                + controller.getBlocksInPattern(RUNE_ACCELERATION)
                + controller.getBlocksInPattern(RUNE_ECONOMY)
                + controller.getBlocksInPattern(RUNE_PURIFICATION)
                + controller.getBlocksInPattern(RUNE_THREAD)
                + controller.getBlocksInPattern(RUNE_PERSONAL);
    }

    private static BigDecimal calculateCapacity(final TileMultiblockMachineController controller) {
        final int capacityRunes = controller.getBlocksInPattern(RUNE_CAPACITY);
        final int augmentedCapacityRunes = controller.getBlocksInPattern(RUNE_AUGMENTED_CAPACITY);
        return new BigDecimal("10000")
                .multiply(new BigDecimal("1.1").pow(capacityRunes))
                .add(new BigDecimal("2000").multiply(BigDecimal.valueOf(augmentedCapacityRunes)))
                .setScale(0, RoundingMode.DOWN);
    }

    private static int calculateSpeed(final int speedRunes, final int efficiencyRunes) {
        final double value = 100.0D * (1.0D + 0.2D * speedRunes) * Math.pow(1.05D, efficiencyRunes);
        return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE : Math.max(0, (int) value);
    }
}
