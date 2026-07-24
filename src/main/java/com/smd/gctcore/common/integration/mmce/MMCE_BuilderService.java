package com.smd.gctcore.common.integration.mmce;

import com.smd.gctcore.common.util.MMCEBuilderUtils;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.block.BlockFactoryController;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import hellfirepvp.modularmachinery.common.util.BlockArrayCache;
import ink.ikx.mmce.common.utils.StructureIngredient;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class MMCE_BuilderService {

    private MMCE_BuilderService() {
    }

    public static void start(EntityPlayerMP player, BlockPos pos, boolean useAeItems, boolean useAeFluids,
                             boolean craftMissing, boolean disassembleMode, int dynamicLength,
                             int tickInterval, int operationsPerTick) {
        start(player, pos, useAeItems, useAeFluids, craftMissing, disassembleMode, dynamicLength,
                "", tickInterval, operationsPerTick);
    }

    public static void start(EntityPlayerMP player, BlockPos pos, boolean useAeItems, boolean useAeFluids,
                             boolean craftMissing, boolean disassembleMode, int dynamicLength,
                             String attachmentModule, int tickInterval, int operationsPerTick) {
        World world = player.world;
        TileEntity tile = world.getTileEntity(pos);
        Block block = world.getBlockState(pos).getBlock();
        if (!(tile instanceof TileMultiblockMachineController)) {
            MMCEBuilderUtils.sendTranslation(player, "message.gctcore.mmce_builder.no_controller");
            return;
        }

        DynamicMachine machine = ((TileMultiblockMachineController) tile).getBlueprintMachine();
        if (machine == null && block instanceof BlockController) {
            machine = ((BlockController) block).getParentMachine();
        }
        if (machine == null && block instanceof BlockFactoryController) {
            machine = ((BlockFactoryController) block).getParentMachine();
        }
        if (machine == null) {
            MMCEBuilderUtils.sendTranslation(player, "message.gctcore.mmce_builder.no_machine");
            return;
        }

        if (MMCE_BuilderTaskManager.hasTask(world, pos)) {
            MMCEBuilderUtils.sendTranslation(player, "message.gctcore.mmce_builder.already_running");
            return;
        }

        EnumFacing controllerFacing = world.getBlockState(pos).getValue(BlockController.FACING);
        BlockArray selectedPattern = MMCE_AttachmentModuleCompat.findPattern(machine, attachmentModule);
        boolean buildingAttachment = selectedPattern != null;
        BlockArray machinePattern;
        if (buildingAttachment) {
            // Attachment effective patterns are created lazily at runtime and are
            // therefore absent from MMCE's startup-built BlockArrayCache.
            machinePattern = rotateToFacing(selectedPattern, controllerFacing);
        } else {
            selectedPattern = machine.getPattern();
            machinePattern = new BlockArray(BlockArrayCache.getBlockArrayCache(selectedPattern, controllerFacing));
            MMCEBuilderUtils.appendDynamicPatterns(machine, machinePattern, controllerFacing, dynamicLength);
        }

        if (disassembleMode) {
            DisassemblyIngredient.Plan plan = MMCEBuilderUtils.createDisassemblyPlan(machinePattern);
            MMCE_BuilderTaskManager.addTask(new ConfigurableMachineDisassembly(world, pos, player, plan, useAeItems, useAeFluids, tickInterval, operationsPerTick));
            MMCEBuilderUtils.sendTranslation(player, "message.gctcore.mmce_builder.disassembly_started");
            return;
        }

        StructureIngredient ingredient = StructureIngredient.of(world, pos, machinePattern);
        if (player.isCreative()) {
            new ConfigurableMachineAssembly(world, pos, player, ingredient, false, false, false, 1, Integer.MAX_VALUE).assemblyCreative();
            return;
        }

        ConfigurableMachineAssembly assembly = new ConfigurableMachineAssembly(world, pos, player, ingredient, useAeItems, useAeFluids, craftMissing, tickInterval, operationsPerTick);
        MMCE_BuilderTaskManager.addTask(assembly);
        assembly.openCraftingGuiIfNeeded();
        MMCEBuilderUtils.sendTranslation(player, "message.gctcore.mmce_builder.started");
    }

    private static BlockArray rotateToFacing(BlockArray pattern, EnumFacing facing) {
        BlockArray rotated = pattern;
        EnumFacing current = EnumFacing.NORTH;
        while (current != facing) {
            current = current.rotateYCCW();
            rotated = rotated.rotateYCCW();
        }
        rotated.flushTileBlocksCache();
        return new BlockArray(rotated);
    }
}
