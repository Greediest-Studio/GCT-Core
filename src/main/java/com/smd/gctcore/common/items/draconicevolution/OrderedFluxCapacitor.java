package com.smd.gctcore.common.items.draconicevolution;

import com.brandon3055.brandonscore.items.ItemEnergyBase;
import com.brandon3055.brandonscore.lib.ChatHelper;
import com.brandon3055.brandonscore.lib.EnergyHelper;
import com.brandon3055.brandonscore.utils.InfoHelper;
import com.brandon3055.brandonscore.utils.ItemNBTHelper;
import com.brandon3055.draconicevolution.api.IInvCharge;
import com.brandon3055.draconicevolution.api.itemupgrade.IUpgradableItem;
import com.brandon3055.draconicevolution.api.itemupgrade.UpgradeHelper;
import com.brandon3055.draconicevolution.entity.EntityPersistentItem;
import com.brandon3055.draconicevolution.items.ToolUpgrade;
import com.brandon3055.draconicevolution.integration.BaublesHelper;
import com.brandon3055.draconicevolution.integration.ModHelper;
import baubles.api.IBauble;
import baubles.api.BaubleType;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * Ordered 级通量容器。
 * - 基础容量 4,096,000,000 RF
 * - 传输速率 2,048,000,000 RF/t
 * - 支持 RF_CAPACITY 升级，最多 4 级，按公式：base + upgrade * (base / 2)
 */
@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")
public class OrderedFluxCapacitor extends ItemEnergyBase implements IInvCharge, IUpgradableItem, IBauble {

    public static final int TRANSFER = 2_048_000_000; // 对外 RF 单次传输上限（int 兼容）
    public static final long BASE_CAPACITY = 4_096_000_000L; // 内部长整形容量
    private static final String NBT_ENERGY_L = "EnergyL";

    public OrderedFluxCapacitor() {
        setHasSubtypes(false);
        setMaxStackSize(1);
        setTranslationKey("gctcore.ordered_flux_capacitor");
        setRegistryName("ordered_flux_capacitor");
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public void getSubItems(CreativeTabs tab, net.minecraft.util.NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) return;
        items.add(new ItemStack(this));
        ItemStack charged = new ItemStack(this);
        setEnergyStoredLong(charged, getCapacityLong(charged));
        items.add(charged);
    }

