package flaxbeard.immersivepetroleum.common.entity;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.items.ItemSpeedboat;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;

public class SpeedboatEntity extends BoatEntity{
	
	public static final EntityType<SpeedboatEntity> TYPE;
	static{
		TYPE = Builder.<SpeedboatEntity> create(SpeedboatEntity::new, EntityClassification.MISC).size(1.375F, 0.5625F).build(ImmersivePetroleum.MODID + ":speedboat");
	}
	
	@SuppressWarnings("unchecked")
	static final DataParameter<Boolean>[] DATA_ID_PADDLE = new DataParameter[]{EntityDataManager.createKey(BoatEntity.class, DataSerializers.BOOLEAN), EntityDataManager.createKey(BoatEntity.class, DataSerializers.BOOLEAN),};
	/**
	 * Storage for {@link ResourceLocation} using
	 * {@link ResourceLocation#toString()}
	 */
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
	public float propellerRotation = 0F;
	
	public SpeedboatEntity(World world, double x, double y, double z){
		this(world);
		setLocationAndAngles(x, y, z, 0.0F, 0.0F);
	}
	
	public SpeedboatEntity(World world){
		this(TYPE, world);
	}
	
	public SpeedboatEntity(EntityType<SpeedboatEntity> type, World world){
		super(type, world);
		this.preventEntitySpawning = true;
	}
	
	@Override
	protected void registerData(){
		for(DataParameter<Boolean> dataparameter:DATA_ID_PADDLE){
			this.dataManager.register(dataparameter, Boolean.valueOf(false));
		}
		this.dataManager.register(TANK_FLUID, "");
		this.dataManager.register(TANK_AMOUNT, Integer.valueOf(0));
		this.dataManager.register(UPGRADE_0, ItemStack.EMPTY);
		this.dataManager.register(UPGRADE_1, ItemStack.EMPTY);
		this.dataManager.register(UPGRADE_2, ItemStack.EMPTY);
		this.dataManager.register(UPGRADE_3, ItemStack.EMPTY);
		
		super.registerData();
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
	
	public void setFluid(FluidStack stack){
		if(stack == null){
			this.dataManager.set(TANK_FLUID, "");
			this.dataManager.set(TANK_AMOUNT, 0);
		}else{
			this.dataManager.set(TANK_FLUID, stack.getFluid() == null ? "" : stack.getFluid().getRegistryName().toString());
			this.dataManager.set(TANK_AMOUNT, stack.getAmount());
		}
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
	
	@Override
	public void notifyDataManagerChange(DataParameter<?> key){
		if(key == UPGRADE_0 || key == UPGRADE_1 || key == UPGRADE_2 || key == UPGRADE_3){
			NonNullList<ItemStack> upgrades = getUpgrades();
			this.isFireproof = false;
			this.hasIcebreaker = false;
			for(ItemStack upgrade:upgrades){
				if(upgrade != null){
					if(upgrade.getItem() == IPContent.itemUpgradeHull){
						isFireproof = true;
					}else if(upgrade.getItem() == IPContent.itemUpgradeBreaker){
						hasIcebreaker = true;
					}else if(upgrade.getItem() == IPContent.itemUpgradeTank){
						hasTank = true;
					}else if(upgrade.getItem() == IPContent.itemUpgradeRudders){
						hasRudders = true;
					}else if(upgrade.getItem() == IPContent.itemUpgradePaddles){
						hasPaddles = true;
					}
				}
			}
		}
		super.notifyDataManagerChange(key);
	}
	
	public void readTank(CompoundNBT nbt){
		FluidTank tank = new FluidTank(getMaxFuel());
		if(nbt != null) tank.readFromNBT(nbt.getCompound("tank"));
		
		setFluid(tank.getFluid());
	}
	
	public void writeTank(CompoundNBT nbt, boolean toItem){
		FluidTank tank = new FluidTank(getMaxFuel());
		tank.setFluid(getContainedFluid());
		
		boolean write = tank.getFluidAmount() > 0;
		CompoundNBT tankTag = tank.writeToNBT(new CompoundNBT());
		if(!toItem || write) nbt.put("tank", tankTag);
	}
	
	@Override
	public void tick(){
		super.tick();
	}
	
	public int getMaxFuel(){
		return this.hasTank ? 16000 : 8000;
	}
	
	@Override
	public Item getItemBoat(){
		return IPContent.itemSpeedboat;
	}
	
	public static DataParameter<Byte> getFlags(){
		return FLAGS;
	}
	
	public NonNullList<ItemStack> getUpgrades(){
		NonNullList<ItemStack> stackList = NonNullList.withSize(4, ItemStack.EMPTY);
		stackList.set(0, this.dataManager.get(UPGRADE_0));
		stackList.set(1, this.dataManager.get(UPGRADE_1));
		stackList.set(2, this.dataManager.get(UPGRADE_2));
		stackList.set(3, this.dataManager.get(UPGRADE_3));
		return stackList;
	}
	
	public FluidStack getContainedFluid(){
		String fluidName = this.dataManager.get(TANK_FLUID);
		int amount = this.dataManager.get(TANK_AMOUNT).intValue();
		
		Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName));
		if(fluid == null || amount == 0) return null;
		
		return new FluidStack(fluid, amount);
	}
	
	public String[] getOverlayText(PlayerEntity player, RayTraceResult mop){
		if(Utils.isFluidRelatedItemStack(player.getHeldItem(Hand.MAIN_HAND))){
			String s = null;
			FluidStack stack = getContainedFluid();
			if(stack != null && stack.getFluid() != null){
				s = stack.getDisplayName() + ": " + stack.getAmount() + "mB";
			}else{
				s = I18n.format(Lib.GUI + "empty");
			}
			return new String[]{s};
			
		}
		return null;
	}
}
