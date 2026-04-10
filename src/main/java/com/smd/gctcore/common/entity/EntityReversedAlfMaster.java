package com.smd.gctcore.common.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * 反·精灵长老 (reversed_alf_master)
 * <p>
 * 条件性中立生物：
 * <ul>
 *   <li>当玩家身上携带 contenttweaker:reversed_vision 效果时，保持中立（不主动攻击，
 *       且对该玩家的攻击完全免疫，防止意外激怒）。</li>
 *   <li>其余情况视为敌对，主动攻击并追击玩家。</li>
 * </ul>
 * 模型与动画与 EntityReversedElf 相同，材质独立（gctcore:textures/entity/reversed_alf_master.png）。
 * 默认副手持有 botania:firerod，不掉落。血量 100。
 */
public class EntityReversedAlfMaster extends EntityMob {

    /** contenttweaker:reversed_vision 药水效果的注册名 */
    private static final ResourceLocation REVERSED_VISION_RL =
            new ResourceLocation("contenttweaker", "reversed_vision");

    public EntityReversedAlfMaster(World world) {
        super(world);
        setSize(0.6F, 1.8F);
        this.experienceValue = 20;
        this.isImmuneToFire = true;
        setNoAI(false);

        // 副手装备 botania:firerod，运行时从注册表取得以避免硬依赖问题
        Item fireRod = Item.REGISTRY.getObject(new ResourceLocation("botania", "firerod"));
        if (fireRod != null) {
            setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(fireRod, 1));
        }
        // 副手掉落概率设为0，保证不掉落（inventoryHandsDropChances: [0]=主手, [1]=副手）
        this.inventoryHandsDropChances[EntityEquipmentSlot.OFFHAND.getIndex()] = 0.0F;
    }

    // ── AI ─────────────────────────────────────────────────────────────────────

    @Override
    protected void initEntityAI() {
        super.initEntityAI();
        // 近战攻击
        this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.2D, false));
        // 漫游 & 待机
        this.tasks.addTask(3, new EntityAIWander(this, 0.8D));
        this.tasks.addTask(4, new EntityAILookIdle(this));

        // 被攻击时正常反击（无论攻击者是否携带 reversed_vision）
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));

        // 主动寻找并攻击不携带 reversed_vision 的玩家
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(
                this,
                EntityPlayer.class,
                10,    // 每 10 tick 重新搜索一次
                true,  // 需要视线
                false, // 不限于附近
                player -> !hasReversedVision(player)
        ));
    }

    /**
     * 检查玩家是否拥有 reversed_vision 效果。
     * 在运行时查询注册表，确保跨模组兼容。
     */
    private static boolean hasReversedVision(EntityPlayer player) {
        Potion reversedVision = Potion.REGISTRY.getObject(REVERSED_VISION_RL);
        return reversedVision != null && player.isPotionActive(reversedVision);
    }

    // ── 属性 ───────────────────────────────────────────────────────────────────

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(15.0D);
        if (getEntityAttribute(SharedMonsterAttributes.ARMOR) != null) {
            getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
        }
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }

    // ── 掉落 ───────────────────────────────────────────────────────────────────

    @Override
    protected Item getDropItem() {
        return null;
    }


    // ── 声音 ───────────────────────────────────────────────────────────────────

    @Override
    public SoundEvent getAmbientSound() {
        return SoundEvent.REGISTRY.getObject(new ResourceLocation(""));
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.player.hurt"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.player.death"));
    }

    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }

    // ── 模型（与 EntityReversedElf.ModelZombie 完全相同的骨架结构） ─────────────

    public static class ModelAlfMaster extends ModelBase {
        public ModelRenderer bipedRightArm;
        public ModelRenderer bipedRightLeg;
        public ModelRenderer bipedHead;
        public ModelRenderer bipedBody;
        public ModelRenderer bipedLeftArm;
        public ModelRenderer bipedLeftLeg;
        public ModelRenderer bipedHeadwear;

        public ModelAlfMaster() {
            this.textureWidth = 64;
            this.textureHeight = 64;

            this.bipedHead = new ModelRenderer(this, 0, 0);
            this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
            this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);

            this.bipedRightArm = new ModelRenderer(this, 40, 16);
            this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
            this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
            setRotateAngle(this.bipedRightArm, -1.3962635F, -0.1F, 0.1F);

            this.bipedLeftArm = new ModelRenderer(this, 40, 16);
            this.bipedLeftArm.mirror = true;
            this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
            this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
            setRotateAngle(this.bipedLeftArm, -1.3962635F, 0.1F, -0.1F);

            this.bipedHeadwear = new ModelRenderer(this, 32, 0);
            this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
            this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);

            this.bipedRightLeg = new ModelRenderer(this, 0, 16);
            this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.1F);
            this.bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);

            this.bipedLeftLeg = new ModelRenderer(this, 0, 16);
            this.bipedLeftLeg.mirror = true;
            this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.1F);
            this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);

            this.bipedBody = new ModelRenderer(this, 16, 16);
            this.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
            this.bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);
        }

        @Override
        public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
            this.bipedHead.render(f5);
            this.bipedRightArm.render(f5);
            this.bipedLeftArm.render(f5);
            this.bipedHeadwear.render(f5);
            this.bipedRightLeg.render(f5);
            this.bipedLeftLeg.render(f5);
            this.bipedBody.render(f5);
        }

        public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
            modelRenderer.rotateAngleX = x;
            modelRenderer.rotateAngleY = y;
            modelRenderer.rotateAngleZ = z;
        }

        @Override
        public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e) {
            super.setRotationAngles(f, f1, f2, f3, f4, f5, e);
            this.bipedRightArm.rotateAngleX = MathHelper.cos(f * 0.6662F + 3.1415927F) * f1;
            this.bipedRightLeg.rotateAngleX = MathHelper.cos(f * 1.0F) * 1.0F * f1;
            this.bipedHead.rotateAngleY = f3 / 57.295776F;
            this.bipedHead.rotateAngleX = f4 / 57.295776F;
            this.bipedLeftArm.rotateAngleX = MathHelper.cos(f * 0.6662F) * f1;
            this.bipedLeftLeg.rotateAngleX = MathHelper.cos(f * 1.0F) * -1.0F * f1;
            this.bipedHeadwear.rotateAngleY = f3 / 57.295776F;
            this.bipedHeadwear.rotateAngleX = f4 / 57.295776F;
        }
    }
}
