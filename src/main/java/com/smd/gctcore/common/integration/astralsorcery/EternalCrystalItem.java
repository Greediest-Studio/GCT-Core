package com.smd.gctcore.common.integration.astralsorcery;

import hellfirepvp.astralsorcery.common.item.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCelestialCrystal;
import hellfirepvp.astralsorcery.common.item.crystal.base.ItemTunedCrystalBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.awt.Color;

/** The eternal tier of the untuned celestial crystal. */
public class EternalCrystalItem extends ItemCelestialCrystal {

    public static final int MAX_SIZE = 1600;
    public static final Color DISPLAY_COLOR = new Color(0x9DEBFF);

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        if (isInCreativeTab(tab)) {
            ItemStack stack = new ItemStack(this);
            CrystalProperties.applyCrystalProperties(stack, EternalCrystalProperties.max());
            subItems.add(stack);
        }
    }

    @Override
    public int getMaxSize(ItemStack stack) {
        return MAX_SIZE;
    }

    @Override
    public Color getHightlightColor(ItemStack stack) {
        return DISPLAY_COLOR;
    }

    @Override
    public ItemTunedCrystalBase getTunedItemVariant() {
        return AstralSorceryExpansion.TUNED_ETERNAL_CRYSTAL;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return AstralSorceryExpansion.ETERNAL_RARITY;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (CrystalProperties.getCrystalProperties(stack) == null) {
            CrystalProperties.applyCrystalProperties(stack, EternalCrystalProperties.random());
        }
        super.onUpdate(stack, world, entity, slot, selected);
    }
}
