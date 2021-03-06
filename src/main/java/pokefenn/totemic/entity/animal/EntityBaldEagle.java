package pokefenn.totemic.entity.animal;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityFlying;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFishFood.FishType;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import pokefenn.totemic.init.ModSounds;
import pokefenn.totemic.lib.Resources;

public class EntityBaldEagle extends EntityTameable implements EntityFlying
{
    public float flap;
    public float flapSpeed;
    public float oFlapSpeed;
    public float oFlap;
    public float flapping = 1.0F;

    public EntityBaldEagle(World world)
    {
        super(world);
        setSize(0.6F, 1.0F);
        moveHelper = new EntityFlyHelper(this);
    }

    @Override
    protected void initEntityAI()
    {
        aiSit = new EntityAISit(this);
        tasks.addTask(0, new EntityAIPanic(this, 1.25D));
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIMate(this, 1.0D));
        tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(3, this.aiSit);
        tasks.addTask(3, new EntityAIFollowOwnerFlying(this, 1.0D, 5.0F, 1.0F));
        tasks.addTask(3, new EntityAIWanderAvoidWaterFlying(this, 1.0D));
        tasks.addTask(4, new EntityAIFollow(this, 1.0D, 3.0F, 7.0F));
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.FLYING_SPEED);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0);
        getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(0.4);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2);
    }

    @Override
    protected PathNavigate createNavigator(World worldIn)
    {
        PathNavigateFlying navigator = new PathNavigateFlying(this, worldIn);
        navigator.setCanOpenDoors(false);
        navigator.setCanFloat(true);
        navigator.setCanEnterDoors(true);
        return navigator;
    }

    @Override
    public float getEyeHeight()
    {
        return height * 0.6F;
    }

    @Override
    public void onLivingUpdate()
    {
        super.onLivingUpdate();
        calculateFlapping();
    }

    private void calculateFlapping()
    {
        this.oFlap = this.flap;
        this.oFlapSpeed = this.flapSpeed;
        this.flapSpeed = (float)(this.flapSpeed + (this.onGround ? -1 : 4) * 0.3D);
        this.flapSpeed = MathHelper.clamp(this.flapSpeed, 0.0F, 1.0F);

        if (!this.onGround && this.flapping < 1.0F)
        {
            this.flapping = 1.0F;
        }

        this.flapping = (float)(this.flapping * 0.9D);

        if (!this.onGround && this.motionY < 0.0D)
        {
            this.motionY *= 0.6D;
        }

        this.flap += this.flapping * 2.0F;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand)
    {
        ItemStack stack = player.getHeldItem(hand);
        if(!isTamed() && stack.getItem() == Items.FISH)
        {
            if(!player.capabilities.isCreativeMode)
                stack.shrink(1);

            if(!isSilent())
                world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_PARROT_EAT, getSoundCategory(), 1.0F, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.2F);

            if(!world.isRemote)
            {
                if(rand.nextInt(10) == 0 && !ForgeEventFactory.onAnimalTame(this, player))
                {
                    setTamedBy(player);
                    playTameEffect(true);
                    world.setEntityState(this, (byte) 7);
                }
                else
                {
                    playTameEffect(false);
                    world.setEntityState(this, (byte) 6);
                }
            }

            return true;
        }
        else
        {
            if(!world.isRemote && !isFlying() && isTamed() && isOwner(player))
                aiSit.setSitting(!isSitting());

            return super.processInteract(player, hand);
        }
    }

    @Override
    public boolean isBreedingItem(ItemStack stack)
    {
        return stack.getItem() == Items.FISH && stack.getMetadata() == FishType.SALMON.getMetadata();
    }

    @Override
    public void fall(float distance, float damageMultiplier)
    { }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos)
    { }

    @Override
    public boolean canMateWith(EntityAnimal otherAnimal)
    {
        if(otherAnimal == this)
            return false;
        else if(!isTamed())
            return false;
        else if(!(otherAnimal instanceof EntityBaldEagle))
            return false;
        else
        {
            EntityBaldEagle otherEagle = (EntityBaldEagle) otherAnimal;
            if(!otherEagle.isTamed())
                return false;
            else if(otherEagle.isSitting())
                return false;
            else
                return isInLove() && otherEagle.isInLove();
        }
    }

    @Override
    @Nullable
    public EntityAgeable createChild(EntityAgeable ageable)
    {
        EntityBaldEagle child = new EntityBaldEagle(world);

        UUID uuid = getOwnerId();
        if(uuid != null)
        {
            child.setOwnerId(uuid);
            child.setTamed(true);
        }

        return child;
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound()
    {
        return ModSounds.baldEagleAmbient;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource)
    {
        return ModSounds.baldEagleHurt;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound()
    {
        return ModSounds.baldEagleDeath;
    }

    @Override
    protected void playStepSound(BlockPos pos, Block block)
    {
        playSound(SoundEvents.ENTITY_PARROT_STEP, 0.15F, 1.0F);
    }

    @Override
    protected float playFlySound(float distance)
    {
        playSound(SoundEvents.ENTITY_PARROT_FLY, 0.15F, 1.0F);
        return distance + this.flapSpeed / 2.0F;
    }

    @Override
    protected boolean makeFlySound()
    {
        return true;
    }

    @Override
    public SoundCategory getSoundCategory()
    {
        return SoundCategory.NEUTRAL;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if(isEntityInvulnerable(source))
            return false;
        else
        {
            if(this.aiSit != null)
                this.aiSit.setSitting(false);

            return super.attackEntityFrom(source, amount);
        }
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable()
    {
        return Resources.LOOT_BALD_EAGLE;
    }

    public boolean isFlying()
    {
        return !this.onGround;
    }
}
