package com.gmm.gctold.common.world.dimension;

import com.gmm.gctold.client.GctAllModels;
import com.gmm.gctold.common.blocks.BlockAstralPortalCore;
import com.gmm.gctold.common.blocks.BlockBesideVoidPortal1;
import com.gmm.gctold.common.blocks.BlockBesideVoidPortal2;
import com.gmm.gctold.common.blocks.BlockBesideVoidPortal3;
import com.gmm.gctold.misc.GctAllCreativeTab;
import com.gmm.gctold.common.items.PortalActivatorItem;
import com.gmm.gctold.common.world.structure.GctAllStructureTemplates;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class GctAllDimensions {
    private static final Block[] PORTALS = {
            WorldDarkerRealm.portal,
            WorldWarpedRuin.portal,
            WorldEverheaven.portal,
            WorldAlfheim.portal,
            WorldAtlantis.portal,
            WorldTheVoid.portal,
            WorldTheNowhere.portal,
            WorldOrderland.portal
    };

    public static final Item DIM_DARKER_REALM = new PortalActivatorItem("dimdarkerrealm", GctAllCreativeTab.TAB,
            WorldDarkerRealm.portal, () -> WorldDarkerRealm.DIMID,
            GctAllStructureTemplates.DIM_54_PORTAL_1, GctAllStructureTemplates.DIM_54_PORTAL_2);
    public static final Item WARPED_RUIN = new PortalActivatorItem("warped_ruin", GctAllCreativeTab.TAB,
            WorldWarpedRuin.portal, () -> WorldWarpedRuin.DIMID,
            GctAllStructureTemplates.DIM_55_PORTAL_1, GctAllStructureTemplates.DIM_55_PORTAL_2);
    public static final Item BESIDE_VOID = new PortalActivatorItem("beside_void", GctAllCreativeTab.TAB,
            BlockBesideVoidPortal2.block, () -> WorldBesideVoid.DIMID, 3, (world, origin, widthDir) -> {
                world.setBlockState(origin, BlockBesideVoidPortal3.block.getDefaultState(), 3);
                world.setBlockState(origin.up(), BlockBesideVoidPortal2.block.getDefaultState(), 3);
                world.setBlockState(origin.up(2), BlockBesideVoidPortal1.block.getDefaultState(), 3);
            });
    public static final Item EVERHEAVEN = new PortalActivatorItem("everheaven", GctAllCreativeTab.TAB,
            WorldEverheaven.portal, () -> WorldEverheaven.DIMID,
            GctAllStructureTemplates.HEAVEN_PORTAL, GctAllStructureTemplates.HEAVEN_PORTAL);
    public static final Item ALFHEIM = new PortalActivatorItem("alfheim", GctAllCreativeTab.TAB,
            WorldAlfheim.portal, () -> WorldAlfheim.DIMID);
    public static final Item ATLANTIS = new PortalActivatorItem("atlantis", GctAllCreativeTab.TAB,
            WorldAtlantis.portal, () -> WorldAtlantis.DIMID);
    public static final Item STARLAND = new PortalActivatorItem("starland", GctAllCreativeTab.TAB,
            BlockAstralPortalCore.block, () -> WorldStarland.DIMID, 1,
            (world, origin, widthDir) -> world.setBlockState(origin, BlockAstralPortalCore.block.getDefaultState(), 3));
    public static final Item THE_VOID = new PortalActivatorItem("the_void", GctAllCreativeTab.TAB,
            WorldTheVoid.portal, () -> WorldTheVoid.DIMID);
    public static final Item THE_NOWHERE = new PortalActivatorItem("the_nowhere", GctAllCreativeTab.TAB,
            WorldTheNowhere.portal, () -> WorldTheNowhere.DIMID);
    public static final Item ORDERLAND = new PortalActivatorItem("orderland", GctAllCreativeTab.TAB,
            WorldOrderland.portal, () -> WorldOrderland.DIMID,
            GctAllStructureTemplates.ORDER_PORTAL, GctAllStructureTemplates.ORDER_PORTAL_2);

    private GctAllDimensions() {
    }

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(PORTALS);
    }

    public static void registerItems(RegistryEvent.Register<Item> event) {
        for (Block portal : PORTALS) {
            event.getRegistry().register(new ItemBlock(portal).setRegistryName(portal.getRegistryName()));
        }

        event.getRegistry().registerAll(
                DIM_DARKER_REALM,
                WARPED_RUIN,
                BESIDE_VOID,
                EVERHEAVEN,
                ALFHEIM,
                ATLANTIS,
                STARLAND,
                THE_VOID,
                THE_NOWHERE,
                ORDERLAND
        );
    }

    public static void registerDimensions() {
        WorldDarkerRealm.registerDimension();
        WorldWarpedRuin.registerDimension();
        WorldBesideVoid.registerDimension();
        WorldEverheaven.registerDimension();
        WorldAlfheim.registerDimension();
        WorldAtlantis.registerDimension();
        WorldStarland.registerDimension();
        WorldTheVoid.registerDimension();
        WorldTheNowhere.registerDimension();
        WorldOrderland.registerDimension();
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        for (Block portal : PORTALS) {
            GctAllModels.block(portal);
        }

        GctAllModels.item(DIM_DARKER_REALM, "dimdarkerrealm");
        GctAllModels.item(WARPED_RUIN, "warped_ruin");
        GctAllModels.item(BESIDE_VOID, "beside_void");
        GctAllModels.item(EVERHEAVEN, "everheaven");
        GctAllModels.item(ALFHEIM, "alfheim");
        GctAllModels.item(ATLANTIS, "atlantis");
        GctAllModels.item(STARLAND, "starland");
        GctAllModels.item(THE_VOID, "the_void");
        GctAllModels.item(THE_NOWHERE, "the_nowhere");
        GctAllModels.item(ORDERLAND, "orderland");
    }
}
