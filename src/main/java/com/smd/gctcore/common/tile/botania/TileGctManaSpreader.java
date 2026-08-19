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

        int adjustedMana = Math.max(1, Math.round(
                burst.getStartingMana() * (getTier().getBurstMana() / 160.0F)));
        if (!fake && getCurrentMana() < adjustedMana) {
            return null;
        }
        burst.setMana(adjustedMana);
        burst.setStartingMana(adjustedMana);

        // EntityManaBurst starts at 0.4 block/tick.  Keep lens modifiers intact
        // while applying this tier's absolute speed to the burst.
        double velocityScale = getTier().getBurstVelocity() / 0.4D;
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
        int color = tier == GctManaPoolTier.JOETUNHEIM ? 0xD58A25
                : tier == GctManaPoolTier.NIDAVELLIR ? 0x86BCC7 : 0xC7A96A;
        HUDHandler.drawSimpleManaHUD(color, getCurrentMana(), getMaxMana(), stack.getDisplayName(), resolution);
    }
}
