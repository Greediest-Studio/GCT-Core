package com.gmm.gctold.common.world.structure;

public final class GctAllStructureTemplates {
    public static final StructureTemplateId DIM_54_PORTAL_1 = template("54portal_1");
    public static final StructureTemplateId DIM_54_PORTAL_2 = template("54portal_2");
    public static final StructureTemplateId DIM_55_PORTAL_1 = template("55portal_1");
    public static final StructureTemplateId DIM_55_PORTAL_2 = template("55portal_2");
    public static final StructureTemplateId ASTRAL_ISLAND_1 = template("astral_island_1");
    public static final StructureTemplateId ASTRAL_ISLAND_2 = template("astral_island_2");
    public static final StructureTemplateId BAR = template("bar");
    public static final StructureTemplateId BLACKSMITH = template("blacksmith");
    public static final StructureTemplateId CHURCH = template("church");
    public static final StructureTemplateId ELVEN_POST = template("elven_post");
    public static final StructureTemplateId END_WITHERIUM = template("end_witherium_1");
    public static final StructureTemplateId EVILEYE_TREE_1 = template("evileye_tree_1");
    public static final StructureTemplateId EVILEYE_TREE_2 = template("evileye_tree_2");
    public static final StructureTemplateId EVILEYE_TREE_3 = template("evileye_tree_3");
    public static final StructureTemplateId FARM = template("farm");
    public static final StructureTemplateId FARMHOUSE = template("farmhouse");
    public static final StructureTemplateId HEAVEN_PORTAL = template("heaven_portal");
    public static final StructureTemplateId LARGE_STONE_POST = template("large_stonepost");
    public static final StructureTemplateId HOUSE = template("house");
    public static final StructureTemplateId LIBRARY = template("library");
    public static final StructureTemplateId METEORITE_BALL = template("meteorite_ball");
    public static final StructureTemplateId OBSIDIORITE_BIG = template("obisidiorite_big");
    public static final StructureTemplateId OBSIDIORITE_MIDDLE = template("obisidiorite_middle");
    public static final StructureTemplateId OBSIDIORITE_SMALL = template("obisidiorite_small");
    public static final StructureTemplateId OBSIDIORITE_TINY = template("obisidiorite_tiny");
    public static final StructureTemplateId ORDER_B = template("order_b");
    public static final StructureTemplateId ORDER_PORTAL = template("order_portal");
    public static final StructureTemplateId ORDER_PORTAL_2 = template("order_portal_2");
    public static final StructureTemplateId ORDER_ROD_1 = template("order_rod_1");
    public static final StructureTemplateId ORDER_ROD_2 = template("order_rod_2");
    public static final StructureTemplateId ORDER_S1 = template("order_s1");
    public static final StructureTemplateId ORDER_S2 = template("order_s2");
    public static final StructureTemplateId REDITRITE_METEOR = template("reditrite_meteor");
    public static final StructureTemplateId REDITRITE_METEOR_SMALL = template("reditrite_meteor_small");
    public static final StructureTemplateId REVERSED_DUNGEON = template("reversed_dungeon");
    public static final StructureTemplateId SEEK_ALTAR = template("seek_altar");
    public static final StructureTemplateId SENTERIAN_LAB = template("senterian_lab");
    public static final StructureTemplateId SMALL_STONE_POST_1 = template("small_stonepost1");
    public static final StructureTemplateId SMALL_STONE_POST_2 = template("small_stonepost2");
    public static final StructureTemplateId STORAGE = template("storage");
    public static final StructureTemplateId TEMPLE = template("temple");
    public static final StructureTemplateId TOWER_1 = template("tower_1");
    public static final StructureTemplateId TOWER_2 = template("tower_2");
    public static final StructureTemplateId VOID_BEDROCK_ROD_1 = template("void_bdr_1");
    public static final StructureTemplateId VOID_BEDROCK_ROD_2 = template("void_bdr_2");
    public static final StructureTemplateId VOID_BEDROCK_ROD_3 = template("void_bdr_3");
    public static final StructureTemplateId VOID_BEDROCK_ROD_4 = template("void_bdr_4");
    public static final StructureTemplateId VOID_BEDROCK_ROD_TREASURE = template("void_bdr_t");

    private GctAllStructureTemplates() {
    }

    private static StructureTemplateId template(String name) {
        return new StructureTemplateId(name);
    }
}
