package com.smd.gctcore.common.events;

import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;
import com.smd.gctcore.common.config.GCTCompatConfig;
import com.smd.gctcore.common.config.GCTCoreConfig;
import com.smd.gctcore.misc.ItemRegistry;
import mod.acgaming.universaltweaks.config.UTConfigTweaks;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.command.CommandDifficulty;
import net.minecraft.command.CommandGameRule;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

public class EventHooks {

    public static final EventHooks INSTANCE = new EventHooks();

    private static final String BIRD_OF_EDWIN_GIVEN = "gctcore:bird_of_edwin_given";

    @SubscribeEvent(priority = EventPriority.LOWEST)
    @SideOnly(Side.CLIENT)
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        BiMap<String, Fluid> masterFluidReference = ObfuscationReflectionHelper.getPrivateValue(FluidRegistry.class, null, "masterFluidReference");
        TextureMap map = event.getMap();

        for (Fluid fluid : masterFluidReference.values()) {
            map.registerSprite(fluid.getStill());
            map.registerSprite(fluid.getFlowing());
        }
    }

    @SubscribeEvent
    public void blockBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!event.getEntityPlayer().world.isRemote) {
            if (!event.getEntityPlayer().onGround && (event.getEntityPlayer().capabilities.isFlying)) {
                event.setNewSpeed(event.getOriginalSpeed() * 5);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        NBTTagCompound entityData = player.getEntityData();
        NBTTagCompound persisted = entityData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        if (!entityData.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            entityData.setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);
        }
        if (persisted.hasKey(BIRD_OF_EDWIN_GIVEN)) {
            return;
        }

        persisted.setBoolean(BIRD_OF_EDWIN_GIVEN, true);
        ItemStack bird = new ItemStack(ItemRegistry.BIRD_OF_EDWIN);
        if (!player.inventory.addItemStackToInventory(bird)) {
            player.dropItem(bird, false);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.SERVER)
    public static void onCommandEvent(CommandEvent event) {
        ICommand command = event.getCommand();

        if (!(command instanceof CommandDifficulty || command instanceof CommandGameRule)) {
            return;
        }

        ICommandSender sender = event.getSender();
        boolean isDedicated = sender.getServer().isDedicatedServer();
        boolean isPlayer = sender instanceof EntityPlayer;
        boolean isPlayerMP = sender instanceof EntityPlayerMP;
        boolean isConsole = sender instanceof DedicatedServer;

        boolean allow = (isDedicated && isPlayer) || (!isDedicated && (isPlayerMP || isConsole));
        event.setCanceled(!allow);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderFog(EntityViewRenderEvent.FogDensity event) {
        if(GCTCoreConfig.cleanwater.enableWater && event.getState().getMaterial() == Material.WATER) {
            GlStateManager.setFog(GlStateManager.FogMode.EXP);
            event.setDensity((float) GCTCoreConfig.cleanwater.fogDensityWater);
            event.setCanceled(true);
        } else if(GCTCoreConfig.cleanwater.enableLava && event.getState().getMaterial() == Material.LAVA) {
            GlStateManager.setFog(GlStateManager.FogMode.EXP);
            event.setDensity((float) GCTCoreConfig.cleanwater.fogDensityLava);
            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public static void onPlayerLogin(PlayerLoggedInEvent event) {

        if (!Loader.isModLoaded("universaltweaks")) {
            return;
        }

        if (UTConfigTweaks.MISC.utRecipeBookToggle) {
            return;
        }

        if (event.player instanceof EntityPlayerMP) {
            ArrayList<IRecipe> recipes = Lists.newArrayList(CraftingManager.REGISTRY);
            recipes.removeIf(recipe -> recipe.getRecipeOutput().isEmpty());
            recipes.removeIf(recipe -> recipe.getIngredients().isEmpty());
            event.player.unlockRecipes(recipes);
        }
    }
}
