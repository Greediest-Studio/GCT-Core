package com.smd.gctcore.common.entity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Blocks;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Exercises the real entity damage/death/NBT paths without a renderer or a game server. */
class CloudGiantPriestTest {
    private EntityCloudGiantPriest priest;
    private EntityCloudGiantPriest attacker;
    private TestWorld world;

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @BeforeEach
    void createEntities() {
        world = new TestWorld();
        priest = new EntityCloudGiantPriest(world);
        attacker = new EntityCloudGiantPriest(world);
        attacker.setPosition(8, 64, 0);
        priest.setPosition(0, 64, 0);
    }

    @Test
    void environmentalDamageCannotHurtButEntityAndProjectileDamageCan() {
        for (DamageSource source : new DamageSource[] {
                DamageSource.GENERIC, DamageSource.FALL, DamageSource.DROWN,
                DamageSource.IN_WALL, DamageSource.LAVA, DamageSource.OUT_OF_WORLD}) {
            assertFalse(priest.attackEntityFrom(source, 200));
        }
        assertEquals(500, priest.getMaxHealth(), 0.001);
        assertEquals(500, priest.getHealth(), 0.001);
        assertTrue(priest.attackEntityFrom(entityDamage(), 5));
        assertEquals(495, priest.getHealth(), 0.001);
        priest.hurtResistantTime = 0;
        assertTrue(priest.attackEntityFrom(new EntityDamageSourceIndirect("arrow", attacker, null), 6));
        assertEquals(489, priest.getHealth(), 0.001);
    }

    @Test
    void damageCapTracksMaxHealthAndPreservesVanillaInvulnerabilityFrames() {
        assertTrue(priest.attackEntityFrom(entityDamage(), 1000));
        assertEquals(480, priest.getHealth(), 0.001);
        assertFalse(priest.attackEntityFrom(entityDamage(), 1000));
        assertEquals(480, priest.getHealth(), 0.001);

        priest.hurtResistantTime = 0;
        priest.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(800);
        priest.setHealth(800);
        assertTrue(priest.attackEntityFrom(entityDamage(), 1000));
        assertEquals(768, priest.getHealth(), 0.001);

        priest.hurtResistantTime = 0;
        assertTrue(priest.attackEntityFrom(entityDamage(), 5));
        assertTrue(priest.attackEntityFrom(entityDamage(), 10));
        assertEquals(758, priest.getHealth(), 0.001);
    }

    @Test
    void damageCapStillAppliesWhenForgeDamageEventsAmplifyTheHit() throws Exception {
        // Register directly because the normal EventBus registration expects
        // constructors injected by Forge's game-launch class transformer.
        Field busIdField = EventBus.class.getDeclaredField("busID");
        busIdField.setAccessible(true);
        int busId = busIdField.getInt(MinecraftForge.EVENT_BUS);
        IEventListener listener = event -> {
            if (event instanceof LivingDamageEvent) {
                LivingDamageEvent damage = (LivingDamageEvent) event;
                if (damage.getEntityLiving() == priest) {
                    damage.setAmount(damage.getAmount() * 100);
                }
            }
        };
        Event rootEvent = new Event();
        rootEvent.getListenerList().register(busId, EventPriority.NORMAL, listener);
        try {
            assertTrue(priest.attackEntityFrom(entityDamage(), 10));
            assertEquals(480, priest.getHealth(), 0.001);
        } finally {
            rootEvent.getListenerList().unregister(busId, listener);
        }
    }

    @Test
    void randomNameSurvivesNbtRoundTrip() {
        String originalName = priest.getName();
        assertTrue(originalName.endsWith(", 云巨人祭司"));
        NBTTagCompound saved = new NBTTagCompound();
        priest.writeEntityToNBT(saved);
        EntityCloudGiantPriest restored = new EntityCloudGiantPriest(world);
        restored.readEntityFromNBT(saved);
        assertEquals(originalName, restored.getName());
    }

