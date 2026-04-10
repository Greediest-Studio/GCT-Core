package com.smd.gctcore.common.mixin.thaumicadditions;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(targets = {"org.zeith.thaumicadditions.tiles.jars.TileMithminiteJar"}, remap = false)
public abstract class MixinTileMithminiteJar
{
    @Shadow
    public int amount;

    @Shadow
    public Object aspect;

    @Shadow
    public World world;

    @Shadow
    public abstract int getCapacity();

    @Shadow
    public abstract World getWorld();

    @Shadow
    public abstract BlockPos getPos();

    @Shadow
    public abstract void markDirty();

    @Shadow
    public abstract void syncTile(boolean full);

    /**
     * Overwrite addToContainer to adjust mithminite jar behavior.
     * This javadoc is intentionally present to satisfy Mixin processor requirements.
     *
     * @author gctcore
     * @reason Adjust mithminite jar filling behavior to prevent issues and add overfill handling
     */
    @Overwrite
    public int addToContainer(Object tt, int am)
    {
        if(am == 0)
            return am;

        boolean wasBelowCap = this.amount < this.getCapacity();

        if(this.aspect == null)
        {
            this.aspect = tt;
        }

        if(tt == this.aspect || this.amount == 0)
        {
            int added = Math.min(am, this.getCapacity() - this.amount);
            this.amount += added;
            am -= added;

            int overfill = this.amount - 4000;
            if(overfill > 0)
            {
                try
                {
                    if(this.world.rand.nextInt(250 - overfill) == 0)
                        net.minecraftforge.fml.common.FMLCommonHandler.instance();
                }
                catch(Throwable ignored) {}
                this.amount -= overfill;
                try
                {
                    Class.forName("thaumcraft.api.aura.AuraHelper").getMethod("polluteAura", net.minecraft.world.World.class, net.minecraft.util.math.BlockPos.class, float.class, boolean.class)
                        .invoke(null, this.getWorld(), this.getPos(), 1F, true);
                }
                catch(Throwable ignored) {}
            }
        }

        if(wasBelowCap)
        {
            try { this.getClass().getMethod("syncTile", boolean.class).invoke(this, false); } catch(Throwable ignored) {}
            try { this.getClass().getMethod("markDirty").invoke(this); } catch(Throwable ignored) {}
        }

        return am;
    }
}
