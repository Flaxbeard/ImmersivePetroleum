package flaxbeard.immersivepetroleum.common.blocks.metal;

import java.util.Arrays;
import java.util.List;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.api.energy.FuelHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class AutoLubricatorNewTileEntity extends TileEntity implements ITickableTileEntity, IPlayerInteraction, IBlockOverlayText, ITileDrop{
	public static TileEntityType<AutoLubricatorNewTileEntity> TYPE;
	
	public boolean isSlave;
	public boolean isActive;
	public boolean predictablyDraining = false;
	public Direction facing;
	public FluidTank tank = new FluidTank(8000, fluid -> (fluid != null && FuelHandler.isValidFuel(fluid.getFluid())));
	
	public AutoLubricatorNewTileEntity(){
		this(TYPE);
	}
	
	public AutoLubricatorNewTileEntity(TileEntityType<?> tileEntityTypeIn){
		super(tileEntityTypeIn);
	}
	
	@Override
	public void read(CompoundNBT compound){
		this.isSlave = compound.getBoolean("slave");
		this.isActive = compound.getBoolean("active");
		this.predictablyDraining = compound.getBoolean("predictablyDraining");
		
		this.facing = Direction.byName(compound.getString("facing"));
		if(!Direction.Plane.HORIZONTAL.test(this.facing))
			this.facing = Direction.NORTH;
		
		this.tank.readFromNBT(compound.getCompound("tank"));
		
		super.read(compound);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound){
		compound.putBoolean("slave", this.isSlave);
		compound.putBoolean("active", this.isActive);
		compound.putBoolean("predictablyDraining", this.predictablyDraining);
		compound.putString("facing", this.facing.getName());
		compound.putInt("count", this.count);
		
		CompoundNBT tank = this.tank.writeToNBT(new CompoundNBT());
		compound.put("tank", tank);
		
		return super.write(compound);
	}
	
	public void readTank(CompoundNBT nbt){
		tank.readFromNBT(nbt.getCompound("tank"));
	}
	
	public void writeTank(CompoundNBT nbt, boolean toItem){
		boolean write = tank.getFluidAmount() > 0;
		CompoundNBT tankTag = tank.writeToNBT(new CompoundNBT());
		if(!toItem || write) nbt.put("tank", tankTag);
	}
	
	public void readOnPlacement(LivingEntity placer, ItemStack stack){
		if(stack.hasTag()){
			readTank(stack.getTag());
		}
	}
	
	@Override
	public List<ItemStack> getTileDrops(LootContext context){
		ItemStack stack = new ItemStack(this.getWorld().getBlockState(getPos()).getBlock(), 1);
		CompoundNBT tag = new CompoundNBT();
		writeTank(tag, true);
		if(!tag.isEmpty())
			stack.setTag(tag);
		return Arrays.asList(stack);
	}
	
	private LazyOptional<IFluidHandler> outputHandler = LazyOptional.of(() -> this.tank);
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side){
		if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && this.isSlave && (this.facing == null || this.facing == Direction.UP)){
			return this.outputHandler.cast();
		}
		return super.getCapability(cap, side);
	}
	
	public Direction getFacing(){
		return this.facing;
	}
	
	@Override
	public String[] getOverlayText(PlayerEntity player, RayTraceResult mop, boolean hammer){
		if(Utils.isFluidRelatedItemStack(player.getHeldItem(Hand.MAIN_HAND))){
			TileEntity master = this.world.getTileEntity(getPos().add(0, this.isSlave ? -1 : 0, 0));
			if(master != null && master instanceof AutoLubricatorNewTileEntity){
				AutoLubricatorNewTileEntity lube = (AutoLubricatorNewTileEntity) master;
				String s = null;
				if(lube.tank.getFluid() != null && lube.tank.getFluid() != FluidStack.EMPTY){
					s = lube.tank.getFluid().getDisplayName() + ": " + lube.tank.getFluidAmount() + "mB";
				}else{
					s = I18n.format(Lib.GUI + "empty");
				}
				return new String[]{s};
			}
		}
		return null;
	}
	
	@Override
	public boolean useNixieFont(PlayerEntity player, RayTraceResult mop){
		return false;
	}
	
	@Override
	public boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ){
		TileEntity master = this.world.getTileEntity(getPos().add(0, this.isSlave ? -1 : 0, 0));
		if(master != null && master instanceof AutoLubricatorNewTileEntity){
			if(FluidUtil.interactWithFluidHandler(player, hand, ((AutoLubricatorNewTileEntity) master).tank)){
				((AutoLubricatorNewTileEntity) master).markForUpdate();
				return true;
			}
		}
		return false;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public double getMaxRenderDistanceSquared(){
		return super.getMaxRenderDistanceSquared() * IEConfig.GENERAL.increasedTileRenderdistance.get();
	}
	
	int count = 0;
	int lastTank = 0;
	int lastTankUpdate = 0;
	int countClient = 0;
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void tick(){
		if(!this.isSlave){
			if(this.tank.getFluid() != null && this.tank.getFluid() != FluidStack.EMPTY && this.tank.getFluidAmount() >= LubricantHandler.getLubeAmount(this.tank.getFluid().getFluid()) && LubricantHandler.isValidLube(this.tank.getFluid().getFluid())){
				BlockPos target = this.pos.offset(this.facing);
				TileEntity te = this.world.getTileEntity(target);
				
				ILubricationHandler handler = LubricatedHandler.getHandlerForTile(te);
				if(handler != null){
					TileEntity master = handler.isPlacedCorrectly(this.world, this, this.facing);
					if(handler.isMachineEnabled(this.world, master)){
						this.count++;
						handler.lubricate(this.world, this.count, master);
						
						if(!this.world.isRemote && this.count % 4 == 0){
							this.tank.drain(LubricantHandler.getLubeAmount(this.tank.getFluid().getFluid()), FluidAction.EXECUTE);
						}
						
						if(this.world.isRemote){
							this.countClient++;
							if(this.countClient % 50 == 0){
								this.countClient = this.world.rand.nextInt(40);
								handler.spawnLubricantParticles(this.world, this, this.facing, master);
							}
						}
					}
				}
			}
			
			if(!this.world.isRemote && this.lastTank != this.tank.getFluidAmount()){
				if(this.predictablyDraining && (this.tank.getFluid() != null && this.tank.getFluid() != FluidStack.EMPTY) && this.lastTank - this.tank.getFluidAmount() == LubricantHandler.getLubeAmount(this.tank.getFluid().getFluid())){
					this.lastTank = this.tank.getFluidAmount();
				}
				
				if(Math.abs(this.lastTankUpdate - this.tank.getFluidAmount()) > 25){
					this.predictablyDraining = (this.tank.getFluid() != null && this.tank.getFluid() != FluidStack.EMPTY) && this.lastTank - this.tank.getFluidAmount() == LubricantHandler.getLubeAmount(this.tank.getFluid().getFluid());
					this.lastTankUpdate = this.tank.getFluidAmount();
					markForUpdate();
				}
			}
		}
	}
	
	public void markForUpdate(){
		BlockPos pos = getPos();
		BlockState state = this.world.getBlockState(pos);
		this.world.notifyBlockUpdate(pos, state, state, 3);
		this.world.notifyNeighborsOfStateChange(pos, state.getBlock());
		
		markDirty(); // Just incase. As im not sure what the above is trying to accomplish, but including it anyway.
	}
}
