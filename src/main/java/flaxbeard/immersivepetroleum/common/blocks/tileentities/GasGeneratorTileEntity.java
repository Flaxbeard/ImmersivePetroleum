package flaxbeard.immersivepetroleum.common.blocks.tileentities;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.impl.ImmersiveConnectableTileEntity;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler;
import blusunrize.immersiveengineering.api.wires.utils.WireUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.api.energy.FuelHandler;
import flaxbeard.immersivepetroleum.common.IPTileTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class GasGeneratorTileEntity extends ImmersiveConnectableTileEntity implements ITickableTileEntity, IEBlockInterfaces.IDirectionalTile, IEBlockInterfaces.IPlayerInteraction, IEBlockInterfaces.IBlockOverlayText, IEBlockInterfaces.ITileDrop, IEBlockInterfaces.ISoundTile, EnergyHelper.IIEInternalFluxConnector, EnergyHelper.IIEInternalFluxHandler, EnergyTransferHandler.EnergyConnector{
	public static final int FLUX_CAPACITY = 8000;
	
	protected WireType wireType;
	protected boolean isActive = false;
	protected Direction facing = Direction.NORTH;
	protected FluxStorage energyStorage = new FluxStorage(getMaxStorage(), Integer.MAX_VALUE, getMaxOutput());
	protected FluidTank tank = new FluidTank(FLUX_CAPACITY, fluid -> (fluid != null && fluid != FluidStack.EMPTY && FuelHandler.isValidFuel(fluid.getFluid())));
	
	public GasGeneratorTileEntity(){
		super(IPTileTypes.GENERATOR.get());
	}
	
	public int getMaxOutput(){
		return IEServerConfig.MACHINES.lvCapConfig.output.getAsInt();
	}
	
	private int getMaxStorage(){
		return IEServerConfig.MACHINES.lvCapConfig.storage.getAsInt();
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt){
		super.read(state, nbt);
		
		this.isActive = nbt.getBoolean("isActive");
		this.tank.readFromNBT(nbt.getCompound("tank"));
		this.energyStorage.readFromNBT(nbt.getCompound("buffer"));
		this.wireType = nbt.contains("wiretype") ? WireUtils.getWireTypeFromNBT(nbt, "wiretype") : null;
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound){
		CompoundNBT nbt = super.write(compound);
		
		nbt.putBoolean("isActive", this.isActive);
		nbt.put("tank", this.tank.writeToNBT(new CompoundNBT()));
		nbt.put("buffer", this.energyStorage.writeToNBT(new CompoundNBT()));
		
		if(this.wireType != null){
			nbt.putString("wiretype", this.wireType.getUniqueName());
		}
		
		return nbt;
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket(){
		return new SUpdateTileEntityPacket(this.pos, 3, getUpdateTag());
	}
	
	@Override
	public void handleUpdateTag(BlockState state, CompoundNBT tag){
		read(state, tag);
	}
	
	@Override
	public CompoundNBT getUpdateTag(){
		return write(new CompoundNBT());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt){
		read(getBlockState(), pkt.getNbtCompound());
	}
	
	@Override
	public void readOnPlacement(LivingEntity placer, ItemStack stack){
		if(stack.hasTag()){
			CompoundNBT nbt = stack.getOrCreateTag();
			
			this.tank.readFromNBT(nbt.getCompound("tank"));
			this.energyStorage.readFromNBT(nbt.getCompound("energy"));
		}
	}
	
	@Override
	public void markDirty(){
		super.markDirty();
		
		BlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 3);
		world.notifyNeighborsOfStateChange(pos, state.getBlock());
	}
	
	@Override
	public List<ItemStack> getTileDrops(LootContext context){
		ItemStack stack;
		if(context != null){
			stack = new ItemStack(context.get(LootParameters.BLOCK_STATE).getBlock());
		}else{
			stack = new ItemStack(getBlockState().getBlock());
		}
		
		CompoundNBT nbt = new CompoundNBT();
		
		if(this.tank.getFluidAmount() > 0){
			CompoundNBT tankNbt = this.tank.writeToNBT(new CompoundNBT());
			nbt.put("tank", tankNbt);
		}
		
		if(this.energyStorage.getEnergyStored() > 0){
			CompoundNBT energyNbt = this.energyStorage.writeToNBT(new CompoundNBT());
			nbt.put("energy", energyNbt);
		}
		
		if(!nbt.isEmpty())
			stack.setTag(nbt);
		return ImmutableList.of(stack);
	}
	
	@Override
	public int getAvailableEnergy(){
		return Math.min(getMaxOutput(), this.energyStorage.getEnergyStored());
	}
	
	@Override
	public void extractEnergy(int amount){
		this.energyStorage.extractEnergy(amount, false);
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
	public boolean shouldPlaySound(String sound){
		return this.isActive;
	}
	
	@Override
	public FluxStorage getFluxStorage(){
		return this.energyStorage;
	}
	
	@Override
	public IOSideConfig getEnergySideConfig(Direction facing){
		return IOSideConfig.OUTPUT;
	}
	
	IEForgeEnergyWrapper energyWrapper;
	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing){
		if(facing != this.facing)
			return null;
		
		if(this.energyWrapper == null || this.energyWrapper.side != this.facing)
			this.energyWrapper = new IEForgeEnergyWrapper(this, this.facing);
		
		return this.energyWrapper;
	}
	
	private LazyOptional<IFluidHandler> fluidHandler;
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side){
		if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && (side == null || side == Direction.UP)){
			if(this.fluidHandler == null){
				fluidHandler = LazyOptional.of(() -> this.tank);
			}
			return this.fluidHandler.cast();
		}
		return super.getCapability(cap, side);
	}
	
	@Override
	protected void invalidateCaps(){
		super.invalidateCaps();
		if(this.fluidHandler != null)
			this.fluidHandler.invalidate();
	}
	
	@Override
	public void remove(){
		super.remove();
		if(this.fluidHandler != null)
			this.fluidHandler.invalidate();
	}
	
	@Override
	public ITextComponent[] getOverlayText(PlayerEntity player, RayTraceResult mop, boolean hammer){
		if(Utils.isFluidRelatedItemStack(player.getHeldItem(Hand.MAIN_HAND))){
			ITextComponent s = null;
			if(tank.getFluid().getAmount() > 0)
				s = ((IFormattableTextComponent) tank.getFluid().getDisplayName()).appendString(": " + tank.getFluidAmount() + "mB");
			else
				s = new TranslationTextComponent(Lib.GUI + "empty");
			return new ITextComponent[]{s};
		}
		return null;
	}
	
	@Override
	public boolean useNixieFont(PlayerEntity player, RayTraceResult mop){
		return false;
	}
	
	@Override
	public boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ){
		if(FluidUtil.interactWithFluidHandler(player, hand, tank)){
			markDirty();
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
			return true;
		}
		return false;
	}
	
	@Override
	public Direction getFacing(){
		return this.facing;
	}
	
	@Override
	public void setFacing(Direction facing){
		this.facing = facing;
	}
	
	@Override
	public PlacementLimitation getFacingLimitation(){
		return PlacementLimitation.HORIZONTAL;
	}
	
	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer){
		return false;
	}
	
	@Override
	public boolean canHammerRotate(Direction side, Vector3d hit, LivingEntity entity){
		return true;
	}
	
	@Override
	public boolean canRotate(Direction axis){
		return true;
	}
	
	@Override
	public void tick(){
		if(this.world.isRemote){
			ImmersiveEngineering.proxy.handleTileSound(IESounds.dieselGenerator, this, this.isActive, .3f, .75f);
			if(this.isActive && this.world.getGameTime() % 4 == 0){
				Direction fl = this.facing;
				Direction fw = this.facing.rotateY();
				
				Vector3i vec = fw.getOpposite().getDirectionVec();
				
				double x = this.pos.getX() + .5 + (fl.getXOffset() * 2 / 16F) + (-fw.getXOffset() * .6125f);
				double y = this.pos.getY() + .4;
				double z = this.pos.getZ() + .5 + (fl.getZOffset() * 2 / 16F) + (-fw.getZOffset() * .6125f);
				
				this.world.addParticle(this.world.rand.nextInt(10) == 0 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SMOKE, x, y, z, vec.getX() * 0.025, 0, vec.getZ() * 0.025);
			}
		}else{
			boolean lastActive = this.isActive;
			this.isActive = false;
			if(!this.world.isBlockPowered(this.pos) && this.tank.getFluid() != null){
				Fluid fluid = this.tank.getFluid().getFluid();
				int amount = FuelHandler.getFuelUsedPerTick(fluid);
				if(amount > 0 && this.tank.getFluidAmount() >= amount && this.energyStorage.receiveEnergy(FuelHandler.getFluxGeneratedPerTick(fluid), false) > 0){
					this.tank.drain(new FluidStack(fluid, amount), FluidAction.EXECUTE);
					this.isActive = true;
				}
			}
			
			if(lastActive != this.isActive || (!this.world.isRemote && this.isActive)){
				markDirty();
			}
		}
	}
	
	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget){
		this.wireType = cableType;
		markDirty();
	}
	
	@Override
	public void removeCable(@Nullable Connection connection, ConnectionPoint attachedPoint){
		this.wireType = null;
		markDirty();
	}
	
	@Override
	public boolean canConnect(){
		return true;
	}
	
	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vector3i offset){
		if(world.getBlockState(target.getPosition()).getBlock() != world.getBlockState(getPos()).getBlock()){
			return false;
		}
		
		return this.wireType == null && (cableType.getCategory().equals(WireType.LV_CATEGORY) || cableType.getCategory().equals(WireType.MV_CATEGORY));
	}
	
	@Override
	public BlockPos getConnectionMaster(@Nullable WireType cableType, TargetingInfo target){
		return pos;
	}
	
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo info, Vector3i offset){
		return new ConnectionPoint(pos, 0);
	}
	
	@Override
	public Collection<ConnectionPoint> getConnectionPoints(){
		return Arrays.asList(new ConnectionPoint(pos, 0));
	}
	
	@Override
	public Vector3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here){
		float xo = facing.getDirectionVec().getX() * .5f + .5f;
		float zo = facing.getDirectionVec().getZ() * .5f + .5f;
		return new Vector3d(xo, .5f, zo);
	}
}
