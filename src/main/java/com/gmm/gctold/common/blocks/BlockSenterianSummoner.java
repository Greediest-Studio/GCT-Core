package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;
import com.gmm.gctold.common.events.SenterianSummonerSummon;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;

public class BlockSenterianSummoner extends Block implements ITileEntityProvider {
    public static final Block block = new BlockSenterianSummoner();
    public static final Item item = new ItemBlock(block);

    public BlockSenterianSummoner() {
        super(Material.ROCK);
        setTranslationKey("senterian_summoner");
        setSoundType(SoundType.STONE);
        setHardness(1.0F);
        setResistance(10.0F);
        setLightLevel(0.6666667F);
        setLightOpacity(15);
        setCreativeTab(GctAllCreativeTab.TAB);
        setBlockUnbreakable();
    }
@SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0,
                new ModelResourceLocation("gctold:senterian_summoner", "inventory"));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new SenterianSummonerTileEntity();
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
        if (tileentity instanceof SenterianSummonerTileEntity) {
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
        if (tileentity instanceof SenterianSummonerTileEntity) {
            return Container.calcRedstoneFromInventory((IInventory) tileentity);
        }
        return 0;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        if (!world.isRemote) {
            world.scheduleUpdate(pos, this, tickRate(world));
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        super.updateTick(world, pos, state, random);
        if (!world.isRemote && !SenterianSummonerSummon.run(world, pos.getX(), pos.getY(), pos.getZ())) {
            world.scheduleUpdate(pos, this, tickRate(world));
        }
    }

    @Override
    public int tickRate(World worldIn) {
        return 40;
    }

    public static class SenterianSummonerTileEntity extends TileEntityLockableLoot {
        private NonNullList<ItemStack> stacks;

        public SenterianSummonerTileEntity() {
            this.stacks = NonNullList.withSize(9, ItemStack.EMPTY);
        }

        public int getSizeInventory() {
            return 9;
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
            return "container.senterian_summoner";
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
            return 64;
        }

        public String getGuiID() {
            return "gctcore:senterian_summoner";
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
