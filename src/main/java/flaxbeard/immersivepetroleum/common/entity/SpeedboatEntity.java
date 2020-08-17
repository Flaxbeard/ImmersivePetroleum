package flaxbeard.immersivepetroleum.common.entity;

import java.util.List;

import javax.vecmath.Vector2f;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.energy.FuelHandler;
import flaxbeard.immersivepetroleum.common.IPContent.BoatUpgrades;
import flaxbeard.immersivepetroleum.common.IPContent.Items;
import flaxbeard.immersivepetroleum.common.items.ItemSpeedboat;
import flaxbeard.immersivepetroleum.common.network.ConsumeBoatFuelPacket;
import flaxbeard.immersivepetroleum.common.network.IPPacketHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.client.CSteerBoatPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

// FIXME Invalid move vehicle packet received. SOMEWHERE!
public class SpeedboatEntity extends BoatEntity{
	
	public static final EntityType<SpeedboatEntity> TYPE = EntityType.Builder.<SpeedboatEntity>create(SpeedboatEntity::new, EntityClassification.MISC)
				.size(1.375F, 0.5625F)
				.build(ImmersivePetroleum.MODID+":speedboat");
	static{
		TYPE.setRegistryName(ImmersivePetroleum.MODID, "speedboat");
	}
	
	/** Storage for {@link ResourceLocation} using {@link ResourceLocation#toString()} */
	static final DataParameter<String> TANK_FLUID = EntityDataManager.createKey(SpeedboatEntity.class, DataSerializers.STRING);
	static final DataParameter<Integer> TANK_AMOUNT = EntityDataManager.createKey(SpeedboatEntity.class, DataSerializers.VARINT);
	
	static final DataParameter<ItemStack> UPGRADE_0 = EntityDataManager.createKey(SpeedboatEntity.class, DataSerializers.ITEMSTACK);
	static final DataParameter<ItemStack> UPGRADE_1 = EntityDataManager.createKey(SpeedboatEntity.class, DataSerializers.ITEMSTACK);
	static final DataParameter<ItemStack> UPGRADE_2 = EntityDataManager.createKey(SpeedboatEntity.class, DataSerializers.ITEMSTACK);
	static final DataParameter<ItemStack> UPGRADE_3 = EntityDataManager.createKey(SpeedboatEntity.class, DataSerializers.ITEMSTACK);
	
	
	public boolean isFireproof = false;
	public boolean hasIcebreaker = false;
	public boolean hasTank = false;
	public boolean hasRudders = false;
	public boolean hasPaddles = false;
	public boolean isBoosting = false;
	public float lastMoving;
	public float propellerRotation = 0F;
	
	public SpeedboatEntity(World world, double x, double y, double z){
		this(TYPE, world);
		setLocationAndAngles(x, y, z, 0.0F, 0.0F);
	}
	
	public SpeedboatEntity(World world){
		this(TYPE, world);
	}
	
	@Override
	public EntityType<?> getType(){
		return TYPE;
	}
	
	public SpeedboatEntity(EntityType<SpeedboatEntity> type, World world){
		super(type, world);
		this.preventEntitySpawning = true;
	}
	
	@Override
	protected void registerData(){
		super.registerData();
		this.dataManager.register(TANK_FLUID, "");
		this.dataManager.register(TANK_AMOUNT, Integer.valueOf(0));
		this.dataManager.register(UPGRADE_0, ItemStack.EMPTY);
		this.dataManager.register(UPGRADE_1, ItemStack.EMPTY);
		this.dataManager.register(UPGRADE_2, ItemStack.EMPTY);
		this.dataManager.register(UPGRADE_3, ItemStack.EMPTY);
	}
	
