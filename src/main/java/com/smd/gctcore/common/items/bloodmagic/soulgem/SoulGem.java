package com.smd.gctcore.common.items.bloodmagic.soulgem;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.items.bloodmagic.MeshDefinition.CustomMeshDefinitionSoulGem;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import WayofTime.bloodmagic.soul.EnumDemonWillType;
import WayofTime.bloodmagic.soul.IDemonWill;
import WayofTime.bloodmagic.soul.PlayerDemonWillHandler;
import WayofTime.bloodmagic.util.Constants;
import WayofTime.bloodmagic.util.helper.NBTHelper;
import WayofTime.bloodmagic.util.helper.TextHelper;
import WayofTime.bloodmagic.client.IMeshProvider;
import WayofTime.bloodmagic.iface.IMultiWillTool;
import WayofTime.bloodmagic.soul.IDemonWillGem;
import WayofTime.bloodmagic.BloodMagic;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;


public  class SoulGem extends Item implements IDemonWillGem, IMeshProvider,IMultiWillTool{

    public static String[] names = {
            "improved",
            "concentra",
            "pure",
            "extreme",
            "origin",
            "primordial",
            "eternal",
            "infinite",
            "realInfinite"
    };
    //数量
    private static final int[] capacities = {
            65536,
            262144,
            1058576,
            4194304,
            16777216,
            67108864,
            268435456,
            1073741824,
            2147483647
    };

    public SoulGem() {
        super();

        setRegistryName("soul_gem");
        setTranslationKey(Tags.MOD_ID + ".ItemSoulGem.");
        setHasSubtypes(true);
        setMaxStackSize(1);
        setCreativeTab(BloodMagic.TAB_BM);
    }

