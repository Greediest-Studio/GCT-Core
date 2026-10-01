package com.gmm.gctold.common.items;

import com.gmm.gctold.misc.GctAllCreativeTab;
import com.google.common.collect.Multimap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemEarthOrb extends Item {
    public static final Item block = new ItemEarthOrb();

    public ItemEarthOrb() {
        setMaxDamage(16);
        setMaxStackSize(1);
        setTranslationKey("earth_orb");
        setRegistryName("earth_orb");
        setCreativeTab(GctAllCreativeTab.TAB);
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);
        if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", -4.0D, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", -4.0D, 0));
        }
        return multimap;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        return 0.0F;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, IBlockState state, BlockPos pos, EntityLivingBase entity) {
        stack.damageItem(0, entity);
        return true;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        stack.damageItem(0, attacker);
        return true;
    }

    @Override
    public boolean isFull3D() {
        return true;
    }

    @Override
    public int getItemEnchantability() {
        return 2;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}
