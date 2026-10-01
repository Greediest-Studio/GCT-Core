package com.gmm.gctold.common.events;

import com.gmm.gctold.common.blocks.BlockAzathothiumOre;
import com.gmm.gctold.common.blocks.BlockAzathothiumOreComplex;
import com.gmm.gctold.common.blocks.BlockNyarlathotepiumOre;
import com.gmm.gctold.common.blocks.BlockNyralathotepiumOreComplex;
import com.gmm.gctold.common.blocks.BlockSeekFinder;
import com.gmm.gctold.common.blocks.BlockShubniggurathiumOre;
import com.gmm.gctold.common.blocks.BlockShubniggurathiumOreComplex;
import com.gmm.gctold.common.blocks.BlockSolidPotEnergy;
import com.gmm.gctold.common.blocks.BlockYogsothothiumOre;
import com.gmm.gctold.common.blocks.BlockYogsothothiumOreComplex;
import com.gmm.gctold.common.entity.EntityZjarugoth;
import com.gmm.gctold.common.potions.PotionAbyssPlague;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public final class SeekAltarUpdateTick {
    private static final int TIMER_FINISH = 40;
    private static final OreRecipe[] ORE_RECIPES = {
            new OreRecipe("azathothium_ore", BlockAzathothiumOreComplex.block, BlockAzathothiumOre.block),
            new OreRecipe("nyarlathotepium_ore", BlockNyralathotepiumOreComplex.block, BlockNyarlathotepiumOre.block),
            new OreRecipe("yogsothothium_ore", BlockYogsothothiumOreComplex.block, BlockYogsothothiumOre.block),
            new OreRecipe("shubniggurathium_ore", BlockShubniggurathiumOreComplex.block, BlockShubniggurathiumOre.block)
    };

    private SeekAltarUpdateTick() {
    }

    public static void run(World world, int x, int y, int z) {
        BlockPos altarPos = new BlockPos(x, y, z);
        TileEntity altar = world.getTileEntity(altarPos);
        if (altar == null) {
            return;
        }

        if (!hasSeekFinders(world, altarPos)) {
            setWorkState(world, altarPos, altar, false, 0.0D);
            return;
        }

        if (!world.isRemote) {
            altar.getTileData().setBoolean("active", true);
            notify(world, altarPos);
        }
        spawnIdleParticles(world, x, y, z);

        String recipeName = altar.getTileData().getString("recipeName");
        OreRecipe oreRecipe = findOreRecipe(recipeName);
        if (oreRecipe != null) {
            processOreRecipe(world, altarPos, altar, oreRecipe);
        } else if ("zjarugoth_summon".equals(recipeName)) {
            processZjarugothSummon(world, altarPos, altar);
        } else {
            setWorkState(world, altarPos, altar, false, 0.0D);
        }
    }

    private static void processOreRecipe(World world, BlockPos altarPos, TileEntity altar, OreRecipe recipe) {
        playChant(world, altarPos);

        double timer = altar.getTileData().getDouble("timer") + 1.0D;
        if (!world.isRemote) {
            altar.getTileData().setBoolean("work", true);
            altar.getTileData().setDouble("timer", timer);
            notify(world, altarPos);
        }

        if (world instanceof WorldServer) {
            ((WorldServer) world).spawnParticle(EnumParticleTypes.LAVA,
                    altarPos.getX(), altarPos.getY() + 11.0D - timer / 4.0D, altarPos.getZ(),
                    30, 0.0D, 0.0D, 0.0D, 0.8D);
        }

        if (timer > TIMER_FINISH) {
            setWorkState(world, altarPos, altar, true, 0.0D);
            return;
        }

        if (timer == TIMER_FINISH) {
            if (hasPotEnergy(world, altarPos) && world.getBlockState(altarPos.up()).getBlock() == recipe.input) {
                consumePotEnergy(world, altarPos);
                world.setBlockState(altarPos.up(), recipe.output.getDefaultState(), 3);
                resetRecipe(world, altarPos, altar);
            } else {
                failRecipe(world, altarPos, altar, true);
            }
        }
    }

    private static void processZjarugothSummon(World world, BlockPos altarPos, TileEntity altar) {
        if (hasPotEnergy(world, altarPos)) {
            consumePotEnergy(world, altarPos);
            sendMessageToNearbyPlayers(world, altarPos, 64.0D, "<Zjarugoth>Ahf' ymg' ah ah geb? llll epgoka ehye!");
            if (!world.isRemote) {
                EntityZjarugoth.ZjarugothEntity zjarugoth = new EntityZjarugoth.ZjarugothEntity(world);
                zjarugoth.setLocationAndAngles(altarPos.getX(), altarPos.getY() + 16, altarPos.getZ(),
                        world.rand.nextFloat() * 360.0F, 0.0F);
                world.spawnEntity(zjarugoth);
            }
            resetRecipe(world, altarPos, altar);
        } else {
            failRecipe(world, altarPos, altar, false);
        }
    }

    private static boolean hasSeekFinders(World world, BlockPos altarPos) {
        return blockAt(world, altarPos.add(3, 6, 0)) == BlockSeekFinder.block
                && blockAt(world, altarPos.add(-3, 6, 0)) == BlockSeekFinder.block
                && blockAt(world, altarPos.add(0, 6, 3)) == BlockSeekFinder.block
                && blockAt(world, altarPos.add(0, 6, -3)) == BlockSeekFinder.block;
    }

    private static boolean hasPotEnergy(World world, BlockPos altarPos) {
        return blockAt(world, altarPos.add(3, 5, 0)) == BlockSolidPotEnergy.block
                && blockAt(world, altarPos.add(-3, 5, 0)) == BlockSolidPotEnergy.block
                && blockAt(world, altarPos.add(0, 5, 3)) == BlockSolidPotEnergy.block
                && blockAt(world, altarPos.add(0, 5, -3)) == BlockSolidPotEnergy.block;
    }

    private static void consumePotEnergy(World world, BlockPos altarPos) {
        world.destroyBlock(altarPos.add(3, 5, 0), false);
        world.destroyBlock(altarPos.add(-3, 5, 0), false);
        world.destroyBlock(altarPos.add(0, 5, 3), false);
        world.destroyBlock(altarPos.add(0, 5, -3), false);
    }

    private static void failRecipe(World world, BlockPos altarPos, TileEntity altar, boolean destroyInput) {
        consumePotEnergy(world, altarPos);
        if (destroyInput) {
            world.destroyBlock(altarPos.up(), false);
        }
        world.addWeatherEffect(new EntityLightningBolt(world, altarPos.getX(), altarPos.getY(), altarPos.getZ(), false));
        EffectHelper.addEffectToPlayers(world, altarPos, 16.0D, PotionAbyssPlague.potion, 300, 0, false, false);
        resetRecipe(world, altarPos, altar);
    }

    private static void resetRecipe(World world, BlockPos altarPos, TileEntity altar) {
        if (!world.isRemote) {
            altar.getTileData().setString("recipeName", "");
            altar.getTileData().setBoolean("work", false);
            altar.getTileData().setDouble("timer", 0.0D);
            notify(world, altarPos);
        }
    }

    private static void setWorkState(World world, BlockPos altarPos, TileEntity altar, boolean work, double timer) {
        if (!world.isRemote) {
            altar.getTileData().setBoolean("work", work);
            altar.getTileData().setDouble("timer", timer);
            notify(world, altarPos);
        }
    }

    private static void spawnIdleParticles(World world, int x, int y, int z) {
        if (world instanceof WorldServer) {
            WorldServer serverWorld = (WorldServer) world;
            serverWorld.spawnParticle(EnumParticleTypes.SPELL_WITCH, x, y + 2, z, 40, 1.0D, 1.0D, 1.0D, 0.1D);
            serverWorld.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x + 3, y + 4, z, 10, 1.0D, 2.0D, 1.0D, 0.1D);
            serverWorld.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x - 3, y + 4, z, 10, 1.0D, 2.0D, 1.0D, 0.1D);
            serverWorld.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, y + 4, z + 3, 10, 1.0D, 2.0D, 1.0D, 0.1D);
            serverWorld.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, y + 4, z - 3, 10, 1.0D, 2.0D, 1.0D, 0.1D);
        }
    }

    private static void playChant(World world, BlockPos altarPos) {
        SoundEvent sound = SoundEvent.REGISTRY.getObject(new ResourceLocation("gctcore:chant_abyss"));
        if (sound != null) {
            world.playSound((EntityPlayer) null, altarPos, sound, SoundCategory.NEUTRAL, 4.0F, 1.0F);
        }
    }

    private static OreRecipe findOreRecipe(String recipeName) {
        for (OreRecipe recipe : ORE_RECIPES) {
            if (recipe.name.equals(recipeName)) {
                return recipe;
            }
        }
        return null;
    }

    private static Block blockAt(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock();
    }

    private static void notify(World world, BlockPos pos) {
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }

    private static void sendMessageToNearbyPlayers(World world, BlockPos pos, double radius, String message) {
        if (world.isRemote) {
            return;
        }
        TextComponentString component = new TextComponentString(message);
        AxisAlignedBB area = new AxisAlignedBB(pos).grow(radius);
        double maxDistance = radius * radius;
        for (EntityPlayer player : world.getEntitiesWithinAABB(EntityPlayer.class, area,
                player -> player.getDistanceSq(pos) <= maxDistance)) {
            player.sendMessage(component);
        }
    }

    private static final class OreRecipe {
        private final String name;
        private final Block input;
        private final Block output;

        private OreRecipe(String name, Block input, Block output) {
            this.name = name;
            this.input = input;
            this.output = output;
        }
    }
}
