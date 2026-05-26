package com.smd.gctcore.common.entity.botania;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.google.common.base.Predicates;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;
import vazkii.botania.api.mana.spark.SparkUpgradeType;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.network.PacketBotaniaEffect;
import vazkii.botania.common.network.PacketHandler;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Abstract base for custom spark entities with configurable transfer rate and scan range.
 */
public abstract class EntityCustomSpark extends Entity implements ISparkEntity {

    private static final String TAG_UPGRADE = "upgrade";
    private static final String TAG_INVIS = "invis";

    private static final DataParameter<Integer> UPGRADE =
            EntityDataManager.createKey(EntityCustomSpark.class, DataSerializers.VARINT);

    private final Set<ISparkEntity> transfers = Collections.newSetFromMap(new WeakHashMap<>());
    private int removeTransferants = 2;

    public EntityCustomSpark(World world) {
        super(world);
        this.isImmuneToFire = true;
    }

    @Override
    protected void entityInit() {
        setSize(0.1F, 0.5F);
        this.dataManager.register(UPGRADE, 0);
    }

    /** Mana transfer rate (per target, per tick) */
    protected abstract int getTransferRate();

    /** Scan range in blocks for finding other sparks */
    protected abstract int getScanRange();

    /** The item stack to drop when this spark dies */
    protected abstract ItemStack createSparkItem();

    @Nonnull
    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return createSparkItem();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (this.world.isRemote) {
            return;
        }

        ISparkAttachable tile = getAttachedTile();
        if (tile == null) {
            dropAndKill();
            return;
        }

        SparkUpgradeType upgrade = getUpgrade();
        List<ISparkEntity> allSparks = null;
        if (upgrade == SparkUpgradeType.DOMINANT || upgrade == SparkUpgradeType.RECESSIVE) {
            allSparks = getSparksInRange(this.world, this.posX, this.posY + this.height / 2.0D, this.posZ);
        }

        Collection<ISparkEntity> transferSet = getTransfers();

        switch (upgrade) {
            case DISPERSIVE: {
                List<EntityPlayer> players = getPlayersInRange(this.world, this.posX, this.posY + this.height / 2.0D, this.posZ);
                Map<EntityPlayer, TObjectIntHashMap<ItemStack>> receivingPlayers = new HashMap<>();

                ItemStack input = createSparkItem();
                for (EntityPlayer player : players) {
                    List<ItemStack> stacks = new ArrayList<>();
                    stacks.addAll(player.inventory.mainInventory);
                    stacks.addAll(player.inventory.armorInventory);

                    IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
                    for (int i = 0; i < baubles.getSlots(); i++) {
                        stacks.add(baubles.getStackInSlot(i));
                    }

                    for (ItemStack stack : stacks) {
                        if (stack.isEmpty() || !(stack.getItem() instanceof IManaItem)) {
                            continue;
                        }
                        IManaItem manaItem = (IManaItem) stack.getItem();
                        if (manaItem.canReceiveManaFromItem(stack, input)) {
                            boolean add = false;
                            TObjectIntHashMap<ItemStack> receivingStacks;
                            if (!receivingPlayers.containsKey(player)) {
                                add = true;
                                receivingStacks = new TObjectIntHashMap<>();
                            } else {
                                receivingStacks = receivingPlayers.get(player);
                            }
                            int recv = Math.min(getAttachedTile().getCurrentMana(),
                                    Math.min(getTransferRate(), manaItem.getMaxMana(stack) - manaItem.getMana(stack)));
                            if (recv > 0) {
                                receivingStacks.put(stack, recv);
                                if (add) {
                                    receivingPlayers.put(player, receivingStacks);
                                }
                            }
                        }
                    }
                }

                if (!receivingPlayers.isEmpty()) {
                    List<EntityPlayer> keys = new ArrayList<>(receivingPlayers.keySet());
                    Collections.shuffle(keys);
                    EntityPlayer player = keys.iterator().next();

                    TObjectIntHashMap<ItemStack> items = receivingPlayers.get(player);
                    ItemStack stack = items.keySet().iterator().next();
                    int cost = items.get(stack);
                    int manaToPut = Math.min(getAttachedTile().getCurrentMana(), cost);
                    ((IManaItem) stack.getItem()).addMana(stack, manaToPut);
                    getAttachedTile().recieveMana(-manaToPut);
                    particlesTowards(player);
                }
                break;
            }

            case DOMINANT: {
                List<ISparkEntity> validSparks = new ArrayList<>();
                for (ISparkEntity spark : allSparks) {
                    if (spark == this) continue;
                    SparkUpgradeType upg = spark.getUpgrade();
                    if (upg == SparkUpgradeType.NONE && spark.getAttachedTile() instanceof vazkii.botania.api.mana.IManaPool) {
                        validSparks.add(spark);
                    }
                }
                if (!validSparks.isEmpty()) {
                    validSparks.get(this.world.rand.nextInt(validSparks.size())).registerTransfer(this);
                }
                break;
            }

            case RECESSIVE: {
                for (ISparkEntity spark : allSparks) {
                    if (spark == this) continue;
                    SparkUpgradeType upg = spark.getUpgrade();
                    if (upg != SparkUpgradeType.DOMINANT
                            && upg != SparkUpgradeType.RECESSIVE
                            && upg != SparkUpgradeType.ISOLATED) {
                        transferSet.add(spark);
                    }
                }
                break;
            }

            default:
                break;
        }