    @Test
    void corpseSurvivesVanillaDeathDeadlineAndDisappearsAfterAnimationPlusThreeSeconds() {
        priest.setHealth(1);
        assertTrue(priest.attackEntityFrom(entityDamage(), 16));
        assertFalse(priest.isEntityAlive());
        for (int tick = 1; tick < 124; tick++) {
            priest.onDeathUpdate();
            assertFalse(priest.isDead, "corpse removed at death tick " + tick);
        }
        priest.onDeathUpdate();
        assertTrue(priest.isDead);
    }

    @Test
    void sweepHasARealHitWindowAndCannotRestartUntilItsAnimationCompletes() throws Exception {
        RecordingTarget target = new RecordingTarget(world);
        target.setPosition(5, 64, 0);
        assertTrue(priest.attackEntityAsMob(target));
        assertFalse(priest.attackEntityAsMob(target));
        for (int tick = 1; tick < EntityCloudGiantPriest.SWEEP_HIT_TICK; tick++) {
            tickAction();
            assertEquals(0, target.hits, "sweep hit before animation tick " + tick);
        }
        tickAction();
        assertEquals(1, target.hits);
        assertTrue(target.lastDamage >= 12 && target.lastDamage <= 18);
        assertEquals(1, getActionCode(), "sweep animation continues after its hit");
        assertFalse(priest.attackEntityAsMob(target));
        for (int tick = EntityCloudGiantPriest.SWEEP_HIT_TICK + 1;
                tick <= EntityCloudGiantPriest.SWEEP_TICKS; tick++) {
            tickAction();
            assertEquals(1, target.hits, "sweep must hit only once");
        }
        assertEquals(0, getActionCode());
        assertTrue(priest.attackEntityAsMob(target));
    }

    @Test
    void movingOutOfMeleeRangeBeforeTheHitAvoidsTheSweep() throws Exception {
        RecordingTarget target = new RecordingTarget(world);
        target.setPosition(5, 64, 0);
        assertTrue(priest.attackEntityAsMob(target));
        for (int tick = 1; tick < EntityCloudGiantPriest.SWEEP_HIT_TICK; tick++) {
            tickAction();
        }
        target.setPosition(6, 64, 0);
        tickAction();
        assertEquals(0, target.hits);
    }

    @Test
    void lightningWaitsForCastingToFinishAndUsesBossDamageWithAnEffectOnlyBolt() throws Exception {
        RecordingTarget target = new RecordingTarget(world);
        target.setPosition(12, 64, 0);
        invoke("beginAction", new Class<?>[] {byte.class, EntityLivingBase.class}, (byte) 3, target);
        for (int tick = 1; tick < 64; tick++) {
            tickAction();
        }
        assertEquals(0, target.hits);
        assertTrue(world.weatherEffects.isEmpty());
        tickAction();
        assertEquals(1, target.hits);
        assertEquals(15, target.lastDamage, 0.001);
        assertSame(priest, target.lastSource.getTrueSource());
        assertFalse(target.lastSource.isDifficultyScaled());
        assertEquals(1, world.weatherEffects.size());
        Field effectOnly = world.weatherEffects.get(0).getClass().getDeclaredField("effectOnly");
        effectOnly.setAccessible(true);
        assertTrue(effectOnly.getBoolean(world.weatherEffects.get(0)));
    }

