package com.smd.gctcore.common.integration.mmce;

import com.smd.gctcore.common.util.MMCEBuilderUtils;
import com.smd.gctcore.common.util.MessageLimiter;
import com.smd.gctcore.misc.Mods;
import appeng.api.networking.crafting.ICraftingLink;
import hellfirepvp.modularmachinery.ModularMachinery;
import ink.ikx.mmce.common.assembly.MachineAssembly;
import ink.ikx.mmce.common.utils.StructureIngredient;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConfigurableMachineAssembly extends MachineAssembly {

    private static final int MAX_MISSING_REPORTS = 8;

    private final boolean useAeItems;
    private final boolean useAeFluids;
    private final boolean craftMissing;
    private final int tickInterval;
    private final int operationsPerTick;
    private final MessageLimiter blockedReportLimiter = new MessageLimiter(MAX_MISSING_REPORTS);
    private final List<MissingItemEntry> missingItems = new ArrayList<>();
    private final List<MissingFluidEntry> missingFluids = new ArrayList<>();
    private final List<RequestedItemCraft> craftRequestedItems = new ArrayList<>();
    private final List<RequestedFluidCraft> craftRequestedFluids = new ArrayList<>();

    public ConfigurableMachineAssembly(World world, BlockPos ctrlPos, EntityPlayer player, StructureIngredient ingredient, boolean useAeItems, boolean useAeFluids, boolean craftMissing, int tickInterval, int operationsPerTick) {
        super(world, ctrlPos, player, ingredient);
        this.useAeItems = useAeItems;
        this.useAeFluids = useAeFluids;
        this.craftMissing = craftMissing;
        this.tickInterval = tickInterval;
        this.operationsPerTick = operationsPerTick;
    }

    public int getTickInterval() {
        return tickInterval;
    }

    public int getOperationsPerTick() {
        return operationsPerTick;
    }

    @Override
    public void assembly(boolean consumeInventory) {
        List<StructureIngredient.ItemIngredient> itemIngredient = getIngredient().itemIngredient();
        List<StructureIngredient.FluidIngredient> fluidIngredient = getIngredient().fluidIngredient();
        if (!itemIngredient.isEmpty()) {
            assemblyItemBlocks(itemIngredient);
        } else if (!fluidIngredient.isEmpty()) {
            assemblyFluidBlocks(fluidIngredient);
        }
    }

    private void assemblyItemBlocks(List<StructureIngredient.ItemIngredient> itemIngredient) {
        Iterator<StructureIngredient.ItemIngredient> iterator = itemIngredient.iterator();
        StructureIngredient.ItemIngredient ingredient = iterator.next();
        BlockPos realPos = getCtrlPos().add(ingredient.pos());
        if (!replaceCheck(realPos)) {
            iterator.remove();
            return;
        }

        Tuple<ItemStack, IBlockState> consumed = consumeFirstAvailableItem(ingredient.ingredientList());
        if (consumed == null) {
            if (shouldWaitForItemCraft(ingredient.ingredientList().get(0).getFirst())) {
                return;
            }
            addMissingItem(ingredient.ingredientList().get(0).getFirst());
            iterator.remove();
            return;
        }
        ItemStack required = consumed.getFirst().copy();
        IBlockState state = consumed.getSecond();

        if (!placeAssemblyBlock(realPos, state)) {
            getPlayer().inventory.addItemStackToInventory(required);
        } else {
            clearRequestedItemCraft(required);
            getWorld().playSound(null, realPos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            applyTileNbt(realPos, state, ingredient);
        }
        iterator.remove();
    }

    private void assemblyFluidBlocks(List<StructureIngredient.FluidIngredient> fluidIngredient) {
        Iterator<StructureIngredient.FluidIngredient> iterator = fluidIngredient.iterator();
        StructureIngredient.FluidIngredient ingredient = iterator.next();
        BlockPos realPos = getCtrlPos().add(ingredient.pos());
        if (!replaceCheck(realPos)) {
            iterator.remove();
            return;
        }

        Tuple<FluidStack, IBlockState> consumed = consumeFirstAvailableFluid(ingredient.ingredientList());
        if (consumed == null) {
            if (shouldWaitForFluidCraft(ingredient.ingredientList().get(0).getFirst())) {
                return;
            }
            addMissingFluid(ingredient.ingredientList().get(0).getFirst());
            iterator.remove();
            return;
        }
        FluidStack required = consumed.getFirst().copy();
        IBlockState state = consumed.getSecond();

        if (placeAssemblyBlock(realPos, state)) {
            clearRequestedFluidCraft(required);
            getWorld().playSound(null, realPos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        iterator.remove();
    }

    private boolean placeAssemblyBlock(BlockPos realPos, IBlockState state) {
        IBlockState original = getWorld().getBlockState(realPos);
        getWorld().setBlockState(realPos, state);
        BlockEvent.PlaceEvent event = new BlockEvent.PlaceEvent(new BlockSnapshot(getWorld(), realPos, state), original, getPlayer(), EnumHand.MAIN_HAND);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            getWorld().setBlockState(realPos, original);
            return false;
        }
        return true;
    }

    private Tuple<ItemStack, IBlockState> consumeFirstAvailableItem(List<Tuple<ItemStack, IBlockState>> candidates) {
        for (Tuple<ItemStack, IBlockState> tuple : candidates) {
            ItemStack required = tuple.getFirst().copy();
            if (consumeItem(required)) {
                return new Tuple<>(required, tuple.getSecond());
            }
        }
        return null;
    }

    private Tuple<FluidStack, IBlockState> consumeFirstAvailableFluid(List<Tuple<FluidStack, IBlockState>> candidates) {
        for (Tuple<FluidStack, IBlockState> tuple : candidates) {
            FluidStack required = tuple.getFirst().copy();
            if (consumeFluid(required)) {
                return new Tuple<>(required, tuple.getSecond());
            }
        }
        return null;
    }

    private boolean consumeItem(ItemStack required) {
        if (MachineAssembly.consumeInventoryItem(required.copy(), getPlayer().inventory.mainInventory)) {
            return true;
        }
        return useAeItems && Mods.AE2.isLoading() && Ae2AssemblyExtractor.extractItem(getPlayer(), required);
    }

    private boolean consumeFluid(FluidStack required) {
        if (MachineAssembly.consumeInventoryFluid(required.copy(), MMCEBuilderUtils.getFluidHandlerItems(getPlayer().inventory.mainInventory))) {
            return true;
        }
        return useAeFluids && Mods.AE2.isLoading() && Ae2AssemblyExtractor.extractFluid(getPlayer(), required);
    }

    private boolean shouldWaitForItemCraft(ItemStack required) {
        if (!useAeItems || !craftMissing || !Mods.AE2.isLoading()) {
            return false;
        }
        CraftRequestState state = getItemCraftState(required);
        if (state == CraftRequestState.WAITING || state == CraftRequestState.FINISHED) {
            return true;
        }
        if (state == CraftRequestState.CANCELED) {
            return false;
        }
        ICraftingLink link = Ae2AssemblyExtractor.requestItemCraft(getPlayer(), required);
        if (link != null) {
            craftRequestedItems.add(new RequestedItemCraft(required, link));
            return true;
        }
        return false;
    }

    private boolean shouldWaitForFluidCraft(FluidStack required) {
        if (!useAeFluids || !craftMissing || !Mods.AE2.isLoading()) {
            return false;
        }
        CraftRequestState state = getFluidCraftState(required);
        if (state == CraftRequestState.WAITING || state == CraftRequestState.FINISHED) {
            return true;
        }
        if (state == CraftRequestState.CANCELED) {
            return false;
        }
        ICraftingLink link = Ae2AssemblyExtractor.requestFluidCraft(getPlayer(), required);
        if (link != null) {
            craftRequestedFluids.add(new RequestedFluidCraft(required, link));
            return true;
        }
        return false;
    }

    private void applyTileNbt(BlockPos realPos, IBlockState state, StructureIngredient.ItemIngredient ingredient) {
        TileEntity te = getWorld().getTileEntity(realPos);
        if (te != null && ingredient.nbt() != null) {
            try {
                te.readFromNBT(ingredient.nbt());
            } catch (Exception e) {
                ModularMachinery.log.warn("Failed to apply NBT to TileEntity!", e);
                getWorld().removeTileEntity(realPos);
                getWorld().setTileEntity(realPos, state.getBlock().createTileEntity(getWorld(), state));
            }
        }
    }

    public void reportMissingMaterials() {
        if (missingItems.isEmpty() && missingFluids.isEmpty()) {
            return;
        }
        MMCEBuilderUtils.sendTranslation(getPlayer(), "message.gctcore.mmce_builder.missing_summary_header");
        for (MissingItemEntry entry : missingItems) {
            MMCEBuilderUtils.sendTranslation(getPlayer(), "message.gctcore.mmce_builder.missing_item_entry",
                    entry.amount, entry.stack.getDisplayName());
        }
        for (MissingFluidEntry entry : missingFluids) {
            MMCEBuilderUtils.sendTranslation(getPlayer(), "message.gctcore.mmce_builder.missing_fluid_entry",
                    entry.amount, entry.fluid.getLocalizedName());
        }
    }

    private void addMissingItem(ItemStack required) {
        if (required.isEmpty()) {
            return;
        }
        for (MissingItemEntry entry : missingItems) {
            if (ItemStack.areItemsEqual(entry.stack, required) && ItemStack.areItemStackTagsEqual(entry.stack, required)) {
                entry.amount += required.getCount();
                return;
            }
        }
        missingItems.add(new MissingItemEntry(required));
    }

    private void addMissingFluid(FluidStack required) {
        if (required == null || required.amount <= 0) {
            return;
        }
        for (MissingFluidEntry entry : missingFluids) {
            if (entry.fluid.isFluidEqual(required)) {
                entry.amount += required.amount;
                return;
            }
        }
        missingFluids.add(new MissingFluidEntry(required));
    }

    private CraftRequestState getItemCraftState(ItemStack required) {
        Iterator<RequestedItemCraft> iterator = craftRequestedItems.iterator();
        while (iterator.hasNext()) {
            RequestedItemCraft entry = iterator.next();
            if (ItemStack.areItemsEqual(entry.stack, required) && ItemStack.areItemStackTagsEqual(entry.stack, required)) {
                if (entry.link.isCanceled()) {
                    iterator.remove();
                    return CraftRequestState.CANCELED;
                }
                if (entry.link.isDone()) {
                    iterator.remove();
                    return CraftRequestState.FINISHED;
                }
                return CraftRequestState.WAITING;
            }
        }
        return CraftRequestState.NONE;
    }

    private CraftRequestState getFluidCraftState(FluidStack required) {
        if (required == null) {
            return CraftRequestState.NONE;
        }
        Iterator<RequestedFluidCraft> iterator = craftRequestedFluids.iterator();
        while (iterator.hasNext()) {
            RequestedFluidCraft entry = iterator.next();
            if (entry.fluid.isFluidEqual(required)) {
                if (entry.link.isCanceled()) {
                    iterator.remove();
                    return CraftRequestState.CANCELED;
                }
                if (entry.link.isDone()) {
                    iterator.remove();
                    return CraftRequestState.FINISHED;
                }
                return CraftRequestState.WAITING;
            }
        }
        return CraftRequestState.NONE;
    }

    private void clearRequestedItemCraft(ItemStack required) {
        Iterator<RequestedItemCraft> iterator = craftRequestedItems.iterator();
        while (iterator.hasNext()) {
            RequestedItemCraft entry = iterator.next();
            if (ItemStack.areItemsEqual(entry.stack, required) && ItemStack.areItemStackTagsEqual(entry.stack, required)) {
                iterator.remove();
                return;
            }
        }
    }

    private void clearRequestedFluidCraft(FluidStack required) {
        if (required == null) {
            return;
        }
        Iterator<RequestedFluidCraft> iterator = craftRequestedFluids.iterator();
        while (iterator.hasNext()) {
            RequestedFluidCraft entry = iterator.next();
            if (entry.fluid.isFluidEqual(required)) {
                iterator.remove();
                return;
            }
        }
    }

    private boolean replaceCheck(BlockPos realPos) {
        if (getWorld().isOutsideBuildHeight(realPos)) {
            reportBlocked(realPos, "message.gctcore.mmce_builder.too_high");
            return false;
        }

        if (MMCEBuilderUtils.isReplaceableForAssembly(getWorld(), realPos)) {
            return true;
        }

        reportBlocked(realPos, "message.gctcore.mmce_builder.cannot_replace");
        return false;
    }

    private void reportBlocked(BlockPos pos, String key) {
        if (blockedReportLimiter.tryAcquire(getPlayer(), "message.gctcore.mmce_builder.blocked_suppressed")) {
            MMCEBuilderUtils.sendTranslation(getPlayer(), key, MMCEBuilderUtils.posToString(pos));
        }
    }

    private static final class MissingItemEntry {
        private final ItemStack stack;
        private int amount;

        private MissingItemEntry(ItemStack stack) {
            this.stack = stack.copy();
            this.stack.setCount(1);
            this.amount = stack.getCount();
        }
    }

    private static final class MissingFluidEntry {
        private final FluidStack fluid;
        private int amount;

        private MissingFluidEntry(FluidStack fluid) {
            this.fluid = fluid.copy();
            this.amount = fluid.amount;
        }
    }

    private static final class RequestedItemCraft {
        private final ItemStack stack;
        private final ICraftingLink link;

        private RequestedItemCraft(ItemStack stack, ICraftingLink link) {
            this.stack = stack.copy();
            this.stack.setCount(1);
            this.link = link;
        }
    }

    private static final class RequestedFluidCraft {
        private final FluidStack fluid;
        private final ICraftingLink link;

        private RequestedFluidCraft(FluidStack fluid, ICraftingLink link) {
            this.fluid = fluid.copy();
            this.link = link;
        }
    }

    private enum CraftRequestState {
        NONE,
        WAITING,
        FINISHED,
        CANCELED
    }
}
