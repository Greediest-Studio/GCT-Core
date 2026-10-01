package com.gmm.gctold.misc;

import com.smd.gctcore.Tags;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public final class GctAllCreativeTab {
    private static final String[] FIRST_ITEMS = {
            "dimdarkerrealm", "warped_ruin", "beside_void", "everheaven", "alfheim", "atlantis", "starland",
            "the_void", "the_nowhere", "orderland",
            "dimdarkerrealm_portal", "warped_ruin_portal", "everheaven_portal", "alfheim_portal",
            "atlantis_portal", "beside_void_portal_1", "beside_void_portal_2", "beside_void_portal_3",
            "astral_portal_core", "the_void_portal", "the_nowhere_portal", "orderland_portal",
            "model_atomic_viberator", "model_atomic_decayer", "model_atomic_acidor", "model_ender_forge"
    };
    private static final Map<String, Integer> FIRST_ITEM_ORDER = new HashMap<>();

    static {
        for (int i = 0; i < FIRST_ITEMS.length; i++) {
            FIRST_ITEM_ORDER.put(FIRST_ITEMS[i], i);
        }
    }

    public static final CreativeTabs TAB = new CreativeTabs("tabc_tab") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Blocks.DIAMOND_BLOCK);
        }

        @Override
        public void displayAllRelevantItems(NonNullList<ItemStack> items) {
            super.displayAllRelevantItems(items);
            items.removeIf(ItemStack::isEmpty);
            items.sort(GctAllCreativeTab::compareStacks);
        }
    };

    private GctAllCreativeTab() {
    }

    private static int compareStacks(ItemStack left, ItemStack right) {
        String leftId = path(left);
        String rightId = path(right);
        int result = Integer.compare(category(leftId), category(rightId));
        if (result != 0) {
            return result;
        }

        result = Integer.compare(firstItemOrder(leftId), firstItemOrder(rightId));
        if (result != 0) {
            return result;
        }

        result = compareFamily(leftId, rightId);
        if (result != 0) {
            return result;
        }

        result = Integer.compare(variantRank(leftId), variantRank(rightId));
        if (result != 0) {
            return result;
        }

        return leftId.compareTo(rightId);
    }

    private static String path(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        Item item = stack.getItem();
        if (item == Items.AIR) {
            return "";
        }
        ResourceLocation registryName = item.getRegistryName();
        return registryName == null ? "" : registryName.getPath();
    }

    private static int firstItemOrder(String id) {
        Integer order = FIRST_ITEM_ORDER.get(id);
        return order == null ? Integer.MAX_VALUE : order;
    }

    private static int category(String id) {
        if (FIRST_ITEM_ORDER.containsKey(id)) {
            return 0;
        }
        if (isFunctionalBlock(id)) {
            return 10;
        }
        if (isFluidOrMoltenMaterial(id)) {
            return 20;
        }
        if (id.contains("ore")) {
            return 30;
        }
        if (isTerrainFamily(id)) {
            return 40;
        }
        if (id.startsWith("order_") || id.startsWith("ordered_")) {
            return 50;
        }
        if (id.startsWith("rainboquartz") || id.startsWith("shalloite")) {
            return 60;
        }
        if (isSenterian(id)) {
            return 70;
        }
        if (isKeyOrTool(id)) {
            return 80;
        }
        if (isMobDropOrRitualItem(id)) {
            return 90;
        }
        if (isFood(id)) {
            return 100;
        }
        return 200;
    }

    private static int compareFamily(String leftId, String rightId) {
        int result = Integer.compare(familyRank(leftId), familyRank(rightId));
        if (result != 0) {
            return result;
        }
        return family(leftId).compareTo(family(rightId));
    }

    private static int familyRank(String id) {
        String family = family(id);
        if (family.startsWith("dim") || family.startsWith("warped") || family.startsWith("everheaven")
                || family.startsWith("alfheim") || family.startsWith("atlantis") || family.startsWith("the")
                || family.startsWith("orderland")) {
            return 0;
        }
        if (family.startsWith("apocalypse") || family.startsWith("apocalypsium")) {
            return 10;
        }
        if (family.startsWith("earthbound")) {
            return 20;
        }
        if (family.startsWith("sanity") || family.startsWith("sanite")) {
            return 30;
        }
        if (family.startsWith("seek")) {
            return 40;
        }
        if (family.startsWith("senterian")) {
            return 50;
        }
        if (family.startsWith("order")) {
            return 60;
        }
        if (family.startsWith("witherium")) {
            return 70;
        }
        return 100;
    }

    private static String family(String id) {
        String family = id;
        String[] suffixes = {
                "_slab_double", "_crashed_slab_double", "_brick_crashed", "_brick_chiseled", "_chiseled",
                "_stairs", "_stair", "_walls", "_wall", "_slab", "_fence", "_leaves", "_log", "_plank",
                "_grass", "_dirt", "_sand", "_stone", "_ore_complex", "_ore", "_portal", "_block", "_brick",
                "_nugget", "_ingot", "_dust", "_rod", "_scrap", "_core", "_plate", "_gear", "_active"
        };
        for (String suffix : suffixes) {
            if (family.endsWith(suffix)) {
                return family.substring(0, family.length() - suffix.length());
            }
        }
        return family;
    }

    private static int variantRank(String id) {
        if (id.endsWith("_portal")) {
            return 0;
        }
        if (id.endsWith("_ore")) {
            return 5;
        }
        if (id.endsWith("_ore_complex")) {
            return 6;
        }
        if (id.endsWith("_block")) {
            return 10;
        }
        if (id.endsWith("_stone") || id.endsWith("_dirt") || id.endsWith("_grass") || id.endsWith("_sand")) {
            return 20;
        }
        if (id.endsWith("_brick")) {
            return 30;
        }
        if (id.endsWith("_chiseled") || id.endsWith("_brick_chiseled")) {
            return 35;
        }
        if (id.endsWith("_slab")) {
            return 40;
        }
        if (id.endsWith("_slab_double")) {
            return 41;
        }
        if (id.endsWith("_stair") || id.endsWith("_stairs")) {
            return 45;
        }
        if (id.endsWith("_wall") || id.endsWith("_walls")) {
            return 50;
        }
        if (id.endsWith("_fence")) {
            return 55;
        }
        if (id.endsWith("_log")) {
            return 60;
        }
        if (id.endsWith("_plank")) {
            return 65;
        }
        if (id.endsWith("_leaves")) {
            return 70;
        }
        if (id.endsWith("_ingot")) {
            return 80;
        }
        if (id.endsWith("_nugget")) {
            return 81;
        }
        if (id.endsWith("_dust")) {
            return 82;
        }
        if (id.endsWith("_rod")) {
            return 83;
        }
        if (id.endsWith("_gear")) {
            return 84;
        }
        if (id.endsWith("_plate")) {
            return 85;
        }
        return 100;
    }

    private static boolean isFunctionalBlock(String id) {
        return id.contains("altar") || id.contains("receiver") || id.contains("reserver") || id.contains("container")
                || id.contains("summoner") || id.contains("barrier") || id.contains("bomb") || id.contains("debris")
                || id.equals("blue_fire") || id.equals("solid_pot_energy") || id.equals("void_seed_container")
                || id.equals("earthbound_core") || id.equals("astral_portal_core");
    }

    private static boolean isFluidOrMoltenMaterial(String id) {
        return id.endsWith("_liquid") || id.equals("adeninite") || id.equals("aetherium")
                || id.equals("awakened_draconium") || id.equals("balanced_matrix") || id.equals("bnightium")
                || id.equals("canopium") || id.equals("chaotic_draconium") || id.equals("densite")
                || id.equals("everite") || id.equals("fire_alloy") || id.equals("freezite") || id.equals("genite")
                || id.equals("guaninite") || id.equals("ice_alloy") || id.equals("lavarite")
                || id.equals("lumixeium") || id.equals("mistium") || id.equals("naturaeum") || id.equals("noxexeum")
                || id.equals("oceanium") || id.equals("plasmarite") || id.equals("reditrite")
                || id.equals("sky_alloy") || id.equals("snowingium") || id.equals("thyminite")
                || id.equals("tonitruium");
    }

    private static boolean isTerrainFamily(String id) {
        return startsWithAny(id, "alf_", "bloody", "botanical_", "corrupt", "crystal", "dense", "dreamwood_",
                "drurce_", "evileye_", "heavite", "livingwood_", "lunar_", "mana_", "meteor", "orichalcos_",
                "polarisite", "primordial_", "reversed_", "solok_", "star_", "warprack", "wildplain_",
                "deepslate", "glowstone_");
    }

    private static boolean isSenterian(String id) {
        return id.startsWith("senterian");
    }

    private static boolean isKeyOrTool(String id) {
        return id.contains("key") || id.contains("dismantler") || id.contains("wrench") || id.contains("siphon")
                || id.contains("observer") || id.contains("changer") || id.contains("relinquisher");
    }

    private static boolean isMobDropOrRitualItem(String id) {
        return startsWithAny(id, "essence", "warped_soul", "further_soul", "ancient", "shoggoth", "shoggoth",
                "shoggy", "shadow", "gravity", "resonated", "naturalline", "bligtz", "bnatuz", "bninz", "bthdz",
                "earth_", "holy", "elf_", "apocalypse_ruin", "apocalypsium_scrap", "order_crystal",
                "ordered_core", "witherium_dust");
    }

    private static boolean isFood(String id) {
        return id.contains("flesh") || id.contains("soup") || id.contains("fruit") || id.contains("cookie")
                || id.contains("tancale");
    }

    private static boolean startsWithAny(String id, String... prefixes) {
        for (String prefix : prefixes) {
            if (id.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
