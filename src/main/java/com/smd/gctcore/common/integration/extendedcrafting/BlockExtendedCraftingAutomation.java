package com.smd.gctcore.common.integration.extendedcrafting;

import com.smd.gctcore.Tags;
import com.smd.gctcore.gctcore;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class BlockExtendedCraftingAutomation extends Block implements ITileEntityProvider {
    public enum Kind { ASSEMBLER, INTERFACE, PATTERN_TERMINAL }

    private final Kind kind;
    private final ExtendedCraftingTier tier;

    public BlockExtendedCraftingAutomation(Kind kind, ExtendedCraftingTier tier) {
        super(Material.IRON);
        this.kind = kind;
        this.tier = tier;
        String name = tier.id() + "_extended_" + kind.name().toLowerCase();
        setRegistryName(Tags.MOD_ID, name);
        setTranslationKey(Tags.MOD_ID + "." + name);
        setCreativeTab(CreativeTabs.REDSTONE);
        setHardness(2.5F);
        setResistance(10.0F);
        if (kind == Kind.ASSEMBLER) setLightOpacity(1);
    }

    public Kind kind() { return kind; }
    public ExtendedCraftingTier tier() { return tier; }

    @Override
    public boolean isFullCube(IBlockState state) {
        return kind != Kind.ASSEMBLER;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return kind != Kind.ASSEMBLER;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return kind == Kind.ASSEMBLER ? BlockRenderLayer.CUTOUT : BlockRenderLayer.SOLID;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == getRenderLayer();
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        switch (kind) {
            case ASSEMBLER: return new TileExtendedMolecularAssembler(tier);
            case INTERFACE: return new TileExtendedInterface(tier);
            case PATTERN_TERMINAL: return new TileExtendedPatternTerminal(tier);
            default: return null;
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            int id = kind == Kind.ASSEMBLER ? ExtendedCraftingGuiHandler.assemblerId(tier)
                    : kind == Kind.INTERFACE ? ExtendedCraftingGuiHandler.interfaceId(tier)
                    : ExtendedCraftingGuiHandler.terminalId(tier);
            player.openGui(gctcore.INSTANCE, id, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileExtendedInterface) ((TileExtendedInterface) tile).dropContents();
        if (tile instanceof TileExtendedPatternTerminal) ((TileExtendedPatternTerminal) tile).dropContents();
        if (tile instanceof TileExtendedMolecularAssembler) ((TileExtendedMolecularAssembler) tile).dropContents();
        super.breakBlock(world, pos, state);
    }
}
