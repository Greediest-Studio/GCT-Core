package com.smd.gctcore.client.extendedcrafting;

import com.google.common.collect.ImmutableList;
import com.smd.gctcore.common.integration.extendedcrafting.ExtendedPatternData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.List;

public final class EncodedExtendedPatternBakedModel implements IBakedModel {
    private final IBakedModel base;
    private final ItemOverrideList overrides = new Overrides();

    public EncodedExtendedPatternBakedModel(IBakedModel base) { this.base = base; }

    public static boolean isShiftDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    @Override public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) { return base.getQuads(state, side, rand); }
    @Override public boolean isAmbientOcclusion() { return base.isAmbientOcclusion(); }
    @Override public boolean isGui3d() { return base.isGui3d(); }
    @Override public boolean isBuiltInRenderer() { return base.isBuiltInRenderer(); }
    @Override public TextureAtlasSprite getParticleTexture() { return base.getParticleTexture(); }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return base.getItemCameraTransforms(); }
    @Override public ItemOverrideList getOverrides() { return overrides; }

    private final class Overrides extends ItemOverrideList {
        private Overrides() { super(ImmutableList.of()); }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack,
                                           @Nullable World world, @Nullable EntityLivingBase entity) {
            if (isShiftDown()) {
                ItemStack output = ExtendedPatternData.readOutput(stack);
                if (!output.isEmpty()) {
                    return Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(output, world, entity);
                }
            }
            return base.getOverrides().handleItemState(base, stack, world, entity);
        }
    }
}
