package com.smd.gctcore.common.mixin.aether;

import com.gildedgames.the_aether.tile_entities.TileEntityTreasureChest;
import com.smd.gctcore.common.integration.aether.AetherDungeonChestKind;
import com.smd.gctcore.gctcore;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(value = TileEntityTreasureChest.class, remap = false)
public abstract class MixinDiamondDungeonLoot {

    private static final String GENERATED_KEY = "GCTCoreDiamondRewardGenerated";
    private static final String[] REWARD_IDS = {
            "aether_legacy:gummy_swet",
            "aether_legacy:gummy_swet",
            "tinkersaether:valkyrie_ingot",
            "aether_legacy:zanite_gemstone",
            "aether_legacy:ambrosium_shard",
            "aether_legacy:golden_amber",
            "additions:aetherium_ingot",
            "gct_additions:gravity_debris",
            "gct_additions:gravity_scrap",
            "additions:aetherite_ingot"
    };
    private static final int[] REWARD_METADATA = {0, 1, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final int[] REWARD_MIN_COUNT = {4, 4, 5, 12, 12, 12, 3, 1, 1, 1};
    private static final int[] REWARD_MAX_COUNT = {20, 20, 8, 16, 20, 16, 6, 1, 2, 1};

    @Inject(method = "fillWithLoot(Lnet/minecraft/entity/player/EntityPlayer;)V",
            at = @At("HEAD"), cancellable = true, remap = true, require = 1)
    private void gctcore$fillDiamondRewards(EntityPlayer player, CallbackInfo ci) {
        TileEntityTreasureChest chest = (TileEntityTreasureChest) (Object) this;
        World world = chest.getWorld();
        if (world == null || world.isRemote
                || chest.getKind() != AetherDungeonChestKind.GCT_DIAMOND_REWARD) {
            return;
        }

        NBTTagCompound data = chest.getTileData();
        if (data.getBoolean(GENERATED_KEY)) {
            ci.cancel();
            return;
        }

        if (!chest.isEmpty()) {
            data.setBoolean(GENERATED_KEY, true);
            chest.setLootTable(null, 0L);
            chest.markDirty();
            ci.cancel();
            return;
        }

        Item[] availableItems = new Item[REWARD_IDS.length];
        List<Integer> available = new ArrayList<>();
        for (int i = 0; i < REWARD_IDS.length; i++) {
            ResourceLocation itemId = new ResourceLocation(REWARD_IDS[i]);
            Item item = ForgeRegistries.ITEMS.getValue(itemId);
            if (item == null) {
                gctcore.LOGGER.warn("Diamond dungeon reward item {} is unavailable; omitting this candidate.", itemId);
            } else {
                availableItems[i] = item;
                available.add(i);
            }
        }

        if (available.isEmpty()) {
            gctcore.LOGGER.error("No diamond dungeon reward items are registered; leaving the chest unfilled.");
            ci.cancel();
            return;
        }

        // Mark before writing slots because setInventorySlotContents re-enters fillWithLoot.
        data.setBoolean(GENERATED_KEY, true);
        chest.setLootTable(null, 0L);
        chest.markDirty();

        int rolls = MathHelper.getInt(world.rand, 10, 12);
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < chest.getSizeInventory(); i++) {
            if (chest.getStackInSlot(i).isEmpty()) {
                slots.add(i);
            }
        }
        Collections.shuffle(slots, world.rand);

        for (int i = 0; i < rolls && i < slots.size(); i++) {
            int reward = available.get(world.rand.nextInt(available.size()));
            int count = MathHelper.getInt(world.rand, REWARD_MIN_COUNT[reward], REWARD_MAX_COUNT[reward]);
            chest.setInventorySlotContents(slots.get(i),
                    new ItemStack(availableItems[reward], count, REWARD_METADATA[reward]));
        }

        chest.markDirty();
        ci.cancel();
    }
}
