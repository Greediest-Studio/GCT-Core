package com.smd.gctcore.common.integration.mmce;

import com.smd.gctcore.common.util.MMCEBuilderUtils;
import com.smd.gctcore.common.util.MessageLimiter;
import com.smd.gctcore.misc.Mods;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.FluidStack;

import java.util.Iterator;
import java.util.List;

public class ConfigurableMachineDisassembly implements MMCE_BuilderTask {

    private static final int MAX_REPORTS = 8;

    private final World world;
    private final BlockPos ctrlPos;
    private final EntityPlayer player;
    private final DisassemblyIngredient.Plan plan;
    private final boolean useAeItems;
    private final boolean useAeFluids;
    private final int tickInterval;
    private final int operationsPerTick;
    private final MessageLimiter reportLimiter = new MessageLimiter(MAX_REPORTS);

    public ConfigurableMachineDisassembly(World world, BlockPos ctrlPos, EntityPlayer player, DisassemblyIngredient.Plan plan, boolean useAeItems, boolean useAeFluids, int tickInterval, int operationsPerTick) {
        this.world = world;
        this.ctrlPos = ctrlPos;
        this.player = player;
        this.plan = plan;
        this.useAeItems = useAeItems;
        this.useAeFluids = useAeFluids;
        this.tickInterval = tickInterval;
        this.operationsPerTick = operationsPerTick;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public BlockPos getCtrlPos() {
        return ctrlPos;
    }

    @Override
    public EntityPlayer getPlayer() {
        return player;
    }

    @Override
    public int getTickInterval() {
        return tickInterval;
    }

    @Override
    public int getOperationsPerTick() {
        return operationsPerTick;
    }

    @Override
    public boolean isControllerInvalid() {
        TileEntity te = world.getTileEntity(ctrlPos);
        return !(te instanceof TileMultiblockMachineController);
    }

    @Override
    public boolean isCompleted() {
        return plan.itemEntries().isEmpty() && plan.fluidEntries().isEmpty();
    }

    @Override
    public void tick() {
        List<DisassemblyIngredient.ItemEntry> itemIngredient = plan.itemEntries();
        List<DisassemblyIngredient.FluidEntry> fluidIngredient = plan.fluidEntries();
        if (!itemIngredient.isEmpty()) {
            disassembleItemBlock(itemIngredient);
        } else if (!fluidIngredient.isEmpty()) {
            disassembleFluidBlock(fluidIngredient);
        }
    }

    @Override
    public void report() {
    }

    @Override
    public String getCancelledMessageKey() {
        return "message.gctcore.mmce_builder.disassembly_cancelled";
    }

    @Override
    public String getSuccessMessageKey() {
        return "message.gctcore.mmce_builder.disassembly_success";
    }

    private void disassembleItemBlock(List<DisassemblyIngredient.ItemEntry> itemIngredient) {
        Iterator<DisassemblyIngredient.ItemEntry> iterator = itemIngredient.iterator();
        DisassemblyIngredient.ItemEntry ingredient = iterator.next();
        BlockPos realPos = ctrlPos.add(ingredient.pos());
        if (realPos.equals(ctrlPos)) {
            iterator.remove();
            return;
        }

        Tuple<ItemStack, IBlockState> matched = MMCEBuilderUtils.findMatchingItemCandidate(world, realPos, ingredient.blockInformation(), ingredient.candidates());
        if (matched == null) {
            iterator.remove();
            return;
        }

        ItemStack recovered = matched.getFirst().copy();
        if (recovered.isEmpty()) {
            iterator.remove();
            return;
        }
        if (useAeItems) {
            if (!Mods.AE2.isLoading() || !Ae2AssemblyExtractor.canInsertItem(player, recovered)) {
                reportLimited("message.gctcore.mmce_builder.ae_insert_failed");
                return;
            }
        }
        if (!breakBlock(realPos)) {
            return;
        }

        if (useAeItems) {
            ItemStack leftover = Ae2AssemblyExtractor.insertItem(player, recovered);
            if (!leftover.isEmpty()) {
                giveOrDrop(leftover);
            }
        } else {
            giveOrDrop(recovered);
        }
        world.playSound(null, realPos, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
        iterator.remove();
    }

    private void disassembleFluidBlock(List<DisassemblyIngredient.FluidEntry> fluidIngredient) {
        Iterator<DisassemblyIngredient.FluidEntry> iterator = fluidIngredient.iterator();
        DisassemblyIngredient.FluidEntry ingredient = iterator.next();
        BlockPos realPos = ctrlPos.add(ingredient.pos());
        if (realPos.equals(ctrlPos)) {
            iterator.remove();
            return;
        }

        Tuple<FluidStack, IBlockState> matched = MMCEBuilderUtils.findMatchingFluidCandidate(world, realPos, ingredient.blockInformation(), ingredient.candidates());
        if (matched == null) {
            iterator.remove();
            return;
        }

        FluidStack recovered = matched.getFirst().copy();
        if (useAeFluids) {
            if (!Mods.AE2.isLoading() || !Ae2AssemblyExtractor.canInsertFluid(player, recovered)) {
                reportLimited("message.gctcore.mmce_builder.ae_insert_failed");
                return;
            }
        }
        if (!breakBlock(realPos)) {
            return;
        }

        if (useAeFluids) {
            Ae2AssemblyExtractor.insertFluid(player, recovered);
        }
        world.playSound(null, realPos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
        iterator.remove();
    }

    private boolean breakBlock(BlockPos realPos) {
        IBlockState current = world.getBlockState(realPos);
        if (current.getBlock() == Blocks.AIR) {
            return true;
        }
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, realPos, current, player);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            reportLimited("message.gctcore.mmce_builder.break_cancelled");
            return false;
        }
        return world.setBlockToAir(realPos);
    }

    private void giveOrDrop(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack remaining = stack.copy();
        if (player.inventory.addItemStackToInventory(remaining) || remaining.isEmpty()) {
            return;
        }
        EntityItem entityItem = new EntityItem(world, player.posX, player.posY, player.posZ, remaining.copy());
        entityItem.setNoPickupDelay();
        world.spawnEntity(entityItem);
    }

    private void reportLimited(String key) {
        if (reportLimiter.tryAcquire(player, "message.gctcore.mmce_builder.disassembly_suppressed")) {
            MMCEBuilderUtils.sendTranslation(player, key);
        }
    }
}
