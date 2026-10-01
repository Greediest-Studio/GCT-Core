package com.gmm.gctold.common.items;

import com.gmm.gctold.misc.GctAllCreativeTab;
import com.gmm.gctold.common.events.CommandDismantlerClick;
import com.google.common.collect.Multimap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemCommandDismantler extends Item {
    public static final Item block = new ItemCommandDismantler();

    public ItemCommandDismantler() {
        setMaxDamage(200);
        setMaxStackSize(1);
        setTranslationKey("command_dismantler");
        setRegistryName("command_dismantler");
        setCreativeTab(GctAllCreativeTab.TAB);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        CommandDismantlerClick.run(player, stack, world, pos.getX(), pos.getY(), pos.getZ());
        return EnumActionResult.SUCCESS;
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);
        if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", 0.0D, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", -3.0D, 0));
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
}
