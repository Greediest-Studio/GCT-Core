package com.smd.gctcore.common.integration.astralsorcery;

import hellfirepvp.astralsorcery.common.item.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.item.crystal.ItemTunedCelestialCrystal;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.awt.Color;

/** Tuned counterpart of the eternal crystal, retaining celestial constellation behavior. */
public class EternalTunedCrystalItem extends ItemTunedCelestialCrystal {

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        if (isInCreativeTab(tab)) {
            super.getSubItems(tab, subItems);
            // The superclass creates celestial-sized stacks. Replace their properties with the eternal cap.
            for (ItemStack stack : subItems) {
                if (stack.getItem() == this) {
                    CrystalProperties.applyCrystalProperties(stack, EternalCrystalProperties.max());
                }
            }
        }
    }

    @Override
    public int getMaxSize(ItemStack stack) {
        return EternalCrystalItem.MAX_SIZE;
    }

    @Override
    public Color getHightlightColor(ItemStack stack) {
        return EternalCrystalItem.DISPLAY_COLOR;
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