    @Test
    void slamTargetCanLeaveTheRadiusBeforeThePointEightSecondHit() throws Exception {
        TestPlayer nearby = new TestPlayer(world, false);
        nearby.setPosition(3, 64, 0);
        nearby.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100);
        nearby.setHealth(100);
        world.entities.add(nearby);
        invoke("performSlam", new Class<?>[] {EntityLivingBase.class}, nearby);
        for (int tick = 1; tick < EntityCloudGiantPriest.SLAM_HIT_TICK; tick++) {
            tickAction();
        }
        assertEquals(100, nearby.getHealth(), 0.001);
        assertEquals(0, nearby.motionX, 0.001);
        assertEquals(0, nearby.motionY, 0.001);
        nearby.setPosition(8, 64, 0);
        tickAction();
        assertEquals(100, nearby.getHealth(), 0.001);
        assertEquals(0, nearby.motionX, 0.001);
        assertEquals(0, nearby.motionZ, 0.001);
    }

    @Test
    void slamDealsTwentyTwoAndKnocksBackAPlayerAtRadiusOnTickSixteen() throws Exception {
        TestPlayer nearby = new TestPlayer(world, false);
        nearby.setPosition(3, 64, 0);
        nearby.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100);
        nearby.setHealth(100);
        world.entities.add(nearby);
        invoke("performSlam", new Class<?>[] {EntityLivingBase.class}, nearby);
        for (int tick = 1; tick < EntityCloudGiantPriest.SLAM_HIT_TICK; tick++) {
            tickAction();
        }
        assertEquals(100, nearby.getHealth(), 0.001);
        assertEquals(0, nearby.motionX, 0.001);
        tickAction();
        assertEquals(78, nearby.getHealth(), 0.001);
        assertEquals(2.4, nearby.motionX, 0.001);
        assertEquals(0, nearby.motionZ, 0.001);
        assertEquals(0.6, nearby.motionY, 0.001);
        assertEquals(2, getActionCode(), "slam animation continues after its hit");
        for (int tick = EntityCloudGiantPriest.SLAM_HIT_TICK + 1;
                tick <= EntityCloudGiantPriest.SLAM_TICKS; tick++) {
            tickAction();
            assertEquals(78, nearby.getHealth(), 0.001, "slam must hit only once");
        }
        assertEquals(0, getActionCode());
    }

    @Test
    void distantCreativeAggroFlierIsGroundedWithoutDamageAtSlamTickSixteen() throws Exception {
        TestPlayer flier = new TestPlayer(world, true);
        flier.setPosition(30, 70, 0);
        flier.capabilities.isFlying = true;
        world.playerEntities.add(flier);
        priest.ticksExisted = 10;
        invoke("updateCombatTarget", new Class<?>[0]);
        assertSame(flier, priest.getAttackTarget(), "creative fliers can become aggro targets");

        invoke("performSlam", new Class<?>[] {EntityLivingBase.class}, flier);
        for (int tick = 1; tick < EntityCloudGiantPriest.SLAM_HIT_TICK; tick++) {
            tickAction();
        }
        assertTrue(flier.capabilities.isFlying);
        assertEquals(20, flier.getHealth(), 0.001);
        tickAction();
        assertFalse(flier.capabilities.isFlying);
        assertEquals(20, flier.getHealth(), 0.001);
        assertEquals(0, flier.damageAttempts);
        assertEquals(-1, flier.motionY, 0.001);
        assertTrue(flier.velocityChanged);
        assertTrue(flier.abilitiesSent);
    }

    @Test
    void creativeTargetRemainsAggroAndCanBeAttackedWithoutTakingDamage() throws Exception {
        TestPlayer creative = new TestPlayer(world, true);
        creative.setPosition(5, 64, 0);
        creative.capabilities.isFlying = true;
        world.playerEntities.add(creative);
        priest.ticksExisted = 10;
        invoke("updateCombatTarget", new Class<?>[0]);
        assertSame(creative, priest.getAttackTarget());

        assertTrue(priest.attackEntityAsMob(creative));
        for (int tick = 0; tick < EntityCloudGiantPriest.SWEEP_TICKS; tick++) {
            tickAction();
        }
        assertSame(creative, priest.getAttackTarget());
        assertEquals(20, creative.getHealth(), 0.001);
        assertEquals(0, creative.damageAttempts);
    }

    @Test
    void secondPhaseBeginsAtHalfHealthOnceAndPlaysTheTransitionAction() throws Exception {
        priest.setHealth(251.0F);
        invoke("activateSecondPhaseIfThresholdReached", new Class<?>[0]);
        assertFalse(priest.isInSecondPhase());

        priest.setHealth(250.0F);
        invoke("activateSecondPhaseIfThresholdReached", new Class<?>[0]);
        assertTrue(priest.isInSecondPhase());
        assertEquals(7, getActionCode());
        int transitionSequence = getActionSequence();

        for (int tick = 0; tick < EntityCloudGiantPriest.PHASE_TRANSITION_TICKS; tick++) {
            tickAction();
        }
        assertEquals(0, getActionCode());
        invoke("activateSecondPhaseIfThresholdReached", new Class<?>[0]);
        assertEquals(transitionSequence, getActionSequence());
        assertEquals(0, getActionCode());
    }

    @Test
    void introStartsOnceAndAnAcceptedHitDoesNotInterruptIt() throws Exception {
        priest.onLivingUpdate();
        assertEquals(5, getActionCode());
        int introSequence = getActionSequence();
        priest.onLivingUpdate();
        assertEquals(introSequence, getActionSequence());

        priest.hurtResistantTime = 0;
        assertTrue(priest.attackEntityFrom(entityDamage(), 1.0F));
        assertEquals(5, getActionCode());
        assertEquals(introSequence, getActionSequence());

        for (int tick = 0; tick < EntityCloudGiantPriest.INTRO_TICKS; tick++) {
            tickAction();
        }
        assertEquals(0, getActionCode());
        priest.hurtResistantTime = 0;
        assertTrue(priest.attackEntityFrom(entityDamage(), 1.0F));
        assertEquals(6, getActionCode());
    }

    @Test
    void damageDuringAnActiveAttackDoesNotInterruptItsAnimation() throws Exception {
        RecordingTarget target = new RecordingTarget(world);
        target.setPosition(5, 64, 0);
        assertTrue(priest.attackEntityAsMob(target));
        int attackSequence = getActionSequence();
        float healthBeforeHit = priest.getHealth();

        priest.hurtResistantTime = 0;
        assertTrue(priest.attackEntityFrom(entityDamage(), 1.0F));
        assertEquals(healthBeforeHit - 1.0F, priest.getHealth(), 0.001);
        assertEquals(1, getActionCode(), "active sweep remains the current animation");
        assertEquals(attackSequence, getActionSequence(), "hurt does not replace the attack action");
    }

    @Test
    void phaseTransitionIsInvulnerableUntilItsAnimationFinishes() throws Exception {
        priest.setHealth(250.0F);
        invoke("activateSecondPhaseIfThresholdReached", new Class<?>[0]);
        assertEquals(7, getActionCode());
        float transitionHealth = priest.getHealth();
        priest.hurtResistantTime = 0;

        assertFalse(priest.attackEntityFrom(entityDamage(), 100.0F));
        assertEquals(transitionHealth, priest.getHealth(), 0.001);
        assertEquals(7, getActionCode());

        for (int tick = 0; tick < EntityCloudGiantPriest.PHASE_TRANSITION_TICKS; tick++) {
            tickAction();
        }
        assertEquals(0, getActionCode());
        priest.hurtResistantTime = 0;
        assertTrue(priest.attackEntityFrom(entityDamage(), 1.0F));
        assertEquals(transitionHealth - 1.0F, priest.getHealth(), 0.001);
    }

    @Test
    void cloudLeapTransitionsFromJumpToThrustAndHitsWhenItsPathCrossesTheTarget() throws Exception {
        RecordingTarget target = new RecordingTarget(world);
        target.setPosition(18, 64, 0);
        priest.setHealth(250.0F);
        invoke("activateSecondPhaseIfThresholdReached", new Class<?>[0]);
        invoke("performCloudLeap", new Class<?>[] {EntityLivingBase.class}, target);
        assertEquals(8, getActionCode());
        assertEquals(0, target.hits);

        for (int tick = 0; tick < EntityCloudGiantPriest.JUMP_TICKS; tick++) {
            tickAction();
            priest.setPosition(priest.posX + priest.motionX, priest.posY, priest.posZ + priest.motionZ);
        }
        assertEquals(9, getActionCode());
        assertEquals(0.72, priest.motionX, 0.001);
        assertEquals(0, target.hits);

        for (int tick = 0; tick < 5; tick++) {
            tickAction();
            assertEquals(0, target.hits);
            priest.setPosition(priest.posX + priest.motionX, priest.posY, priest.posZ + priest.motionZ);
        }
        tickAction();
        assertEquals(1, target.hits);
        assertEquals(25, target.lastDamage, 0.001);
        for (int tick = 6; tick < EntityCloudGiantPriest.THRUST_TICKS; tick++) {
            tickAction();
            assertEquals(1, target.hits, "thrust damage resolves at most once");
            priest.setPosition(priest.posX + priest.motionX, priest.posY, priest.posZ + priest.motionZ);
        }
    }

    @Test
    void phaseTwoWhirlwindHealsPlayersInRangeByHalfPercentPerSecond() throws Exception {
        TestPlayer player = new TestPlayer(world, false);
        player.setPosition(5, 64, 0);
        player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100.0D);
        player.setHealth(90.0F);
        attacker.setSize(0.6F, 0.8F);
        attacker.setPosition(6.25D, 64.0D, 0.0D);
        world.playerEntities.add(player);
        world.loadedEntityList.add(attacker);
        @SuppressWarnings("unchecked")
        Set<UUID> summoned = (Set<UUID>) getField("summonedWhirlwinds");
        summoned.add(attacker.getUniqueID());

        priest.ticksExisted = 20;
        invoke("healPlayersCaughtInWhirlwinds", new Class<?>[0]);
        assertEquals(90.0F, player.getHealth(), 0.001);

        priest.setHealth(250.0F);
        invoke("activateSecondPhaseIfThresholdReached", new Class<?>[0]);
        invoke("healPlayersCaughtInWhirlwinds", new Class<?>[0]);
        assertEquals(90.5F, player.getHealth(), 0.001);

        priest.ticksExisted = 39;
        invoke("healPlayersCaughtInWhirlwinds", new Class<?>[0]);
        assertEquals(90.5F, player.getHealth(), 0.001);
        priest.ticksExisted = 40;
        invoke("healPlayersCaughtInWhirlwinds", new Class<?>[0]);
        assertEquals(91.0F, player.getHealth(), 0.001);

        attacker.setPosition(7.0D, 64.0D, 0.0D);
        priest.ticksExisted = 60;
        invoke("healPlayersCaughtInWhirlwinds", new Class<?>[0]);
        assertEquals(91.0F, player.getHealth(), 0.001);
    }

    @Test
    void actionAnimationDurationsMatchTheCombatClocks() throws Exception {
        String resource = "/assets/gctcore/animations/cloud_giant_wanderer.animation.json";
        try (InputStream stream = getClass().getResourceAsStream(resource)) {
            assertNotNull(stream);
            JsonObject animations = new JsonParser().parse(new InputStreamReader(stream,
                    StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("animations");
            assertAnimationTicks(animations, "jump", EntityCloudGiantPriest.JUMP_TICKS);
            assertAnimationTicks(animations, "forward_thrust", EntityCloudGiantPriest.THRUST_TICKS);
            assertAnimationTicks(animations, "whirlwind_cast", EntityCloudGiantPriest.WHIRLWIND_CAST_TICKS);
            assertAnimationTicks(animations, "phase_transition", EntityCloudGiantPriest.PHASE_TRANSITION_TICKS);
            assertAnimationTicks(animations, "intro", EntityCloudGiantPriest.INTRO_TICKS);
            assertAnimationTicks(animations, "hurt", EntityCloudGiantPriest.HURT_TICKS);
            assertNoPositionTracks(animations, "jump");
            JsonObject whirlwind = animations.getAsJsonObject("animation.cloud_giant_wanderer.whirlwind_cast")
                    .getAsJsonObject("bones");
            assertTrue(whirlwind.has("arm_left"));
            assertTrue(whirlwind.has("arm_right"));
            assertTrue(whirlwind.has("weapon"));
        }
    }

    private void assertNoPositionTracks(JsonObject animations, String name) {
        JsonObject bones = animations.getAsJsonObject("animation.cloud_giant_wanderer." + name)
                .getAsJsonObject("bones");
        for (Map.Entry<String, com.google.gson.JsonElement> entry : bones.entrySet()) {
            assertFalse(entry.getValue().getAsJsonObject().has("position"),
                    name + " must not translate the " + entry.getKey() + " model bone");
        }
    }

    @Test
    void combatClocksMatchTheFinalAuthoredAnimations() throws Exception {
        String resource = "/assets/gctcore/animations/cloud_giant_wanderer.animation.json";
        try (InputStream stream = getClass().getResourceAsStream(resource)) {
            assertNotNull(stream);
            JsonObject animations = new JsonParser().parse(new InputStreamReader(stream,
                    StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("animations");
            assertAnimationTicks(animations, "sweep", EntityCloudGiantPriest.SWEEP_TICKS);
            assertAnimationTicks(animations, "slam", EntityCloudGiantPriest.SLAM_TICKS);
            assertAnimationTicks(animations, "storm_cast", EntityCloudGiantPriest.STORM_CAST_TICKS);
            assertAnimationTicks(animations, "death", EntityCloudGiantPriest.DEATH_ANIMATION_TICKS);
        }
    }

    private void assertAnimationTicks(JsonObject animations, String name, int ticks) {
        double seconds = animations.getAsJsonObject("animation.cloud_giant_wanderer." + name)
                .get("animation_length").getAsDouble();
        assertEquals(ticks, seconds * 20, 0.001, name);
    }

    private void tickAction() throws Exception {
        invoke("tickAction", new Class<?>[0]);
    }

    private void invoke(String name, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = EntityCloudGiantPriest.class.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        method.invoke(priest, args);
    }

    private int getActionCode() throws Exception {
        Method method = EntityCloudGiantPriest.class.getDeclaredMethod("getAction");
        method.setAccessible(true);
        return (Byte) method.invoke(priest);
    }

    @SuppressWarnings("unchecked")
    private int getActionSequence() throws Exception {
        Field field = EntityCloudGiantPriest.class.getDeclaredField("ACTION_SEQUENCE");
        field.setAccessible(true);
        DataParameter<Integer> parameter = (DataParameter<Integer>) field.get(null);
        return priest.getDataManager().get(parameter);
    }

    private Object getField(String name) throws Exception {
        Field field = EntityCloudGiantPriest.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(priest);
    }

    private DamageSource entityDamage() {
        return new EntityDamageSource("mob", attacker);
    }

    private static class RecordingTarget extends EntityCloudGiantPriest {
        int hits;
        float lastDamage;
        DamageSource lastSource;

        RecordingTarget(World world) {
            super(world);
        }

        @Override
        public boolean attackEntityFrom(DamageSource source, float amount) {
            hits++;
            lastSource = source;
            lastDamage = amount;
            return true;
        }
    }

    private static class TestPlayer extends EntityPlayer {
        private final boolean creative;
        boolean abilitiesSent;
        int damageAttempts;

        TestPlayer(World world, boolean creative) {
            super(world, new GameProfile(UUID.randomUUID(), "cloud-priest-test"));
            this.creative = creative;
            capabilities.isCreativeMode = creative;
            capabilities.disableDamage = creative;
            capabilities.allowFlying = creative;
        }

        @Override
        public boolean isSpectator() {
            return false;
        }

        @Override
        public boolean isCreative() {
            return creative;
        }

        @Override
        public void sendPlayerAbilities() {
            abilitiesSent = true;
        }

        @Override
        public boolean attackEntityFrom(DamageSource source, float amount) {
            damageAttempts++;
            return super.attackEntityFrom(source, amount);
        }
    }

    private static class TestWorld extends World {
        final List<EntityLivingBase> entities = new ArrayList<>();
        TestWorld() {
            super(null, new WorldInfo(new WorldSettings(0, GameType.SURVIVAL,
                    false, false, WorldType.DEFAULT), "cloud-priest-test"),
                    new WorldProviderSurface(), new Profiler(), false);
            worldInfo.setDifficulty(EnumDifficulty.PEACEFUL);
            getGameRules().setOrCreateGameRule("doMobLoot", "false");
        }

        @Override
        protected IChunkProvider createChunkProvider() {
            return null;
        }

        @Override
        protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
            return false;
        }

        @Override
        public IBlockState getBlockState(BlockPos pos) {
            return Blocks.AIR.getDefaultState();
        }

        @Override
        public BlockPos getSpawnPoint() {
            return new BlockPos(0, 64, 0);
        }

        @Override
        public boolean addWeatherEffect(Entity effect) {
            weatherEffects.add(effect);
            return true;
        }

        @Override
        public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> type, AxisAlignedBB bounds) {
            List<T> result = new ArrayList<>();
            for (Entity entity : entities) {
                if (type.isInstance(entity) && entity.getEntityBoundingBox().intersects(bounds)) {
                    result.add(type.cast(entity));
                }
            }
            return result;
        }
    }
}