	public void setUpgrades(NonNullList<ItemStack> stacks){
		if(stacks != null && stacks.size() > 0){
			ItemStack o0 = stacks.get(0) == null ? ItemStack.EMPTY : stacks.get(0);
			ItemStack o1 = stacks.get(1) == null ? ItemStack.EMPTY : stacks.get(1);
			ItemStack o2 = stacks.get(2) == null ? ItemStack.EMPTY : stacks.get(2);
			ItemStack o3 = stacks.get(3) == null ? ItemStack.EMPTY : stacks.get(3);
			this.dataManager.set(UPGRADE_0, o0);
			this.dataManager.set(UPGRADE_1, o1);
			this.dataManager.set(UPGRADE_2, o2);
			this.dataManager.set(UPGRADE_3, o3);
		}
	}
	
	@Override
	public void notifyDataManagerChange(DataParameter<?> key){
		super.notifyDataManagerChange(key);
		if(key == UPGRADE_0 || key == UPGRADE_1 || key == UPGRADE_2 || key == UPGRADE_3){
			NonNullList<ItemStack> upgrades = getUpgrades();
			for(ItemStack upgrade:upgrades){
				if(upgrade != null && upgrade!=ItemStack.EMPTY){
					if(upgrade.getItem() == BoatUpgrades.itemUpgradeHull){
						this.isFireproof = true;
					}else if(upgrade.getItem() == BoatUpgrades.itemUpgradeBreaker){
						this.hasIcebreaker = true;
					}else if(upgrade.getItem() == BoatUpgrades.itemUpgradeTank){
						this.hasTank = true;
					}else if(upgrade.getItem() == BoatUpgrades.itemUpgradeRudders){
						this.hasRudders = true;
					}else if(upgrade.getItem() == BoatUpgrades.itemUpgradePaddles){
						this.hasPaddles = true;
					}
				}
			}
		}
	}
	
	public void setContainedFluid(FluidStack stack){
		if(stack == null){
			this.dataManager.set(TANK_FLUID, "");
			this.dataManager.set(TANK_AMOUNT, 0);
		}else{
			this.dataManager.set(TANK_FLUID, stack.getFluid() == null ? "" : stack.getFluid().getRegistryName().toString());
			this.dataManager.set(TANK_AMOUNT, stack.getAmount());
		}
	}
	
