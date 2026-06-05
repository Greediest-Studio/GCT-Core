package com.smd.gctcore.common.integration.mmce;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.fluids.util.AEFluidStack;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.me.helpers.PlayerSource;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.smd.gctcore.common.util.MMCEBuilderUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class Ae2AssemblyExtractor {

    private static final int DIAGNOSTIC_INTERVAL_TICKS = 100;
    private static final int CRAFTING_JOB_TIMEOUT_MILLIS = 50;

    private Ae2AssemblyExtractor() {
    }

    public static boolean extractItem(EntityPlayer player, ItemStack required) {
        if (required.isEmpty()) {
            return true;
        }
        IAEItemStack request = AEItemStack.fromItemStack(required);
        if (request == null) {
            return false;
        }
        request.setStackSize(required.getCount());
        List<WirelessTerminalAccess> terminals = findWirelessTerminals(player);
        if (terminals.isEmpty()) {
            sendDiagnostic(player, "message.gctcore.mmce_builder.ae_no_terminal");
            return false;
        }
        boolean inaccessibleNetwork = false;
        boolean storageMissing = false;
        boolean insufficientAmount = false;
        for (WirelessTerminalAccess terminal : terminals) {
            IGridNode node = terminal.guiObject.getActionableNode();
            if (!isAccessible(player, node, SecurityPermissions.EXTRACT)) {
                inaccessibleNetwork = true;
                continue;
            }
            IGrid grid = node.getGrid();
            IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
            if (storageGrid == null) {
                storageMissing = true;
                continue;
            }
            IMEMonitor<IAEItemStack> monitor = storageGrid.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
            IAEItemStack simulated = Platform.poweredExtraction(terminal.guiObject, monitor, request.copy(), new PlayerSource(player, terminal.guiObject), Actionable.SIMULATE);
            if (simulated == null || simulated.getStackSize() < required.getCount()) {
                insufficientAmount = true;
                continue;
            }
            IAEItemStack extracted = Platform.poweredExtraction(terminal.guiObject, monitor, request.copy(), new PlayerSource(player, terminal.guiObject), Actionable.MODULATE);
            if (extracted != null && extracted.getStackSize() >= required.getCount()) {
                terminal.guiObject.saveChanges();
                return true;
            }
        }
        reportExtractionFailure(player, inaccessibleNetwork, storageMissing, insufficientAmount);
        return false;
    }

    public static boolean extractFluid(EntityPlayer player, FluidStack required) {
        if (required == null || required.amount <= 0) {
            return true;
        }
        IAEFluidStack request = AEFluidStack.fromFluidStack(required.copy());
        if (request == null) {
            return false;
        }
        request.setStackSize(required.amount);
        List<WirelessTerminalAccess> terminals = findWirelessTerminals(player);
        if (terminals.isEmpty()) {
            sendDiagnostic(player, "message.gctcore.mmce_builder.ae_no_terminal");
            return false;
        }
        boolean inaccessibleNetwork = false;
        boolean storageMissing = false;
        boolean insufficientAmount = false;
        for (WirelessTerminalAccess terminal : terminals) {
            IGridNode node = terminal.guiObject.getActionableNode();
            if (!isAccessible(player, node, SecurityPermissions.EXTRACT)) {
                inaccessibleNetwork = true;
                continue;
            }
            IGrid grid = node.getGrid();
            IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
            if (storageGrid == null) {
                storageMissing = true;
                continue;
            }
            IMEMonitor<IAEFluidStack> monitor = storageGrid.getInventory(AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class));
            IAEFluidStack simulated = Platform.poweredExtraction(terminal.guiObject, monitor, request.copy(), new PlayerSource(player, terminal.guiObject), Actionable.SIMULATE);
            if (simulated == null || simulated.getStackSize() < required.amount) {
                insufficientAmount = true;
                continue;
            }
            IAEFluidStack extracted = Platform.poweredExtraction(terminal.guiObject, monitor, request.copy(), new PlayerSource(player, terminal.guiObject), Actionable.MODULATE);
            if (extracted != null && extracted.getStackSize() >= required.amount) {
                terminal.guiObject.saveChanges();
                return true;
            }
        }
        return false;
    }

    public static ICraftingLink requestItemCraft(EntityPlayer player, ItemStack required) {
        if (required.isEmpty()) {
            return null;
        }
        IAEItemStack request = AEItemStack.fromItemStack(required);
        if (request == null) {
            return null;
        }
        request.setStackSize(required.getCount());
        return requestCraft(player, request);
    }

    public static ICraftingLink requestFluidCraft(EntityPlayer player, FluidStack required) {
        if (required == null || required.amount <= 0) {
            return null;
        }
        IAEItemStack request = FakeFluids.packFluid2AEDrops(required.copy());
        if (request == null) {
            return null;
        }
        request.setStackSize(required.amount);
        return requestCraft(player, request);
    }

    private static ICraftingLink requestCraft(EntityPlayer player, IAEItemStack request) {
        List<WirelessTerminalAccess> terminals = findWirelessTerminals(player);
        if (terminals.isEmpty()) {
            sendDiagnostic(player, "message.gctcore.mmce_builder.ae_no_terminal");
            return null;
        }
        boolean inaccessibleNetwork = false;
        boolean craftingMissing = false;
        boolean craftingUnavailable = false;
        for (WirelessTerminalAccess terminal : terminals) {
            IGridNode node = terminal.guiObject.getActionableNode();
            if (!isAccessible(player, node, SecurityPermissions.CRAFT)) {
                inaccessibleNetwork = true;
                continue;
            }
            IGrid grid = node.getGrid();
            ICraftingGrid craftingGrid = grid.getCache(ICraftingGrid.class);
            if (craftingGrid == null) {
                craftingMissing = true;
                continue;
            }
            Future<ICraftingJob> futureJob = null;
            try {
                PlayerSource source = new PlayerSource(player, terminal.guiObject);
                futureJob = craftingGrid.beginCraftingJob(player.world, grid, source, request.copy(), null);
                ICraftingJob job = futureJob.get(CRAFTING_JOB_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
                if (job == null || job.isSimulation()) {
                    craftingUnavailable = true;
                    continue;
                }
                ICraftingLink link = craftingGrid.submitJob(job, null, null, true, source);
                if (link != null) {
                    terminal.guiObject.saveChanges();
                    sendDiagnostic(player, "message.gctcore.mmce_builder.ae_craft_requested");
                    return link;
                }
                craftingUnavailable = true;
            } catch (TimeoutException e) {
                if (futureJob != null) {
                    futureJob.cancel(true);
                }
                craftingUnavailable = true;
            } catch (Exception e) {
                if (futureJob != null) {
                    futureJob.cancel(true);
                }
                craftingUnavailable = true;
            }
        }
        reportCraftingFailure(player, inaccessibleNetwork, craftingMissing, craftingUnavailable);
        return null;
    }

    private static List<WirelessTerminalAccess> findWirelessTerminals(EntityPlayer player) {
        List<WirelessTerminalAccess> terminals = new ArrayList<>();
        for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
            addWirelessTerminal(player, terminals, player.inventory.mainInventory.get(i), i, false);
        }
        for (int i = 0; i < player.inventory.armorInventory.size(); i++) {
            addWirelessTerminal(player, terminals, player.inventory.armorInventory.get(i), i, false);
        }
        for (int i = 0; i < player.inventory.offHandInventory.size(); i++) {
            addWirelessTerminal(player, terminals, player.inventory.offHandInventory.get(i), i, false);
        }
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        if (baubles != null) {
            for (int i = 0; i < baubles.getSlots(); i++) {
                addWirelessTerminal(player, terminals, baubles.getStackInSlot(i), i, true);
            }
        }
        return terminals;
    }

    private static void addWirelessTerminal(EntityPlayer player, List<WirelessTerminalAccess> terminals, ItemStack stack, int slot, boolean baubleSlot) {
        if (stack.isEmpty() || !AEApi.instance().registries().wireless().isWirelessTerminal(stack)) {
            return;
        }
        IWirelessTermHandler handler = AEApi.instance().registries().wireless().getWirelessTerminalHandler(stack);
        if (handler == null || !handler.canHandle(stack) || !handler.hasPower(player, 1.0D, stack)) {
            return;
        }
        WirelessTerminalGuiObject guiObject = new WirelessTerminalGuiObject(handler, stack, player, player.world, slot, baubleSlot ? 1 : 0, 0);
        if (guiObject.rangeCheck()) {
            terminals.add(new WirelessTerminalAccess(guiObject));
        }
    }

    private static boolean isAccessible(EntityPlayer player, IGridNode node, SecurityPermissions permission) {
        if (node == null || !node.isActive()) {
            return false;
        }
        IGrid grid = node.getGrid();
        ISecurityGrid securityGrid = grid.getCache(ISecurityGrid.class);
        return securityGrid == null || securityGrid.hasPermission(player, permission);
    }

    private static void reportExtractionFailure(EntityPlayer player, boolean inaccessibleNetwork, boolean storageMissing, boolean insufficientAmount) {
        if (inaccessibleNetwork) {
            sendDiagnostic(player, "message.gctcore.mmce_builder.ae_inaccessible");
        } else if (storageMissing) {
            sendDiagnostic(player, "message.gctcore.mmce_builder.ae_no_storage");
        } else if (insufficientAmount) {
            sendDiagnostic(player, "message.gctcore.mmce_builder.ae_missing");
        } else {
            sendDiagnostic(player, "message.gctcore.mmce_builder.ae_extract_failed");
        }
    }

    private static void reportCraftingFailure(EntityPlayer player, boolean inaccessibleNetwork, boolean craftingMissing, boolean craftingUnavailable) {
        if (inaccessibleNetwork) {
            sendDiagnostic(player, "message.gctcore.mmce_builder.ae_craft_inaccessible");
        } else if (craftingMissing) {
            sendDiagnostic(player, "message.gctcore.mmce_builder.ae_no_crafting");
        } else if (craftingUnavailable) {
            sendDiagnostic(player, "message.gctcore.mmce_builder.ae_uncraftable");
        } else {
            sendDiagnostic(player, "message.gctcore.mmce_builder.ae_craft_failed");
        }
    }

    private static void sendDiagnostic(EntityPlayer player, String key) {
        long now = player.world.getTotalWorldTime();
        if (now - player.getEntityData().getLong("gct_mmce_builder_last_ae_diag") < DIAGNOSTIC_INTERVAL_TICKS) {
            return;
        }
        player.getEntityData().setLong("gct_mmce_builder_last_ae_diag", now);
        MMCEBuilderUtils.sendTranslation(player, key);
    }

    private static final class WirelessTerminalAccess {
        private final WirelessTerminalGuiObject guiObject;

        private WirelessTerminalAccess(WirelessTerminalGuiObject guiObject) {
            this.guiObject = guiObject;
        }
    }
}
