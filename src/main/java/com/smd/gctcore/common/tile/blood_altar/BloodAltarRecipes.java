package com.smd.gctcore.common.tile.blood_altar;

import WayofTime.bloodmagic.core.data.SoulNetwork;
import WayofTime.bloodmagic.core.RegistrarBloodMagic;
import WayofTime.bloodmagic.core.registry.OrbRegistry;
import WayofTime.bloodmagic.orb.BloodOrb;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import com.smd.gctcore.gctcore;
import github.kasuminova.mmce.common.event.recipe.FactoryRecipeFinishEvent;
import github.kasuminova.mmce.common.event.recipe.FactoryRecipeStartEvent;
import github.kasuminova.mmce.common.event.recipe.FactoryRecipeTickEvent;
import github.kasuminova.mmce.common.event.recipe.RecipeCheckEvent;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFluid;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.RecipePrimer;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Java migration of the RecipeBuilder recipes outside {@code addAltarRecipe}.
 * RequirementItem and RequirementFluid are MMCE's native recipe components,
 * so their normal JEI integration is preserved without CraftTweaker wrappers.
 */
public final class BloodAltarRecipes {
    private static final int[] ORB_CAPACITY = {
            0, 5000, 25000, 150000, 1000000, 10000000, 30000000, 80000000, 200000000
    };
    private static final BigInteger MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);
    private static final String TOOLTIP_PURIFICATION =
            "gui.gctcore.blood_altar.recipe.tooltip.purification";
    private static final String TOOLTIP_ORB_RATE =
            "gui.gctcore.blood_altar.recipe.tooltip.orb_rate";
    private static final String TOOLTIP_ORB_PERSONAL =
            "gui.gctcore.blood_altar.recipe.tooltip.orb_personal";
    private static final String TOOLTIP_ORB_PREVIEW =
            "gui.gctcore.blood_altar.recipe.tooltip.orb_preview";
    private static final String FAILURE_MISSING_PURIFICATION =
            "gui.gctcore.blood_altar.recipe.failure.missing_purification";
    private static final String FAILURE_ALTAR_FULL =
            "gui.gctcore.blood_altar.recipe.failure.altar_full";
    private static final String FAILURE_INVALID_PLAYER =
            "gui.gctcore.blood_altar.recipe.failure.invalid_player";
    private static final String FAILURE_MISSING_PERSONAL =
            "gui.gctcore.blood_altar.recipe.failure.missing_personal";
    private static final String FAILURE_PLAYER_NETWORK_FULL =
            "gui.gctcore.blood_altar.recipe.failure.player_network_full";
    private static final String FAILURE_LEVEL =
            "gui.gctcore.blood_altar.recipe.failure.level";
    private static final String FAILURE_LIFE_ESSENCE =
            "gui.gctcore.blood_altar.recipe.failure.life_essence";
    private static final String TOOLTIP_LEVEL =
            "gui.gctcore.blood_altar.recipe.tooltip.level";
    private static final String TOOLTIP_COST =
            "gui.gctcore.blood_altar.recipe.tooltip.cost";
    private static final String TOOLTIP_TIME =
            "gui.gctcore.blood_altar.recipe.tooltip.time";
    private static final int[] ALTAR_LEVEL_SPEED = {0, 1, 1, 2, 4, 20, 80, 400, 2000, 10000};
    private static final String DATA_ALTAR_COST = "gctcore_altar_cost";
    private static final String DATA_ALTAR_REMAINING_COST = "gctcore_altar_remaining_cost";
    private static final String DATA_ALTAR_REMAINING_TICKS = "gctcore_altar_remaining_ticks";
    private static final String DATA_ALTAR_LEVEL = "gctcore_altar_level";
    private static boolean registered;

    private BloodAltarRecipes() {
    }

    /** Must run during init, after ContentTweaker has registered its fluid and before MMCE post-init loads recipes. */
    public static synchronized void register() {
        if (registered) {
            return;
        }

        registerFillRecipe();
        registerPurificationRecipe();
        registerOrbRecipe(false);
        registerOrbRecipe(true);
        registerAltarRecipes();
        registered = true;
    }

    private static void registerFillRecipe() {
        final RequirementItem dagger = nonConsumedItem("test", "input", "bloodmagic:sacrificial_dagger", 1);
        if (dagger == null) {
            return;
        }
        final RecipePrimer recipe = newRecipe("test", 1);
        recipe.appendComponent(dagger);
        recipe.addFactoryFinishHandler(event ->
                BloodAltarMachine.setStoredLifeEssence(event.getController(),
                        BloodAltarMachine.getStoredCapacity(event.getController())));
        recipe.setLoadJEI(false);
        recipe.setThreadName(BloodAltarMachine.THREAD_CRAFTING);
        recipe.build();
    }

    private static void registerPurificationRecipe() {
        final Fluid substrate = findFluid("purify", "input", "substrate_lifeessence");
        if (substrate == null) {
            return;
        }

        final RecipePrimer recipe = newRecipe("purify", 1);
        recipe.appendComponent(new RequirementFluid(IOType.INPUT, new FluidStack(substrate, 1000)));
        recipe.addPreCheckHandler(BloodAltarRecipes::checkPurification);
        recipe.addFactoryPreTickHandler(BloodAltarRecipes::tickPurification);
        recipe.addFactoryFinishHandler(BloodAltarRecipes::finishPurification);
        recipe.addRecipeTooltip(TOOLTIP_PURIFICATION);
        recipe.setThreadName(BloodAltarMachine.THREAD_PURIFICATION);
        recipe.build();
    }

    private static void registerOrbRecipe(final boolean personalRuneOnly) {
        final RequirementItem orb = personalRuneOnly ? null : nonConsumedOrb();
        if (!personalRuneOnly && orb == null) {
            return;
        }
        final RecipePrimer recipe = newRecipe(personalRuneOnly ? "orb1" : "orb", 20);
        recipe.addPreCheckHandler(event -> checkOrbTransfer(event, personalRuneOnly));
        if (!personalRuneOnly) {
            recipe.appendComponent(orb);
            recipe.addRecipeTooltip(TOOLTIP_ORB_RATE);
            recipe.addRecipeTooltip(TOOLTIP_ORB_PERSONAL);
            recipe.addRecipeTooltip(TOOLTIP_ORB_PREVIEW);
        } else {
            recipe.setLoadJEI(false);
        }
        recipe.setParallelized(false);
        recipe.addFactoryFinishHandler(BloodAltarRecipes::finishOrbTransfer);
        recipe.setThreadName(BloodAltarMachine.THREAD_ORB);
        recipe.build();
    }

    private static void checkPurification(final RecipeCheckEvent event) {
        final TileMultiblockMachineController controller = event.getController();
        if (controller.getBlocksInPattern(BloodAltarMachine.RUNE_PURIFICATION) < 1) {
            event.setFailed(FAILURE_MISSING_PURIFICATION);
            return;
        }
        if (!hasPurificationCapacity(controller, event.getActiveRecipe().getParallelism())) {
            event.setFailed(FAILURE_ALTAR_FULL);
        }
    }

    private static void tickPurification(final FactoryRecipeTickEvent event) {
        if (!hasPurificationCapacity(event.getController(), event.getActiveRecipe().getParallelism())) {
            event.preventProgressing(FAILURE_ALTAR_FULL);
        }
    }

    private static void finishPurification(final FactoryRecipeFinishEvent event) {
        final TileMultiblockMachineController controller = event.getController();
        final BigInteger gained = purificationOutput(controller, event.getActiveRecipe().getParallelism());
        final BigInteger stored = BloodAltarMachine.getStoredLifeEssence(controller);
        final BigInteger capacity = BloodAltarMachine.getStoredCapacity(controller);
        BloodAltarMachine.setStoredLifeEssence(controller, stored.add(gained).min(capacity));
    }

    private static boolean hasPurificationCapacity(final TileMultiblockMachineController controller,
                                                   final int parallelism) {
        final BigInteger produced = purificationOutput(controller, parallelism);
        return produced.signum() > 0 && BloodAltarMachine.getStoredLifeEssence(controller)
                .add(produced).compareTo(BloodAltarMachine.getStoredCapacity(controller)) <= 0;
    }

    private static BigInteger purificationOutput(final TileMultiblockMachineController controller,
                                                 final int parallelism) {
        return BigInteger.valueOf(Math.max(0, parallelism))
                .multiply(BigInteger.valueOf(Math.max(0,
                        controller.getBlocksInPattern(BloodAltarMachine.RUNE_PURIFICATION))));
    }

    private static void checkOrbTransfer(final RecipeCheckEvent event, final boolean personalRuneOnly) {
        final TileMultiblockMachineController controller = event.getController();
        final SoulNetwork network = getOwnerNetwork(controller);
        if (network == null) {
            event.setFailed(FAILURE_INVALID_PLAYER);
            return;
        }
        if (personalRuneOnly && controller.getBlocksInPattern(BloodAltarMachine.RUNE_PERSONAL) < 1) {
            event.setFailed(FAILURE_MISSING_PERSONAL);
            return;
        }
        if (Math.max(0, network.getCurrentEssence()) >= getNetworkCapacity(controller, network)) {
            event.setFailed(FAILURE_PLAYER_NETWORK_FULL);
        }
    }

    private static void finishOrbTransfer(final FactoryRecipeFinishEvent event) {
        final TileMultiblockMachineController controller = event.getController();
        final SoulNetwork network = getOwnerNetwork(controller);
        if (network == null) {
            return;
        }

        final int currentEssence = Math.max(0, network.getCurrentEssence());
        final int capacity = getNetworkCapacity(controller, network);
        final BigInteger transferable = BloodAltarMachine.getStoredLifeEssence(controller)
                .min(getNetworkTransferLimit(controller))
                .min(BigInteger.valueOf(Math.max(0L, (long) capacity - currentEssence)));
        if (transferable.signum() <= 0) {
            if (network.getCurrentEssence() < 0) {
                network.setCurrentEssence(0);
            }
            return;
        }

        final int transferred = transferable.intValue();
        network.setCurrentEssence(currentEssence + transferred);
        BloodAltarMachine.setStoredLifeEssence(controller,
                BloodAltarMachine.getStoredLifeEssence(controller).subtract(transferable));
    }

    private static SoulNetwork getOwnerNetwork(final TileMultiblockMachineController controller) {
        final UUID owner = controller.getOwner();
        return owner == null ? null : NetworkHelper.getSoulNetwork(owner);
    }

    private static int getNetworkCapacity(final TileMultiblockMachineController controller,
                                          final SoulNetwork network) {
        final int tier = Math.max(0, Math.min(ORB_CAPACITY.length - 1, network.getOrbTier()));
        final BigDecimal capacity = BigDecimal.valueOf(ORB_CAPACITY[tier])
                .multiply(BigDecimal.ONE.add(BigDecimal.valueOf(0.02D)
                        .multiply(BigDecimal.valueOf(Math.max(0,
                                controller.getBlocksInPattern(BloodAltarMachine.RUNE_ORB))))));
        return toIntLimit(capacity);
    }

    private static BigInteger getNetworkTransferLimit(final TileMultiblockMachineController controller) {
        final int acceleration = Math.min(19, Math.max(0,
                controller.getBlocksInPattern(BloodAltarMachine.RUNE_ACCELERATION)));
        final int speed = Math.max(0, controller.getBlocksInPattern(BloodAltarMachine.RUNE_SPEED));
        final int dislocation = Math.max(0, controller.getBlocksInPattern(BloodAltarMachine.RUNE_DISLOCATION));
        final int personal = Math.max(0, controller.getBlocksInPattern(BloodAltarMachine.RUNE_PERSONAL));
        return BigDecimal.valueOf(20L)
                .multiply(BigDecimal.valueOf(1L + acceleration))
                .multiply(BigDecimal.ONE.add(BigDecimal.valueOf(0.2D).multiply(BigDecimal.valueOf(speed))))
                .multiply(new BigDecimal("1.2").pow(dislocation))
                .multiply(BigDecimal.valueOf(2L).pow(personal))
                .setScale(0, RoundingMode.DOWN)
                .toBigInteger().min(MAX_INT);
    }

    private static int toIntLimit(final BigDecimal value) {
        return value.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0
                ? Integer.MAX_VALUE : Math.max(0, value.setScale(0, RoundingMode.DOWN).intValue());
    }

    /** Registers the former CraftTweaker addAltarRecipe table.  All migrated
     * calls stay together so pack authors can audit them without following
     * registration code through the event handlers. */
    private static void registerAltarRecipes() {
        addAltarRecipe("ore:gemEmerald", "bloodmagic:blood_orb", "2000", 1);
        addAltarRecipe("ore:ingotAstralStarmetal", "bloodmagic:blood_orb", "5000", 2);
        addAltarRecipe("ore:blockCompressedExperience", "bloodmagic:blood_orb", "25000", 3);
        addAltarRecipe("bloodmagic:blood_shard", "bloodmagic:blood_orb", "40000", 4);
        addAltarRecipe("ore:netherStar", "bloodmagic:blood_orb", "80000", 5);
        //addAltarRecipe("draconicevolution:wyvern_core", "bloodmagic:blood_orb", "300000", 6);
        addAltarRecipe("ore:gemPrismarine", "animus:fragmenthealing", "1000", 2);
        addAltarRecipe("ore:gemAmber", "thaumcraft:curio@1", "80000", 6);
        addAltarRecipe("ore:ingotDurasteel", "tconevo:metal@25", "10000", 3);
        addAltarRecipe("ore:blockLapis", "bloodmagic:inscription_tool@1", "1000", 3);
        addAltarRecipe("minecraft:magma_cream", "bloodmagic:inscription_tool@2", "1000", 3);
        addAltarRecipe("ore:obsidian", "bloodmagic:inscription_tool@3", "1000", 3);
        addAltarRecipe("minecraft:ghast_tear", "bloodmagic:inscription_tool@4", "1000", 3);
        addAltarRecipe("ore:blockCoal", "bloodmagic:inscription_tool@5", "2000", 4);
        addAltarRecipe("ore:glowstone", "bloodmagic:inscription_tool@6", "200000", 6);
        addAltarRecipe("bloodarsenal:blood_diamond@1", "bloodarsenal:blood_diamond@2", "100000", 5);
        addAltarRecipe("additions:blood_sigil", "additions:true_blood_sigil", "150000", 6);
        addAltarRecipe("minecraft:book", "bloodmagic:sanguine_book", "1000", 1);
        addAltarRecipe("bloodmagic:teleposition_focus", "bloodmagic:teleposition_focus@1", "10000", 4);
        addAltarRecipe("ore:ingotIron", "bloodarsenal:base_item@4", "5000", 3);
        addAltarRecipe("ore:gemAmbrosium", "thaumcraft:curio@4", "80000", 6);
        addAltarRecipe("minecraft:glass_bottle", "twilightforest:fiery_blood", "7000", 4);
        addAltarRecipe("ore:enderpearl", "bloodmagic:teleposition_focus", "2000", 4);
        addAltarRecipe("bloodmagic:lava_crystal", "bloodmagic:activation_crystal", "10000", 3);
        addAltarRecipe("ore:dustGlowstone", "bloodarsenal:base_item@2", "2500", 3);
        addAltarRecipe("ore:ingotGold", "animus:keybinding", "1000", 3);
        addAltarRecipe("ore:gemShadow", "thaumcraft:curio@3", "80000", 6);
        addAltarRecipe("minecraft:bucket", "forge:bucketfilled", "1000", 1);
        addAltarRecipe("ore:manaPearl", "additions:pearl_of_knowledge", "250000", 6);
        addAltarRecipe("minecraft:iron_sword", "bloodmagic:dagger_of_sacrifice", "3000", 2);
        addAltarRecipe("ore:logWood", "bloodarsenal:blood_infused_wooden_log", "2000", 2);
        addAltarRecipe("ore:blockCosmilite", "additions:creative_shard", "850000", 6);
        addAltarRecipe("ore:dyeOrange", "bloodarsenal:blood_orange", "500", 2);
        addAltarRecipe("ore:blockCrystalMatrix", "bloodmagic:decorative_brick@2", "15000", 5);
        addAltarRecipe("ore:ingotCosmilite", "additions:ghost_metal", "2560000", 7);
        addAltarRecipe("ore:blockDarkest", "additions:darkest_stonebrick_large", "3840000", 7);
        addAltarRecipe("additions:catalyst_star", "additions:proliferation_star", "6400000", 7);
        addAltarRecipe("minecraft:bone_block", "additions:ivorium_ingot", "1280000", 7);
        addAltarRecipe("additions:balanced_slate", "additions:murderite_ingot", "80000000", 8);
        addAltarRecipe("gct_ores:blue_print_forge", "modularmachinery:itemblueprint", "100000", 6);

        addAltarRecipe("ore:stone", "bloodmagic:slate", "1000", 1);
        addAltarRecipe("bloodmagic:slate", "bloodmagic:slate@1", "2000", 2);
        addAltarRecipe("bloodmagic:slate@1", "bloodmagic:slate@2", "5000", 3);
        addAltarRecipe("bloodmagic:slate@2", "bloodmagic:slate@3", "15000", 4);
        addAltarRecipe("bloodmagic:slate@3", "bloodmagic:slate@4", "30000", 5);
        addAltarRecipe("bloodmagic:slate@4", "additions:slate_6", "200000", 6);
        addAltarRecipe("additions:slate_6", "additions:slate_7", "1000000", 7);
        addAltarRecipe("additions:slate_7", "additions:slate_8", "50000000", 8);
        addAltarRecipe("additions:slate_8", "additions:slate_9", "300000000", 9);
    }

    /**
     * Native Java replacement for the migration script's addAltarRecipe.
     * Inputs and outputs are registry descriptors; an input may use the
     * {@code ore:} prefix and direct stacks may append {@code @metadata}.
     */
    public static void addAltarRecipe(final String input, final String output,
                                      final String costLP, final int level) {
        final BigInteger cost;
        try {
            cost = new BigInteger(costLP);
        } catch (NumberFormatException ex) {
            logSkippedRecipe("altar_" + input, "LP cost", costLP);
            return;
        }
        if (cost.signum() < 0 || level < 1 || level >= ALTAR_LEVEL_SPEED.length) {
            logSkippedRecipe("altar_" + input, "invalid level/cost", level + "/" + costLP);
            return;
        }

        final RequirementItem inputRequirement = parseInputRequirement(input);
        final ItemStack outputStack = resolveOutputStack(output, level, input);
        if (inputRequirement == null || outputStack == null || outputStack.isEmpty()) {
            return;
        }

        final String recipeName = altarRecipeName(input, output, level);
        final RecipePrimer recipe = newRecipe(recipeName, 1);
        recipe.appendComponent(inputRequirement);
        final RequirementItem outputRequirement = new RequirementItem(IOType.OUTPUT, outputStack);
        if (outputStack.hasTagCompound()) {
            outputRequirement.tag = outputStack.getTagCompound().copy();
            outputRequirement.previewDisplayTag = outputStack.getTagCompound().copy();
        }
        recipe.appendComponent(outputRequirement);
        recipe.addPreCheckHandler(event -> {
            final int altarLevel = event.getController().getCustomDataTag().getInteger(BloodAltarMachine.DATA_LEVEL);
            if (altarLevel < level) {
                event.setFailed(FAILURE_LEVEL);
            }
        });
        recipe.addFactoryStartHandler(event -> startAltarRecipe(event, cost, level));
        recipe.addFactoryPreTickHandler(BloodAltarRecipes::tickAltarRecipe);
        // A tiny client-side MMCE mixin expands the argument suffix while
        // retaining RecipePrimer's normal localization-key storage.
        recipe.addRecipeTooltip(TOOLTIP_LEVEL + "|" + level);
        recipe.addRecipeTooltip(TOOLTIP_COST + "|" + costLP);
        recipe.addRecipeTooltip(TOOLTIP_TIME);
        recipe.setThreadName(BloodAltarMachine.THREAD_CRAFTING);
        recipe.build();
    }

    @Nullable
    private static RequirementItem parseInputRequirement(final String descriptor) {
        if (descriptor == null || descriptor.isEmpty()) {
            logSkippedRecipe("altar", "input item", String.valueOf(descriptor));
            return null;
        }
        if (descriptor.startsWith("ore:")) {
            final String oreName = descriptor.substring("ore:".length());
            if (OreDictionary.getOres(oreName).isEmpty()) {
                logSkippedRecipe("altar", "input ore", oreName);
                return null;
            }
            return new RequirementItem(IOType.INPUT, oreName, 1);
        }
        ItemStack stack = resolveItemStack(descriptor);
        if (stack == null || stack.isEmpty()) {
            logSkippedRecipe("altar", "input item", descriptor);
            return null;
        }
        return new RequirementItem(IOType.INPUT, stack);
    }

    @Nullable
    private static ItemStack resolveOutputStack(final String descriptor, final int level,
                                                final String recipeInput) {
        ItemStack stack = resolveItemStack(descriptor);
        if (stack == null || stack.isEmpty()) {
            logSkippedRecipe("altar_" + recipeInput, "output item", descriptor);
            return null;
        }
        final ResourceLocation id = stack.getItem().getRegistryName();
        if (id == null) {
            logSkippedRecipe("altar_" + recipeInput, "output item", descriptor);
            return null;
        }
        if ("bloodmagic:blood_orb".equals(id.toString())) {
            final String[] orbNames = {"", "weak", "apprentice", "magician", "master", "archmage", "transcendent"};
            final BloodOrb[] orbs = {
                    null, RegistrarBloodMagic.ORB_WEAK, RegistrarBloodMagic.ORB_APPRENTICE,
                    RegistrarBloodMagic.ORB_MAGICIAN, RegistrarBloodMagic.ORB_MASTER,
                    RegistrarBloodMagic.ORB_ARCHMAGE, RegistrarBloodMagic.ORB_TRANSCENDENT
            };
            if (level >= orbs.length) {
                logSkippedRecipe("altar_" + recipeInput, "orb level", String.valueOf(level));
                return null;
            }
            // OrbRegistry creates the canonical Blood Magic stack and writes
            // the exact registered orb NBT. During early init ObjectHolder
            // injection can still be pending, so retain the script-compatible
            // NBT fallback instead of dropping the recipe.
            if (orbs[level] != null && orbs[level].getRegistryName() != null) {
                try {
                    final ItemStack canonicalOrb = OrbRegistry.getOrbStack(orbs[level]);
                    if (!canonicalOrb.isEmpty()) {
                        stack = canonicalOrb;
                    }
                } catch (RuntimeException ignored) {
                    // The explicit NBT fallback below is authoritative.
                }
            }
            final String expectedOrbId = "bloodmagic:" + orbNames[level];
            if (stack.getTagCompound() == null
                    || !expectedOrbId.equals(stack.getTagCompound().getString("orb"))) {
                final NBTTagCompound tag = new NBTTagCompound();
                tag.setString("orb", expectedOrbId);
                stack.setTagCompound(tag);
            }
        } else if ("forge:bucketfilled".equals(id.toString())) {
            if (FluidRegistry.getFluid("lifeessence") == null) {
                logSkippedRecipe("altar_" + recipeInput, "output fluid", "lifeessence");
                return null;
            }
            final NBTTagCompound tag = new NBTTagCompound();
            tag.setString("FluidName", "lifeessence");
            tag.setInteger("Amount", 1000);
            stack.setTagCompound(tag);
        } else if ("modularmachinery:itemblueprint".equals(id.toString())) {
            final NBTTagCompound tag = new NBTTagCompound();
            tag.setString("dynamicmachine", "modularmachinery:builder_3");
            stack.setTagCompound(tag);
        }
        return stack;
    }

    @Nullable
    private static ItemStack resolveItemStack(final String descriptor) {
        final String[] parts = descriptor.split("@", 2);
        final Item item;
        try {
            item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(parts[0]));
        } catch (RuntimeException ex) {
            return null;
        }
        if (item == null) {
            return null;
        }
        int metadata = 0;
        if (parts.length > 1) {
            if ("*".equals(parts[1])) {
                metadata = OreDictionary.WILDCARD_VALUE;
            } else {
                try {
                    metadata = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ex) {
                    return null;
                }
            }
        }
        return new ItemStack(item, 1, metadata);
    }

    private static String altarRecipeName(final String input, final String output, final int level) {
        String name = "altar_" + input + "_to_" + output + "_" + level;
        name = name.replace(':', '_').replace('@', '_').replace('*', 'w').replaceAll("[^a-zA-Z0-9_.-]", "_");
        return name.length() > 240 ? name.substring(0, 240) : name;
    }

    private static void startAltarRecipe(final FactoryRecipeStartEvent event,
                                         final BigInteger baseCost, final int level) {
        final TileMultiblockMachineController controller = event.getController();
        final int altarSpeed = Math.max(0, controller.getCustomDataTag().getInteger(BloodAltarMachine.DATA_SPEED));
        final int economyRunes = Math.min(16, Math.max(0, controller.getBlocksInPattern(BloodAltarMachine.RUNE_ECONOMY)));
        final BigDecimal economy = new BigDecimal("0.9").pow(economyRunes);
        final BigDecimal speed = BigDecimal.valueOf(altarSpeed)
                .divide(BigDecimal.valueOf(20L), 8, RoundingMode.DOWN)
                .multiply(BigDecimal.valueOf(ALTAR_LEVEL_SPEED[level]));
        final BigDecimal effectiveSpeed = speed.max(BigDecimal.ONE);
        final BigInteger discountedCost = new BigDecimal(baseCost).multiply(economy)
                .setScale(0, RoundingMode.DOWN).toBigInteger();
        // Keep the script's timing semantics: divide the discounted decimal
        // cost first, then round the resulting duration down (the LP charge
        // itself is rounded independently to an integer).
        final int duration = Math.max(1, new BigDecimal(baseCost).multiply(economy)
                .divide(effectiveSpeed, 0, RoundingMode.DOWN).intValue());
        final int parallelism = Math.max(1, event.getActiveRecipe().getParallelism());
        final NBTTagCompound data = event.getActiveRecipe().getDataCompound();
        data.setString(DATA_ALTAR_COST, discountedCost.multiply(BigInteger.valueOf(parallelism)).toString());
        data.setString(DATA_ALTAR_REMAINING_COST,
                discountedCost.multiply(BigInteger.valueOf(parallelism)).toString());
        data.setInteger(DATA_ALTAR_REMAINING_TICKS, duration);
        data.setInteger(DATA_ALTAR_LEVEL, level);
        event.getFactoryRecipeThread().addModifier("gctcore_altar_duration",
                new RecipeModifier(RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT,
                        Math.max(1, duration), RecipeModifier.OPERATION_MULTIPLY, false));
    }

    private static void tickAltarRecipe(final FactoryRecipeTickEvent event) {
        final NBTTagCompound data = event.getActiveRecipe().getDataCompound();
        if (!data.hasKey(DATA_ALTAR_REMAINING_TICKS) || !data.hasKey(DATA_ALTAR_REMAINING_COST)) {
            return;
        }
        final BigInteger remainingCost;
        try {
            remainingCost = new BigInteger(data.getString(DATA_ALTAR_REMAINING_COST));
        } catch (NumberFormatException ex) {
            return;
        }
        final int remainingTicks = Math.max(1, data.getInteger(DATA_ALTAR_REMAINING_TICKS));
        if (remainingCost.signum() <= 0) {
            data.setInteger(DATA_ALTAR_REMAINING_TICKS, Math.max(0, remainingTicks - 1));
            return;
        }
        final BigInteger charge = remainingCost.add(BigInteger.valueOf(remainingTicks - 1L))
                .divide(BigInteger.valueOf(remainingTicks));
        final BigInteger stored = BloodAltarMachine.getStoredLifeEssence(event.getController());
        if (stored.compareTo(charge) < 0) {
            event.preventProgressing(FAILURE_LIFE_ESSENCE);
            return;
        }
        BloodAltarMachine.setStoredLifeEssence(event.getController(), stored.subtract(charge));
        data.setString(DATA_ALTAR_REMAINING_COST, remainingCost.subtract(charge).toString());
        data.setInteger(DATA_ALTAR_REMAINING_TICKS, Math.max(0, remainingTicks - 1));
    }

    private static RecipePrimer newRecipe(final String name, final int ticks) {
        ResourceLocation recipeId = new ResourceLocation(name);
        if ("minecraft".equals(recipeId.getNamespace())) {
            recipeId = new ResourceLocation("modularmachinery", recipeId.getPath());
        }
        return new RecipePrimer(recipeId, BloodAltarMachine.MACHINE_ID, ticks, 0, false);
    }

    @Nullable
    private static RequirementItem nonConsumedItem(final String recipeName, final String role,
                                                   final String registryName, final int metadata) {
        final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(registryName));
        if (item == null) {
            logSkippedRecipe(recipeName, role + " item", registryName);
            return null;
        }
        final RequirementItem requirement = new RequirementItem(IOType.INPUT, new ItemStack(item, 1, metadata));
        requirement.setChance(0.0F);
        return requirement;
    }

    @Nullable
    private static RequirementItem nonConsumedOrb() {
        final RequirementItem requirement = nonConsumedItem("orb", "input", "bloodmagic:blood_orb",
                OreDictionary.WILDCARD_VALUE);
        if (requirement == null) {
            return null;
        }

        // RequirementItem matches NBT only through its `tag` field. Keep it
        // null: previewDisplayTag below is for JEI only, so every Blood Magic
        // orb NBT variant is accepted by the actual recipe check.
        requirement.tag = null;
        final NBTTagCompound display = new NBTTagCompound();
        final NBTTagList lore = new NBTTagList();
        display.setTag("Lore", lore);
        final NBTTagCompound preview = new NBTTagCompound();
        preview.setString("orb", "bloodmagic:weak");
        preview.setTag("display", display);
        requirement.previewDisplayTag = preview;
        return requirement;
    }

    @Nullable
    private static Fluid findFluid(final String recipeName, final String role, final String fluidName) {
        final Fluid fluid = FluidRegistry.getFluid(fluidName);
        if (fluid == null) {
            logSkippedRecipe(recipeName, role + " fluid", fluidName);
        }
        return fluid;
    }

    /** Use this helper for every future item or fluid input/output before building its recipe. */
    private static void logSkippedRecipe(final String recipeName, final String missingType,
                                         final String registryName) {
        gctcore.LOGGER.warn("[Blood Altar] Skipping recipe '{}': {} '{}' is missing or invalid.",
                recipeName, missingType, registryName);
    }
}