    //region Item entity persistence
    @Override
    public boolean hasCustomEntity(ItemStack stack) { return true; }

    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        return new EntityPersistentItem(world, location, itemstack);
    }
    //endregion

    //region Energy (long 内部 + RF 桥接)
    @Override
    public int getCapacity(ItemStack stack) {
        long capL = getCapacityLong(stack);
        return capL > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) capL;
    }

    private long getCapacityLong(ItemStack stack) {
        int upgrade = UpgradeHelper.getUpgradeLevel(stack, ToolUpgrade.RF_CAPACITY);
        long inc = (BASE_CAPACITY / 2L) * upgrade;
        return BASE_CAPACITY + inc;
    }

    @Override
    public int getMaxReceive(ItemStack stack) { return TRANSFER; }

    @Override
    public int getMaxExtract(ItemStack stack) { return TRANSFER; }

    @Override
    public int getEnergyStored(ItemStack container) {
        long energy = getEnergyStoredLong(container);
        return energy > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) energy;
    }

    public long getEnergyStoredLong(ItemStack stack) {
        long energy = ItemNBTHelper.getLong(stack, NBT_ENERGY_L, 0L);
        long cap = getCapacityLong(stack);
        if (energy < 0) energy = 0;
        if (energy > cap) energy = cap;
        return energy;
    }

    private void setEnergyStoredLong(ItemStack stack, long value) {
        long cap = getCapacityLong(stack);
        if (value < 0) value = 0;
        if (value > cap) value = cap;
        ItemNBTHelper.setLong(stack, NBT_ENERGY_L, value);
    }

    @Override
    public int receiveEnergy(ItemStack stack, int maxReceive, boolean simulate) {
        if (maxReceive <= 0) return 0;
        long energy = getEnergyStoredLong(stack);
        long cap = getCapacityLong(stack);
        long canReceive = Math.min(Integer.toUnsignedLong(Math.max(0, maxReceive)), TRANSFER);
        long space = cap - energy;
        long toReceive = Math.min(canReceive, space);
        if (toReceive <= 0) return 0;
        if (!simulate) setEnergyStoredLong(stack, energy + toReceive);
        return (int) toReceive;
    }

    @Override
    public int extractEnergy(ItemStack stack, int maxExtract, boolean simulate) {
        if (maxExtract <= 0) return 0;
        long energy = getEnergyStoredLong(stack);
        long canExtract = Math.min(Integer.toUnsignedLong(Math.max(0, maxExtract)), TRANSFER);
        long toExtract = Math.min(canExtract, energy);
        if (toExtract <= 0) return 0;
        if (!simulate) setEnergyStoredLong(stack, energy - toExtract);
        return (int) toExtract;
    }
    //endregion

    //region Activation & update
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack;
        if (hand == EnumHand.MAIN_HAND) {
            int idx = player.inventory.currentItem;
            if (idx >= 0 && idx < player.inventory.mainInventory.size()) {
                stack = player.inventory.getStackInSlot(idx);
            } else {
                stack = ItemStack.EMPTY;
            }
        } else {
            stack = player.inventory.offHandInventory.isEmpty() ? ItemStack.EMPTY : player.inventory.offHandInventory.get(0);
        }

        boolean sneaking = false;
        try {
            sneaking = player.isSneaking();
        } catch (NoSuchMethodError err) {
            sneaking = world.isRemote && isClientSneakKeyDown();
        }
        if (sneaking) {
            int mode = ItemNBTHelper.getShort(stack, "Mode", (short) 0);
            int newMode = mode == 4 ? 0 : mode + 1;
            ItemNBTHelper.setShort(stack, "Mode", (short) newMode);
            if (world.isRemote) {
                ChatHelper.indexedMsg(player, InfoHelper.ITC() + I18n.format("info.de.capacitorMode.txt") + ": " + InfoHelper.HITC() + I18n.format("info.de.capacitorMode" + ItemNBTHelper.getShort(stack, "Mode", (short) 0) + ".txt"));
            }
        }
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    @Override
    public void onUpdate(ItemStack container, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (!(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entity;
        List<ItemStack> baubleStacks = ModHelper.isBaublesInstalled ? getBaubles(player) : new ArrayList<>();
        updateEnergy(container, player, baubleStacks);
    }

    private void updateEnergy(ItemStack capacitor, EntityPlayer player, List<ItemStack> stacks) {
        int mode = ItemNBTHelper.getShort(capacitor, "Mode", (short) 0);
        if (mode == 0) return;

        if (mode == 4) { // 全部
            stacks.addAll(player.inventory.armorInventory);
            stacks.addAll(player.inventory.mainInventory);
            stacks.addAll(player.inventory.offHandInventory);
        } else {
            if (mode == 1 || mode == 3) { // 护甲
                stacks.addAll(player.inventory.armorInventory);
            } else {
                stacks.clear(); // 不给 baubles 充电
            }
            if (mode == 2 || mode == 3) { // 手持
                ItemStack off = player.inventory.offHandInventory.isEmpty() ? ItemStack.EMPTY : player.inventory.offHandInventory.get(0);
                if (off != null && !off.isEmpty()) stacks.add(off);
                int idx = player.inventory.currentItem;
                if (idx >= 0 && idx < player.inventory.mainInventory.size()) {
                    ItemStack main = player.inventory.getStackInSlot(idx);
                    if (main != null && !main.isEmpty()) stacks.add(main);
                }
            }
        }

        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) continue;
            int max = Math.min(getEnergyStored(capacitor), getMaxExtract(capacitor));
            if (max <= 0) break;

            if (EnergyHelper.canReceiveEnergy(stack)) {
                Item item = stack.getItem();
                if (item instanceof IInvCharge && !((IInvCharge) item).canCharge(stack, player)) continue;
                int sent = EnergyHelper.insertEnergy(stack, max, false);
                if (sent > 0) extractEnergy(capacitor, sent, false);
            }
        }
    }

    @Override
    public boolean canCharge(ItemStack stack, EntityPlayer player) {
        return false;
    }

    //endregion

    //region Display
    @Override
    public boolean hasEffect(ItemStack stack) {
        return ItemNBTHelper.getShort(stack, "Mode", (short) 0) > 0;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> tooltip, ITooltipFlag advanced) {
        if (InfoHelper.holdShiftForDetails(tooltip)) {
            tooltip.add(I18n.format("info.de.changwMode.txt"));
            tooltip.add(InfoHelper.ITC() + I18n.format("info.de.capacitorMode.txt") + ": " + InfoHelper.HITC() + I18n.format("info.de.capacitorMode" + ItemNBTHelper.getShort(stack, "Mode", (short) 0) + ".txt"));
        }
        com.brandon3055.draconicevolution.items.tools.ToolBase.holdCTRLForUpgrades(tooltip, stack);
        long energy = getEnergyStoredLong(stack);
        long cap = getCapacityLong(stack);
        tooltip.add(formatEnergyLine(energy, cap));
    }

    private static String formatEnergyLine(long energy, long capacity) {
        return InfoHelper.ITC() + I18n.format("info.gct.energy") + ": " + InfoHelper.HITC() +
                formatWithUnit(energy) + " / " + formatWithUnit(capacity) + " RF";
    }

    private static String formatWithUnit(long value) {
        final long K = 1_000L;
        final long M = 1_000_000L;
        final long G = 1_000_000_000L;
        final long T = 1_000_000_000_000L;
        String unit; double num;
        if (value >= T) { unit = "T"; num = value / 1_000_000_000_000d; }
        else if (value >= G) { unit = "G"; num = value / 1_000_000_000d; }
        else if (value >= M) { unit = "M"; num = value / 1_000_000d; }
        else if (value >= K) { unit = "K"; num = value / 1_000d; }
        else { return Long.toString(value); }
        String s = String.format(java.util.Locale.ROOT, "%.2f", num);
        while (s.contains(".") && (s.endsWith("0") || s.endsWith("."))) { s = s.substring(0, s.length() - 1); }
        return s + unit;
    }
    //endregion

    //region IUpgradableItem
    @Override
    public List<String> getValidUpgrades(ItemStack stack) {
        return new ArrayList<String>() {{ add(ToolUpgrade.RF_CAPACITY); }};
    }

    @Override
    public int getMaxUpgradeLevel(ItemStack stack, String upgrade) { return ToolUpgrade.RF_CAPACITY.equals(upgrade) ? 4 : 0; }
    //endregion

    //region Baubles
    @Override
    @Optional.Method(modid = "baubles")
    public BaubleType getBaubleType(ItemStack itemstack) { return BaubleType.TRINKET; }

    @Override
    @Optional.Method(modid = "baubles")
    public void onWornTick(ItemStack itemstack, net.minecraft.entity.EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entity;
        updateEnergy(itemstack, player, getBaubles(player));
    }

    private static List<ItemStack> getBaubles(EntityPlayer player) { return BaublesHelper.getBaubles(player); }
    //endregion

    @SideOnly(Side.CLIENT)
    private static boolean isClientSneakKeyDown() {
        return net.minecraft.client.Minecraft.getMinecraft().gameSettings.keyBindSneak.isKeyDown();
    }
}
