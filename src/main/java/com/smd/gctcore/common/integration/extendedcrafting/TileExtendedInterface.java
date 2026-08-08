package com.smd.gctcore.common.integration.extendedcrafting;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEPartLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TileExtendedInterface extends TileExtendedGridMachine
        implements IInventory, IActionHost, ICraftingProvider, ITickable {
    public static final int PATTERN_SLOTS = 36;
    private static final double CRAFT_POWER = 500.0D;
    private final NonNullList<ItemStack> patterns = NonNullList.withSize(PATTERN_SLOTS, ItemStack.EMPTY);
    private final NonNullList<ItemStack> pendingOutputs = NonNullList.create();
    private List<BlockPos> lastAssemblers = new ArrayList<>();
    private boolean patternChangePending;
    private int refreshTicks = 20;

    public TileExtendedInterface() { }
    TileExtendedInterface(ExtendedCraftingTier tier) { super(tier); }

    @Override protected BlockExtendedCraftingAutomation.Kind machineKind() { return BlockExtendedCraftingAutomation.Kind.INTERFACE; }
    @Override protected boolean requiresChannel() { return true; }
    @Override protected void onGridChanged() { queuePatternChange(); }
    @Override public IGridNode getActionableNode() { return getGridNode(AEPartLocation.INTERNAL); }

    @MENetworkEventSubscribe public void onChannelsChanged(MENetworkChannelsChanged event) { queuePatternChange(); }
    @MENetworkEventSubscribe public void onPowerChanged(MENetworkPowerStatusChange event) { queuePatternChange(); }

    @Override
    public void update() {
        if (world == null || world.isRemote) return;
        if (refreshTicks-- > 0) {
            refreshNode();
            queuePatternChange();
        }
        retryOutputs();
        List<BlockPos> current = assemblerPositions();
        if (!current.equals(lastAssemblers)) {
            lastAssemblers = current;
            queuePatternChange();
        }
        if (patternChangePending) patternChangePending = !postPatternChange();
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper helper) {
        IGridNode node = getGridNode(AEPartLocation.INTERNAL);
        if (node == null || !node.isActive()) return;
        List<BlockPos> assemblers = assemblerPositions();
        for (ItemStack pattern : patterns) {
            if (ExtendedPatternData.isValid(pattern, world) && ExtendedPatternData.tier(pattern) == tier) {
                ExtendedCraftingPatternDetails details = new ExtendedCraftingPatternDetails(pattern, world);
                for (BlockPos assembler : assemblers) {
                    helper.addCraftingOption(new AssemblerProvider(this, assembler), details);
                }
            }
        }
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails details, InventoryCrafting table) {
        TileExtendedMolecularAssembler assembler = idleAssembler();
        return assembler != null && pushPatternTo(assembler.getPos(), details, table);
    }

    private boolean pushPatternTo(BlockPos assemblerPos, ICraftingPatternDetails details, InventoryCrafting table) {
        if (!pendingOutputs.isEmpty() || !(details instanceof ExtendedCraftingPatternDetails)) return false;
        if (ExtendedPatternData.tier(details.getPattern()) != tier) return false;
        TileEntity candidate = world.getTileEntity(assemblerPos);
        if (!(candidate instanceof TileExtendedMolecularAssembler)) return false;
        TileExtendedMolecularAssembler assembler = (TileExtendedMolecularAssembler) candidate;
        if (assembler.tier() != tier || assembler.isBusy()) return false;
        IEnergyGrid energy = energyGrid();
        if (energy == null || energy.extractAEPower(CRAFT_POWER, Actionable.SIMULATE, PowerMultiplier.CONFIG) < CRAFT_POWER) return false;
        energy.extractAEPower(CRAFT_POWER, Actionable.MODULATE, PowerMultiplier.CONFIG);
        NonNullList<ItemStack> inputs = NonNullList.create();
        for (int i = 0; i < table.getSizeInventory(); i++) {
            ItemStack stack = table.getStackInSlot(i);
            if (!stack.isEmpty()) inputs.add(stack.copy());
        }
        NonNullList<ItemStack> outputs = NonNullList.create();
        for (IAEItemStack output : details.getCondensedOutputs()) {
            if (output != null) outputs.add(output.createItemStack());
        }
        return assembler.start(this, inputs, outputs);
    }

    @Override public boolean isBusy() { return !pendingOutputs.isEmpty() || idleAssembler() == null; }

    boolean receiveAssemblerOutputs(List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            ItemStack remaining = insertToNetwork(stack);
            if (!remaining.isEmpty()) pendingOutputs.add(remaining);
        }
        markDirty();
        return true;
    }

    private void retryOutputs() {
        if (pendingOutputs.isEmpty()) return;
        for (int i = 0; i < pendingOutputs.size(); i++) {
            ItemStack remaining = insertToNetwork(pendingOutputs.get(i));
            if (remaining.isEmpty()) pendingOutputs.remove(i--); else pendingOutputs.set(i, remaining);
        }
        markDirty();
    }

    private ItemStack insertToNetwork(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        IGridNode node = getGridNode(AEPartLocation.INTERNAL);
        if (node == null || !node.isActive() || node.getGrid() == null) return stack;
        IStorageGrid storage = node.getGrid().getCache(IStorageGrid.class);
        IEnergyGrid energy = node.getGrid().getCache(IEnergyGrid.class);
        if (storage == null || energy == null || !energy.isNetworkPowered()) return stack;
        IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
        IMEMonitor<IAEItemStack> inventory = storage.getInventory(channel);
        IAEItemStack input = channel.createStack(stack);
        IAEItemStack leftover = AEApi.instance().storage().poweredInsert(energy, inventory, input,
                new InterfaceActionSource(this), Actionable.MODULATE);
        return leftover == null ? ItemStack.EMPTY : leftover.createItemStack();
    }

    private IEnergyGrid energyGrid() {
        IGridNode node = getGridNode(AEPartLocation.INTERNAL);
        if (node == null || !node.isActive() || node.getGrid() == null) return null;
        IEnergyGrid energy = node.getGrid().getCache(IEnergyGrid.class);
        return energy != null && energy.isNetworkPowered() ? energy : null;
    }

    private List<BlockPos> assemblerPositions() {
        List<BlockPos> positions = new ArrayList<>();
        if (world == null) return positions;
        for (EnumFacing face : EnumFacing.values()) {
            TileEntity candidate = world.getTileEntity(pos.offset(face));
            if (candidate instanceof TileExtendedMolecularAssembler
                    && ((TileExtendedMolecularAssembler) candidate).tier() == tier) positions.add(candidate.getPos());
        }
        return positions;
    }

    private TileExtendedMolecularAssembler idleAssembler() {
        if (world == null) return null;
        for (BlockPos assemblerPos : assemblerPositions()) {
            TileExtendedMolecularAssembler assembler = (TileExtendedMolecularAssembler) world.getTileEntity(assemblerPos);
            if (assembler != null && !assembler.isBusy()) return assembler;
        }
        return null;
    }

    private boolean assemblerBusy(BlockPos assemblerPos) {
        if (!pendingOutputs.isEmpty() || world == null) return true;
        TileEntity candidate = world.getTileEntity(assemblerPos);
        return !(candidate instanceof TileExtendedMolecularAssembler)
                || ((TileExtendedMolecularAssembler) candidate).tier() != tier
                || ((TileExtendedMolecularAssembler) candidate).isBusy();
    }

    private void queuePatternChange() { patternChangePending = true; }
    private boolean postPatternChange() {
        IGridNode node = getGridNode(AEPartLocation.INTERNAL);
        if (node != null && node.isActive() && node.getGrid() != null) {
            node.getGrid().postEvent(new MENetworkCraftingPatternChange(this, node));
            return true;
        }
        return false;
    }

    @Override public int getSizeInventory() { return PATTERN_SLOTS; }
    @Override public boolean isEmpty() { for (ItemStack stack : patterns) if (!stack.isEmpty()) return false; return true; }
    @Override public ItemStack getStackInSlot(int index) { return patterns.get(index); }
    @Override public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = patterns.get(index);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack taken = stack.splitStack(count);
        if (stack.isEmpty()) patterns.set(index, ItemStack.EMPTY);
        changed();
        return taken;
    }
    @Override public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = patterns.get(index);
        patterns.set(index, ItemStack.EMPTY);
        changed();
        return stack;
    }
    @Override public void setInventorySlotContents(int index, ItemStack stack) {
        ItemStack value = stack == null ? ItemStack.EMPTY : stack;
        if (!value.isEmpty()) value.setCount(1);
        patterns.set(index, value);
        changed();
    }
    private void changed() { markDirty(); patternChangePending = !postPatternChange(); }
    @Override public String getName() { return "container.gctcore.extended_interface"; }
    @Override public boolean hasCustomName() { return false; }
    @Override public int getInventoryStackLimit() { return 1; }
    @Override public boolean isUsableByPlayer(EntityPlayer player) {
        return world != null && world.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D) <= 64.0D;
    }
    @Override public void openInventory(EntityPlayer player) { }
    @Override public void closeInventory(EntityPlayer player) { }
    @Override public boolean isItemValidForSlot(int index, ItemStack stack) {
        return ExtendedPatternData.isEncoded(stack) && ExtendedPatternData.tier(stack) == tier;
    }
    @Override public int getField(int id) { return 0; }
    @Override public void setField(int id, int value) { }
    @Override public int getFieldCount() { return 0; }
    @Override public void clear() { for (int i = 0; i < patterns.size(); i++) patterns.set(i, ItemStack.EMPTY); changed(); }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("Patterns", writeList(patterns, true));
        compound.setTag("PendingOutputs", writeList(pendingOutputs, false));
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        readFixed(compound.getTagList("Patterns", 10), patterns);
        readGrowing(compound.getTagList("PendingOutputs", 10), pendingOutputs);
        patternChangePending = true;
    }

    public void dropContents() {
        if (world == null) return;
        for (ItemStack stack : patterns) drop(stack);
        for (ItemStack stack : pendingOutputs) drop(stack);
    }
    private void drop(ItemStack stack) { if (!stack.isEmpty()) InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack); }

    private static NBTTagList writeList(List<ItemStack> stacks, boolean slots) {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < stacks.size(); i++) if (!stacks.get(i).isEmpty()) {
            NBTTagCompound tag = stacks.get(i).writeToNBT(new NBTTagCompound());
            if (slots) tag.setInteger("Slot", i);
            list.appendTag(tag);
        }
        return list;
    }
    private static void readFixed(NBTTagList list, NonNullList<ItemStack> stacks) {
        for (int i = 0; i < stacks.size(); i++) stacks.set(i, ItemStack.EMPTY);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getInteger("Slot");
            if (slot >= 0 && slot < stacks.size()) stacks.set(slot, new ItemStack(tag));
        }
    }
    private static void readGrowing(NBTTagList list, NonNullList<ItemStack> stacks) {
        stacks.clear();
        for (int i = 0; i < list.tagCount(); i++) {
            ItemStack stack = new ItemStack(list.getCompoundTagAt(i));
            if (!stack.isEmpty()) stacks.add(stack);
        }
    }

    private static final class InterfaceActionSource implements IActionSource {
        private final TileExtendedInterface tile;
        private InterfaceActionSource(TileExtendedInterface tile) { this.tile = tile; }
        @Override @Nonnull public Optional<EntityPlayer> player() { return Optional.empty(); }
        @Override @Nonnull public Optional<IActionHost> machine() { return Optional.of(tile); }
        @Override @Nonnull public <T> Optional<T> context(@Nonnull Class<T> key) { return Optional.empty(); }
    }

    private static final class AssemblerProvider implements ICraftingProvider {
        private final TileExtendedInterface owner;
        private final BlockPos assembler;
        private AssemblerProvider(TileExtendedInterface owner, BlockPos assembler) { this.owner = owner; this.assembler = assembler; }
        @Override public void provideCrafting(ICraftingProviderHelper helper) { }
        @Override public boolean pushPattern(ICraftingPatternDetails details, InventoryCrafting table) {
            return owner.pushPatternTo(assembler, details, table);
        }
        @Override public boolean isBusy() { return owner.assemblerBusy(assembler); }
    }
}
