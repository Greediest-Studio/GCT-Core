package com.smd.gctcore.common.tile.blood_altar;

import com.smd.gctcore.Tags;
import com.smd.gctcore.gctcore;
import com.smd.gctcore.proxy.CommonProxy;
import hellfirepvp.modularmachinery.common.block.BlockFactoryController;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The dedicated factory controller for the Blood Altar machine.
 *
 * <p>The parent machine is intentionally resolved from MMCE's registry instead
 * of being retained during block registration. MMCE finishes loading machines
 * after Forge registers blocks.</p>
 */
public class BlockBloodAltarController extends BlockFactoryController {

    public BlockBloodAltarController() {
        setHardness(5.0F);
        setResistance(10.0F);
        setRegistryName(new ResourceLocation(Tags.MOD_ID, "blood_altar"));
        setTranslationKey(Tags.MOD_ID + ".blood_altar");
    }

    @Override
    public DynamicMachine getParentMachine() {
        return BloodAltarMachine.getRegisteredMachine();
    }

    /** MMCE's base implementation reads its eagerly populated parent field.
     * This controller resolves the JSON machine lazily, so provide the name
     * directly rather than falling back to the generic factory-controller key.
     */
    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public String getLocalizedName() {
        return I18n.format("tile.gctcore.blood_altar.name");
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final World world, final IBlockState state) {
        return new BloodAltarFactoryController(state);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(final World world, final int meta) {
        return new BloodAltarFactoryController(getStateFromMeta(meta));
    }

    /**
     * MMCE normally assigns this in ItemBlockController.  Keep the block-side
     * fallback as well so controllers placed by a generic ItemBlock, including
     * controllers from the prior build, are never left without an owner.
     */
    @Override
    public void onBlockPlacedBy(final World world, final BlockPos pos, final IBlockState state,
                                final EntityLivingBase placer, final ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (!world.isRemote && placer instanceof EntityPlayer) {
            claimIfUnowned(world.getTileEntity(pos), (EntityPlayer) placer);
        }
    }

    @Override
    public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state,
                                    final EntityPlayer player, final EnumHand hand, final EnumFacing facing,
                                    final float hitX, final float hitY, final float hitZ) {
        if (!world.isRemote) {
            // Upgrade an already placed controller from the previous generic
            // ItemBlock registration when its owner next opens the GUI.
            claimIfUnowned(world.getTileEntity(pos), player);
            player.openGui(gctcore.INSTANCE, CommonProxy.BLOOD_ALTAR_GUI_ID,
                    world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    private static void claimIfUnowned(final TileEntity tile, final EntityPlayer player) {
        if (!(tile instanceof BloodAltarFactoryController) || player == null) {
            return;
        }

        final BloodAltarFactoryController controller = (BloodAltarFactoryController) tile;
        if (controller.getOwner() != null) {
            return;
        }

        controller.setOwner(player.getGameProfile().getId());
        controller.markDirty();
        controller.markForUpdateSync();
    }
}
