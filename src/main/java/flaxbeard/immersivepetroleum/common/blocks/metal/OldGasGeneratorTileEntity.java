package flaxbeard.immersivepetroleum.common.blocks.metal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.ImmersiveConnectableTileEntity;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxConnector;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.api.energy.FuelHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class OldGasGeneratorTileEntity extends ImmersiveConnectableTileEntity implements IDirectionalTile, ITickable, IPlayerInteraction, IBlockOverlayText, IIEInternalFluxConnector, ITileDrop, IIEInternalFluxHandler, IEBlockInterfaces.ISoundTile, EnergyConnector{
	public static TileEntityType<OldGasGeneratorTileEntity> TYPE;
	
	public boolean active;
	int lastTank = 0;
	public int currentTickAccepted = 0;
	boolean firstTick = true;
	public Direction facing = Direction.NORTH;
	private FluxStorage energyStorage = new FluxStorage(getMaxStorage(), getMaxInput(), 0);
	public FluidTank tank = new FluidTank(8000){
		@Override
		public boolean isFluidValid(FluidStack fluid){
			return fluid != null && FuelHandler.isValidFuel(fluid.getFluid());
		}
	};
	
	public OldGasGeneratorTileEntity(){
		super(TYPE);
	}
	
	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket){
		facing = Direction.byIndex(nbt.getInt("facing"));
		active = nbt.getBoolean("active");
		tank.readFromNBT(nbt.getCompound("tank"));
		energyStorage.readFromNBT(nbt);
		
		if(descPacket) this.markContainingBlockForUpdate(null);
	}
	
	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket){
		nbt.putInt("facing", facing.ordinal());
		nbt.putBoolean("active", active);
		
		CompoundNBT tankTag = tank.writeToNBT(new CompoundNBT());
		nbt.put("tank", tankTag);
		energyStorage.writeToNBT(nbt);
		
	}
	
	@Override
	public Direction getFacing(){
		return facing;
	}
	
	@Override
	public void setFacing(Direction facing){
		this.facing = facing;
	}
	
	@Override
	public PlacementLimitation getFacingLimitation(){
		return PlacementLimitation.VERTICAL;
	}
	
	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer){
		return false;
	}
	
	@Override
	public boolean canHammerRotate(Direction side, Vec3d hit, LivingEntity entity){
		return true;
	}
	
	@Override
	public boolean canRotate(Direction axis){
		return true;
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap){
		return super.getCapability(cap);
	}
	
	/*
	@Override
	public <T> T getCapability(Capability<T> capability, Direction facing)
	{
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing == Direction.UP)
		{
			return (T) tank;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, Direction facing)
	{
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
		{
			return facing == Direction.UP;
		}
		return super.hasCapability(capability, facing);
	}
	*/

	@Override
	public void tick(){
		if(!world.isRemote && lastTank != this.tank.getFluidAmount()){
			markContainingBlockForUpdate(null);
			lastTank = this.tank.getFluidAmount();
		}
		
		active = false;
		
		if(!world.isBlockPowered(pos) && this.tank.getFluid() != null){
			Fluid fuel = this.tank.getFluid().getFluid();
			if(FuelHandler.isValidFuel(fuel)){
				int amount = FuelHandler.getFuelUsedPerTick(fuel);
				if(this.tank.getFluidAmount() >= amount){
					if(!this.world.isRemote){
						if(this.energyStorage.receiveEnergy(FuelHandler.getFluxGeneratedPerTick(fuel), false) > 0){
							this.tank.drain(new FluidStack(fuel, amount), FluidAction.EXECUTE);
						}
					}else{
						if(this.energyStorage.receiveEnergy(FuelHandler.getFluxGeneratedPerTick(fuel), true) > 0){
							active = true;
						}
					}
				}
			}
		}

		if(!world.isRemote){
			if(energyStorage.getEnergyStored() > 0){
				int temp = this.transferEnergy(energyStorage.getEnergyStored(), true, 0);
				if(temp > 0){
					energyStorage.modifyEnergyStored(-this.transferEnergy(temp, false, 0));
					markDirty();
				}
			}
			currentTickAccepted = 0;
		}else if(firstTick){
			Collection<Connection> conns = GlobalWireNetwork.getNetwork(world).getLocalNet(pos).getConnections(pos);// ImmersiveNetHandler.INSTANCE.getConnections(world, pos);
			if(conns != null)
				for(Connection conn:conns){ // TODO Effeciency is not *yet* the point, just want to get it working, no matter the cost.
					if(pos.compareTo(conn.getEndFor(pos).getPosition()) < 0 && world.isAreaLoaded(pos, 1))
						this.markContainingBlockForUpdate(null);
//					if(pos.compareTo(conn.end) < 0 && world.isBlockLoaded(conn.end))
//						this.markContainingBlockForUpdate(null);
			}
			firstTick = false;
		}
		
		if(this.world.isRemote){
			ImmersiveEngineering.proxy.handleTileSound(IESounds.dieselGenerator, this, active, .3f, .5f);
			if(active && world.getGameTime() % 4 == 0){
				BlockPos exhaust = pos;
				Direction fl = facing;
				Direction fw = facing.rotateY();
				world.addParticle(world.rand.nextInt(10) == 0 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SMOKE,
						exhaust.getX() + .5 + (fl.getXOffset() * 2 / 16F) + (-fw.getXOffset() * .6125f),
						exhaust.getY() + .4,
						exhaust.getZ() + .5 + (fl.getZOffset() * 2 / 16F) + (-fw.getZOffset() * .6125f),
						0, 0, 0);
			}
		}
		
	}

	@Override
	public boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ){
		// FluidStack f = FluidUtil.getFluidContained(heldItem).orElse(null);
		if(FluidUtil.interactWithFluidHandler(player, hand, tank)){
			markContainingBlockForUpdate(null);
			return true;
		}else if(player.isSneaking()){
			boolean added = false;
			if(player.inventory.getCurrentItem().isEmpty()){
				added = true;
				player.inventory.setInventorySlotContents(player.inventory.currentItem, getTileDrops(null).get(0));
			}else{
				added = player.inventory.addItemStackToInventory(getTileDrops(null).get(0));
			}
			
			if(added){
				world.setBlockState(pos, Blocks.AIR.getDefaultState());
			}
		}
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public double getMaxRenderDistanceSquared(){
		return super.getMaxRenderDistanceSquared() * IEConfig.GENERAL.increasedTileRenderdistance.get();
	}


	@Override
	public String[] getOverlayText(PlayerEntity player, RayTraceResult mop, boolean hammer){
		if(Utils.isFluidRelatedItemStack(player.getHeldItem(Hand.MAIN_HAND))){
			String s = null;
			if(tank.getFluid() != null)
				s = tank.getFluid().getDisplayName() + ": " + tank.getFluidAmount() + "mB";
			else
				s = I18n.format(Lib.GUI + "empty");
			return new String[]{s};
		}
		return null;
	}

	@Override
	public boolean useNixieFont(PlayerEntity player, RayTraceResult mop){
		return false;
	}
	
	public void readTank(CompoundNBT nbt){
		tank.readFromNBT(nbt.getCompound("tank"));
	}
	
	public void writeTank(CompoundNBT nbt, boolean toItem){
		boolean write = tank.getFluidAmount() > 0;
		CompoundNBT tankTag = tank.writeToNBT(new CompoundNBT());
		if(!toItem || write)
			nbt.put("tank", tankTag);
	}
	
	@Override
	public void readOnPlacement(LivingEntity placer, ItemStack stack){
		if(stack.hasTag()){
			readTank(stack.getTag());
			if(ItemNBTHelper.hasKey(stack, "energyStorage"))
				energyStorage.setEnergy(ItemNBTHelper.getInt(stack, "energyStorage"));
		}
	}

	@Override
	public List<ItemStack> getTileDrops(LootContext context){
		BlockState state = this.world.getBlockState(pos);
		
		ItemStack stack = new ItemStack(state.getBlock(), 1);
		CompoundNBT tag = new CompoundNBT();
		writeTank(tag, true);
		
		if(!tag.isEmpty())
			stack.setTag(tag);
		
		if(energyStorage.getEnergyStored() > 0)
			ItemNBTHelper.putInt(stack, "energyStorage", energyStorage.getEnergyStored());
		
		return Collections.singletonList(stack);
	}

	public Vec3d getRaytraceOffset(IImmersiveConnectable link){
		float xo = facing.getDirectionVec().getX() * .6f + .5f;
		float zo = facing.getDirectionVec().getZ() * .6f + .5f;
		return new Vec3d(xo, .5f, zo);
	}
	
	@Override
	public Vec3d getConnectionOffset(Connection con, ConnectionPoint here){
		float xo = facing.getDirectionVec().getX() * .5f + .5f;
		float zo = facing.getDirectionVec().getZ() * .5f + .5f;
		return new Vec3d(xo, .5f, zo);
	}

	IEForgeEnergyWrapper energyWrapper;

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing){
		if(facing != this.facing) return null;
		
		if(energyWrapper == null || energyWrapper.side != this.facing)
			energyWrapper = new IEForgeEnergyWrapper(this, this.facing);
		
		return energyWrapper;
	}
	
	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset){
		return WireType.LV_CATEGORY.equals(cableType.getCategory());
	}
	
	@Override
	public boolean isSource(ConnectionPoint cp){
		return true;
	}
	
	@Override
	public boolean isSink(ConnectionPoint cp){
		return false;
	}
	
	@Override
	public void extractEnergy(int amount){
		EnergyConnector.super.extractEnergy(amount);
	}
	
	public int getMaxInput(){
		return IEConfig.MACHINES.wireConnectorInput.get().get(0); // LV
	}
	
	public int getMaxOutput(){
		return IEConfig.MACHINES.wireConnectorInput.get().get(0); // LV
	}

	@Override
	public FluxStorage getFluxStorage(){
		return energyStorage;
	}
	
	@Override
	public int getEnergyStored(Direction from){
		return energyStorage.getEnergyStored();
	}
	
	@Override
	public IOSideConfig getEnergySideConfig(Direction facing){
		return IOSideConfig.OUTPUT;
	}
	
	@Override
	public boolean canConnectEnergy(Direction from){
		return false;
	}
	
	private int getMaxStorage(){
		return IEConfig.MACHINES.capacitorLvStorage.get();
	}
	
	@Override
	public int getMaxEnergyStored(Direction from){
		return energyStorage.getMaxEnergyStored();
	}
	
	@Override
	public int extractEnergy(Direction from, int energy, boolean simulate){
		return 0;
	}

	// Is this complexity realy needed..
	public int transferEnergy(int energy, boolean simulate, final int energyType)
	{
		int received = 0;
		if (!world.isRemote)
		{
			EnergyTransferHandler handler=globalNet.getLocalNet(pos).getHandler(EnergyTransferHandler.ID, EnergyTransferHandler.class);
			if(handler!=null){
				
			}
			
			Set<AbstractConnection> outputs = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(Utils.toCC(this), world);
			int powerLeft = Math.min(Math.min(getMaxOutput(), getMaxInput()), energy);
			final int powerForSort = powerLeft;

			if (outputs.size() < 1)
				return 0;

			int sum = 0;
			HashMap<AbstractConnection, Integer> powerSorting = new HashMap<AbstractConnection, Integer>();
			for (AbstractConnection con : outputs)
			{
				IImmersiveConnectable end = ApiUtils.toIIC(con.end, world);
				if (con.cableType != null && end != null)
				{
					int atmOut = Math.min(powerForSort, con.cableType.getTransferRate());
					int tempR = end.outputEnergy(atmOut, true, energyType);
					if (tempR > 0)
					{
						powerSorting.put(con, tempR);
						sum += tempR;
					}
				}
			}

			if (sum > 0)
				for (AbstractConnection con : powerSorting.keySet())
				{
					IImmersiveConnectable end = ApiUtils.toIIC(con.end, world);
					if (con.cableType != null && end != null)
					{
						float prio = powerSorting.get(con) / (float) sum;
						int output = (int) (powerForSort * prio);

						int tempR = end.outputEnergy(Math.min(output, con.cableType.getTransferRate()), true, energyType);
						int r = tempR;
						int maxInput = getMaxInput();
						tempR -= (int) Math.max(0, Math.floor(tempR * con.getPreciseLossRate(tempR, maxInput)));
						end.outputEnergy(tempR, simulate, energyType);
						HashSet<IImmersiveConnectable> passedConnectors = new HashSet<IImmersiveConnectable>();
						float intermediaryLoss = 0;
						for (Connection sub : con.subConnections)
						{
							float length = sub.length / (float) sub.cableType.getMaxLength();
							float baseLoss = (float) sub.cableType.getLossRatio();
							float mod = (((maxInput - tempR) / (float) maxInput) / .25f) * .1f;
							intermediaryLoss = MathHelper.clamp(intermediaryLoss + length * (baseLoss + baseLoss * mod), 0, 1);

							int transferredPerCon = ImmersiveNetHandler.INSTANCE.getTransferedRates(world.provider.getDimension()).containsKey(sub) ? ImmersiveNetHandler.INSTANCE.getTransferedRates(world.provider.getDimension()).get(sub) : 0;
							transferredPerCon += r;
							if (!simulate)
							{
								ImmersiveNetHandler.INSTANCE.getTransferedRates(world.provider.getDimension()).put(sub, transferredPerCon);
								IImmersiveConnectable subStart = ApiUtils.toIIC(sub.start, world);
								IImmersiveConnectable subEnd = ApiUtils.toIIC(sub.end, world);
								if (subStart != null && passedConnectors.add(subStart))
									subStart.onEnergyPassthrough((int) (r - r * intermediaryLoss));
								if (subEnd != null && passedConnectors.add(subEnd))
									subEnd.onEnergyPassthrough((int) (r - r * intermediaryLoss));
							}
						}
						received += r;
						powerLeft -= r;
						if (powerLeft <= 0)
							break;
					}
				}
		}
		return received;
	}

	@Override
	public boolean shouldPlaySound(String sound){
		return this.active;
	}
}