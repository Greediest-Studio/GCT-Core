package com.smd.gctcore.common.mixin.twilightforest;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import twilightforest.structures.StructureTFComponentOld;
import twilightforest.structures.finalcastle.ComponentTFFinalCastleBossGazebo;


@Mixin(ComponentTFFinalCastleBossGazebo.class)
public abstract class MixinComponentTFFinalCastleBossGazebo extends StructureTFComponentOld {

    @Redirect(
            method = "addComponentParts",
            at = @At(
                    value = "INVOKE",
                    target = "Ltwilightforest/structures/finalcastle/ComponentTFFinalCastleBossGazebo;setBlockState(Lnet/minecraft/world/World;Lnet/minecraft/block/state/IBlockState;IIILnet/minecraft/world/gen/structure/StructureBoundingBox;)V"
            ),
            remap = true
    )
    private void gctcore$replaceFinalCastleSpawner(ComponentTFFinalCastleBossGazebo self, World world, IBlockState state, int x, int y, int z, StructureBoundingBox boundingBox) {
        Block apocalypseAltar = ForgeRegistries.BLOCKS.getValue(
                new ResourceLocation("gct_mobs", "apocalypse_altar"));
        ResourceLocation stateName = state.getBlock().getRegistryName();

        // The only setBlockState call in this method is the final-boss spawner.
        // Do not delegate to StructureComponent#setBlockState here: its bounding
        // box check can reject the y-1 position at the bottom of the gazebo.
        if (new ResourceLocation("twilightforest", "boss_spawner").equals(stateName)) {
            if (apocalypseAltar != null) {
                BlockPos altarPos = new BlockPos(
                        this.getXWithOffset(x, z),
                        this.getYWithOffset(y - 1),
                        this.getZWithOffset(x, z));
                world.setBlockState(altarPos, apocalypseAltar.getDefaultState(), 2);
            }
            // Never fall back to placing the final-boss spawner.
            return;
        }

        // Keep the original behaviour for any unexpected setBlockState call.
        this.setBlockState(world, state, x, y, z, boundingBox);
    }

    @Redirect(
            method = "addComponentParts",
            at = @At(
                    value = "INVOKE",
                    target = "Ltwilightforest/structures/finalcastle/ComponentTFFinalCastleBossGazebo;setInvisibleTextEntity(Lnet/minecraft/world/World;IIILnet/minecraft/world/gen/structure/StructureBoundingBox;Ljava/lang/String;ZF)V"
            ),
            remap = false
    )
    private void gctcore$skipFinalCastleFloatingText(ComponentTFFinalCastleBossGazebo self, World world, int x, int y, int z, StructureBoundingBox boundingBox, String text, boolean alwaysRenderName, float yOffset) {
    }

}
