package knightminer.tcomplement.library;

import net.minecraft.item.ItemStack;

/** 仅供编译；实现由 Tinkers' Complement 提供，不打入 GCT-Core。 */
public interface IBlacklist {
    boolean matches(ItemStack stack);
}
