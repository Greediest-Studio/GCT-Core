package com.gmm.gctold.misc.registry;

import com.gmm.gctold.client.GctAllModels;
import com.gmm.gctold.misc.GctAllCreativeTab;
import com.gmm.gctold.common.items.InfoItem;
import com.gmm.gctold.common.items.ItemCommandDismantler;
import com.gmm.gctold.common.items.ItemCreepyWitherDoll;
import com.gmm.gctold.common.items.ItemCreepyWitherstormDoll;
import com.gmm.gctold.common.items.ItemDoorKeyOfOrderland;
import com.gmm.gctold.common.items.ItemEarthOrb;
import com.gmm.gctold.common.items.ItemFruitOfMind;
import com.gmm.gctold.common.items.ItemFruitOfMindEnchanted;
import com.gmm.gctold.common.items.ItemGreedycraftModChanger;
import com.gmm.gctold.common.items.ItemKeyOfDark;
import com.gmm.gctold.common.items.ItemKeyOfWarpedActive;
import com.gmm.gctold.common.items.ItemMuddyFlesh;
import com.gmm.gctold.common.items.ItemRNGRelinquisher;
import com.gmm.gctold.common.items.ItemRemnantCookie;
import com.gmm.gctold.common.items.ItemSenterianWrench;
import com.gmm.gctold.common.items.ItemShoggothTancale;
import com.gmm.gctold.common.items.ItemShoggothTancaleSoup;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class GctAllItems {
    private GctAllItems() {
    }

    public static final Item ESSENCE_OF_DARKERREALM = simple("essenceofdarkerrealm");
    public static final Item SHOGGOTH_TOOTH = simple("shoggothtooth");

    public static final Item APOCALYPSIUM_SCRAP = simple("apocalypsium_scrap");
    public static final Item GRAVITY_SCRAP = simple("gravity_scrap");
    public static final Item RESONATED_SCRAP = simple("resonated_scrap");

    public static final Item EARTH_INGOT = simple("earth_ingot");
    public static final Item HOLYSTEEL_INGOT = simple("holysteel_ingot");

    public static final Item STORM_BLIGTZ_DUST = simple("bligtz_dust");
    public static final Item STORM_BLIGTZ_ROD = simple("bligtz_rod");
    public static final Item STORM_BNATUZ_DUST = simple("bnatuz_dust");
    public static final Item STORM_BNATUZ_ROD = simple("bnatuz_rod");
    public static final Item STORM_BNINZ_DUST = simple("bninz_dust");
    public static final Item STORM_BNINZ_ROD = simple("bninz_rod");
    public static final Item STORM_BTHDZ_DUST = simple("bthdz_dust");
    public static final Item STORM_BTHDZ_ROD = simple("bthdz_rod");

    public static final Item RAINBOQUARTZ = simple("rainboquartz");
    public static final Item SHALLOITE = simple("shalloite");
    public static final Item WITHERIUM_DUST = simple("witherium_dust");

    public static final Item WARPED_SOUL = info("warped_soul", 64, 0, "§f扭曲遗址内不屈的冤魂", false);
    public static final Item FURTHER_SOUL = info("further_soul", 64, 0, "§b来自遥远的记忆。", true);
    public static final Item SOUL_STEALER_SCROLL = info("soul_stealer_scroll", 64, 0, "§c似乎是某位神明留下的遗物？", false);
    public static final Item ANCIENT_MUD = info("ancientmud", 64, 0, "§e在灵神台座上右击以召唤远古修格斯", false);
    public static final Item ANCIENT_SHOGGOTH_MUD = info("ancient_shoggoth_mud", 64, 0,
            "§f在一只§c血红修格斯融合体§f面前击杀一只§c远古修格斯§f获得", true);
    public static final Item SHOGGOTH_CLUMP = info("shoggoth_clump", 64, 0,
            "§c命令一只修格斯融合体击杀一只远古修格斯……？", true);
    public static final Item KEY_OF_WARPED = info("key_of_warped", 1, 0, "§2似乎不能正常使用", false);

    public static final Item APOCALYPSE_RUIN = info("apocalypse_ruin", 64, 0, "§e右键天启祭坛以召唤天启骑士", true);
    public static final Item SHADOW_NUCLEAR = info("shadownuclear", 64, 0, "§4似乎是阴影怪物产生的源泉？", false);
    public static final Item NATURALLINE_SCRAP = info("naturalline_scrap", 64, 0, null, true);

    public static final Item ELF_PASSES = info("elf_passes", 64, 0, "§e右键精灵可以得到自然结晶碎片", true);

    public static final Item ORDER_CRYSTAL = info("order_crystal", 64, 0, "秩序，蕴含于这一方小小水晶中", true);
    public static final Item ORDERED_CORE = info("ordered_core", 64, 0, null, true);
    public static final Item DOOR_KEY_EMPTY = info("door_key_empty", 1, 0, "可以在深渊仪式上绑定对应门钥匙！", false);

    public static final Item SENTERIAN_KEY = info("senterian_key", 64, 0, "用于打开黑岩之锁", false);

    public static final Item SANITE_SIPHON = info("sanite_siphon", 1, 100, "§c用于采集修格斯软泥", false);
    public static final Item SANITY_OBSERVER = info("sanity_observer", 1, 0, "探测当前San值", false);

    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                ESSENCE_OF_DARKERREALM,
                WARPED_SOUL,
                FURTHER_SOUL,
                SOUL_STEALER_SCROLL,
                ANCIENT_MUD,
                ANCIENT_SHOGGOTH_MUD,
                ItemMuddyFlesh.block,
                SHOGGOTH_CLUMP,
                ItemShoggothTancale.block,
                ItemShoggothTancaleSoup.block,
                SHOGGOTH_TOOTH,
                ItemKeyOfDark.block,
                KEY_OF_WARPED,
                ItemKeyOfWarpedActive.block,

                APOCALYPSE_RUIN,
                APOCALYPSIUM_SCRAP,
                SHADOW_NUCLEAR,
                GRAVITY_SCRAP,
                RESONATED_SCRAP,
                NATURALLINE_SCRAP,

                EARTH_INGOT,
                ItemEarthOrb.block,
                ELF_PASSES,
                HOLYSTEEL_INGOT,

                ORDER_CRYSTAL,
                ORDERED_CORE,
                ItemCommandDismantler.block,
                DOOR_KEY_EMPTY,
                ItemDoorKeyOfOrderland.block,

                SENTERIAN_KEY,
                ItemSenterianWrench.block,

                STORM_BLIGTZ_DUST,
                STORM_BLIGTZ_ROD,
                STORM_BNATUZ_DUST,
                STORM_BNATUZ_ROD,
                STORM_BNINZ_DUST,
                STORM_BNINZ_ROD,
                STORM_BTHDZ_DUST,
                STORM_BTHDZ_ROD,

                ItemFruitOfMind.block,
                ItemFruitOfMindEnchanted.block,
                ItemRemnantCookie.block,
                SANITE_SIPHON,
                SANITY_OBSERVER,

                ItemCreepyWitherDoll.block,
                ItemCreepyWitherstormDoll.block,
                ItemGreedycraftModChanger.block,
                RAINBOQUARTZ,
                ItemRNGRelinquisher.block,
                SHALLOITE,
                WITHERIUM_DUST
        );
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        GctAllModels.item(ESSENCE_OF_DARKERREALM, "essenceofdarkerrealm");
        GctAllModels.item(WARPED_SOUL, "warped_soul");
        GctAllModels.item(FURTHER_SOUL, "further_soul");
        GctAllModels.item(SOUL_STEALER_SCROLL, "soul_stealer_scroll");
        GctAllModels.item(ANCIENT_MUD, "ancientmud");
        GctAllModels.item(ANCIENT_SHOGGOTH_MUD, "ancient_shoggoth_mud");
        GctAllModels.item(ItemMuddyFlesh.block, "muddy_flesh");
        GctAllModels.item(SHOGGOTH_CLUMP, "shoggoth_clump");
        GctAllModels.item(ItemShoggothTancale.block, "shoggothtancale");
        GctAllModels.item(ItemShoggothTancaleSoup.block, "shoggoth_tancale_soup");
        GctAllModels.item(SHOGGOTH_TOOTH, "shoggothtooth");
        GctAllModels.item(ItemKeyOfDark.block, "keyofdark");
        GctAllModels.item(KEY_OF_WARPED, "key_of_warped");
        GctAllModels.item(ItemKeyOfWarpedActive.block, "key_of_warped_active");

        GctAllModels.item(APOCALYPSE_RUIN, "apocalypse_ruin");
        GctAllModels.item(APOCALYPSIUM_SCRAP, "apocalypsium_scrap");
        GctAllModels.item(SHADOW_NUCLEAR, "shadownuclear");
        GctAllModels.item(GRAVITY_SCRAP, "gravity_scrap");
        GctAllModels.item(RESONATED_SCRAP, "resonated_scrap");
        GctAllModels.item(NATURALLINE_SCRAP, "naturalline_scrap");

        GctAllModels.item(EARTH_INGOT, "earth_ingot");
        GctAllModels.item(ItemEarthOrb.block, "earth_orb");
        GctAllModels.item(ELF_PASSES, "elf_passes");
        GctAllModels.item(HOLYSTEEL_INGOT, "holysteel_ingot");

        GctAllModels.item(ORDER_CRYSTAL, "order_crystal");
        GctAllModels.item(ORDERED_CORE, "ordered_core");
        GctAllModels.item(ItemCommandDismantler.block, "command_dismantler");
        GctAllModels.item(DOOR_KEY_EMPTY, "door_key_empty");
        GctAllModels.item(ItemDoorKeyOfOrderland.block, "door_key_of_orderland");

        GctAllModels.item(SENTERIAN_KEY, "senterian_key");
        GctAllModels.item(ItemSenterianWrench.block, "senterian_wrench");

        GctAllModels.item(STORM_BLIGTZ_DUST, "bligtz_dust");
        GctAllModels.item(STORM_BLIGTZ_ROD, "bligtz_rod");
        GctAllModels.item(STORM_BNATUZ_DUST, "bnatuz_dust");
        GctAllModels.item(STORM_BNATUZ_ROD, "bnatuz_rod");
        GctAllModels.item(STORM_BNINZ_DUST, "bninz_dust");
        GctAllModels.item(STORM_BNINZ_ROD, "bninz_rod");
        GctAllModels.item(STORM_BTHDZ_DUST, "bthdz_dust");
        GctAllModels.item(STORM_BTHDZ_ROD, "bthdz_rod");

        GctAllModels.item(ItemFruitOfMind.block, "fruit_of_mind");
        GctAllModels.item(ItemFruitOfMindEnchanted.block, "fruit_of_mind_enchanted");
        GctAllModels.item(ItemRemnantCookie.block, "remnant_cookie");
        GctAllModels.item(SANITE_SIPHON, "sanite_siphon");
        GctAllModels.item(SANITY_OBSERVER, "sanity_observer");

        GctAllModels.item(ItemCreepyWitherDoll.block, "creepy_wither_doll");
        GctAllModels.item(ItemCreepyWitherstormDoll.block, "creepy_witherstorm_doll");
        GctAllModels.item(ItemGreedycraftModChanger.block, "greedycraft_mod_changer");
        GctAllModels.item(RAINBOQUARTZ, "rainboquartz");
        GctAllModels.item(ItemRNGRelinquisher.block, "rng_relinquisher");
        GctAllModels.item(SHALLOITE, "shalloite");
        GctAllModels.item(WITHERIUM_DUST, "witherium_dust");
    }

    private static Item simple(String name) {
        Item item = new Item();
        item.setMaxDamage(0);
        item.setMaxStackSize(64);
        item.setTranslationKey(name);
        item.setRegistryName(name);
        item.setCreativeTab(GctAllCreativeTab.TAB);
        return item;
    }

    private static Item info(String name, int maxStackSize, int maxDamage, String tooltip, boolean glowing) {
        return new InfoItem(name, maxStackSize, maxDamage, tooltip, glowing);
    }
}