	public FluidStack getContainedFluid(){
		String fluidName = this.dataManager.get(TANK_FLUID);
		int amount = this.dataManager.get(TANK_AMOUNT).intValue();
		
		if(fluidName==null || fluidName.isEmpty())
			return FluidStack.EMPTY;
		
		Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName));
		if(fluid == null || amount == 0)
			return FluidStack.EMPTY;
		
		return new FluidStack(fluid, amount);
	}
	
	@Override
	public double getMountedYOffset(){
		return isInLava() ? -0.1D + 3.9F / 16F : -0.1D;
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount){
		if(isInvulnerableTo(source)){
			return false;
		}else if(!this.world.isRemote && isAlive()){
			if(source instanceof IndirectEntityDamageSource && source.getTrueSource() != null && isPassenger(source.getTrueSource())){
				return false;
			}else{
				setForwardDirection(-getForwardDirection());
				setTimeSinceHit(10);
				setDamageTaken(getDamageTaken() + amount * 10.0F);
				markVelocityChanged();
				boolean flag0 = source.getTrueSource() instanceof PlayerEntity;
				boolean flag1 = flag0 && ((PlayerEntity) source.getTrueSource()).abilities.isCreativeMode;
				if(flag1 || (getDamageTaken() > 40.0F && (!this.isFireproof || flag0)) || (getDamageTaken() > 240.0F)){
					if(!flag1 && this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)){
						ItemSpeedboat item = (ItemSpeedboat) getItemBoat();
						ItemStack stack = new ItemStack(item, 1);
						
						CompoundNBT data = stack.getOrCreateChildTag("data");
						{
							ListNBT upgradeList = new ListNBT();
							NonNullList<ItemStack> upgrades = getUpgrades();
							for(ItemStack upgrade:upgrades){
								CompoundNBT upgradeNbt = new CompoundNBT();
								upgrade.write(upgradeNbt);
								upgradeList.add(upgradeNbt);
							}
							data.put("upgrades", upgradeList);
							writeTank(data, true);
						}
						
						if(source.getImmediateSource() instanceof PlayerEntity){
							PlayerEntity player = (PlayerEntity) source.getImmediateSource();
							if(!player.addItemStackToInventory(stack)){
								ItemEntity itemEntity = new ItemEntity(this.world, player.posX, player.posY, player.posZ, stack);
								itemEntity.setNoPickupDelay();
								this.world.addEntity(itemEntity);
							}
						}else{
							entityDropItem(stack);
						}
					}
					
					remove();
				}
			}
		}else{
			return true;
		}
		return super.attackEntityFrom(source, amount);
	}
	
	public void readTank(CompoundNBT nbt){
		FluidTank tank = new FluidTank(getMaxFuel());
		if(nbt != null)
			tank.readFromNBT(nbt.getCompound("tank"));
		
		setContainedFluid(tank.getFluid());
	}
	
	public void writeTank(CompoundNBT nbt, boolean toItem){
		FluidTank tank = new FluidTank(getMaxFuel());
		tank.setFluid(getContainedFluid());
		
		boolean write = tank.getFluidAmount() > 0;
		if(!toItem || write)
			nbt.put("tank", tank.writeToNBT(new CompoundNBT()));
	}
	
	@Override
	public boolean processInitialInteract(PlayerEntity player, Hand hand){
		ItemStack stack = player.getHeldItem(hand);
		if(Utils.isFluidRelatedItemStack(stack)){
			FluidTank tank = new FluidTank(getMaxFuel());
			
			FluidStack fs = getContainedFluid();
			tank.setFluid(fs);
			
			FluidUtil.interactWithFluidHandler(player, hand, tank);
			
			setContainedFluid(tank.getFluid());
			return true;
		}
		
		if(!this.world.isRemote && !player.isSneaking() && this.outOfControlTicks < 60.0F && !player.isRidingOrBeingRiddenBy(this)){
			player.startRiding(this);
			return true;
		}
		
		return false;
	}
	
	@Override
	public void updateInputs(boolean p_184442_1_, boolean p_184442_2_, boolean p_184442_3_, boolean p_184442_4_){
		super.updateInputs(p_184442_1_, p_184442_2_, p_184442_3_, p_184442_4_);
		this.isBoosting = isEmergency() ? false : (forwardInputDown && Minecraft.getInstance().gameSettings.keyBindJump.isKeyDown());
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void tick(){
		this.previousStatus = this.status;
		this.status = this.getBoatStatus();
		if(this.status != BoatEntity.Status.UNDER_WATER && this.status != BoatEntity.Status.UNDER_FLOWING_WATER){
			this.outOfControlTicks = 0.0F;
		}else{
			++this.outOfControlTicks;
		}
		
		if(!this.world.isRemote && this.outOfControlTicks >= 60.0F){
			this.removePassengers();
		}
		
		if(this.getTimeSinceHit() > 0){
			this.setTimeSinceHit(this.getTimeSinceHit() - 1);
		}
		
		if(this.getDamageTaken() > 0.0F){
			this.setDamageTaken(this.getDamageTaken() - 1.0F);
		}
		
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		
		{ // From Entity.tick()
			if(!this.world.isRemote){
				this.setFlag(6, this.isGlowing());
			}
			this.baseTick();
		}
		this.tickLerp();
		
		if(this.canPassengerSteer()){
			if(this.getPassengers().isEmpty() || !(this.getPassengers().get(0) instanceof PlayerEntity)){
				this.setPaddleState(false, false);
			}
			
			this.updateMotion();
			if(this.world.isRemote){
				this.controlBoat();
				this.world.sendPacketToServer(new CSteerBoatPacket(this.getPaddleState(0), this.getPaddleState(1)));
			}
			
			this.move(MoverType.SELF, this.getMotion());
		}else{
			this.setMotion(Vec3d.ZERO);
		}
		
		this.updateRocking();
		
		if(this.world.isRemote){
			if(!isEmergency()){
				float moving = (this.forwardInputDown || this.backInputDown) ? (isBoosting ? .9F : .7F) : 0.5F;
				if(lastMoving != moving){
					ImmersivePetroleum.proxy.handleEntitySound(IESounds.dieselGenerator, this, false, .5f, 0.5F);
				}
				ImmersivePetroleum.proxy.handleEntitySound(IESounds.dieselGenerator, this, this.isBeingRidden() && this.getContainedFluid() != FluidStack.EMPTY && this.getContainedFluid().getAmount() > 0, this.forwardInputDown || this.backInputDown ? .5f : .3f, moving);
				lastMoving = moving;
				
				if(this.forwardInputDown && this.world.rand.nextInt(2) == 0){
					if(inLava){
						if(this.world.rand.nextInt(3) == 0){
							float xO = (float) (MathHelper.sin(-this.rotationYaw * 0.017453292F)) + (world.rand.nextFloat() - .5F) * .3F;
							float zO = (float) (MathHelper.cos(this.rotationYaw * 0.017453292F)) + (world.rand.nextFloat() - .5F) * .3F;
							float yO = .4F + (world.rand.nextFloat() - .5F) * .3F;
							Vec3d motion=getMotion();
							world.addParticle(ParticleTypes.LAVA, posX - xO * 1.5F, posY + yO, posZ - zO * 1.5F, -2 * motion.getX(), 0, -2 * motion.getZ());
						}
					}else{
						float xO = (float) (MathHelper.sin(-this.rotationYaw * 0.017453292F)) + (world.rand.nextFloat() - .5F) * .3F;
						float zO = (float) (MathHelper.cos(this.rotationYaw * 0.017453292F)) + (world.rand.nextFloat() - .5F) * .3F;
						float yO = .1F + (world.rand.nextFloat() - .5F) * .3F;
						world.addParticle(ParticleTypes.BUBBLE, posX - xO * 1.5F, posY + yO, posZ - zO * 1.5F, 0, 0, 0);
					}
				}
				if(isBoosting && this.world.rand.nextInt(2) == 0){
					float xO = (float) (MathHelper.sin(-this.rotationYaw * 0.017453292F)) + (world.rand.nextFloat() - .5F) * .3F;
					float zO = (float) (MathHelper.cos(this.rotationYaw * 0.017453292F)) + (world.rand.nextFloat() - .5F) * .3F;
					float yO = .8F + (world.rand.nextFloat() - .5F) * .3F;
					world.addParticle(ParticleTypes.SMOKE, posX - xO * 1.3F, posY + yO, posZ - zO * 1.3F, 0, 0, 0);
				}
			}else{
				ImmersivePetroleum.proxy.handleEntitySound(IESounds.dieselGenerator, this, false, .5f, 0.5F);
			}
		}
		
		if(!this.isEmergency()){
			if(this.getPaddleState(0)){
				this.paddlePositions[0] = (float) ((double) this.paddlePositions[0] + (isBoosting ? 0.02D : 0.01D));
			}else if(this.getPaddleState(1)){
				this.paddlePositions[0] = (float) ((double) this.paddlePositions[0] - 0.01D);
			}
		}else{
			for(int i = 0;i <= 1;++i){
				if(this.getPaddleState(i)){
					this.paddlePositions[i] = (float)((double)this.paddlePositions[i] + (double)((float)Math.PI / 4F));
				}else{
					this.paddlePositions[i] = 0.0F;
				}
			}
		}
		
		float xO = (float) (MathHelper.sin(-this.rotationYaw * 0.017453292F));
		float zO = (float) (MathHelper.cos(this.rotationYaw * 0.017453292F));
		Vector2f vec = new Vector2f(xO, zO);
		vec.normalize();
		
		if(this.hasIcebreaker && !isEmergency()){
			AxisAlignedBB bb=getBoundingBox().grow(0.1);
			BlockPos.PooledMutableBlockPos mutableBlockPos0 = BlockPos.PooledMutableBlockPos.retain(bb.minX + 0.001D, bb.minY + 0.001D, bb.minZ + 0.001D);
			BlockPos.PooledMutableBlockPos mutableBlockPos1 = BlockPos.PooledMutableBlockPos.retain(bb.maxX - 0.001D, bb.maxY - 0.001D, bb.maxZ - 0.001D);
			BlockPos.PooledMutableBlockPos mutableBlockPos2 = BlockPos.PooledMutableBlockPos.retain();
			
			if(this.world.isAreaLoaded(mutableBlockPos0, mutableBlockPos1)){
				for(int i = mutableBlockPos0.getX();i <= mutableBlockPos1.getX();++i){
					for(int j = mutableBlockPos0.getY();j <= mutableBlockPos1.getY();++j){
						for(int k = mutableBlockPos0.getZ();k <= mutableBlockPos1.getZ();++k){
							mutableBlockPos2.setPos(i, j, k);
							BlockState BlockState = this.world.getBlockState(mutableBlockPos2);
							
							Vector2f vec2 = new Vector2f((float) (i + 0.5f - posX), (float) (k + 0.5f - posZ));
							vec2.normalize();
							
							float sim = vec2.dot(vec);
							
							if(BlockState.getBlock() == Blocks.ICE && sim > .3f){
								this.world.destroyBlock(mutableBlockPos2, false);
								this.world.setBlockState(mutableBlockPos2, Blocks.WATER.getDefaultState());
							}
						}
					}
				}
			}
		}
		
		this.doBlockCollisions();
		List<Entity> list = this.world.getEntitiesInAABBexcluding(this, this.getBoundingBox().grow((double) 0.2F, (double) -0.01F, (double) 0.2F), EntityPredicates.pushableBy(this));
		if(!list.isEmpty()){
			boolean flag = !this.world.isRemote && !(this.getControllingPassenger() instanceof PlayerEntity);
			
			for(int j = 0;j < list.size();++j){
				Entity entity = list.get(j);
				
				if(!entity.isPassenger(this)){
					if(flag && this.getPassengers().size() < 2 && !entity.isPassenger() && entity.getWidth() < this.getWidth() && entity instanceof LivingEntity && !(entity instanceof WaterMobEntity) && !(entity instanceof PlayerEntity)){
						entity.startRiding(this);
					}else{
						this.applyEntityCollision(entity);
						
						if(this.hasIcebreaker){
							if(entity instanceof LivingEntity && !(entity instanceof PlayerEntity) && this.getControllingPassenger() instanceof PlayerEntity){
								Vector2f vec2 = new Vector2f((float) (entity.posX - posX), (float) (entity.posZ - posZ));
								vec2.normalize();
								
								float sim = vec2.dot(vec);
								
								if(sim > .5f){
									Vec3d motion=entity.getMotion();
									entity.attackEntityFrom(DamageSource.causePlayerDamage((PlayerEntity) this.getControllingPassenger()), 4);
									entity.setMotion(new Vec3d(motion.x+(vec2.x * .75F), motion.y, motion.z+(vec2.y * .75F)));
								}
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public void controlBoat(){
		if(isBeingRidden()){
			float f = 0.0F;
			
			if(isEmergency()){
				if(this.leftInputDown){
					--this.deltaRotation;
				}
				
				if(this.rightInputDown){
					++this.deltaRotation;
				}
				
				if(this.rightInputDown != this.leftInputDown && !this.forwardInputDown && !this.backInputDown){
					f += 0.005F;
				}
				
				this.rotationYaw += this.deltaRotation;
				if(this.forwardInputDown){
					f += 0.04F;
				}
				
				if(this.backInputDown){
					f -= 0.005F;
				}
				
				this.setMotion(this.getMotion().add((double) (MathHelper.sin(-this.rotationYaw * ((float) Math.PI / 180F)) * f), 0.0D, (double) (MathHelper.cos(this.rotationYaw * ((float) Math.PI / 180F)) * f)));
				this.setPaddleState(this.rightInputDown && !this.leftInputDown || this.forwardInputDown, this.leftInputDown && !this.rightInputDown || this.forwardInputDown);
			}else{
				FluidStack fluid = getContainedFluid();
				int consumeAmount = 0;
				if(fluid != FluidStack.EMPTY){
					consumeAmount = FuelHandler.getBoatFuelUsedPerTick(fluid.getFluid());
				}
				
				if(fluid != FluidStack.EMPTY && fluid.getAmount() >= consumeAmount && (this.forwardInputDown || this.backInputDown)){
					int toConsume = consumeAmount;
					if(this.forwardInputDown){
						f += 0.05F;
						if(this.isBoosting && fluid.getAmount() >= 3 * consumeAmount){
							f *= 1.6;
							toConsume *= 3;
						}
					}
					
					if(this.backInputDown){
						f -= 0.01F;
					}
					
					fluid.setAmount(Math.max(0, fluid.getAmount() - toConsume));
					setContainedFluid(fluid);
					
					if(this.world.isRemote)
						IPPacketHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new ConsumeBoatFuelPacket(toConsume));
					
					setPaddleState(this.forwardInputDown, this.backInputDown);
				}else{
					setPaddleState(false, false);
				}
				
				this.setMotion(this.getMotion().add((double) (MathHelper.sin(-this.rotationYaw * ((float) Math.PI / 180F)) * f), 0.0D, (double) (MathHelper.cos(this.rotationYaw * ((float) Math.PI / 180F)) * f)));
				this.setPaddleState(this.rightInputDown && !this.leftInputDown || this.forwardInputDown, this.leftInputDown && !this.rightInputDown || this.forwardInputDown);
				
				Vec3d motion = getMotion();
				float speed = (float) Math.sqrt((motion.x * motion.x) + (motion.z + motion.z));
				
				if(this.leftInputDown){
					this.deltaRotation += -1.1F * speed * (this.hasRudders ? 1.5F : 1F) * (this.isBoosting ? 0.5F : 1) * (this.backInputDown && !this.forwardInputDown ? 2F : 1F);
					if(this.propellerRotation > -1F){
						this.propellerRotation -= 0.2F;
					}
				}
				
				if(this.rightInputDown){
					this.deltaRotation += 1.1F * speed * (this.hasRudders ? 1.5F : 1F) * (this.isBoosting ? 0.5F : 1) * (this.backInputDown && !this.forwardInputDown ? 2F : 1F);
					if(this.propellerRotation < 1F){
						this.propellerRotation += 0.2F;
					}
				}
				
				this.rotationYaw += this.deltaRotation;
				
				if(!this.rightInputDown && !this.leftInputDown){
					this.propellerRotation *= 0.7F;
				}
			}
		}
	}
	
	public int getMaxFuel(){
		return this.hasTank ? 16000 : 8000;
	}
	
	@Override
	public Item getItemBoat(){
		return Items.itemSpeedboat;
	}
	
	public static DataParameter<Byte> getFlags(){
		return FLAGS;
	}
	
	public boolean isEmergency(){
		FluidStack fluid=getContainedFluid();
		if(fluid!=FluidStack.EMPTY){
			int consumeAmount = FuelHandler.getBoatFuelUsedPerTick(fluid.getFluid());
			return fluid.getAmount() <= consumeAmount && this.hasPaddles;
		}
		
		return this.hasPaddles;
	}
	
	public NonNullList<ItemStack> getUpgrades(){
		NonNullList<ItemStack> stackList = NonNullList.withSize(4, ItemStack.EMPTY);
		stackList.set(0, this.dataManager.get(UPGRADE_0));
		stackList.set(1, this.dataManager.get(UPGRADE_1));
		stackList.set(2, this.dataManager.get(UPGRADE_2));
		stackList.set(3, this.dataManager.get(UPGRADE_3));
		return stackList;
	}
	
	public String[] getOverlayText(PlayerEntity player, RayTraceResult mop){
		if(Utils.isFluidRelatedItemStack(player.getHeldItem(Hand.MAIN_HAND))){
			String s = null;
			FluidStack stack = getContainedFluid();
			if(stack != FluidStack.EMPTY){
				s = stack.getDisplayName().getFormattedText() + ": " + stack.getAmount() + "mB";
			}else{
				s = I18n.format(Lib.GUI + "empty");
			}
			return new String[]{s};
			
		}
		return null;
	}
	
	@Override
	public IPacket<?> createSpawnPacket(){
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
