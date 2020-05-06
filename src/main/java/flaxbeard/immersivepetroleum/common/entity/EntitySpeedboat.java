package flaxbeard.immersivepetroleum.common.entity;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.energy.FuelHandler;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.items.ItemSpeedboat;
import flaxbeard.immersivepetroleum.common.network.ConsumeBoatFuelPacket;
import flaxbeard.immersivepetroleum.common.network.IPPacketHandler;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.client.CPacketSteerBoat;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import javax.vecmath.Vector2f;
import java.util.List;

public class EntitySpeedboat extends EntityBoat
{
	private static final DataParameter<Boolean>[] DATA_ID_PADDLE = new DataParameter[]{EntityDataManager.createKey(EntityBoat.class, DataSerializers.BOOLEAN), EntityDataManager.createKey(EntityBoat.class, DataSerializers.BOOLEAN)};
	private static final DataParameter<String> TANK_FLUID = EntityDataManager.<String>createKey(EntitySpeedboat.class, DataSerializers.STRING);
	private static final DataParameter<Integer> TANK_AMOUNT = EntityDataManager.<Integer>createKey(EntitySpeedboat.class, DataSerializers.VARINT);

	private static final DataParameter<ItemStack> UPGRADE_0 = EntityDataManager.createKey(EntitySpeedboat.class, DataSerializers.ITEM_STACK);
	private static final DataParameter<ItemStack> UPGRADE_1 = EntityDataManager.createKey(EntitySpeedboat.class, DataSerializers.ITEM_STACK);
	private static final DataParameter<ItemStack> UPGRADE_2 = EntityDataManager.createKey(EntitySpeedboat.class, DataSerializers.ITEM_STACK);
	private static final DataParameter<ItemStack> UPGRADE_3 = EntityDataManager.createKey(EntitySpeedboat.class, DataSerializers.ITEM_STACK);

	private final float[] paddlePositions;
	/**
	 * How much of current speed to retain. Value zero to one.
	 */
	private float momentum;
	private float outOfControlTicks;
	private float deltaRotation;
	private int lerpSteps;
	private double boatPitch;
	private double lerpY;
	private double lerpZ;
	private double boatYaw;
	private double lerpXRot;
	public boolean leftInputDown;
	public boolean rightInputDown;
	private boolean forwardInputDown;
	private boolean backInputDown;
	public boolean isBoosting;
	private double waterLevel;
	/**
	 * How much the boat should glide given the slippery blocks it's currently gliding over.
	 * Halved every tick.
	 */
	private float boatGlide;
	private EntityBoat.Status status;
	private EntityBoat.Status previousStatus;
	private double lastYd;
	private float lastMoving;
	public float propellerRotation = 0F;

	public boolean inLava = false;

	public EntitySpeedboat(World worldIn)
	{
		super(worldIn);
		this.paddlePositions = new float[2];
		this.preventEntitySpawning = true;
		this.setSize(1.375F, 0.5625F);
	}

	public EntitySpeedboat(World worldIn, double x, double y, double z)
	{
		this(worldIn);
		this.setPosition(x, y, z);
		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;
		this.prevPosX = x;
		this.prevPosY = y;
		this.prevPosZ = z;
	}

	@Override
	protected void entityInit()
	{
		for (DataParameter<Boolean> dataparameter : DATA_ID_PADDLE)
		{
			this.dataManager.register(dataparameter, Boolean.valueOf(false));
		}
		this.dataManager.register(TANK_FLUID, "");
		this.dataManager.register(TANK_AMOUNT, Integer.valueOf(0));
		this.dataManager.register(UPGRADE_0, ItemStack.EMPTY);
		this.dataManager.register(UPGRADE_1, ItemStack.EMPTY);
		this.dataManager.register(UPGRADE_2, ItemStack.EMPTY);
		this.dataManager.register(UPGRADE_3, ItemStack.EMPTY);

		super.entityInit();
	}

	/**
	 * Returns a boundingBox used to collide the entity with other entities and blocks. This enables the entity to be
	 * pushable on contact, like boats or minecarts.
	 */
	@Nullable
	@Override
	public AxisAlignedBB getCollisionBox(Entity entityIn)
	{
		return entityIn.getEntityBoundingBox();
	}

	/**
	 * Returns the collision bounding box for this entity
	 */
	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox()
	{
		return this.getEntityBoundingBox();
	}

	/**
	 * Returns true if this entity should push and be pushed by other entities when colliding.
	 */
	@Override
	public boolean canBePushed()
	{
		return true;
	}

	/**
	 * Returns the Y offset from the entity's position for any entity riding this one.
	 */
	@Override
	public double getMountedYOffset()
	{
		return inLava ? -0.1D + 3.9F / 16F : -0.1D;
	}

