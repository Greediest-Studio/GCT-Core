package com.gmm.gctold.common.blocks;

import com.gmm.gctold.common.events.SenterianLockUnlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.gmm.gctold.misc.GctAllCreativeTab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
public class BlockSenterianLock extends Block {
    public static final Block block = new BlockSenterianLock();

    public BlockSenterianLock() {
    super(Material.ROCK);
    this.setTranslationKey("senterian_lock");
    this.setSoundType(SoundType.STONE);
    this.setHardness(1.0f);
    this.setResistance(4000.0f);
    this.setLightLevel(0.0f);
    this.setLightOpacity(15);
    this.setCreativeTab(GctAllCreativeTab.TAB);
    this.setBlockUnbreakable();

    }



    public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state, final EntityPlayer entity, final EnumHand hand, final EnumFacing direction, final float hitX, final float hitY, final float hitZ) {
    super.onBlockActivated(world, pos, state, entity, hand, direction, hitX, hitY, hitZ);
    final int x = pos.getX();
    final int y = pos.getY();
    final int z = pos.getZ();
    SenterianLockUnlock.run(entity, world, x, y, z);
    return true;
    }

}
