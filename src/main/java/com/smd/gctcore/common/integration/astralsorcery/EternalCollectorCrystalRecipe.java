package com.smd.gctcore.common.integration.astralsorcery;

import hellfirepvp.astralsorcery.common.block.network.BlockCollectorCrystalBase;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crafting.altar.recipes.ConstellationRecipe;
import hellfirepvp.astralsorcery.common.crafting.helper.ShapeMap;
import hellfirepvp.astralsorcery.common.crafting.helper.ShapedRecipeSlot;
import hellfirepvp.astralsorcery.common.item.ItemCraftingComponent;
import hellfirepvp.astralsorcery.common.item.block.ItemCollectorCrystal;
import hellfirepvp.astralsorcery.common.item.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.item.crystal.base.ItemTunedCrystalBase;
import hellfirepvp.astralsorcery.common.item.useables.ItemUsableDust;
import hellfirepvp.astralsorcery.common.tile.TileAltar;
import hellfirepvp.astralsorcery.common.util.OreDictAlias;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * Eternal counterpart of Astral Sorcery's celestial collector crystal recipe.
 * The altar layout, inputs, starlight cost and constellation slots are identical.
 */
public class EternalCollectorCrystalRecipe extends ConstellationRecipe {

    private final ItemStack renderOutput;

    public EternalCollectorCrystalRecipe() {
        super(shapedRecipe("eternalcollectorcrystal", AstralSorceryExpansion.ETERNAL_COLLECTOR_CRYSTAL)
                .addPart(AstralSorceryExpansion.TUNED_ETERNAL_CRYSTAL, ShapedRecipeSlot.CENTER)
                .forceEmptySpaces()
                .unregisteredAccessibleShapedRecipe());
        setAttItem(ItemCraftingComponent.MetaType.RESO_GEM.asStack(), AttunementAltarSlot.values());
        setCstItem(OreDictAlias.ITEM_STARMETAL_DUST,
                ConstellationAtlarSlot.UP_UP_LEFT,
                ConstellationAtlarSlot.UP_UP_RIGHT,
                ConstellationAtlarSlot.DOWN_DOWN_LEFT,
                ConstellationAtlarSlot.DOWN_DOWN_RIGHT);
        setCstItem(ItemUsableDust.DustType.ILLUMINATION.asStack(),
                ConstellationAtlarSlot.UP_LEFT_LEFT,
                ConstellationAtlarSlot.UP_RIGHT_RIGHT,
                ConstellationAtlarSlot.DOWN_LEFT_LEFT,
                ConstellationAtlarSlot.DOWN_RIGHT_RIGHT);
        setPassiveStarlightRequirement(2600);

        renderOutput = new ItemStack(AstralSorceryExpansion.ETERNAL_COLLECTOR_CRYSTAL);
        ItemCollectorCrystal.setType(renderOutput,
                BlockCollectorCrystalBase.CollectorCrystalType.CELESTIAL_CRYSTAL);
    }

    @Nonnull
    @Override
    public ItemStack getOutput(ShapeMap centralGridMap, TileAltar altar) {
        ItemStack center = centralGridMap.get(ShapedRecipeSlot.CENTER).getApplicableItems().get(0);
        if (center.isEmpty() || !(center.getItem() instanceof ItemTunedCrystalBase)) {
            return ItemStack.EMPTY;
        }

        ItemStack output = super.getOutput(centralGridMap, altar);
        IWeakConstellation attuned = ItemTunedCrystalBase.getMainConstellation(center);
        IMinorConstellation trait = ItemTunedCrystalBase.getTrait(center);
        CrystalProperties properties = CrystalProperties.getCrystalProperties(center);
        if (attuned == null || properties == null) {
            return ItemStack.EMPTY;
        }

        ItemCollectorCrystal.setConstellation(output, attuned);
        ItemCollectorCrystal.setTraitConstellation(output, trait);
        CrystalProperties.applyCrystalProperties(output, properties);
        ItemCollectorCrystal.setType(output,
                BlockCollectorCrystalBase.CollectorCrystalType.CELESTIAL_CRYSTAL);
        return output;
    }

    @Nonnull
    @Override
    public ItemStack getOutputForRender() {
        return renderOutput;
    }
}
