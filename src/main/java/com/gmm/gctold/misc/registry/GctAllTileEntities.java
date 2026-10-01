package com.gmm.gctold.misc.registry;

import com.gmm.gctold.common.blocks.BlockBesideVoidPortal1;
import com.gmm.gctold.common.blocks.BlockBesideVoidPortal2;
import com.gmm.gctold.common.blocks.BlockBesideVoidPortal3;
import com.gmm.gctold.common.blocks.BlockFinalliumContainer;
import com.gmm.gctold.common.blocks.BlockFinalliumContainerActive;
import com.gmm.gctold.common.blocks.BlockSenterianSummoner;
import com.gmm.gctold.common.tile.TileEntityBesideVoidPortal1;
import com.gmm.gctold.common.tile.TileEntityBesideVoidPortal2;
import com.gmm.gctold.common.tile.TileEntityBesideVoidPortal3;
import com.gmm.gctold.common.tile.TileEntityEarthboundAltar;
import com.gmm.gctold.common.tile.TileEntityEarthboundReceiver;
import com.gmm.gctold.common.tile.TileEntityPrimordialPortalHolder;
import com.gmm.gctold.common.tile.TileEntitySanityAltar;
import com.gmm.gctold.common.tile.TileEntitySeekAltar;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class GctAllTileEntities {
    private GctAllTileEntities() {
    }

    public static void register() {
        register(TileEntityBesideVoidPortal1.class, "tileentitybeside_void_portal_1");
        register(TileEntityBesideVoidPortal2.class, "tileentitybeside_void_portal_2");
        register(TileEntityBesideVoidPortal3.class, "tileentitybeside_void_portal_3");
        register(TileEntityEarthboundAltar.class, "tileentityearthbound_altar");
        register(TileEntityEarthboundReceiver.class, "tileentityearthbound_receiver");
        register(BlockFinalliumContainer.FinalliumContainerTileEntity.class, "tileentityfinallium_container");
        register(BlockFinalliumContainerActive.ActiveFinalliumContainerTileEntity.class, "tileentityfinallium_container_active");
        register(TileEntityPrimordialPortalHolder.class, "tileentityprimordial_portal_holder_up");
        register(TileEntitySanityAltar.class, "tileentitysanity_altar");
        register(TileEntitySeekAltar.class, "tileentityseek_altar");
        register(BlockSenterianSummoner.SenterianSummonerTileEntity.class, "tileentitysenterian_summoner");
    }

    private static void register(Class<? extends TileEntity> tileEntityClass, String name) {
        GameRegistry.registerTileEntity(tileEntityClass, new ResourceLocation("gctcore", name));
    }
}