    @Override
    public ResourceLocation getCustomLocation() {
        return new ResourceLocation(Tags.MOD_ID, "bloodmagic/soul_gem");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemMeshDefinition getMeshDefinition() {
        return new CustomMeshDefinitionSoulGem();
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey(stack) + names[stack.getItemDamage()];
    }

    @Override
    public EnumDemonWillType getCurrentType(ItemStack stack) {
        NBTHelper.checkNBT(stack);
        NBTTagCompound tag = stack.getTagCompound();

        if (!tag.hasKey(Constants.NBT.WILL_TYPE)) {
            return EnumDemonWillType.DEFAULT;
        }
        return EnumDemonWillType.valueOf(tag.getString(Constants.NBT.WILL_TYPE).toUpperCase(Locale.ENGLISH));
    }

    //其他意志类型
    @Override
    public void gatherVariants(Consumer<String> variants) {
        for (EnumDemonWillType type : EnumDemonWillType.values()) {
            for (String name : names) {
                variants.accept("type=" + name.toLowerCase() + "_" + type.getName().toLowerCase());
            }
        }
    }

    @Override
    public ItemStack fillDemonWillGem(ItemStack willGemStack, ItemStack willStack) {
        if (willStack != null && willStack.getItem() instanceof IDemonWill) {
            EnumDemonWillType thisType = this.getCurrentType(willGemStack);
            if (thisType != ((IDemonWill) willStack.getItem()).getType(willStack)) {
                return willStack;
            }
            IDemonWill soul = (IDemonWill) willStack.getItem();
            double soulsLeft = getWill(thisType, willGemStack);

            if (soulsLeft < getMaxWill(thisType, willGemStack)) {
                double newSoulsLeft = Math.min(soulsLeft + soul.getWill(thisType, willStack), getMaxWill(thisType, willGemStack));
                soul.drainWill(thisType, willStack, newSoulsLeft - soulsLeft);

                setWill(thisType, willGemStack, newSoulsLeft);
                if (soul.getWill(thisType, willStack) <= 0) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return willStack;
    }

    @Override
    public double getWill(EnumDemonWillType type, ItemStack willGemStack) {
        if (!type.equals(getCurrentType(willGemStack))) {
            return 0;
        }
        NBTTagCompound tag = willGemStack.getTagCompound();
        return tag.getDouble(Constants.NBT.SOULS);
    }

    @Override
    public void setWill(EnumDemonWillType type, ItemStack willGemStack, double amount) {
        setCurrentType(type, willGemStack);
        NBTTagCompound tag = willGemStack.getTagCompound();
        tag.setDouble(Constants.NBT.SOULS, amount);
    }

    @Override
    public int getMaxWill(EnumDemonWillType type, ItemStack willGemStack) {
        EnumDemonWillType currentType = getCurrentType(willGemStack);
        if (!type.equals(currentType) && currentType != EnumDemonWillType.DEFAULT) {
            return 0;
        }

        int metadata = willGemStack.getItemDamage();
        if (metadata >= 0 && metadata < capacities.length) {
            return capacities[metadata];
        }
        return 65536;
    }

    @Override
    public double drainWill(EnumDemonWillType type, ItemStack stack, double drainAmount, boolean doDrain) {
        EnumDemonWillType currentType = this.getCurrentType(stack);
        if (currentType != type) {
            return 0;
        }
        double souls = getWill(type, stack);
        double soulsDrained = Math.min(drainAmount, souls);

        if (doDrain) {
            setWill(type, stack, souls - soulsDrained);
        }
        return soulsDrained;
    }

    @Override
    public double fillWill(EnumDemonWillType type, ItemStack stack, double fillAmount, boolean doFill) {
        if (!type.equals(getCurrentType(stack)) && this.getWill(getCurrentType(stack), stack) > 0) {
            return 0;
        }
        double current = this.getWill(type, stack);
        double maxWill = this.getMaxWill(type, stack);
        double filled = Math.min(fillAmount, maxWill - current);

        if (doFill) {
            this.setWill(type, stack, filled + current);
        }
        return filled;
    }

    public void setCurrentType(EnumDemonWillType type, ItemStack stack) {
        NBTHelper.checkNBT(stack);
        NBTTagCompound tag = stack.getTagCompound();

        if (type == EnumDemonWillType.DEFAULT) {
            if (tag.hasKey(Constants.NBT.WILL_TYPE)) {
                tag.removeTag(Constants.NBT.WILL_TYPE);
            }
            return;
        }
        tag.setString(Constants.NBT.WILL_TYPE, type.toString());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        EnumDemonWillType type = this.getCurrentType(stack);
        double drain = Math.min(this.getWill(type, stack), this.getMaxWill(type, stack) / 10);

        double filled = PlayerDemonWillHandler.addDemonWill(type, player, drain, stack);
        this.drainWill(type, stack, filled, true);

        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> list) {
        if (!isInCreativeTab(creativeTab))
            return;

        for (int i = 0; i < names.length; i++) {
            ItemStack emptyStack = new ItemStack(this, 1, i);
            list.add(emptyStack);
        }
        for (EnumDemonWillType type : EnumDemonWillType.values()) {
            for (int i = 0; i < names.length; i++) {
                ItemStack fullStack = new ItemStack(this, 1, i);
                setWill(type, fullStack, getMaxWill(EnumDemonWillType.DEFAULT, fullStack));
                list.add(fullStack);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        if (!stack.hasTagCompound())
            return;

        EnumDemonWillType type = this.getCurrentType(stack);
        tooltip.add(TextHelper.localize("tooltip.bloodmagic.soulgem." + names[stack.getItemDamage()]));
        tooltip.add(TextHelper.localize("tooltip.bloodmagic.will", getWill(type, stack)));
        tooltip.add(TextHelper.localizeEffect("tooltip.bloodmagic.currentType." + getCurrentType(stack).getName().toLowerCase()));

        super.addInformation(stack, world, tooltip, flag);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        EnumDemonWillType type = this.getCurrentType(stack);
        double maxWill = getMaxWill(type, stack);
        if (maxWill <= 0) {
            return 1;
        }
        return 1.0 - (getWill(type, stack) / maxWill);
    }
}