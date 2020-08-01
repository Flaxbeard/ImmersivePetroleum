package flaxbeard.immersivepetroleum.common.blocks.metal;

import java.util.List;
import java.util.function.Predicate;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.ImmersiveConnectableTileEntity;
import blusunrize.immersiveengineering.api.wires.WireType;
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
import flaxbeard.immersivepetroleum.api.energy.FuelHandler;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class GeneratorTileEntity extends ImmersiveConnectableTileEntity implements IDirectionalTile, ITickable, IPlayerInteraction, IBlockOverlayText, IIEInternalFluxConnector, ITileDrop, IIEInternalFluxHandler, IEBlockInterfaces.ISoundTile, EnergyConnector{
	public static TileEntityType<GeneratorTileEntity> TYPE;

	protected boolean isActive=false;
	protected Direction facing = Direction.NORTH;
	protected FluxStorage energyStorage = new FluxStorage(getMaxStorage(), getMaxInput(), 0);
	protected FluidTank tank = new FluidTank(8000, new Predicate<FluidStack>(){
		@Override
		public boolean test(FluidStack fluid){
			return fluid != null && FuelHandler.isValidFuel(fluid.getFluid());
		}
	});
	
	public GeneratorTileEntity(){
		super(TYPE);
	}
	
	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket){
		super.writeCustomNBT(nbt, descPacket);
		
		nbt.putBoolean("isActive", this.isActive);
		nbt.put("tank", this.tank.writeToNBT(new CompoundNBT()));
		nbt.put("buffer", this.energyStorage.writeToNBT(new CompoundNBT()));
	}
	
	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket){
		super.readCustomNBT(nbt, descPacket);
		
		this.isActive=nbt.getBoolean("isActive");
		this.tank.readFromNBT(nbt.getCompound("tank"));
		this.energyStorage.readFromNBT(nbt.getCompound("buffer"));
	}
	
	@Override
	public void tick(){
		boolean lastActive=this.isActive;
		this.isActive=false;
		if(!this.world.isBlockPowered(this.pos) && this.tank.getFluid()!=null){
			Fluid fluid=this.tank.getFluid().getFluid();
			if(FuelHandler.isValidFuel(fluid)){
				int amount = FuelHandler.getFuelUsedPerTick(fluid);
				if(this.tank.getFluidAmount()>=amount){
					if(!this.world.isRemote){
						if(this.energyStorage.receiveEnergy(FuelHandler.getFluxGeneratedPerTick(fluid), false)>0){
							this.tank.drain(new FluidStack(fluid, amount), FluidAction.EXECUTE);
						}
					}else{
						if(this.energyStorage.receiveEnergy(FuelHandler.getFluxGeneratedPerTick(fluid), true)>0){
							this.isActive=true;
						}
					}
				}
			}
		}
		
		if(this.world.isRemote){
			ImmersiveEngineering.proxy.handleTileSound(IESounds.dieselGenerator, this, this.isActive, .3f, .5f);
			if(this.isActive && this.world.getGameTime() % 4 == 0){
				Direction fl = this.facing;
				Direction fw = this.facing.rotateY();
				this.world.addParticle(this.world.rand.nextInt(10) == 0 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SMOKE,
						this.pos.getX() + .5 + (fl.getXOffset() * 2 / 16F) + (-fw.getXOffset() * .6125f),
						this.pos.getY() + .4,
						this.pos.getZ() + .5 + (fl.getZOffset() * 2 / 16F) + (-fw.getZOffset() * .6125f),
						0, 0, 0);
			}
		}
		
		if(lastActive!=this.isActive){
			markDirty();
			markContainingBlockForUpdate(null);
		}
	}
	
	public int getMaxInput(){
		return IEConfig.MACHINES.wireConnectorInput.get().get(0); // LV
	}
	
	public int getMaxOutput(){
		return IEConfig.MACHINES.wireConnectorInput.get().get(0); // LV
	}
	
	private int getMaxStorage(){
		return IEConfig.MACHINES.capacitorLvStorage.get();
	}
	
	@Override
	public void readOnPlacement(LivingEntity placer, ItemStack stack){
	}
	
	@Override
	public List<ItemStack> getTileDrops(LootContext context){
		return null;
	}
	
	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset){
		return false;
	}
	
	@Override
	public Vec3d getConnectionOffset(Connection con, ConnectionPoint here){
		return null;
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
	public int getAvailableEnergy(){
		return this.energyStorage.getEnergyStored();
	}
	
	@Override
	public boolean shoudlPlaySound(String sound){
		return false;
	}
	
	@Override
	public FluxStorage getFluxStorage(){
		return this.energyStorage;
	}
	
	@Override
	public IOSideConfig getEnergySideConfig(Direction facing){
		return null;
	}
	
	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing){
		return null;
	}
	
	@Override
	public String[] getOverlayText(PlayerEntity player, RayTraceResult mop, boolean hammer){
		return null;
	}
	
	@Override
	public boolean useNixieFont(PlayerEntity player, RayTraceResult mop){
		return true;
	}
	
	@Override
	public boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ){
		return false;
	}
	
	@Override
	public Direction getFacing(){
		return null;
	}
	
	@Override
	public void setFacing(Direction facing){
	}
	
	@Override
	public PlacementLimitation getFacingLimitation(){
		return null;
	}
	
	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer){
		return false;
	}
	
	@Override
	public boolean canHammerRotate(Direction side, Vec3d hit, LivingEntity entity){
		return false;
	}
	
	@Override
	public boolean canRotate(Direction axis){
		return false;
	}
}
