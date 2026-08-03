package com.smd.gctcore.client.extendedcrafting;

import com.smd.gctcore.common.integration.extendedcrafting.ExtendedCraftingTier;

final class ExtendedGuiTheme {
    private ExtendedGuiTheme() { }

    static int accent(ExtendedCraftingTier tier) {
        switch (tier) {
            case ADVANCED: return 0xFFFFD83D;
            case ELITE: return 0xFF21DCE9;
            case ULTIMATE: return 0xFF47E97B;
            default: return 0xFFD9E1E8;
        }
    }

    static int text(ExtendedCraftingTier tier) {
        switch (tier) {
            case ADVANCED: return 0xFFFFF19A;
            case ELITE: return 0xFFA8FBFF;
            case ULTIMATE: return 0xFFB5FFC9;
            default: return 0xFFF4F7FA;
        }
    }
}
