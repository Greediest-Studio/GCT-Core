package com.smd.gctcore.common.mixin.extrabotany;


import com.meteor.extrabotany.common.brew.ModPotions;
import com.meteor.extrabotany.common.item.equipment.bauble.ItemBaubleRelic;
import com.meteor.extrabotany.common.item.equipment.bauble.ItemFrostStar;
import com.meteor.extrabotany.common.item.equipment.bauble.ItemSilentEternity;
import net.minecraft.enchantment.EnchantmentFrostWalker;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ItemSilentEternity.class)
public abstract class MixinItemSilentEternity extends ItemBaubleRelic {

    public MixinItemSilentEternity(String name) {
        super(name);
    }

    @Shadow(remap = false)
    public double getX(ItemStack stack) {return 0;}
    @Shadow(remap = false)
    public double getY(ItemStack stack) {return 0;}
    @Shadow(remap = false)
    public double getZ(ItemStack stack) {return 0;}
    @Shadow(remap = false)
    public void setX(ItemStack stack, double d) {}
    @Shadow(remap = false)
    public void setY(ItemStack stack, double d) {}
    @Shadow(remap = false)
    public void setZ(ItemStack stack, double d) {}
    @Shadow(remap = false)
    public void addMana(ItemStack stack, int mana) {}
    @Shadow(remap = false)
    public int getStopticks(ItemStack stack) {return 0;}
    @Shadow(remap = false)
    public void setStopticks(ItemStack stack, int i) {}

    /**
     * @author Gct-Core
     * @reason 可能修了假死
     */
    @Overwrite(remap = false)
    public void onWornTick(ItemStack stack, EntityLivingBase entity) {
        super.onWornTick(stack, entity);
        if(!(entity instanceof EntityPlayer))
            return;
        EntityPlayer player = (EntityPlayer) entity;
        addMana(stack, 666);
        if(!entity.world.isRemote) {
            boolean lastOnGround = entity.onGround;
            entity.onGround = true;
            EnchantmentFrostWalker.freezeNearby(entity, entity.world, new BlockPos(entity), 4);
            ItemFrostStar.freezeLava(entity, entity.world, new BlockPos(entity), 4);
            entity.onGround = lastOnGround;
            if(getX(stack) == player.posX && getY(stack) == player.posY && getZ(stack) == player.posZ) {
                setStopticks(stack, getStopticks(stack)+1);
                if(getStopticks(stack) > 15) {
                    if (!player.isDead && player.getHealth() > 1F) {
                        player.setHealth(Math.min(player.getMaxHealth(), player.getHealth()+0.4F));
                        player.addPotionEffect(new PotionEffect(ModPotions.eternity, 10));
                    }
                }
            }else
                setStopticks(stack, 0);
            setX(stack,player.lastTickPosX);
            setY(stack,player.lastTickPosY);
            setZ(stack,player.lastTickPosZ);
        }
    }
}
