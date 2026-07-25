package com.smd.gctcore.common.mixin.dungeonsmod;

import com.dainxt.dungeonsmod.entity.boss.EntityIronSlime;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityIronSlime.class)
public abstract class MixinEntityIronSlime extends EntityMob {

    @Unique
    private static final ResourceLocation GCTCORE$TCONSTRUCT_PICKAXE = new ResourceLocation("tconstruct", "pickaxe");

    public MixinEntityIronSlime(World worldIn) {
        super(worldIn);
    }

    @Shadow(remap = false)
    public abstract int getPhase();

    @Shadow(remap = false)
    public abstract void setLastMinedTick(int ticks);

    @Shadow(remap = false)
    public abstract int getShellIntegrity();

    @Shadow(remap = false)
    public abstract void setShellIntegrity(int integrity);

    @Inject(method = "attackEntityFrom", at = @At("HEAD"), cancellable = true)
    private void gctcore$allowTConstructPickaxe(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity trueSource = source.getTrueSource();
        if (!(trueSource instanceof EntityPlayer) || this.getPhase() < 2) {
            return;
        }

        EntityPlayer player = (EntityPlayer) trueSource;
        ItemStack heldItem = player.getHeldItemMainhand();
        Item item = heldItem.getItem();
        if (!GCTCORE$TCONSTRUCT_PICKAXE.equals(item.getRegistryName())) {
            return;
        }

        if (!this.world.isRemote && player.getCooldownTracker().getCooldown(item, 1.0F) == 0.0F) {
            player.getCooldownTracker().setCooldown(item, 10);
            this.setLastMinedTick(this.ticksExisted);
            int damage = this.isBurning() ? 2 : 1;
            this.setShellIntegrity(this.getShellIntegrity() - damage);
            cir.setReturnValue(true);
            return;
        }

        if (player.getCooldownTracker().getCooldown(item, 1.0F) != 0.0F) {
            this.playSound(SoundEvents.ENTITY_SLIME_HURT, 1.0F, 1.0F);
            cir.setReturnValue(true);
            return;
        }

        cir.setReturnValue(false);
    }
}
