package com.smd.gctcore.common.stats;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public final class LazyStatNameHelper {

    public static final byte KIND_NONE = 0;
    public static final byte KIND_MINE_BLOCK = 1;
    public static final byte KIND_CRAFT_ITEM = 2;
    public static final byte KIND_USE_ITEM = 3;
    public static final byte KIND_BREAK_ITEM = 4;
    public static final byte KIND_PICKUP = 5;
    public static final byte KIND_DROP = 6;

    private static final Style NOOP_STYLE = new Style() {
        @Override
        public Style setParentStyle(Style parent) {
            return this;
        }
    };

    /**
     * Singleton placeholder returned in place of {@code new ItemStack(...).getTextComponent()} inside
     * {@link net.minecraft.stats.StatList}. Never holds parent styles or item NBT.
     */
    public static final ITextComponent LAZY_STAT_SENTINEL = new TextComponentString("") {
        @Override
        public Style getStyle() {
            return NOOP_STYLE;
        }

        @Override
        public TextComponentString createCopy() {
            return this;
        }
    };

    private LazyStatNameHelper() {
    }

    public static byte detectLazyKind(ITextComponent component) {
        if (!(component instanceof TextComponentTranslation)) {
            return KIND_NONE;
        }
        TextComponentTranslation translation = (TextComponentTranslation) component;
        Object[] args = translation.getFormatArgs();
        if (args == null || args.length != 1 || args[0] != LAZY_STAT_SENTINEL) {
            return KIND_NONE;
        }
        String key = translation.getKey();
        if ("stat.mineBlock".equals(key)) return KIND_MINE_BLOCK;
        if ("stat.craftItem".equals(key)) return KIND_CRAFT_ITEM;
        if ("stat.useItem".equals(key)) return KIND_USE_ITEM;
        if ("stat.breakItem".equals(key)) return KIND_BREAK_ITEM;
        if ("stat.pickup".equals(key)) return KIND_PICKUP;
        if ("stat.drop".equals(key)) return KIND_DROP;
        return KIND_NONE;
    }

    public static String getTranslationKey(byte kind) {
        switch (kind) {
            case KIND_MINE_BLOCK:
                return "stat.mineBlock";
            case KIND_CRAFT_ITEM:
                return "stat.craftItem";
            case KIND_USE_ITEM:
                return "stat.useItem";
            case KIND_BREAK_ITEM:
                return "stat.breakItem";
            case KIND_PICKUP:
                return "stat.pickup";
            case KIND_DROP:
                return "stat.drop";
            default:
                return null;
        }
    }
}
