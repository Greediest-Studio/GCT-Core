package com.smd.gctcore.common.integration.mmce;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.implementations.ContainerCraftConfirm;
import com.google.common.collect.ImmutableSet;
import com.smd.gctcore.common.util.MMCEBuilderUtils;
import com.smd.gctcore.common.util.MessageLimiter;
import com.smd.gctcore.misc.Mods;
import hellfirepvp.modularmachinery.ModularMachinery;
import ink.ikx.mmce.common.assembly.MachineAssembly;
import ink.ikx.mmce.common.utils.StructureIngredient;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConfigurableMachineAssembly extends MachineAssembly implements MMCE_BuilderTask, MMCE_CraftingRequester {

    private static final int MAX_MISSING_REPORTS = 8;
    private static final int CRAFTING_BUILD_INTERVAL_TICKS = 10;
    private static final int FINISHED_CRAFT_GRACE_TICKS = 20;
    private static final ResourceLocation MMCE_BLOCK_CASING = new ResourceLocation("modularmachinery", "blockcasing");

    private final boolean useAeItems;
    private final boolean useAeFluids;
    private final boolean craftMissing;
    private final int tickInterval;
    private final int operationsPerTick;
    private final MessageLimiter blockedReportLimiter = new MessageLimiter(MAX_MISSING_REPORTS);
    private final List<MissingItemEntry> missingItems = new ArrayList<>();
    private final List<MissingFluidEntry> missingFluids = new ArrayList<>();
    private final List<CraftableMissingEntry> craftableMissingEntries = new ArrayList<>();
    private final List<ItemStack> craftedItemBuffer = new ArrayList<>();
    private final List<FluidStack> craftedFluidBuffer = new ArrayList<>();
    private List<IFluidHandlerItem> batchFluidHandlers;
    private Ae2AssemblyExtractor.CraftingGuiRequest activeCraftRequest;
    private CraftableMissingEntry activeCraftEntry;
    private long activeCraftRemaining;
    private ICraftingCPU activeCraftCpu;
    private ICraftingLink activeCraftLink;
    private IGridNode activeCraftNode;
    private boolean activeCraftStarted;
    private boolean waitingForCraftStart;
    private long activeCraftOutputReceived;
    private long finishedCraftGraceUntilTick;
    private long lastCraftStateCheckTick = -1;
    private boolean skippedMissingMaterials;
    private boolean cancelled;
    private boolean craftGuiOpened;
    private int nextCraftableIndex;

    public ConfigurableMachineAssembly(World world, BlockPos ctrlPos, EntityPlayer player, StructureIngredient ingredient, boolean useAeItems, boolean useAeFluids, boolean craftMissing, int tickInterval, int operationsPerTick) {
        super(world, ctrlPos, player, ingredient);
        this.useAeItems = useAeItems;
        this.useAeFluids = useAeFluids;
        this.craftMissing = craftMissing;
        this.tickInterval = tickInterval;
        this.operationsPerTick = operationsPerTick;
        cacheInitialCraftingShortages();
    }

    public int getTickInterval() {
        return tickInterval;
    }

    public int getOperationsPerTick() {
        return operationsPerTick;
    }

    @Override
    public void beginBatch() {
        batchFluidHandlers = null;
    }

    @Override
    public void endBatch() {
        batchFluidHandlers = null;
    }

    @Override
    public void tick() {
        if (cancelled) {
            return;
        }
        if (isAeCraftFlowActive()) {
            updateActiveCraftState();
            if (cancelled || waitingForCraftStart) {
                return;
            }
            if (getWorld().getTotalWorldTime() % CRAFTING_BUILD_INTERVAL_TICKS != 0) {
                return;
            }
        }
        assembly(true);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void cancel() {
        cancelled = true;
        activeCraftRequest = null;
        if (activeCraftLink != null && !activeCraftLink.isDone() && !activeCraftLink.isCanceled()) {
            activeCraftLink.cancel();
        }
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return activeCraftLink == null ? ImmutableSet.of() : ImmutableSet.of(activeCraftLink);
    }

    @Override
    public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack stack, Actionable mode) {
        if (stack == null || stack.getStackSize() <= 0) {
            return null;
        }
        if (activeCraftEntry == null || !stack.isSameType(activeCraftEntry.request)) {
            return stack;
        }
        if (mode == Actionable.SIMULATE) {
            return null;
        }
        if (activeCraftEntry.isFluid()) {
            FluidStack fluid = activeCraftEntry.fluid.copy();
            fluid.amount = (int) Math.min(Integer.MAX_VALUE, stack.getStackSize());
            addCraftedFluidToBuffer(fluid);
            activeCraftOutputReceived += stack.getStackSize();
            return null;
        }
        ItemStack itemStack = stack.createItemStack();
        if (!itemStack.isEmpty()) {
            addCraftedItemToBuffer(itemStack);
            activeCraftOutputReceived += stack.getStackSize();
        }
        return null;
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        if (link != null && link == activeCraftLink && link.isCanceled()) {
            failActiveCraft();
        }
    }

    @Override
    public void gctcore$setCraftingLink(ICraftingLink link) {
        activeCraftLink = link;
        activeCraftStarted = link != null;
        waitingForCraftStart = false;
    }

    @Override
    public IGridNode getActionableNode() {
        return activeCraftNode;
    }

    @Override
    public void report() {
        reportMissingMaterials();
    }

    @Override
    public String getCancelledMessageKey() {
        return "message.gctcore.mmce_builder.cancelled";
    }

    @Override
    public String getSuccessMessageKey() {
        return "message.gctcore.mmce_builder.success";
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

        List<Tuple<ItemStack, IBlockState>> candidates = selectItemCandidatesForPosition(ingredient.pos(), ingredient.ingredientList());
        Tuple<ItemStack, IBlockState> consumed = consumeFirstAvailableItem(candidates);
        if (consumed == null) {
            ItemStack required = selectRequirementItemStack(candidates);
            if (shouldWaitForItemCraft(ingredient.pos(), required)) {
                iterator.remove();
                itemIngredient.add(ingredient);
                return;
            }
            addMissingItemIfUntracked(required);
            iterator.remove();
            return;
        }
        ItemStack required = consumed.getFirst().copy();
        IBlockState state = consumed.getSecond();

        if (!placeAssemblyBlock(realPos, state)) {
            getPlayer().inventory.addItemStackToInventory(required);
        } else {
            clearRequestedCraft(required);
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

        List<Tuple<FluidStack, IBlockState>> candidates = selectFluidCandidatesForPosition(ingredient.pos(), ingredient.ingredientList());
        Tuple<FluidStack, IBlockState> consumed = consumeFirstAvailableFluid(candidates);
        if (consumed == null) {
            FluidStack required = selectRequirementFluidStack(candidates);
            if (shouldWaitForFluidCraft(ingredient.pos(), required)) {
                iterator.remove();
                fluidIngredient.add(ingredient);
                return;
            }
            addMissingFluidIfUntracked(required);
            iterator.remove();
            return;
        }
        FluidStack required = consumed.getFirst().copy();
        IBlockState state = consumed.getSecond();

        if (placeAssemblyBlock(realPos, state)) {
            clearRequestedCraft(required);
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
            if (consumeItem(tuple.getFirst())) {
                return tuple;
            }
        }
        return null;
    }

    private List<Tuple<ItemStack, IBlockState>> selectItemCandidatesForPosition(BlockPos relativePos, List<Tuple<ItemStack, IBlockState>> candidates) {
        Tuple<ItemStack, IBlockState> casingCandidate = findMechanicalCasingCandidate(candidates);
        if (casingCandidate != null) {
            return singleCandidate(casingCandidate);
        }
        Tuple<ItemStack, IBlockState> craftReservedCandidate = findCraftReservedItemCandidate(relativePos, candidates);
        if (craftReservedCandidate != null) {
            return singleCandidate(craftReservedCandidate);
        }
        return candidates;
    }

    private Tuple<FluidStack, IBlockState> consumeFirstAvailableFluid(List<Tuple<FluidStack, IBlockState>> candidates) {
        for (Tuple<FluidStack, IBlockState> tuple : candidates) {
            if (consumeFluid(tuple.getFirst())) {
                return tuple;
            }
        }
        return null;
    }

    private List<Tuple<FluidStack, IBlockState>> selectFluidCandidatesForPosition(BlockPos relativePos, List<Tuple<FluidStack, IBlockState>> candidates) {
        Tuple<FluidStack, IBlockState> craftReservedCandidate = findCraftReservedFluidCandidate(relativePos, candidates);
        if (craftReservedCandidate != null) {
            return singleCandidate(craftReservedCandidate);
        }
        return candidates;
    }

    private Tuple<ItemStack, IBlockState> findMechanicalCasingCandidate(List<Tuple<ItemStack, IBlockState>> candidates) {
        for (Tuple<ItemStack, IBlockState> tuple : candidates) {
            if (isMechanicalCasing(tuple.getFirst())) {
                return tuple;
            }
        }
        return null;
    }

    private Tuple<ItemStack, IBlockState> findCraftReservedItemCandidate(BlockPos relativePos, List<Tuple<ItemStack, IBlockState>> candidates) {
        for (Tuple<ItemStack, IBlockState> tuple : candidates) {
            CraftableMissingEntry entry = findManagedCraftableMissing(relativePos, tuple.getFirst());
            if (entry != null && entry.shouldReserveCandidate()) {
                return tuple;
            }
        }
        return null;
    }

    private Tuple<FluidStack, IBlockState> findCraftReservedFluidCandidate(BlockPos relativePos, List<Tuple<FluidStack, IBlockState>> candidates) {
        for (Tuple<FluidStack, IBlockState> tuple : candidates) {
            CraftableMissingEntry entry = findManagedCraftableMissing(relativePos, tuple.getFirst());
            if (entry != null && entry.shouldReserveCandidate()) {
                return tuple;
            }
        }
        return null;
    }

    private <T> List<Tuple<T, IBlockState>> singleCandidate(Tuple<T, IBlockState> candidate) {
        List<Tuple<T, IBlockState>> selected = new ArrayList<>(1);
        selected.add(candidate);
        return selected;
    }

    private boolean consumeItem(ItemStack required) {
        CraftableMissingEntry managedEntry = findManagedCraftableMissing(required);
        if (managedEntry != null && !managedEntry.shouldManageConsumption()) {
            managedEntry = null;
        }
        if (managedEntry != null) {
            if (consumeCraftedItemBuffer(required)) {
                return true;
            }
            if (managedEntry.isCraftCompleted()) {
                return MachineAssembly.consumeInventoryItem(required, getPlayer().inventory.mainInventory)
                        || useAeItems && Mods.AE2.isLoading() && Ae2AssemblyExtractor.extractItem(getPlayer(), required);
            }
            if (managedEntry.consumeDirect(required.getCount())) {
                if (MachineAssembly.consumeInventoryItem(required, getPlayer().inventory.mainInventory)) {
                    return true;
                }
                if (useAeItems && Mods.AE2.isLoading() && Ae2AssemblyExtractor.extractItem(getPlayer(), required)) {
                    return true;
                }
                managedEntry.restoreDirect(required.getCount());
            }
            return false;
        }
        if (MachineAssembly.consumeInventoryItem(required, getPlayer().inventory.mainInventory)) {
            return true;
        }
        return useAeItems && Mods.AE2.isLoading() && Ae2AssemblyExtractor.extractItem(getPlayer(), required);
    }

    private boolean consumeFluid(FluidStack required) {
        CraftableMissingEntry managedEntry = findManagedCraftableMissing(required);
        if (managedEntry != null && !managedEntry.shouldManageConsumption()) {
            managedEntry = null;
        }
        if (managedEntry != null) {
            if (consumeCraftedFluidBuffer(required)) {
                return true;
            }
            if (managedEntry.isCraftCompleted()) {
                return MachineAssembly.consumeInventoryFluid(required, getBatchFluidHandlers())
                        || useAeFluids && Mods.AE2.isLoading() && Ae2AssemblyExtractor.extractFluid(getPlayer(), required);
            }
            if (managedEntry.consumeDirect(required.amount)) {
                if (MachineAssembly.consumeInventoryFluid(required, getBatchFluidHandlers())) {
                    return true;
                }
                if (useAeFluids && Mods.AE2.isLoading() && Ae2AssemblyExtractor.extractFluid(getPlayer(), required)) {
                    return true;
                }
                managedEntry.restoreDirect(required.amount);
            }
            return false;
        }
        if (MachineAssembly.consumeInventoryFluid(required, getBatchFluidHandlers())) {
            return true;
        }
        return useAeFluids && Mods.AE2.isLoading() && Ae2AssemblyExtractor.extractFluid(getPlayer(), required);
    }

    private List<IFluidHandlerItem> getBatchFluidHandlers() {
        if (batchFluidHandlers == null) {
            batchFluidHandlers = MMCEBuilderUtils.getFluidHandlerItems(getPlayer().inventory.mainInventory);
        }
        return batchFluidHandlers;
    }

    public void openCraftingGuiIfNeeded() {
        if (craftGuiOpened || isAeCraftFlowActive() || nextCraftableIndex >= craftableMissingEntries.size()) {
            return;
        }
        CraftableMissingEntry entry = craftableMissingEntries.get(nextCraftableIndex);
        craftGuiOpened = true;
        activeCraftEntry = entry;
        activeCraftRemaining = entry.assemblyAmount;
        activeCraftLink = null;
        activeCraftNode = null;
        activeCraftStarted = false;
        waitingForCraftStart = true;
        activeCraftRequest = Ae2AssemblyExtractor.openCraftConfirmGui(getPlayer(), entry.request.copy(), this);
        if (activeCraftRequest != null) {
            activeCraftNode = activeCraftRequest.getNode();
            nextCraftableIndex++;
            reportMissingMaterials();
        } else {
            entry.markCraftUnavailable();
            nextCraftableIndex++;
            resetActiveCraft(false);
            craftGuiOpened = false;
        }
    }

    private void cacheInitialCraftingShortages() {
        if (!craftMissing || !Mods.AE2.isLoading()) {
            return;
        }
        if (useAeItems) {
            cacheInitialItemCraftingShortages();
        }
        if (useAeFluids) {
            cacheInitialFluidCraftingShortages();
        }
    }

    private void cacheInitialItemCraftingShortages() {
        List<RequiredItemEntry> requiredItems = collectRequiredItemEntries();
        subtractPlayerInventory(requiredItems);
        for (RequiredItemEntry entry : requiredItems) {
            if (entry.amount <= 0) {
                continue;
            }
            long afterPlayer = entry.amount;
            long stored = useAeItems ? Ae2AssemblyExtractor.getStoredItemAmount(getPlayer(), entry.stack) : 0;
            long storedUsed = Math.min(afterPlayer, stored);
            long shortage = afterPlayer - storedUsed;
            if (shortage <= 0) {
                continue;
            }
            addMissingItem(entry.stack, shortage);
            IAEItemStack request = Ae2AssemblyExtractor.toAeItemRequest(entry.stack, shortage);
            if (request != null && Ae2AssemblyExtractor.canCraftAeItem(getPlayer(), request)) {
                long playerUsed = entry.totalAmount - afterPlayer;
                craftableMissingEntries.add(CraftableMissingEntry.item(entry.stack, request, entry.amount, playerUsed + storedUsed, entry.positions));
            }
        }
    }

    private void cacheInitialFluidCraftingShortages() {
        List<RequiredFluidEntry> requiredFluids = collectRequiredFluidEntries();
        subtractPlayerFluids(requiredFluids);
        for (RequiredFluidEntry entry : requiredFluids) {
            if (entry.amount <= 0) {
                continue;
            }
            long afterPlayer = entry.amount;
            long stored = useAeFluids ? Ae2AssemblyExtractor.getStoredFluidAmount(getPlayer(), entry.fluid) : 0;
            long storedUsed = Math.min(afterPlayer, stored);
            long shortage = afterPlayer - storedUsed;
            if (shortage <= 0) {
                continue;
            }
            addMissingFluid(entry.fluid, shortage);
            IAEItemStack request = Ae2AssemblyExtractor.toAeFluidRequest(entry.fluid, shortage);
            if (request != null && Ae2AssemblyExtractor.canCraftAeItem(getPlayer(), request)) {
                long playerUsed = entry.totalAmount - afterPlayer;
                craftableMissingEntries.add(CraftableMissingEntry.fluid(entry.fluid, request, entry.amount, playerUsed + storedUsed, entry.positions));
            }
        }
    }

    private List<RequiredItemEntry> collectRequiredItemEntries() {
        List<RequiredItemEntry> requiredItems = new ArrayList<>();
        for (StructureIngredient.ItemIngredient ingredient : getIngredient().itemIngredient()) {
            if (ingredient.ingredientList().isEmpty()) {
                continue;
            }
            ItemStack stack = selectRequirementItemStack(ingredient.pos(), ingredient.ingredientList());
            if (stack.isEmpty()) {
                continue;
            }
            addRequiredItem(requiredItems, stack, stack.getCount(), ingredient.pos());
        }
        return requiredItems;
    }

    private ItemStack selectRequirementItemStack(List<Tuple<ItemStack, IBlockState>> candidates) {
        return selectRequirementItemStack(null, candidates);
    }

    private ItemStack selectRequirementItemStack(BlockPos relativePos, List<Tuple<ItemStack, IBlockState>> candidates) {
        Tuple<ItemStack, IBlockState> casingCandidate = findMechanicalCasingCandidate(candidates);
        if (casingCandidate != null) {
            return casingCandidate.getFirst();
        }
        for (Tuple<ItemStack, IBlockState> candidate : candidates) {
            ItemStack stack = candidate.getFirst();
            if (!stack.isEmpty() && hasPlayerItem(stack)) {
                return stack;
            }
        }
        if (useAeItems && Mods.AE2.isLoading()) {
            for (Tuple<ItemStack, IBlockState> candidate : candidates) {
                ItemStack stack = candidate.getFirst();
                if (!stack.isEmpty() && Ae2AssemblyExtractor.getStoredItemAmount(getPlayer(), stack) >= stack.getCount()) {
                    return stack;
                }
            }
            for (Tuple<ItemStack, IBlockState> candidate : candidates) {
                ItemStack stack = candidate.getFirst();
                IAEItemStack request = Ae2AssemblyExtractor.toAeItemRequest(stack, stack.getCount());
                if (request != null && Ae2AssemblyExtractor.canCraftAeItem(getPlayer(), request)) {
                    return stack;
                }
            }
        }
        if (relativePos != null) {
            for (Tuple<ItemStack, IBlockState> candidate : candidates) {
                CraftableMissingEntry entry = findManagedCraftableMissing(relativePos, candidate.getFirst());
                if (entry != null && entry.shouldReserveCandidate()) {
                    return candidate.getFirst();
                }
            }
        }
        return candidates.isEmpty() ? ItemStack.EMPTY : candidates.get(0).getFirst();
    }

    private List<RequiredFluidEntry> collectRequiredFluidEntries() {
        List<RequiredFluidEntry> requiredFluids = new ArrayList<>();
        for (StructureIngredient.FluidIngredient ingredient : getIngredient().fluidIngredient()) {
            if (ingredient.ingredientList().isEmpty()) {
                continue;
            }
            FluidStack fluid = selectRequirementFluidStack(ingredient.pos(), ingredient.ingredientList());
            if (fluid == null || fluid.amount <= 0) {
                continue;
            }
            addRequiredFluid(requiredFluids, fluid, fluid.amount, ingredient.pos());
        }
        return requiredFluids;
    }

    private FluidStack selectRequirementFluidStack(List<Tuple<FluidStack, IBlockState>> candidates) {
        return selectRequirementFluidStack(null, candidates);
    }

    private FluidStack selectRequirementFluidStack(BlockPos relativePos, List<Tuple<FluidStack, IBlockState>> candidates) {
        for (Tuple<FluidStack, IBlockState> candidate : candidates) {
            FluidStack fluid = candidate.getFirst();
            if (fluid != null && fluid.amount > 0 && hasPlayerFluid(fluid)) {
                return fluid;
            }
        }
        if (useAeFluids && Mods.AE2.isLoading()) {
            for (Tuple<FluidStack, IBlockState> candidate : candidates) {
                FluidStack fluid = candidate.getFirst();
                if (fluid != null && fluid.amount > 0 && Ae2AssemblyExtractor.getStoredFluidAmount(getPlayer(), fluid) >= fluid.amount) {
                    return fluid;
                }
            }
            for (Tuple<FluidStack, IBlockState> candidate : candidates) {
                FluidStack fluid = candidate.getFirst();
                IAEItemStack request = Ae2AssemblyExtractor.toAeFluidRequest(fluid, fluid == null ? 0 : fluid.amount);
                if (request != null && Ae2AssemblyExtractor.canCraftAeItem(getPlayer(), request)) {
                    return fluid;
                }
            }
        }
        if (relativePos != null) {
            for (Tuple<FluidStack, IBlockState> candidate : candidates) {
                CraftableMissingEntry entry = findManagedCraftableMissing(relativePos, candidate.getFirst());
                if (entry != null && entry.shouldReserveCandidate()) {
                    return candidate.getFirst();
                }
            }
        }
        return candidates.isEmpty() ? null : candidates.get(0).getFirst();
    }

    private void subtractPlayerInventory(List<RequiredItemEntry> requiredItems) {
        for (ItemStack inventoryStack : getPlayer().inventory.mainInventory) {
            if (inventoryStack.isEmpty()) {
                continue;
            }
            int remaining = inventoryStack.getCount();
            for (RequiredItemEntry entry : requiredItems) {
                if (remaining <= 0) {
                    break;
                }
                if (!MMCEBuilderUtils.areItemStacksEqual(inventoryStack, entry.stack)) {
                    continue;
                }
                long consumed = Math.min(entry.amount, remaining);
                entry.amount -= consumed;
                remaining -= consumed;
            }
        }
    }

    private void subtractPlayerFluids(List<RequiredFluidEntry> requiredFluids) {
        for (IFluidHandlerItem handler : MMCEBuilderUtils.getFluidHandlerItems(getPlayer().inventory.mainInventory)) {
            for (IFluidTankProperties property : handler.getTankProperties()) {
                FluidStack contained = property.getContents();
                if (contained == null || contained.amount <= 0) {
                    continue;
                }
                int remaining = contained.amount;
                for (RequiredFluidEntry entry : requiredFluids) {
                    if (remaining <= 0) {
                        break;
                    }
                    if (!MMCEBuilderUtils.areFluidsEqual(contained, entry.fluid)) {
                        continue;
                    }
                    long consumed = Math.min(entry.amount, remaining);
                    entry.amount -= consumed;
                    remaining -= consumed;
                }
            }
        }
    }

    private boolean hasPlayerItem(ItemStack required) {
        if (required.isEmpty()) {
            return false;
        }
        int amount = 0;
        for (ItemStack inventoryStack : getPlayer().inventory.mainInventory) {
            if (MMCEBuilderUtils.areItemStacksEqual(inventoryStack, required)) {
                amount += inventoryStack.getCount();
                if (amount >= required.getCount()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasPlayerFluid(FluidStack required) {
        if (required == null || required.amount <= 0) {
            return false;
        }
        int amount = 0;
        for (IFluidHandlerItem handler : MMCEBuilderUtils.getFluidHandlerItems(getPlayer().inventory.mainInventory)) {
            for (IFluidTankProperties property : handler.getTankProperties()) {
                FluidStack contained = property.getContents();
                if (MMCEBuilderUtils.areFluidsEqual(contained, required)) {
                    amount += contained.amount;
                    if (amount >= required.amount) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void addRequiredItem(List<RequiredItemEntry> requiredItems, ItemStack stack, long amount, BlockPos relativePos) {
        for (RequiredItemEntry entry : requiredItems) {
            if (MMCEBuilderUtils.areItemStacksEqual(entry.stack, stack)) {
                entry.totalAmount += amount;
                entry.amount += amount;
                entry.positions.add(relativePos);
                return;
            }
        }
        requiredItems.add(new RequiredItemEntry(stack, amount, relativePos));
    }

    private void addRequiredFluid(List<RequiredFluidEntry> requiredFluids, FluidStack fluid, long amount, BlockPos relativePos) {
        for (RequiredFluidEntry entry : requiredFluids) {
            if (MMCEBuilderUtils.areFluidsEqual(entry.fluid, fluid)) {
                entry.totalAmount += amount;
                entry.amount += amount;
                entry.positions.add(relativePos);
                return;
            }
        }
        requiredFluids.add(new RequiredFluidEntry(fluid, amount, relativePos));
    }

    private boolean shouldWaitForItemCraft(BlockPos relativePos, ItemStack required) {
        if (required.isEmpty()) {
            return false;
        }
        if (isActiveCraft(relativePos, required)) {
            return shouldWaitForActiveCraft();
        }
        if (isQueuedCraftableMissing(relativePos, required)) {
            openCraftingGuiIfNeeded();
            return true;
        }
        return false;
    }

    private boolean shouldWaitForFluidCraft(BlockPos relativePos, FluidStack required) {
        if (required == null || required.amount <= 0) {
            return false;
        }
        if (isActiveCraft(relativePos, required)) {
            return shouldWaitForActiveCraft();
        }
        if (isQueuedCraftableMissing(relativePos, required)) {
            openCraftingGuiIfNeeded();
            return true;
        }
        return false;
    }

    private boolean shouldWaitForActiveCraft() {
        if (activeCraftRequest != null || waitingForCraftStart) {
            return true;
        }
        if (activeCraftStarted && getWorld().getTotalWorldTime() <= finishedCraftGraceUntilTick) {
            return true;
        }
        if (activeCraftStarted) {
            finishActiveCraft();
            return true;
        }
        return false;
    }

    private boolean isQueuedCraftableMissing(BlockPos relativePos, ItemStack required) {
        for (int i = nextCraftableIndex; i < craftableMissingEntries.size(); i++) {
            CraftableMissingEntry entry = craftableMissingEntries.get(i);
            if (entry.shouldReserveCandidate() && entry.matches(relativePos, required)) {
                return true;
            }
        }
        return false;
    }

    private boolean isQueuedCraftableMissing(BlockPos relativePos, FluidStack required) {
        for (int i = nextCraftableIndex; i < craftableMissingEntries.size(); i++) {
            CraftableMissingEntry entry = craftableMissingEntries.get(i);
            if (entry.shouldReserveCandidate() && entry.matches(relativePos, required)) {
                return true;
            }
        }
        return false;
    }

    private CraftableMissingEntry findManagedCraftableMissing(ItemStack required) {
        for (CraftableMissingEntry entry : craftableMissingEntries) {
            if (entry.matches(required)) {
                return entry;
            }
        }
        return null;
    }

    private CraftableMissingEntry findManagedCraftableMissing(FluidStack required) {
        for (CraftableMissingEntry entry : craftableMissingEntries) {
            if (entry.matches(required)) {
                return entry;
            }
        }
        return null;
    }

    private CraftableMissingEntry findManagedCraftableMissing(BlockPos relativePos, ItemStack required) {
        for (CraftableMissingEntry entry : craftableMissingEntries) {
            if (entry.matches(relativePos, required)) {
                return entry;
            }
        }
        return null;
    }

    private CraftableMissingEntry findManagedCraftableMissing(BlockPos relativePos, FluidStack required) {
        for (CraftableMissingEntry entry : craftableMissingEntries) {
            if (entry.matches(relativePos, required)) {
                return entry;
            }
        }
        return null;
    }

    private boolean isMechanicalCasing(ItemStack stack) {
        return !stack.isEmpty() && MMCE_BLOCK_CASING.equals(stack.getItem().getRegistryName()) && stack.getMetadata() == 0;
    }

    private boolean isActiveCraft(BlockPos relativePos, ItemStack required) {
        return activeCraftEntry != null && activeCraftEntry.matches(relativePos, required);
    }

    private boolean isActiveCraft(BlockPos relativePos, FluidStack required) {
        return activeCraftEntry != null && activeCraftEntry.matches(relativePos, required);
    }

    private boolean isAeCraftFlowActive() {
        return activeCraftEntry != null && (activeCraftRequest != null || waitingForCraftStart || activeCraftStarted);
    }

    private void updateActiveCraftState() {
        long now = getWorld().getTotalWorldTime();
        if (lastCraftStateCheckTick == now) {
            return;
        }
        lastCraftStateCheckTick = now;
        if (activeCraftEntry == null) {
            return;
        }
        if (activeCraftLink != null) {
            if (activeCraftLink.isCanceled()) {
                failActiveCraft();
                return;
            }
            if (activeCraftLink.isDone()) {
                if (activeCraftOutputReceived >= activeCraftEntry.request.getStackSize()) {
                    finishActiveCraft();
                    return;
                }
                if (finishedCraftGraceUntilTick == 0) {
                    finishedCraftGraceUntilTick = now + FINISHED_CRAFT_GRACE_TICKS;
                } else if (now > finishedCraftGraceUntilTick) {
                    finishActiveCraft();
                }
                return;
            }
        }
        if (activeCraftStarted) {
            bindActiveCraftCpu();
            if (activeCraftCpu != null) {
                if (isActiveCraftCpuFinished()) {
                    finishActiveCraft();
                }
                return;
            }
            if (isActiveCraftOutputAvailable()) {
                finishActiveCraft();
                return;
            }
        }
        if (activeCraftRequest == null) {
            if (activeCraftStarted) {
                if (finishedCraftGraceUntilTick == 0) {
                    finishedCraftGraceUntilTick = now + FINISHED_CRAFT_GRACE_TICKS;
                } else if (now > finishedCraftGraceUntilTick) {
                    finishActiveCraft();
                }
            }
            return;
        }
        boolean confirmOpen = getPlayer().openContainer instanceof ContainerCraftConfirm;
        if (confirmOpen) {
            ContainerCraftConfirm confirm = (ContainerCraftConfirm) getPlayer().openContainer;
            if (confirm.isSimulation() && confirm.getUsedBytes() > 0) {
                failActiveCraft();
                return;
            }
        }
        if (activeCraftRequest.isRequesting()) {
            activeCraftStarted = true;
            waitingForCraftStart = false;
            return;
        }
        if (!activeCraftStarted) {
            if (!confirmOpen) {
                failActiveCraft();
            }
            return;
        }
        activeCraftRequest = null;
        finishedCraftGraceUntilTick = now + FINISHED_CRAFT_GRACE_TICKS;
    }

    private void finishActiveCraft() {
        if (activeCraftEntry != null) {
            activeCraftEntry.markCraftCompleted();
        }
        resetActiveCraft(true);
        craftGuiOpened = false;
        openCraftingGuiIfNeeded();
    }

    private void bindActiveCraftCpu() {
        if (activeCraftCpu != null || activeCraftRequest == null) {
            return;
        }
        activeCraftCpu = activeCraftRequest.findCpuForRequest();
    }

    private boolean isActiveCraftCpuFinished() {
        if (activeCraftCpu == null || activeCraftEntry == null) {
            return false;
        }
        IAEItemStack output = activeCraftCpu.getFinalOutput();
        return !activeCraftCpu.isBusy() || output == null || !output.isSameType(activeCraftEntry.request);
    }

    private boolean isActiveCraftOutputAvailable() {
        if (activeCraftEntry == null || activeCraftEntry.request == null) {
            return false;
        }
        long requested = activeCraftEntry.request.getStackSize();
        if (requested <= 0) {
            return false;
        }
        if (activeCraftEntry.isFluid()) {
            return useAeFluids && Mods.AE2.isLoading()
                    && Ae2AssemblyExtractor.getStoredFluidAmount(getPlayer(), activeCraftEntry.fluid) >= requested;
        }
        return useAeItems && Mods.AE2.isLoading()
                && Ae2AssemblyExtractor.getStoredItemAmount(getPlayer(), activeCraftEntry.item) >= requested;
    }

    private void failActiveCraft() {
        if (activeCraftEntry != null) {
            activeCraftEntry.markCraftUnavailable();
        }
        resetActiveCraft(true);
        craftGuiOpened = false;
        openCraftingGuiIfNeeded();
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
        if (isCompleted() && !skippedMissingMaterials) {
            return;
        }
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
        addMissingItem(required, required.getCount());
    }

    private void addMissingItem(ItemStack required, long amount) {
        if (required.isEmpty() || amount <= 0) {
            return;
        }
        for (MissingItemEntry entry : missingItems) {
            if (MMCEBuilderUtils.areItemStacksEqual(entry.stack, required)) {
                entry.amount += amount;
                return;
            }
        }
        missingItems.add(new MissingItemEntry(required, amount));
    }

    private void addMissingItemIfUntracked(ItemStack required) {
        skippedMissingMaterials = true;
        if (!hasMissingItem(required)) {
            addMissingItem(required);
        }
    }

    private boolean hasMissingItem(ItemStack required) {
        if (required.isEmpty()) {
            return false;
        }
        for (MissingItemEntry entry : missingItems) {
            if (MMCEBuilderUtils.areItemStacksEqual(entry.stack, required)) {
                return true;
            }
        }
        return false;
    }

    private void addMissingFluid(FluidStack required) {
        if (required == null) {
            return;
        }
        addMissingFluid(required, required.amount);
    }

    private void addMissingFluid(FluidStack required, long amount) {
        if (required == null || required.amount <= 0 || amount <= 0) {
            return;
        }
        for (MissingFluidEntry entry : missingFluids) {
            if (MMCEBuilderUtils.areFluidsEqual(entry.fluid, required)) {
                entry.amount += amount;
                return;
            }
        }
        missingFluids.add(new MissingFluidEntry(required, amount));
    }

    private void addMissingFluidIfUntracked(FluidStack required) {
        skippedMissingMaterials = true;
        if (!hasMissingFluid(required)) {
            addMissingFluid(required);
        }
    }

    private boolean hasMissingFluid(FluidStack required) {
        if (required == null || required.amount <= 0) {
            return false;
        }
        for (MissingFluidEntry entry : missingFluids) {
            if (MMCEBuilderUtils.areFluidsEqual(entry.fluid, required)) {
                return true;
            }
        }
        return false;
    }

    private void addCraftedItemToBuffer(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        for (ItemStack buffered : craftedItemBuffer) {
            if (MMCEBuilderUtils.areItemStacksEqual(buffered, stack)) {
                buffered.grow(stack.getCount());
                return;
            }
        }
        craftedItemBuffer.add(stack.copy());
    }

    private void addCraftedFluidToBuffer(FluidStack fluid) {
        if (fluid == null || fluid.amount <= 0) {
            return;
        }
        for (FluidStack buffered : craftedFluidBuffer) {
            if (MMCEBuilderUtils.areFluidsEqual(buffered, fluid)) {
                buffered.amount += fluid.amount;
                return;
            }
        }
        craftedFluidBuffer.add(fluid.copy());
    }

    private boolean consumeCraftedItemBuffer(ItemStack required) {
        if (required.isEmpty()) {
            return true;
        }
        for (Iterator<ItemStack> iterator = craftedItemBuffer.iterator(); iterator.hasNext(); ) {
            ItemStack buffered = iterator.next();
            if (!MMCEBuilderUtils.areItemStacksEqual(buffered, required) || buffered.getCount() < required.getCount()) {
                continue;
            }
            buffered.shrink(required.getCount());
            if (buffered.isEmpty()) {
                iterator.remove();
            }
            return true;
        }
        return false;
    }

    private boolean consumeCraftedFluidBuffer(FluidStack required) {
        if (required == null || required.amount <= 0) {
            return true;
        }
        for (Iterator<FluidStack> iterator = craftedFluidBuffer.iterator(); iterator.hasNext(); ) {
            FluidStack buffered = iterator.next();
            if (!MMCEBuilderUtils.areFluidsEqual(buffered, required) || buffered.amount < required.amount) {
                continue;
            }
            buffered.amount -= required.amount;
            if (buffered.amount <= 0) {
                iterator.remove();
            }
            return true;
        }
        return false;
    }

    private void clearRequestedCraft(ItemStack required) {
        if (activeCraftEntry != null && activeCraftEntry.matches(required)) {
            activeCraftRemaining -= required.getCount();
        }
    }

    private void clearRequestedCraft(FluidStack required) {
        if (activeCraftEntry != null && activeCraftEntry.matches(required)) {
            activeCraftRemaining -= required.amount;
        }
    }

    private void resetActiveCraft(boolean clearLink) {
        activeCraftEntry = null;
        activeCraftRequest = null;
        activeCraftRemaining = 0;
        activeCraftCpu = null;
        activeCraftNode = null;
        activeCraftStarted = false;
        waitingForCraftStart = false;
        activeCraftOutputReceived = 0;
        finishedCraftGraceUntilTick = 0;
        if (clearLink) {
            activeCraftLink = null;
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
        private long amount;

        private MissingItemEntry(ItemStack stack, long amount) {
            this.stack = stack.copy();
            this.stack.setCount(1);
            this.amount = amount;
        }
    }

    private static final class MissingFluidEntry {
        private final FluidStack fluid;
        private long amount;

        private MissingFluidEntry(FluidStack fluid, long amount) {
            this.fluid = fluid.copy();
            this.amount = amount;
        }
    }

    private static final class RequiredItemEntry {
        private final ItemStack stack;
        private final List<BlockPos> positions = new ArrayList<>();
        private long totalAmount;
        private long amount;

        private RequiredItemEntry(ItemStack stack, long amount, BlockPos relativePos) {
            this.stack = stack.copy();
            this.stack.setCount(1);
            this.totalAmount = amount;
            this.amount = amount;
            this.positions.add(relativePos);
        }
    }

    private static final class RequiredFluidEntry {
        private final FluidStack fluid;
        private final List<BlockPos> positions = new ArrayList<>();
        private long totalAmount;
        private long amount;

        private RequiredFluidEntry(FluidStack fluid, long amount, BlockPos relativePos) {
            this.fluid = fluid.copy();
            this.totalAmount = amount;
            this.amount = amount;
            this.positions.add(relativePos);
        }
    }

    private static final class CraftableMissingEntry {
        private final ItemStack item;
        private final FluidStack fluid;
        private final IAEItemStack request;
        private final List<BlockPos> positions;
        private final long assemblyAmount;
        private long directRemaining;
        private boolean craftUnavailable;
        private boolean craftCompleted;

        private CraftableMissingEntry(ItemStack item, FluidStack fluid, IAEItemStack request, long assemblyAmount, long directAmount, List<BlockPos> positions) {
            this.item = item.isEmpty() ? ItemStack.EMPTY : item.copy();
            if (!this.item.isEmpty()) {
                this.item.setCount(1);
            }
            this.fluid = fluid == null ? null : fluid.copy();
            this.request = request;
            this.positions = new ArrayList<>(positions);
            this.assemblyAmount = assemblyAmount;
            this.directRemaining = directAmount;
        }

        private static CraftableMissingEntry item(ItemStack item, IAEItemStack request, long assemblyAmount, long directAmount, List<BlockPos> positions) {
            return new CraftableMissingEntry(item, null, request, assemblyAmount, directAmount, positions);
        }

        private static CraftableMissingEntry fluid(FluidStack fluid, IAEItemStack request, long assemblyAmount, long directAmount, List<BlockPos> positions) {
            return new CraftableMissingEntry(ItemStack.EMPTY, fluid, request, assemblyAmount, directAmount, positions);
        }

        private boolean isFluid() {
            return fluid != null;
        }

        private boolean shouldReserveCandidate() {
            return !craftUnavailable;
        }

        private boolean shouldManageConsumption() {
            return !craftUnavailable || craftCompleted;
        }

        private boolean isCraftCompleted() {
            return craftCompleted;
        }

        private void markCraftCompleted() {
            craftCompleted = true;
        }

        private void markCraftUnavailable() {
            craftUnavailable = true;
        }

        private boolean matches(ItemStack required) {
            return !item.isEmpty() && MMCEBuilderUtils.areItemStacksEqual(item, required);
        }

        private boolean matches(FluidStack required) {
            return fluid != null && MMCEBuilderUtils.areFluidsEqual(fluid, required);
        }

        private boolean matches(BlockPos relativePos, ItemStack required) {
            return positions.contains(relativePos) && matches(required);
        }

        private boolean matches(BlockPos relativePos, FluidStack required) {
            return positions.contains(relativePos) && matches(required);
        }

        private boolean consumeDirect(long amount) {
            if (amount <= 0 || directRemaining < amount) {
                return false;
            }
            directRemaining -= amount;
            return true;
        }

        private void restoreDirect(long amount) {
            if (amount > 0) {
                directRemaining += amount;
            }
        }
    }
}
