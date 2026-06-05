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

    public static void start(EntityPlayerMP player, BlockPos pos, boolean useAeItems, boolean useAeFluids, boolean craftMissing, int dynamicLength, int tickInterval, int operationsPerTick) {
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
        BlockArray machinePattern = new BlockArray(BlockArrayCache.getBlockArrayCache(machine.getPattern(), controllerFacing));
        MMCEBuilderUtils.appendDynamicPatterns(machine, machinePattern, controllerFacing, dynamicLength);

        StructureIngredient ingredient = StructureIngredient.of(world, pos, machinePattern);
        if (player.isCreative()) {
            new ConfigurableMachineAssembly(world, pos, player, ingredient, false, false, false, 1, Integer.MAX_VALUE).assemblyCreative();
            return;
        }

        MMCE_BuilderTaskManager.addTask(new ConfigurableMachineAssembly(world, pos, player, ingredient, useAeItems, useAeFluids, craftMissing, tickInterval, operationsPerTick));
        MMCEBuilderUtils.sendTranslation(player, "message.gctcore.mmce_builder.started");
    }
}
