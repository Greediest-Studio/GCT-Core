package com.smd.gctcore.common.tile.botania;

import com.smd.gctcore.common.blocks.botania.BlockGctManaSpreader;
import com.smd.gctcore.common.botania.GctManaPoolTier;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.client.core.handler.HUDHandler;
import vazkii.botania.common.block.tile.mana.TileSpreader;
import vazkii.botania.common.entity.EntityManaBurst;

import javax.annotation.Nonnull;

public class TileGctManaSpreader extends TileSpreader {

    private static final int BURST_DELAY_TICKS = 20;
    private static final float BURST_ACTIVE_TICKS = 140.0F;

    @Override
    public boolean isRedstone() {
        return false;
    }

    @Override
    public boolean isDreamwood() {
        return false;
    }

    @Override
    public boolean isULTRA_SPREADER() {
        return false;
    }

    @Override
    public int getMaxMana() {
        return getTier().getSpreaderCapacity();
    }

    @Override
    public EntityManaBurst getBurst(boolean fake) {
        EntityManaBurst burst = super.getBurst(fake);
        if (burst == null) {
            return null;
        }

        GctManaPoolTier tier = getTier();
        int adjustedMana = Math.max(1, Math.round(
                burst.getStartingMana() * (tier.getBurstMana() / 160.0F)));
        if (!fake && getCurrentMana() < adjustedMana) {
            return null;
        }
        burst.setMana(adjustedMana);
        burst.setStartingMana(adjustedMana);
        // Use the same average texture colour as this tier's spark and shell.
        burst.setColor(tier.getColor());

        // Keep every added tier alive for the same 140-tick active window after
        // its initial 20-tick delay.  This places Niflheim above Gaia's range
        // and lets the remaining tiers continue increasing with their speed.
        burst.setMinManaLoss(BURST_DELAY_TICKS);
        burst.setManaLossPerTick(Math.max(1.0F, adjustedMana / BURST_ACTIVE_TICKS));

        // EntityManaBurst starts at 0.4 block/tick.  Keep lens modifiers intact
        // while applying this tier's absolute speed to the burst.
        double velocityScale = tier.getBurstVelocity() / 0.4D;
        burst.setMotion(burst.motionX * velocityScale,
                burst.motionY * velocityScale,
                burst.motionZ * velocityScale);
        return burst;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, @Nonnull IBlockState oldState,
                                 @Nonnull IBlockState newState) {
        if (oldState.getBlock() != newState.getBlock()) {
            return true;
        }
        return oldState.getBlock() instanceof BlockGctManaSpreader
                && oldState.getValue(BlockGctManaSpreader.TIER)
                != newState.getValue(BlockGctManaSpreader.TIER);
    }

    public GctManaPoolTier getTier() {
        if (world != null) {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BlockGctManaSpreader) {
                return state.getValue(BlockGctManaSpreader.TIER);
            }
        }
        return GctManaPoolTier.JOETUNHEIM;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUD(Minecraft mc, ScaledResolution resolution) {
        GctManaPoolTier tier = getTier();
        ItemStack stack = new ItemStack(world.getBlockState(pos).getBlock(), 1, tier.ordinal());
        HUDHandler.drawSimpleManaHUD(tier.getColor(), getCurrentMana(), getMaxMana(),
                stack.getDisplayName(), resolution);
    }
}