        if (!transferSet.isEmpty()) {
            int rate = getTransferRate();
            int manaTotal = Math.min(rate * transferSet.size(), tile.getCurrentMana());
            int manaForEach = manaTotal / transferSet.size();
            int manaSpent = 0;

            if (manaForEach > 0) {
                for (ISparkEntity spark : transferSet) {
                    if (spark.getAttachedTile() == null
                            || spark.getAttachedTile().isFull()
                            || spark.areIncomingTransfersDone()) {
                        manaTotal -= manaForEach;
                        continue;
                    }
                    ISparkAttachable attached = spark.getAttachedTile();
                    int spend = Math.min(attached.getAvailableSpaceForMana(), manaForEach);
                    attached.recieveMana(spend);
                    manaSpent += spend;
                    particlesTowards((Entity) spark);
                }
                tile.recieveMana(-manaSpent);
            }
        }

        if (this.removeTransferants > 0) {
            this.removeTransferants--;
        }
        filterTransfers();
    }

    private void particlesTowards(Entity e) {
        PacketHandler.sendToNearby(this.world, this,
                (IMessage) new PacketBotaniaEffect(PacketBotaniaEffect.EffectType.SPARK_MANA_FLOW,
                        this.posX, this.posY, this.posZ, new int[]{getEntityId(), e.getEntityId()}));
    }

    public static void particleBeam(EntityPlayer player, Entity e1, Entity e2) {
        if (e1 != null && e2 != null && !e1.world.isRemote) {
            PacketHandler.sendTo((EntityPlayerMP) player,
                    (IMessage) new PacketBotaniaEffect(PacketBotaniaEffect.EffectType.SPARK_NET_INDICATOR,
                            e1.posX, e1.posY, e1.posZ, new int[]{e1.getEntityId(), e2.getEntityId()}));
        }
    }

    private void dropAndKill() {
        SparkUpgradeType upgrade = getUpgrade();
        entityDropItem(createSparkItem(), 0.0F);
        if (upgrade != SparkUpgradeType.NONE) {
            entityDropItem(new ItemStack(ModItems.sparkUpgrade, 1, upgrade.ordinal() - 1), 0.0F);
        }
        setDead();
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!this.isDead && !stack.isEmpty()) {
            if (this.world.isRemote) {
                boolean valid = (stack.getItem() == ModItems.twigWand
                        || stack.getItem() == ModItems.sparkUpgrade
                        || stack.getItem() == ModItems.phantomInk);
                if (valid) player.swingArm(hand);
                return valid;
            }

            SparkUpgradeType upgrade = getUpgrade();
            if (stack.getItem() == ModItems.twigWand) {
                if (player.isSneaking()) {
                    if (upgrade != SparkUpgradeType.NONE) {
                        entityDropItem(new ItemStack(ModItems.sparkUpgrade, 1, upgrade.ordinal() - 1), 0.0F);
                        setUpgrade(SparkUpgradeType.NONE);
                        this.transfers.clear();
                        this.removeTransferants = 2;
                    } else {
                        dropAndKill();
                    }
                    return true;
                }
                for (ISparkEntity spark : getSparksInRange(this.world, this.posX, this.posY + this.height / 2.0D, this.posZ)) {
                    particleBeam(player, this, (Entity) spark);
                }
                return true;
            }
            if (stack.getItem() == ModItems.sparkUpgrade && upgrade == SparkUpgradeType.NONE) {
                int newUpgrade = stack.getItemDamage() + 1;
                setUpgrade(SparkUpgradeType.values()[newUpgrade]);
                stack.shrink(1);
                return true;
            }
            if (stack.getItem() == ModItems.phantomInk) {
                setInvisible(true);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void readEntityFromNBT(@Nonnull NBTTagCompound cmp) {
        setUpgrade(SparkUpgradeType.values()[cmp.getInteger(TAG_UPGRADE)]);
        setInvisible(cmp.getInteger(TAG_INVIS) == 1);
    }

    @Override
    protected void writeEntityToNBT(@Nonnull NBTTagCompound cmp) {
        cmp.setInteger(TAG_UPGRADE, getUpgrade().ordinal());
        cmp.setInteger(TAG_INVIS, isInvisible() ? 1 : 0);
    }

    @Override
    public ISparkAttachable getAttachedTile() {
        int x = MathHelper.floor(this.posX);
        int y = MathHelper.floor(this.posY) - 1;
        int z = MathHelper.floor(this.posZ);
        TileEntity tile = this.world.getTileEntity(new BlockPos(x, y, z));
        if (tile instanceof ISparkAttachable) {
            return (ISparkAttachable) tile;
        }
        return null;
    }

    private void filterTransfers() {
        Iterator<ISparkEntity> iter = this.transfers.iterator();
        while (iter.hasNext()) {
            ISparkEntity spark = iter.next();
            SparkUpgradeType upgr = getUpgrade();
            SparkUpgradeType supgr = spark.getUpgrade();
            ISparkAttachable atile = spark.getAttachedTile();

            if (spark == this
                    || spark.areIncomingTransfersDone()
                    || atile == null
                    || atile.isFull()
                    || ((upgr != SparkUpgradeType.NONE || supgr != SparkUpgradeType.DOMINANT)
                    && (upgr != SparkUpgradeType.RECESSIVE || (supgr != SparkUpgradeType.NONE && supgr != SparkUpgradeType.DISPERSIVE))
                    && atile instanceof vazkii.botania.api.mana.IManaPool)) {
                iter.remove();
            }
        }
    }

    @Override
    public Collection<ISparkEntity> getTransfers() {
        filterTransfers();
        return this.transfers;
    }

    private boolean hasTransfer(ISparkEntity entity) {
        return this.transfers.contains(entity);
    }

    @Override
    public void registerTransfer(ISparkEntity entity) {
        if (hasTransfer(entity)) return;
        this.transfers.add(entity);
    }

    @Override
    public SparkUpgradeType getUpgrade() {
        return SparkUpgradeType.values()[this.dataManager.get(UPGRADE)];
    }

    @Override
    public void setUpgrade(SparkUpgradeType upgrade) {
        this.dataManager.set(UPGRADE, upgrade.ordinal());
    }

    @Override
    public boolean areIncomingTransfersDone() {
        ISparkAttachable tile = getAttachedTile();
        if (tile instanceof vazkii.botania.api.mana.IManaPool) {
            return this.removeTransferants > 0;
        }
        return tile != null && tile.areIncomingTranfersDone();
    }

    @SuppressWarnings("unchecked")
    private List<ISparkEntity> getSparksInRange(World world, double x, double y, double z) {
        int r = getScanRange();
        return (List<ISparkEntity>) (List<?>) world.getEntitiesWithinAABB(
                Entity.class,
                new AxisAlignedBB(x - r, y - r, z - r, x + r, y + r, z + r),
                Predicates.instanceOf(ISparkEntity.class));
    }

    @SuppressWarnings("unchecked")
    private List<EntityPlayer> getPlayersInRange(World world, double x, double y, double z) {
        int r = getScanRange();
        return (List<EntityPlayer>) (List<?>) world.getEntitiesWithinAABB(
                Entity.class,
                new AxisAlignedBB(x - r, y - r, z - r, x + r, y + r, z + r),
                Predicates.instanceOf(EntityPlayer.class));
    }
}
