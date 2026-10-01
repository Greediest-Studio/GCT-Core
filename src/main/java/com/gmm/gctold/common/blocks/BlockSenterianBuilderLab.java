package com.gmm.gctold.common.blocks;

import com.gmm.gctold.common.events.SenterianLabBuilderClick;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import com.gmm.gctold.misc.GctAllCreativeTab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
public class BlockSenterianBuilderLab extends Block {
    public static final Block block = new BlockSenterianBuilderLab();

    public BlockSenterianBuilderLab() {
    super(Material.ROCK);
    this.setTranslationKey("senterian_builder_lab");
    this.setSoundType(SoundType.STONE);
    this.setHarvestLevel("pickaxe", 8);
    this.setHardness(1.0f);
    this.setResistance(10.0f);
    this.setLightLevel(0.0f);
    this.setLightOpacity(15);
    this.setCreativeTab(GctAllCreativeTab.TAB);

    }



    public void addInformation(final ItemStack itemstack, final World world, final List<String> list, final ITooltipFlag flag) {
    super.addInformation(itemstack, world, (List)list, flag);
    list.add("\u53ea\u80fd\u5728\u9ed1\u5ca9\u8ff7\u5baby=21\u683c\u5904\u4f7f\u7528\uff01");
    }

    public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state, final EntityPlayer entity, final EnumHand hand, final EnumFacing direction, final float hitX, final float hitY, final float hitZ) {
    super.onBlockActivated(world, pos, state, entity, hand, direction, hitX, hitY, hitZ);
    final int x = pos.getX();
    final int y = pos.getY();
    final int z = pos.getZ();
    SenterianLabBuilderClick.run(world, entity, x, y, z);
    return true;
    }

}
