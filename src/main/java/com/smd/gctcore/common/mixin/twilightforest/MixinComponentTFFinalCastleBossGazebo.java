package com.smd.gctcore.common.mixin.twilightforest;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
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
            remap = false
    )
    private void gctcore$replaceFinalCastleSpawner(ComponentTFFinalCastleBossGazebo self, World world, IBlockState state, int x, int y, int z, StructureBoundingBox boundingBox) {
        IBlockState replacement = gctcore$getReplacementState(state);
        int targetY = replacement == state ? y : y - 1;
        this.setBlockState(world, replacement, x, targetY, z, boundingBox);
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

    private static IBlockState gctcore$getReplacementState(IBlockState original) {
        ResourceLocation originalName = original.getBlock().getRegistryName();
        if (!new ResourceLocation("twilightforest", "boss_spawner").equals(originalName)) {
            return original;
        }

        Block apocalypseAltar = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("gct_mobs", "apocalypse_altar"));
        return apocalypseAltar == null ? original : apocalypseAltar.getDefaultState();
    }
}
