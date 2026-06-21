package com.smd.gctcore.common.integration;

import com.smd.gctcore.common.world.AirportDim.DimensionTypeAirport;
import com.smd.gctcore.common.world.NothingnessDim.DimensionTypeNothingness;
import com.smd.gctcore.misc.Mods;
import de.ellpeck.naturesaura.api.NaturesAuraAPI;
import hellfirepvp.astralsorcery.common.data.config.Config;
import net.minecraft.world.DimensionType;

import java.util.Collections;
import java.util.List;

public final class WorldDimensionIntegrations {

    public static void init() {
        if (Mods.AS.isLoading()) {
            initAstralSorcery();
        }
        if (Mods.NATURES_AURA.isLoading()) {
            initNaturesAura();
        }
    }

    private static void initAstralSorcery() {
        int airport = DimensionTypeAirport.Airport.getId();
        int nothingness = DimensionTypeNothingness.nothingness.getId();

        addSorted(Config.constellationSkyDimWhitelist, airport);
        addSorted(Config.constellationSkyDimWhitelist, nothingness);
        addSorted(Config.weakSkyRendersWhitelist, airport);
        addSorted(Config.weakSkyRendersWhitelist, nothingness);
        addSorted(Config.worldGenDimWhitelist, airport);
        addSorted(Config.worldGenDimWhitelist, nothingness);
    }

    private static void initNaturesAura() {
        addAuraDimension(NaturesAuraAPI.TYPE_OVERWORLD, DimensionTypeAirport.Airport);
        addAuraDimension(NaturesAuraAPI.TYPE_OVERWORLD, DimensionTypeNothingness.nothingness);
    }

    private static void addAuraDimension(de.ellpeck.naturesaura.api.aura.type.BasicAuraType type, DimensionType dimensionType) {
        if (type != null && dimensionType != null) {
            type.addDimensionType(dimensionType);
        }
    }

    private static void addSorted(List<Integer> values, int value) {
        if (values == null || values.contains(value)) {
            return;
        }
        values.add(value);
        Collections.sort(values);
    }
}