	/**
	 * Called when the entity is attacked.
	 */
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount)
	{
		if (this.isEntityInvulnerable(source))
		{
			return false;
		}
		else if (!this.world.isRemote && !this.isDead)
		{
			if (source instanceof EntityDamageSourceIndirect && source.getImmediateSource() != null && this.isPassenger(source.getImmediateSource()))
			{
				return false;
			}
			else
			{
				this.setForwardDirection(-this.getForwardDirection());
				this.setTimeSinceHit(10);
				this.setDamageTaken(this.getDamageTaken() + amount * 10.0F);
				this.markVelocityChanged();
				boolean flag = source.getImmediateSource() instanceof EntityPlayer && ((EntityPlayer) source.getImmediateSource()).capabilities.isCreativeMode;
				boolean flag2 = source.getImmediateSource() instanceof EntityPlayer;
				if (flag || (this.getDamageTaken() > 40.0F && (!this.isFireproof || flag2)) || (this.getDamageTaken() > 240.0F))
				{
					if (!flag && this.world.getGameRules().getBoolean("doEntityDrops"))
					{
						ItemSpeedboat item = (ItemSpeedboat) getItemBoat();
						ItemStack stack = new ItemStack(item, 1, 0);
						NonNullList<ItemStack> contained = item.getContainedItems(stack);
						NonNullList<ItemStack> upgrades = this.getUpgrades();
						for (int i = 0; i < contained.size() && i < upgrades.size(); i++)
						{
							contained.set(i, upgrades.get(i));
						}
						stack.setTagCompound(new NBTTagCompound());
						item.setContainedItems(stack, contained);
						writeTank(stack.getTagCompound(), true);

						if (source.getImmediateSource() instanceof EntityPlayer)
						{
							EntityPlayer player = (EntityPlayer) source.getImmediateSource();
							if (!player.addItemStackToInventory(stack))
							{
								EntityItem entityitem = new EntityItem(this.world, player.posX, player.posY, player.posZ, stack);
								world.spawnEntity(entityitem);
							}
						}
						else
						{
							this.entityDropItem(stack, 0F);
						}
					}

					this.setDead();
				}

				return true;
			}
		}
		else
		{
			return true;
		}
	}

	public void readTank(NBTTagCompound nbt)
	{
		FluidTank tank = new FluidTank(getMaxFuel());
		if (nbt != null)
			tank.readFromNBT(nbt.getCompoundTag("tank"));
		setContainedFluid(tank.getFluid());
	}

	public void writeTank(NBTTagCompound nbt, boolean toItem)
	{
		FluidTank tank = new FluidTank(getMaxFuel());
		FluidStack fs = getContainedFluid();
		tank.setFluid(fs);
		boolean write = tank.getFluidAmount() > 0;
		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		if (!toItem || write)
			nbt.setTag("tank", tankTag);
	}


	@Override
	public Item getItemBoat()
	{
		return IPContent.itemSpeedboat;
	}

	/**
	 * Setups the entity to do the hurt animation. Only used by packets in multiplayer.
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public void performHurtAnimation()
	{
		this.setForwardDirection(-this.getForwardDirection());
		this.setTimeSinceHit(10);
		this.setDamageTaken(this.getDamageTaken() * 11.0F);
	}

	/**
	 * Returns true if other Entities should be prevented from moving through this Entity.
	 */
	@Override
	public boolean canBeCollidedWith()
	{
		return !this.isDead;
	}

	/**
	 * Set the position and rotation values directly without any clamping.
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport)
	{
		this.boatPitch = x;
		this.lerpY = y;
		this.lerpZ = z;
		this.boatYaw = (double) yaw;
		this.lerpXRot = (double) pitch;
		this.lerpSteps = 10;
	}

	/**
	 * Gets the horizontal facing direction of this Entity, adjusted to take specially-treated entity types into
	 * account.
	 */
	@Override
	public EnumFacing getAdjustedHorizontalFacing()
	{
		return this.getHorizontalFacing().rotateY();
	}

	public static DataParameter<Byte> getFlags()
	{
		return FLAGS;
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate()
	{
		this.previousStatus = this.status;
		this.status = this.getBoatStatus();

		if (this.status != EntityBoat.Status.UNDER_WATER && this.status != EntityBoat.Status.UNDER_FLOWING_WATER)
		{
			this.outOfControlTicks = 0.0F;
		}
		else
		{
			++this.outOfControlTicks;
		}

		if (!this.world.isRemote && this.outOfControlTicks >= 60.0F)
		{
			this.removePassengers();
		}

		if (this.getTimeSinceHit() > 0)
		{
			this.setTimeSinceHit(this.getTimeSinceHit() - 1);
		}

		if (this.getDamageTaken() > 0.0F)
		{
			this.setDamageTaken(this.getDamageTaken() - 1.0F);
		}

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		if (!this.world.isRemote)
		{
			this.setFlag(6, this.isGlowing());
		}

		this.onEntityUpdate();
		this.tickLerp();

		if (this.canPassengerSteer())
		{
			if (this.getPassengers().size() == 0 || !(this.getPassengers().get(0) instanceof EntityPlayer))
			{
				this.setPaddleState(false, false);
			}

			this.updateMotion();

			if (this.world.isRemote)
			{
				this.controlBoat();
				this.world.sendPacketToServer(new CPacketSteerBoat(this.getPaddleState(0), this.getPaddleState(1)));
			}

			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		}
		else
		{
			this.motionX = 0.0D;
			this.motionY = 0.0D;
			this.motionZ = 0.0D;
		}

		if (this.world.isRemote)
		{
			if (!isEmergency())
			{
				float moving = (this.forwardInputDown || this.backInputDown) ? (isBoosting ? .9F : .7F) : 0.5F;
				if (lastMoving != moving)
				{
					ImmersivePetroleum.proxy.handleEntitySound(IESounds.dieselGenerator, this, false, .5f, 0.5F);
				}
				ImmersivePetroleum.proxy.handleEntitySound(IESounds.dieselGenerator, this, this.isBeingRidden() && this.getContainedFluid() != null && this.getContainedFluid().amount > 0, this.forwardInputDown || this.backInputDown ? .5f : .3f, moving);
				lastMoving = moving;

				if (this.forwardInputDown && this.world.rand.nextInt(2) == 0)
				{
					if (inLava)
					{
						if (this.world.rand.nextInt(3) == 0)
						{
							float xO = (float) (MathHelper.sin(-this.rotationYaw * 0.017453292F)) + (world.rand.nextFloat() - .5F) * .3F;
							float zO = (float) (MathHelper.cos(this.rotationYaw * 0.017453292F)) + (world.rand.nextFloat() - .5F) * .3F;
							float yO = .4F + (world.rand.nextFloat() - .5F) * .3F;
							world.spawnParticle(EnumParticleTypes.LAVA, posX - xO * 1.5F, posY + yO, posZ - zO * 1.5F, -2 * motionX, 0, -2 * motionZ, 5);
						}
					}
					else
					{
						float xO = (float) (MathHelper.sin(-this.rotationYaw * 0.017453292F)) + (world.rand.nextFloat() - .5F) * .3F;
						float zO = (float) (MathHelper.cos(this.rotationYaw * 0.017453292F)) + (world.rand.nextFloat() - .5F) * .3F;
						float yO = .1F + (world.rand.nextFloat() - .5F) * .3F;
						world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX - xO * 1.5F, posY + yO, posZ - zO * 1.5F, 0, 0, 0, 5);
					}
				}
				if (isBoosting && this.world.rand.nextInt(2) == 0)
				{
					float xO = (float) (MathHelper.sin(-this.rotationYaw * 0.017453292F)) + (world.rand.nextFloat() - .5F) * .3F;
					float zO = (float) (MathHelper.cos(this.rotationYaw * 0.017453292F)) + (world.rand.nextFloat() - .5F) * .3F;
					float yO = .8F + (world.rand.nextFloat() - .5F) * .3F;
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX - xO * 1.3F, posY + yO, posZ - zO * 1.3F, 0, 0, 0, 5);
				}
			}
			else
			{
				ImmersivePetroleum.proxy.handleEntitySound(IESounds.dieselGenerator, this, false, .5f, 0.5F);
			}
		}

		if (!this.isEmergency())
		{
			if (this.getPaddleState(0))
			{
				this.paddlePositions[0] = (float) ((double) this.paddlePositions[0] + (isBoosting ? 0.02D : 0.01D));
			}
			else if (this.getPaddleState(1))
			{
				this.paddlePositions[0] = (float) ((double) this.paddlePositions[0] - 0.01D);
			}
		}
		else
		{
			for (int i = 0; i <= 1; ++i)
			{
				if (this.getPaddleState(i))
				{
					this.paddlePositions[i] = (float) ((double) this.paddlePositions[i] + 0.01D);
				}
				else
				{
					this.paddlePositions[i] = 0.0F;
				}
			}
		}

		//


		float xO = (float) (MathHelper.sin(-this.rotationYaw * 0.017453292F));
		float zO = (float) (MathHelper.cos(this.rotationYaw * 0.017453292F));
		Vector2f vec = new Vector2f(xO, zO);
		vec.normalize();

		if (hasIcebreaker && !isEmergency())
		{
			AxisAlignedBB axisalignedbb = this.getEntityBoundingBox().grow(0.1f);
			BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain(axisalignedbb.minX + 0.001D, axisalignedbb.minY + 0.001D, axisalignedbb.minZ + 0.001D);
			BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos1 = BlockPos.PooledMutableBlockPos.retain(axisalignedbb.maxX - 0.001D, axisalignedbb.maxY - 0.001D, axisalignedbb.maxZ - 0.001D);
			BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos2 = BlockPos.PooledMutableBlockPos.retain();

			if (this.world.isAreaLoaded(blockpos$pooledmutableblockpos, blockpos$pooledmutableblockpos1))
			{
				for (int i = blockpos$pooledmutableblockpos.getX(); i <= blockpos$pooledmutableblockpos1.getX(); ++i)
				{
					for (int j = blockpos$pooledmutableblockpos.getY(); j <= blockpos$pooledmutableblockpos1.getY(); ++j)
					{
						for (int k = blockpos$pooledmutableblockpos.getZ(); k <= blockpos$pooledmutableblockpos1.getZ(); ++k)
						{
							blockpos$pooledmutableblockpos2.setPos(i, j, k);
							IBlockState iblockstate = this.world.getBlockState(blockpos$pooledmutableblockpos2);

							Vector2f vec2 = new Vector2f((float) (i + 0.5f - posX), (float) (k + 0.5f - posZ));
							vec2.normalize();

							float sim = vec2.dot(vec);

							if (iblockstate.getBlock() == Blocks.ICE && sim > .3f)
							{
								this.world.destroyBlock(blockpos$pooledmutableblockpos2, false);
								this.world.setBlockState(blockpos$pooledmutableblockpos2, Blocks.FLOWING_WATER.getDefaultState());
							}
						}
					}
				}
			}

			blockpos$pooledmutableblockpos.release();
			blockpos$pooledmutableblockpos1.release();
			blockpos$pooledmutableblockpos2.release();
		}


		//

		this.doBlockCollisions();
		List<Entity> list = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().expand(0.20000000298023224D, -0.009999999776482582D, 0.20000000298023224D), EntitySelectors.<Entity>getTeamCollisionPredicate(this));

		if (!list.isEmpty())
		{
			boolean flag = !this.world.isRemote && !(this.getControllingPassenger() instanceof EntityPlayer);

			for (int j = 0; j < list.size(); ++j)
			{
				Entity entity = (Entity) list.get(j);

				if (!this.isPassenger(entity))
				{
					if (flag && this.getPassengers().size() < 2 && !entity.isRiding() && entity.width < this.width && entity instanceof EntityLivingBase && !(entity instanceof EntityWaterMob) && !(entity instanceof EntityPlayer))
					{
						entity.startRiding(this);
					}
					else
					{
						this.applyEntityCollision(entity);

						if (hasIcebreaker)
						{
							if (entity instanceof EntityLivingBase && !(entity instanceof EntityPlayer) && this.getControllingPassenger() instanceof EntityPlayer)
							{

								Vector2f vec2 = new Vector2f((float) (entity.posX - posX), (float) (entity.posZ - posZ));
								vec2.normalize();

								float sim = vec2.dot(vec);

								if (sim > .5f)
								{
									entity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.getControllingPassenger()), 4);
									entity.motionX += vec2.x * .75F;
									entity.motionZ += vec2.y * .75F;
								}
							}
						}
					}
				}
			}
		}
	}

	private void tickLerp()
	{
		if (this.lerpSteps > 0 && !this.canPassengerSteer())
		{
			double d0 = this.posX + (this.boatPitch - this.posX) / (double) this.lerpSteps;
			double d1 = this.posY + (this.lerpY - this.posY) / (double) this.lerpSteps;
			double d2 = this.posZ + (this.lerpZ - this.posZ) / (double) this.lerpSteps;
			double d3 = MathHelper.wrapDegrees(this.boatYaw - (double) this.rotationYaw);
			this.rotationYaw = (float) ((double) this.rotationYaw + d3 / (double) this.lerpSteps);
			this.rotationPitch = (float) ((double) this.rotationPitch + (this.lerpXRot - (double) this.rotationPitch) / (double) this.lerpSteps);
			--this.lerpSteps;
			this.setPosition(d0, d1, d2);
			this.setRotation(this.rotationYaw, this.rotationPitch);
		}
	}

	@Override
	public void setPaddleState(boolean p_184445_1_, boolean p_184445_2_)
	{
		this.dataManager.set(DATA_ID_PADDLE[0], Boolean.valueOf(p_184445_1_));
		this.dataManager.set(DATA_ID_PADDLE[1], Boolean.valueOf(p_184445_2_));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float getRowingTime(int p_184448_1_, float limbSwing)
	{
		if (isEmergency())
		{
			return this.getPaddleState(p_184448_1_) ? (float) MathHelper.clampedLerp((double) this.paddlePositions[p_184448_1_] - 0.01D, (double) this.paddlePositions[p_184448_1_], (double) limbSwing) : 0.0F;
		}

		if (this.getPaddleState(0))
		{
			return (float) MathHelper.clampedLerp((double) this.paddlePositions[p_184448_1_] - (isBoosting ? 0.02D : 0.01D), (double) this.paddlePositions[p_184448_1_], (double) limbSwing);
		}
		else
		{
			return this.getPaddleState(1) ? (float) MathHelper.clampedLerp((double) this.paddlePositions[p_184448_1_] + 0.01D, (double) this.paddlePositions[p_184448_1_], (double) limbSwing) : this.paddlePositions[p_184448_1_];
		}
	}

	/**
	 * Determines whether the boat is in water, gliding on land, or in air
	 */
	private EntityBoat.Status getBoatStatus()
	{
		EntityBoat.Status entityboat$status = this.getUnderwaterStatus();

		if (entityboat$status != null)
		{
			this.waterLevel = this.getEntityBoundingBox().maxY;
			return entityboat$status;
		}
		else if (this.checkInWater())
		{
			return EntityBoat.Status.IN_WATER;
		}
		else
		{
			float f = this.getBoatGlide();

			if (f > 0.0F)
			{
				this.boatGlide = f;
				return EntityBoat.Status.ON_LAND;
			}
			else
			{
				return EntityBoat.Status.IN_AIR;
			}
		}
	}

	@Override
	public float getWaterLevelAbove()
	{
		AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
		int i = MathHelper.floor(axisalignedbb.minX);
		int j = MathHelper.ceil(axisalignedbb.maxX);
		int k = MathHelper.floor(axisalignedbb.maxY);
		int l = MathHelper.ceil(axisalignedbb.maxY - this.lastYd);
		int i1 = MathHelper.floor(axisalignedbb.minZ);
		int j1 = MathHelper.ceil(axisalignedbb.maxZ);
		BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

		try
		{
			label78:

			for (int k1 = k; k1 < l; ++k1)
			{
				float f = 0.0F;
				int l1 = i;

				while (true)
				{
					if (l1 >= j)
					{
						if (f < 1.0F)
						{
							float f2 = (float) blockpos$pooledmutableblockpos.getY() + f;
							return f2;
						}

						break;
					}

					for (int i2 = i1; i2 < j1; ++i2)
					{
						blockpos$pooledmutableblockpos.setPos(l1, k1, i2);
						IBlockState iblockstate = this.world.getBlockState(blockpos$pooledmutableblockpos);

						if (iblockstate.getMaterial() == Material.WATER || (isFireproof && iblockstate.getMaterial() == Material.LAVA))
						{
							f = Math.max(f, BlockLiquid.getBlockLiquidHeight(iblockstate, this.world, blockpos$pooledmutableblockpos));
						}

						if (f >= 1.0F)
						{
							continue label78;
						}
					}

					++l1;
				}
			}

			float f1 = (float) (l + 1);
			return f1;
		} finally
		{
			blockpos$pooledmutableblockpos.release();
		}
	}

	/**
	 * Decides how much the boat should be gliding on the land (based on any slippery blocks)
	 */
	@Override
	public float getBoatGlide()
	{
		AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
		AxisAlignedBB axisalignedbb1 = new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY - 0.001D, axisalignedbb.minZ, axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ);
		int i = MathHelper.floor(axisalignedbb1.minX) - 1;
		int j = MathHelper.ceil(axisalignedbb1.maxX) + 1;
		int k = MathHelper.floor(axisalignedbb1.minY) - 1;
		int l = MathHelper.ceil(axisalignedbb1.maxY) + 1;
		int i1 = MathHelper.floor(axisalignedbb1.minZ) - 1;
		int j1 = MathHelper.ceil(axisalignedbb1.maxZ) + 1;
		List<AxisAlignedBB> list = Lists.<AxisAlignedBB>newArrayList();
		float f = 0.0F;
		int k1 = 0;
		BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

		try
		{
			for (int l1 = i; l1 < j; ++l1)
			{
				for (int i2 = i1; i2 < j1; ++i2)
				{
					int j2 = (l1 != i && l1 != j - 1 ? 0 : 1) + (i2 != i1 && i2 != j1 - 1 ? 0 : 1);

					if (j2 != 2)
					{
						for (int k2 = k; k2 < l; ++k2)
						{
							if (j2 <= 0 || k2 != k && k2 != l - 1)
							{
								blockpos$pooledmutableblockpos.setPos(l1, k2, i2);
								IBlockState iblockstate = this.world.getBlockState(blockpos$pooledmutableblockpos);
								iblockstate.addCollisionBoxToList(this.world, blockpos$pooledmutableblockpos, axisalignedbb1, list, this, false);
								if (!list.isEmpty())
								{
									f += iblockstate.getBlock().slipperiness;
									++k1;
								}

								list.clear();
							}
						}
					}
				}
			}
		} finally
		{
			blockpos$pooledmutableblockpos.release();
		}

		return f / (float) k1;
	}

	private boolean checkInWater()
	{
		AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
		int i = MathHelper.floor(axisalignedbb.minX);
		int j = MathHelper.ceil(axisalignedbb.maxX);
		int k = MathHelper.floor(axisalignedbb.minY);
		int l = MathHelper.ceil(axisalignedbb.minY + 0.001D);
		int i1 = MathHelper.floor(axisalignedbb.minZ);
		int j1 = MathHelper.ceil(axisalignedbb.maxZ);
		boolean flag = false;
		this.waterLevel = Double.MIN_VALUE;
		BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();
		inLava = false;
		try
		{
			for (int k1 = i; k1 < j; ++k1)
			{
				for (int l1 = k; l1 < l; ++l1)
				{
					for (int i2 = i1; i2 < j1; ++i2)
					{
						blockpos$pooledmutableblockpos.setPos(k1, l1, i2);
						IBlockState iblockstate = this.world.getBlockState(blockpos$pooledmutableblockpos);

						if (iblockstate.getMaterial() == Material.WATER || (isFireproof && iblockstate.getMaterial() == Material.LAVA))
						{
							if (iblockstate.getMaterial() == Material.LAVA)
							{
								inLava = true;
							}
							float f = BlockLiquid.getLiquidHeight(iblockstate, this.world, blockpos$pooledmutableblockpos);
							this.waterLevel = Math.max((double) f, this.waterLevel);
							flag |= axisalignedbb.minY < (double) f;
						}
					}
				}
			}
		} finally
		{
			blockpos$pooledmutableblockpos.release();
		}

		return flag;
	}

	/**
	 * Decides whether the boat is currently underwater.
	 */
	@Nullable
	private EntityBoat.Status getUnderwaterStatus()
	{
		AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
		double d0 = axisalignedbb.maxY + 0.001D;
		int i = MathHelper.floor(axisalignedbb.minX);
		int j = MathHelper.ceil(axisalignedbb.maxX);
		int k = MathHelper.floor(axisalignedbb.maxY);
		int l = MathHelper.ceil(d0);
		int i1 = MathHelper.floor(axisalignedbb.minZ);
		int j1 = MathHelper.ceil(axisalignedbb.maxZ);
		boolean flag = false;
		BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

		try
		{
			for (int k1 = i; k1 < j; ++k1)
			{
				for (int l1 = k; l1 < l; ++l1)
				{
					for (int i2 = i1; i2 < j1; ++i2)
					{
						blockpos$pooledmutableblockpos.setPos(k1, l1, i2);
						IBlockState iblockstate = this.world.getBlockState(blockpos$pooledmutableblockpos);

						if ((iblockstate.getMaterial() == Material.WATER || (isFireproof && iblockstate.getMaterial() == Material.LAVA)) && d0 < (double) BlockLiquid.getLiquidHeight(iblockstate, this.world, blockpos$pooledmutableblockpos))
						{
							if (((Integer) iblockstate.getValue(BlockLiquid.LEVEL)).intValue() != 0)
							{
								EntityBoat.Status entityboat$status = EntityBoat.Status.UNDER_FLOWING_WATER;
								return entityboat$status;
							}

							flag = true;
						}
					}
				}
			}
		} finally
		{
			blockpos$pooledmutableblockpos.release();
		}

		return flag ? EntityBoat.Status.UNDER_WATER : null;
	}

	/**
	 * Update the boat's speed, based on momentum.
	 */
	private void updateMotion()
	{
		double d0 = -0.03999999910593033D;
		double d1 = this.hasNoGravity() ? 0.0D : -0.03999999910593033D;
		double d2 = 0.0D;
		this.momentum = 0.05F;

		if (this.previousStatus == EntityBoat.Status.IN_AIR && this.status != EntityBoat.Status.IN_AIR && this.status != EntityBoat.Status.ON_LAND)
		{
			this.waterLevel = this.getEntityBoundingBox().minY + (double) this.height;
			this.setPosition(this.posX, (double) (this.getWaterLevelAbove() - this.height) + 0.101D, this.posZ);
			this.motionY = 0.0D;
			this.lastYd = 0.0D;
			this.status = EntityBoat.Status.IN_WATER;
		}
		else
		{
			if (this.status == EntityBoat.Status.IN_WATER)
			{
				d2 = (this.waterLevel - this.getEntityBoundingBox().minY) / (double) this.height;
				this.momentum = 0.9F;
			}
			else if (this.status == EntityBoat.Status.UNDER_FLOWING_WATER)
			{
				d1 = -7.0E-4D;
				this.momentum = 0.9F;
			}
			else if (this.status == EntityBoat.Status.UNDER_WATER)
			{
				d2 = 0.009999999776482582D;
				this.momentum = 0.45F;
			}
			else if (this.status == EntityBoat.Status.IN_AIR)
			{
				this.momentum = 0.9F;
			}
			else if (this.status == EntityBoat.Status.ON_LAND)
			{
				this.momentum = this.boatGlide;

				if (this.getControllingPassenger() instanceof EntityPlayer)
				{
					this.boatGlide /= 2.0F;
				}
			}


			this.motionX *= (double) this.momentum;
			this.motionZ *= (double) this.momentum;
			this.deltaRotation *= this.momentum;
			this.motionY += d1;

			if (d2 > 0.0D)
			{
				double d3 = 0.65D;
				this.motionY += d2 * 0.06153846016296973D;
				double d4 = 0.75D;
				this.motionY *= 0.75D;
			}
		}
	}

	public boolean isEmergency()
	{
		FluidStack fluid = this.getContainedFluid();
		int consumeAmount = 0;
		if (fluid != null)
		{
			consumeAmount = FuelHandler.getBoatFuelUsedPerTick(fluid.getFluid());
			return fluid.amount <= consumeAmount && hasPaddles;
		}
		else
		{
			return hasPaddles;
		}
	}

	private void controlBoat()
	{
		if (this.isBeingRidden())
		{
			float f = 0.0F;

			if (isEmergency())
			{
				if (this.leftInputDown)
				{
					this.deltaRotation += -1.0F;
				}

				if (this.rightInputDown)
				{
					++this.deltaRotation;
				}

				if (this.rightInputDown != this.leftInputDown && !this.forwardInputDown && !this.backInputDown)
				{
					f += 0.005F;
				}

				this.rotationYaw += this.deltaRotation;

				if (this.forwardInputDown)
				{
					f += 0.04F;
				}

				if (this.backInputDown)
				{
					f -= 0.005F;
				}

				this.motionX += (double) (MathHelper.sin(-this.rotationYaw * 0.017453292F) * f);
				this.motionZ += (double) (MathHelper.cos(this.rotationYaw * 0.017453292F) * f);
				this.setPaddleState(this.rightInputDown || this.forwardInputDown, this.leftInputDown || this.forwardInputDown);
			}
			else
			{


				this.rotationYaw += this.deltaRotation;

				FluidStack fluid = this.getContainedFluid();
				int consumeAmount = 0;
				if (fluid != null)
				{
					consumeAmount = FuelHandler.getBoatFuelUsedPerTick(fluid.getFluid());
				}
				if (fluid != null && fluid.amount >= consumeAmount && (forwardInputDown || backInputDown))
				{
					int toConsume = consumeAmount;
					if (this.forwardInputDown)
					{
						f += 0.04F * 1.25F;
						if (this.isBoosting && fluid.amount >= 3 * consumeAmount)
						{
							f *= 1.6;
							toConsume *= 3;
						}
					}

					if (this.backInputDown)
					{
						f -= 0.005F * 2F;
					}
					fluid.amount = Math.max(0, fluid.amount - toConsume);
					this.setContainedFluid(fluid);
					IPPacketHandler.INSTANCE.sendToServer(new ConsumeBoatFuelPacket(toConsume));

					this.setPaddleState(this.forwardInputDown, this.backInputDown);

				}
				else
				{
					this.setPaddleState(false, false);
				}

				this.motionX += (double) (MathHelper.sin(-this.rotationYaw * 0.017453292F) * f);
				this.motionZ += (double) (MathHelper.cos(this.rotationYaw * 0.017453292F) * f);

				float speed = (float) Math.sqrt(motionX * motionX + motionZ * motionZ);

				if (this.leftInputDown)
				{
					this.deltaRotation += -1.1F * speed * (hasRudders ? 1.5F : 1F) * (isBoosting ? 0.5F : 1) * (backInputDown && !forwardInputDown ? 2F : 1F);
					if (propellerRotation > -1F)
					{
						propellerRotation -= 0.2F;
					}
				}

				if (this.rightInputDown)
				{
					this.deltaRotation += 1.1F * speed * (hasRudders ? 1.5F : 1F) * (isBoosting ? 0.5F : 1) * (backInputDown && !forwardInputDown ? 2F : 1F);
					if (propellerRotation < 1F)
					{
						propellerRotation += 0.2F;
					}
				}

				if (!this.rightInputDown && !this.leftInputDown)
				{
					propellerRotation *= 0.7F;
				}


			}
		}
	}

	@Override
	public void updatePassenger(Entity passenger)
	{
		if (this.isPassenger(passenger))
		{

			float f = 0.0F;
			float f1 = (float) ((this.isDead ? 0.009999999776482582D : this.getMountedYOffset()) + passenger.getYOffset());

			if (this.getPassengers().size() > 1)
			{
				int i = this.getPassengers().indexOf(passenger);

				if (i == 0)
				{
					f = 0.2F;
				}
				else
				{
					f = -0.6F;
				}

				if (passenger instanceof EntityAnimal)
				{
					f = (float) ((double) f + 0.2D);
				}
			}

			Vec3d vec3d = (new Vec3d((double) f, 0.0D, 0.0D)).rotateYaw(-this.rotationYaw * 0.017453292F - ((float) Math.PI / 2F));
			passenger.setPosition(this.posX + vec3d.x, this.posY + (double) f1, this.posZ + vec3d.z);
			passenger.rotationYaw += this.deltaRotation;
			passenger.setRotationYawHead(passenger.getRotationYawHead() + this.deltaRotation);
			this.applyYawToEntity(passenger);

			if (passenger instanceof EntityAnimal && this.getPassengers().size() > 1)
			{
				int j = passenger.getEntityId() % 2 == 0 ? 90 : 270;
				passenger.setRenderYawOffset(((EntityAnimal) passenger).renderYawOffset + (float) j);
				passenger.setRotationYawHead(passenger.getRotationYawHead() + (float) j);
			}
		}
	}

	/**
	 * Applies this boat's yaw to the given entity. Used to update the orientation of its passenger.
	 */
	@Override
	protected void applyYawToEntity(Entity entityToUpdate)
	{
		entityToUpdate.setRenderYawOffset(this.rotationYaw);
		float f = MathHelper.wrapDegrees(entityToUpdate.rotationYaw - this.rotationYaw);
		float f1 = MathHelper.clamp(f, -105.0F, 105.0F);
		entityToUpdate.prevRotationYaw += f1 - f;
		entityToUpdate.rotationYaw += f1 - f;
		entityToUpdate.setRotationYawHead(entityToUpdate.rotationYaw);
	}

	/**
	 * Applies this entity's orientation (pitch/yaw) to another entity. Used to update passenger orientation.
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public void applyOrientationToEntity(Entity entityToUpdate)
	{
		this.applyYawToEntity(entityToUpdate);
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	@Override
	protected void writeEntityToNBT(NBTTagCompound compound)
	{
		compound.setString("Type", this.getBoatType().getName());
		FluidStack fs = getContainedFluid();
		compound.setString("tank_fluid", fs == null ? "" : fs.getFluid().getName());
		compound.setInteger("tank_amount", fs == null ? 0 : fs.amount);
		NBTTagList list = new NBTTagList();

		NonNullList<ItemStack> upgrades = getUpgrades();
		for (int i = 0; i < upgrades.size(); i++)
		{
			NBTTagCompound nbt = new NBTTagCompound();
			if (!upgrades.get(i).isEmpty())
				upgrades.get(i).writeToNBT(nbt);
			list.appendTag(nbt);
		}
		compound.setTag("upgrades", list);
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound)
	{
		if (compound.hasKey("Type", 8))
		{
			this.setBoatType(EntityBoat.Type.getTypeFromString(compound.getString("Type")));
		}

		String fluidName = compound.getString("tank_fluid");
		Fluid f = FluidRegistry.getFluid(fluidName);
		int amount = compound.getInteger("tank_amount");
		setContainedFluid(f == null ? null : new FluidStack(f, amount));

		NBTTagList list = (NBTTagList) compound.getTag("upgrades");

		NonNullList<ItemStack> upgrades = NonNullList.withSize(4, ItemStack.EMPTY);
		for (int i = 0; i < list.tagCount(); i++)
		{
			upgrades.set(i, new ItemStack(list.getCompoundTagAt(i)));
		}
		setUpgrades(upgrades);
	}

	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if (Utils.isFluidRelatedItemStack(stack))
		{
			FluidTank tank = new FluidTank(getMaxFuel())
			{
				@Override
				public int fill(FluidStack resource, boolean doFill)
				{
					if (!isFluidValid(resource))
						return 0;

					return super.fill(resource, doFill);
				}
			};
			FluidStack fs = getContainedFluid();
			tank.setFluid(fs);
			FluidUtil.interactWithFluidHandler(player, hand, tank);
			setContainedFluid(tank.getFluid());
			return true;
		}

		if (!this.world.isRemote && !player.isSneaking() && this.outOfControlTicks < 60.0F && !player.isRidingOrBeingRiddenBy(this))
		{
			player.startRiding(this);
			return true;
		}

		return false;
	}

	@Override
	protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos)
	{
		this.lastYd = this.motionY;

		if (!this.isRiding())
		{
			if (onGroundIn)
			{
				if (this.fallDistance > 3.0F)
				{
					if (this.status != EntityBoat.Status.ON_LAND)
					{
						this.fallDistance = 0.0F;
						return;
					}

					this.fall(this.fallDistance, 1.0F);

					if (!this.world.isRemote && !this.isDead)
					{
						this.setDead();

						if (this.world.getGameRules().getBoolean("doEntityDrops"))
						{
							this.dropItemWithOffset(this.getItemBoat(), 1, 0.0F);

						}
					}
				}

				this.fallDistance = 0.0F;
			}
			else if (this.world.getBlockState((new BlockPos(this)).down()).getMaterial() != Material.WATER && y < 0.0D)
			{
				this.fallDistance = (float) ((double) this.fallDistance - y);
			}
		}
	}

	public boolean getPaddleState(int p_184457_1_)
	{
		return ((Boolean) this.dataManager.get(DATA_ID_PADDLE[p_184457_1_])).booleanValue() && this.getControllingPassenger() != null;
	}

	@Override
	protected boolean canFitPassenger(Entity passenger)
	{
		return this.getPassengers().size() < 2;
	}

	/**
	 * For vehicles, the first passenger is generally considered the controller and "drives" the vehicle. For example,
	 * Pigs, Horses, and Boats are generally "steered" by the controlling passenger.
	 */
	@Nullable
	public Entity getControllingPassenger()
	{
		List<Entity> list = this.getPassengers();
		return list.isEmpty() ? null : (Entity) list.get(0);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void updateInputs(boolean p_184442_1_, boolean p_184442_2_, boolean p_184442_3_, boolean p_184442_4_)
	{
		this.leftInputDown = p_184442_1_;
		this.rightInputDown = p_184442_2_;
		this.forwardInputDown = p_184442_3_;
		this.backInputDown = p_184442_4_;
		this.isBoosting = isEmergency() ? false : (forwardInputDown && Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown());
	}

	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop)
	{
		if (Utils.isFluidRelatedItemStack(player.getHeldItem(EnumHand.MAIN_HAND)))
		{
			String s = null;
			FluidStack stack = getContainedFluid();
			if (stack != null && stack.getFluid() != null)
				s = stack.getFluid().getLocalizedName(stack) + ": " + stack.amount + "mB";
			else
				s = I18n.format(Lib.GUI + "empty");
			return new String[]{s};

		}
		return null;
	}

	protected boolean isFluidValid(FluidStack resource)
	{
		return resource != null && resource.getFluid() != null && FuelHandler.isValidBoatFuel(resource.getFluid());
	}

	public int getMaxFuel()
	{
		return hasTank ? 16000 : 8000;
	}

	public FluidStack getContainedFluid()
	{
		String fluidName = ((String) this.dataManager.get(TANK_FLUID));
		Fluid f = FluidRegistry.getFluid(fluidName);
		if (f == null) return null;

		int amount = (Integer) this.dataManager.get(TANK_AMOUNT);
		if (amount == 0) return null;

		return new FluidStack(f, amount);
	}

	public void setContainedFluid(FluidStack stack)
	{
		if (stack == null)
		{
			this.dataManager.set(TANK_FLUID, "");
			this.dataManager.set(TANK_AMOUNT, 0);
		}
		else
		{
			this.dataManager.set(TANK_FLUID, stack.getFluid() == null ? "" : stack.getFluid().getName());
			this.dataManager.set(TANK_AMOUNT, stack.amount);
		}

	}

	public boolean canRiderInteract()
	{
		return true;
	}

	public NonNullList<ItemStack> getUpgrades()
	{
		NonNullList<ItemStack> stackList = NonNullList.withSize(4, ItemStack.EMPTY);
		ItemStack o0 = this.dataManager.get(UPGRADE_0);
		stackList.set(0, o0);
		ItemStack o1 = this.dataManager.get(UPGRADE_1);
		stackList.set(1, o1);
		ItemStack o2 = this.dataManager.get(UPGRADE_2);
		stackList.set(2, o2);
		ItemStack o3 = this.dataManager.get(UPGRADE_3);
		stackList.set(3, o3);
		return stackList;
	}

	public void setUpgrades(NonNullList<ItemStack> nonNullList)
	{
		if (nonNullList != null && nonNullList.size() >= 4)
		{
			ItemStack o0 = nonNullList.get(0) == null ? ItemStack.EMPTY : nonNullList.get(0);
			ItemStack o1 = nonNullList.get(1) == null ? ItemStack.EMPTY : nonNullList.get(1);
			ItemStack o2 = nonNullList.get(2) == null ? ItemStack.EMPTY : nonNullList.get(2);
			ItemStack o3 = nonNullList.get(3) == null ? ItemStack.EMPTY : nonNullList.get(3);
			this.dataManager.set(UPGRADE_0, o0);
			this.dataManager.set(UPGRADE_1, o1);
			this.dataManager.set(UPGRADE_2, o2);
			this.dataManager.set(UPGRADE_3, o3);

		}
	}


	@Override
	public void notifyDataManagerChange(DataParameter<?> key)
	{
		if (key == UPGRADE_0 || key == UPGRADE_1 || key == UPGRADE_2 || key == UPGRADE_3)
		{
			NonNullList<ItemStack> upgrades = getUpgrades();
			isFireproof = false;
			hasIcebreaker = false;
			isImmuneToFire = false;
			for (ItemStack upgrade : upgrades)
			{
				if (upgrade != null)
				{
					if (upgrade.getItem() == IPContent.itemUpgrades && upgrade.getItemDamage() == 0)
					{
						isFireproof = true;
						isImmuneToFire = true;
					}
					else if (upgrade.getItem() == IPContent.itemUpgrades && upgrade.getItemDamage() == 1)
					{
						hasIcebreaker = true;
					}
					else if (upgrade.getItem() == IPContent.itemUpgrades && upgrade.getItemDamage() == 2)
					{
						hasTank = true;
					}
					else if (upgrade.getItem() == IPContent.itemUpgrades && upgrade.getItemDamage() == 3)
					{
						hasRudders = true;
					}
					else if (upgrade.getItem() == IPContent.itemUpgrades && upgrade.getItemDamage() == 4)
					{
						hasPaddles = true;
					}
				}
			}
		}
		super.notifyDataManagerChange(key);
	}

	public boolean isFireproof = false;
	public boolean hasIcebreaker = false;
	public boolean hasTank = false;
	public boolean hasRudders = false;
	public boolean hasPaddles = false;

}