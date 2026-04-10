package com.smd.gctcore.common.mixin.thaumicrestoration;

import com.Zoko061602.ThaumicRestoration.tile.TileAdvRechargePedestal;
import com.Zoko061602.ThaumicRestoration.util.BlockPosUtil;
import com.Zoko061602.ThaumicRestoration.util.IterUtil;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.common.tiles.devices.TileRechargePedestal;

import java.util.ArrayList;

@Mixin(TileAdvRechargePedestal.class)
public class MixinTileAdvRechargePedestal extends TileRechargePedestal {

    @Shadow(remap = false)
    Block[] metals = new Block[] {
            BlocksTC.metalBlockBrass,
            BlocksTC.metalBlockThaumium,
            BlocksTC.metalBlockVoid
    };

    /**
     * @author Gct-Core
     * @reason 修复结构检测
     */
    @Overwrite(remap = false)
    private boolean checkStructure() {

        for (int i = 0; i < 8; i++) {
            // checks -2, 1, -2 || 2, 1, -2 || -2, 1, 2 || 2, 1, 2 || -2, 2, -2 || 2, 2, -2 || -2, 2, 2 || 2, 2, 2
            if (world.getBlockState(BlockPosUtil.translateToBlockPos(pos, IterUtil.tick1(i) * 2, (i / 4), IterUtil.tick2(i) * 2)).getBlock() != Blocks.QUARTZ_BLOCK)
                return false;
        }

        ArrayList<Block> l = new ArrayList<Block>();
        for (int i = 0; i < 4; i++) {
            if (!l.contains(world.getBlockState(BlockPosUtil.translateToBlockPos(pos, IterUtil.tick1(i) * 2, 2, IterUtil.tick2(i) * 2)).getBlock()))
                l.add(world.getBlockState(BlockPosUtil.translateToBlockPos(pos, IterUtil.tick1(i) * 2, 2, IterUtil.tick2(i) * 2)).getBlock());
        }

        if (l.size() != 1)
            return false;

        for (Block b : metals) {
            if (l.get(0) == b)
                break;
            if (b == BlocksTC.metalBlockVoid)
                return false;
        }
        return true;
    }

}
