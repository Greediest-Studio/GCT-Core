package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;
import com.gmm.gctold.common.events.FinalliumContainerRightclick;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockFinalliumContainer extends Block implements ITileEntityProvider {
    public static final Block block = new BlockFinalliumContainer();
    public static final Item item = new ItemBlock(block);

    public BlockFinalliumContainer() {
        super(Material.IRON);
        setTranslationKey("finallium_container");
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 13);
        setHardness(50.0F);
        setResistance(10000.0F);
        setLightLevel(0.0F);
        setLightOpacity(0);
        setCreativeTab(GctAllCreativeTab.TAB);
    }
@SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0,
                new ModelResourceLocation("gctold:finallium_container", "inventory"));
    }

    @Override
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        super.addInformation(itemstack, world, list, flag);
        list.add("\u51dd\u805a\u80fd\u91cf");
    }

    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new FinalliumContainerTileEntity();
    }

    @Override
    public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int eventID, int eventParam) {
        super.eventReceived(state, worldIn, pos, eventID, eventParam);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(eventID, eventParam);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof FinalliumContainerTileEntity) {
            InventoryHelper.dropInventoryItems(world, pos, (IInventory) tileentity);
        }
        world.removeTileEntity(pos);
        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World worldIn, BlockPos pos) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof FinalliumContainerTileEntity) {
            return Container.calcRedstoneFromInventory((IInventory) tileentity);
        }
        return 0;
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        for (int l = 0; l < 4; ++l) {
            double d0 = x + random.nextFloat();
            double d2 = y + random.nextFloat();
            double d3 = z + random.nextFloat();
            double d4 = (random.nextFloat() - 0.5) * 0.5;
            double d5 = (random.nextFloat() - 0.5) * 0.5;
            double d6 = (random.nextFloat() - 0.5) * 0.5;
            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, d0, d2, d3, d4, d5, d6, new int[0]);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, EnumHand hand,
            EnumFacing direction, float hitX, float hitY, float hitZ) {
        super.onBlockActivated(world, pos, state, entity, hand, direction, hitX, hitY, hitZ);
      FinalliumContainerRightclick.run(entity, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    public static class FinalliumContainerTileEntity extends TileEntityLockableLoot {
        private NonNullList<ItemStack> stacks;

        public FinalliumContainerTileEntity() {
            this.stacks = NonNullList.withSize(1, ItemStack.EMPTY);
        }

        public int getSizeInventory() {
            return 1;
        }

        public boolean isEmpty() {
            for (ItemStack itemstack : this.stacks) {
                if (!itemstack.isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        public boolean isItemValidForSlot(int index, ItemStack stack) {
            return true;
        }

        public ItemStack getStackInSlot(int slot) {
            return this.stacks.get(slot);
        }

        public String getName() {
            return "container.finallium_container";
        }

        public void readFromNBT(NBTTagCompound compound) {
            super.readFromNBT(compound);
            this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
            if (!this.checkLootAndRead(compound)) {
                ItemStackHelper.loadAllItems(compound, this.stacks);
            }
        }

        public NBTTagCompound writeToNBT(NBTTagCompound compound) {
            super.writeToNBT(compound);
            if (!this.checkLootAndWrite(compound)) {
                ItemStackHelper.saveAllItems(compound, this.stacks);
            }
            return compound;
        }

        public SPacketUpdateTileEntity getUpdatePacket() {
            return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
        }

        public NBTTagCompound getUpdateTag() {
            return this.writeToNBT(new NBTTagCompound());
        }

        public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
            this.readFromNBT(pkt.getNbtCompound());
        }

        public void handleUpdateTag(NBTTagCompound tag) {
            this.readFromNBT(tag);
        }

        public int getInventoryStackLimit() {
            return 1;
        }

        public String getGuiID() {
            return "gctcore:finallium_container";
        }

        public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
            this.fillWithLoot(playerIn);
            return new ContainerChest(playerInventory, this, playerIn);
        }

        protected NonNullList<ItemStack> getItems() {
            return this.stacks;
        }
    }
}
