package com.gmm.gctold.common.entity;

import java.util.ArrayList;
import java.util.Iterator;
import com.gmm.gctold.common.blocks.BlockApocalypseAltar;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelSilverfish;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class EntityApocalypseHolder {


  private Biome[] allbiomes(RegistryNamespaced<ResourceLocation, Biome> in) {
    Iterator<Biome> itr = in.iterator();
    ArrayList<Biome> ls = new ArrayList<>();
    while (itr.hasNext())
      ls.add(itr.next());
    return ls.<Biome>toArray(new Biome[ls.size()]);
  }

  @SideOnly(Side.CLIENT)
  public static void registerRenderers(FMLPreInitializationEvent event) {
    RenderingRegistry.registerEntityRenderingHandler(ApocalypseHolderEntity.class, renderManager -> {
          RenderLiving customRender = new RenderLiving(renderManager, (ModelBase)new ModelSilverfish(), 0.5F) {
              protected ResourceLocation getEntityTexture(Entity entity) {
                return new ResourceLocation("gctold:textures/holder.png");
              }
            };
          customRender.addLayer((LayerRenderer)new LayerHeldItem((RenderLivingBase)customRender));
          return (Render)customRender;
        });
  }

  public static class ApocalypseHolderEntity extends EntityMob {
    public ApocalypseHolderEntity(World world) {
      super(world);
      setSize(0.4F, 0.3F);
      this.experienceValue = 0;
      this.isImmuneToFire = true;
      setNoAI(true);
      enablePersistence();
    }

    public EnumCreatureAttribute getCreatureAttribute() {
      return EnumCreatureAttribute.UNDEFINED;
    }

    protected boolean canDespawn() {
      return false;
    }

    protected Item getDropItem() {
      return null;
    }

    public SoundEvent getAmbientSound() {
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation(""));
    }

    public SoundEvent getHurtSound(DamageSource ds) {
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation(""));
    }

    public SoundEvent getDeathSound() {
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation(""));
    }

    protected float getSoundVolume() {
      return 1.0F;
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
      if (source.getImmediateSource() instanceof net.minecraft.entity.projectile.EntityArrow)
        return false;
      if (source.getImmediateSource() instanceof net.minecraft.entity.player.EntityPlayer)
        return false;
      if (source.getImmediateSource() instanceof net.minecraft.entity.projectile.EntityPotion)
        return false;
      if (source == DamageSource.FALL)
        return false;
      if (source == DamageSource.CACTUS)
        return false;
      if (source == DamageSource.DROWN)
        return false;
      if (source == DamageSource.LIGHTNING_BOLT)
        return false;
      return super.attackEntityFrom(source, amount);
    }

    public void onEntityUpdate() {
      super.onEntityUpdate();
      if (!this.world.isRemote) {
        this.world.setBlockState(new BlockPos(this), BlockApocalypseAltar.block.getDefaultState(), 3);
        setDead();
      }
    }

    protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      if (getEntityAttribute(SharedMonsterAttributes.ARMOR) != null)
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
      if (getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null)
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
      if (getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null)
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
      if (getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null)
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
    }

    public boolean canBeCollidedWith() {
      return false;
    }
  }
}

