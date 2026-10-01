package com.gmm.gctold.misc.registry;

import com.smd.gctcore.Tags;
import java.util.Arrays;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

public final class GctAllOreDictionary {
    private static final List<Entry> ENTRIES = Arrays.asList(
            entry("oreSanite", "sanite_ore"),
            entry("oreEthaxium", "ethauxium_ore"),
            entry("oreDreadium", "dreadium_ore"),
            entry("blockPe", "solid_pot_energy"),
            entry("oreDawnium", "dawnium_ore"),
            entry("oreAzathothium", "azathothium_ore"),
            entry("oreNyarlathotepium", "nyarlathotepium_ore"),
            entry("oreYogsothothium", "yogsothothium_ore"),
            entry("oreShubniggurathium", "shubniggurathium_ore"),
            entry("oreAzathothiumComplex", "azathothium_ore_complex"),
            entry("oreNyarlathotepiumComplex", "nyralathotepium_ore_complex"),
            entry("oreYogsothothiumComplex", "yogsothothium_ore_complex"),
            entry("oreShubniggurathiumComplex", "shubniggurathium_ore_complex"),
            entry("oreAbyssComplex", "azathothium_ore_complex"),
            entry("oreAbyssComplex", "nyralathotepium_ore_complex"),
            entry("oreAbyssComplex", "yogsothothium_ore_complex"),
            entry("oreAbyssComplex", "shubniggurathium_ore_complex"),
            entry("stoneAbyssExtended", "densedarkstone"),
            entry("stoneAbyssExtended", "warprack"),
            entry("cobblestoneAlf", "alf_cobble_stone"),
            entry("cobblestoneMana", "mana_cobble_stone"),
            entry("dirtAlf", "alf_dirt"),
            entry("dirtBesidevoid", "bloodydirt"),
            entry("dirtBesidevoid", "corruptdirt"),
            entry("dirtMana", "mana_dirt"),
            entry("fenceWood", "evileye_fence"),
            entry("gemResonantDebris", "resonated_scrap"),
            entry("grassAlf", "alf_grass"),
            entry("grassBesidevoid", "bloodygrass"),
            entry("grassBesidevoid", "corruptgrass"),
            entry("grassMana", "mana_grass"),
            entry("logWoodDream", "dreamwood_log"),
            entry("logWoodDream", "reversed_dreamwood_log"),
            entry("logWood", "evileye_log"),
            entry("logWoodLiving", "livingwood_log"),
            entry("logWoodMana", "mana_log"),
            entry("oreElementium", "elementium_ore"),
            entry("oreGravityDebris", "gravity_debris"),
            entry("oreOrichalcos", "orichalcos_ore"),
            entry("oreResonantDebris", "resonate_debris"),
            entry("oreTerrasteel", "terrasteel_ore"),
            entry("plankWood", "evileye_plank"),
            entry("plankWoodMana", "mana_plank"),
            entry("rodBligtz", "bligtz_rod"),
            entry("rodBnatuz", "bnatuz_rod"),
            entry("rodBninz", "bninz_rod"),
            entry("rodBthdz", "bthdz_rod"),
            entry("sandAlf", "alf_sand"),
            entry("scrapApocalypsium", "apocalypsium_scrap"),
            entry("slabWood", "evileye_slab"),
            entry("stairWood", "evileye_stair"),
            entry("stoneAlf", "alf_stone"),
            entry("stoneBesidevoid", "corruptrock"),
            entry("stoneBesidevoid", "bloodyrock"),
            entry("stoneHeavite", "heavite"),
            entry("stoneMana", "mana_stone"),
            entry("treeLeaves", "evileye_leaves"),
            entry("treeLeavesDream", "dreamwood_leaves"),
            entry("treeLeavesDream", "reversed_dreamwood_leaves"),
            entry("treeLeavesLiving", "livingwood_leaves"),
            entry("treeLeavesMana", "mana_leaves"),
            entry("blockDensite", "densite_block"),
            entry("blockDraconiumChaotic", "chaotic_draconium_block"),
            entry("blockDraconiumRuled", "ruled_draconium_block"),
            entry("blockEverite", "everite_block"),
            entry("blockFallenMetal", "fallen_metal_block"),
            entry("blockFireAlloy", "fire_alloy_block"),
            entry("blockGenite", "genite_block"),
            entry("blockIceAlloy", "ice_alloy_block"),
            entry("blockOrderCrystal", "order_crystal_block"),
            entry("blockOrderedMetal", "ordered_metal_block"),
            entry("blockReditrite", "reditrite_block"),
            entry("blockReditritecobble", "reditrite_cobblestone"),
            entry("blockRelifedMetal", "relifed_metal_block"),
            entry("blockSkyAlloy", "sky_alloy_block"),
            entry("blockStormyMetal", "stormy_metal_block"),
            entry("blockWitheriumEquipment", "equipment_witherium_block"),
            entry("blockWitheriumRelifed", "relifed_witherium_block"),
            entry("blockWitheriumStormy", "stormy_witherium_block"),
            entry("cobblestoneOrdered", "order_cobblestone"),
            entry("coreOrdered", "ordered_core"),
            entry("dustDensite", "densite_dust"),
            entry("dustDraconiumChaotic", "chaotic_draconium_dust"),
            entry("dustDraconiumRuled", "ruled_draconium_dust"),
            entry("dustEverite", "everite_dust"),
            entry("dustFallenMetal", "fallen_metal_dust"),
            entry("dustFireAlloy", "fire_alloy_dust"),
            entry("dustGenite", "genite_dust"),
            entry("dustIceAlloy", "ice_alloy_dust"),
            entry("dustOrderedMetal", "ordered_metal_dust"),
            entry("dustReditrite", "reditrite_dust"),
            entry("dustRelifedMetal", "relifed_metal_dust"),
            entry("dustSkyAlloy", "sky_alloy_dust"),
            entry("dustStormyMetal", "stormy_metal_dust"),
            entry("dustWitherium", "witherium_dust"),
            entry("dustWitheriumEquipment", "equipment_witherium_dust"),
            entry("dustWitheriumRelifed", "relifed_witherium_dust"),
            entry("dustWitheriumStormy", "stormy_witherium_dust"),
            entry("Geardensite", "densite_gear"),
            entry("gearDraconiumChaotic", "chaotic_draconium_gear"),
            entry("gearDraconiumRuled", "ruled_draconium_gear"),
            entry("gearEverite", "everite_gear"),
            entry("gearFallenMetal", "fallen_metal_gear"),
            entry("gearFireAlloy", "fire_alloy_gear"),
            entry("gearGenite", "genite_gear"),
            entry("gearIceAlloy", "ice_alloy_gear"),
            entry("gearOrderedMetal", "ordered_metal_gear"),
            entry("gearReditrite", "reditrite_gear"),
            entry("gearRelifedMetal", "relifed_metal_gear"),
            entry("gearSkyAlloy", "sky_alloy_gear"),
            entry("gearStormyMetal", "stormy_metal_gear"),
            entry("gearWitheriumEquipment", "equipment_witherium_gear"),
            entry("gearWitheriumRelifed", "relifed_witherium_gear"),
            entry("gearWitheriumStormy", "stormy_witherium_gear"),
            entry("gemOrderCrystal", "order_crystal"),
            entry("ingotBalancedMatrix", "balanced_matrix_ingot"),
            entry("ingotDensite", "densite_ingot"),
            entry("ingotDraconiumChaotic", "chaotic_draconium_ingot"),
            entry("ingotDraconiumRuled", "ruled_draconium_ingot"),
            entry("ingotEverite", "everite_ingot"),
            entry("ingotFallenMetal", "fallen_metal_ingot"),
            entry("ingotFireAlloy", "fire_alloy_ingot"),
            entry("ingotGenite", "genite_ingot"),
            entry("ingotIceAlloy", "ice_alloy_ingot"),
            entry("ingotOrderedMetal", "ordered_metal_ingot"),
            entry("ingotReditrite", "reditrite_ingot"),
            entry("ingotRelifedMetal", "relifed_metal_ingot"),
            entry("ingotSkyAlloy", "sky_alloy_ingot"),
            entry("ingotStormyMetal", "stormy_metal_ingot"),
            entry("ingotWitheriumEquipment", "equipment_witherium_ingot"),
            entry("ingotWitheriumRelifed", "relifed_witherium_ingot"),
            entry("ingotWitheriumStormy", "stormy_witherium_ingot"),
            entry("nuggetAdeninite", "adeninite_nugget"),
            entry("nuggetBnightium", "bnightium_nugget"),
            entry("nuggetCanopium", "canopium_nugget"),
            entry("nuggetCosmilite", "cosmilite_nugget"),
            entry("nuggetDensite", "densite_nugget"),
            entry("nuggetDraconiumChaotic", "chaotic_draconium_nugget"),
            entry("nuggetDraconiumRuled", "ruled_draconium_nugget"),
            entry("nuggetEverite", "everite_nugget"),
            entry("nuggetFallenMetal", "fallen_metal_nugget"),
            entry("nuggetFireAlloy", "fire_alloy_nugget"),
            entry("nuggetFreezite", "freezite_nugget"),
            entry("nuggetGenite", "genite_nugget"),
            entry("nuggetGuaninite", "guaninite_nugget"),
            entry("nuggetIceAlloy", "ice_alloy_nugget"),
            entry("nuggetLavarite", "lavarite_nugget"),
            entry("nuggetMistium", "mistium_nugget"),
            entry("nuggetOceanium", "oceanium_nugget"),
            entry("nuggetOrderedMetal", "ordered_metal_nugget"),
            entry("nuggetPlasmarite", "plasmarite_nugget"),
            entry("nuggetReditrite", "reditrite_nugget"),
            entry("nuggetRelifedMetal", "relifed_metal_nugget"),
            entry("nuggetSkyAlloy", "sky_alloy_nugget"),
            entry("nuggetStormyMetal", "stormy_metal_nugget"),
            entry("nuggetThyminite", "thyminite_nugget"),
            entry("nuggetWitheriumEquipment", "equipment_witherium_nugget"),
            entry("nuggetWitheriumRelifed", "relifed_witherium_nugget"),
            entry("nuggetWitheriumStormy", "stormy_witherium_nugget"),
            entry("oreOrderCrystal", "order_crystal_ore"),
            entry("oreWitherium", "witherium_ore_end"),
            entry("oreWitherium", "witherium_ore_nether"),
            entry("oreWitherium", "witherium_ore_overworld"),
            entry("plateDensite", "densite_plate"),
            entry("plateDraconiumChaotic", "chaotic_draconium_plate"),
            entry("plateDraconiumRuled", "ruled_draconium_plate"),
            entry("plateEverite", "everite_plate"),
            entry("plateFallenMetal", "fallen_metal_plate"),
            entry("plateFireAlloy", "fire_alloy_plate"),
            entry("plateGenite", "genite_plate"),
            entry("plateIceAlloy", "ice_alloy_plate"),
            entry("plateOrderedMetal", "ordered_metal_plate"),
            entry("plateReditrite", "reditrite_plate"),
            entry("plateRelifedMetal", "relifed_metal_plate"),
            entry("plateSkyAlloy", "sky_alloy_plate"),
            entry("plateStormyMetal", "stormy_metal_plate"),
            entry("plateWitheriumEquipment", "equipment_witherium_plate"),
            entry("plateWitheriumRelifed", "relifed_witherium_plate"),
            entry("plateWitheriumStormy", "stormy_witherium_plate"),
            entry("stoneOrdered", "order_stone"),
            entry("stoneOrdered", "order_stone_brick"),
            entry("stoneOrdered", "order_stone_brick_chiseled")
    );

    private GctAllOreDictionary() {
    }

    public static void register() {
        for (Entry entry : ENTRIES) {
            ItemStack stack = stack(entry.itemName);
            if (!stack.isEmpty()) {
                OreDictionary.registerOre(entry.oreName, stack);
            }
        }
    }

    private static ItemStack stack(String itemName) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(Tags.MOD_ID, itemName));
        return item == null ? ItemStack.EMPTY : new ItemStack(item, 1);
    }

    private static Entry entry(String oreName, String itemName) {
        return new Entry(oreName, itemName);
    }

    private static final class Entry {
        private final String oreName;
        private final String itemName;

        private Entry(String oreName, String itemName) {
            this.oreName = oreName;
            this.itemName = itemName;
        }
    }
}
