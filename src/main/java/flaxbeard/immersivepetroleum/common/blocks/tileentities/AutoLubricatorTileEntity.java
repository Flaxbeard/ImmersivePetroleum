package flaxbeard.immersivepetroleum.common.blocks.tileentities;

import java.util.Arrays;
import java.util.List;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.common.IPTileTypes;
import flaxbeard.immersivepetroleum.common.blocks.metal.AutoLubricatorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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

public class AutoLubricatorTileEntity extends IPTileEntityBase implements ITickableTileEntity, IPlayerInteraction, IBlockOverlayText, ITileDrop{
	public boolean isSlave;
	public boolean isActive;
	public boolean predictablyDraining = false;
	public Direction facing;
	public FluidTank tank = new FluidTank(8000, fluid -> (fluid != null && LubricantHandler.isValidLube(fluid.getFluid())));
	
	public AutoLubricatorTileEntity(){
		super(IPTileTypes.AUTOLUBE.get());
	}
	
	@Override
	protected void readCustom(BlockState state, CompoundNBT compound){
		this.isSlave = compound.getBoolean("slave");
		this.isActive = compound.getBoolean("active");
		this.predictablyDraining = compound.getBoolean("predictablyDraining");
		
		Direction facing = Direction.byName(compound.getString("facing"));
		if(facing.getHorizontalIndex() == -1)
			facing = Direction.NORTH;
		this.facing = facing;
		
		this.tank.readFromNBT(compound.getCompound("tank"));
	}
	
	@Override
	protected void writeCustom(CompoundNBT compound){
		compound.putBoolean("slave", this.isSlave);
		compound.putBoolean("active", this.isActive);
		compound.putBoolean("predictablyDraining", this.predictablyDraining);
		compound.putString("facing", this.facing.getName2());
		compound.putInt("count", this.count);
		
		CompoundNBT tank = this.tank.writeToNBT(new CompoundNBT());
		compound.put("tank", tank);
	}
	
	public void readTank(CompoundNBT nbt){
		this.tank.readFromNBT(nbt.getCompound("tank"));
	}
	
	public void writeTank(CompoundNBT nbt, boolean toItem){
		boolean write = this.tank.getFluidAmount() > 0;
		CompoundNBT tankTag = this.tank.writeToNBT(new CompoundNBT());
		if(!toItem || write)
			nbt.put("tank", tankTag);
	}
	
	public void readOnPlacement(LivingEntity placer, ItemStack stack){
		if(stack.hasTag()){
			readTank(stack.getTag());
		}
	}
	
	@Override
	public List<ItemStack> getTileDrops(LootContext context){
		BlockState state = context.get(LootParameters.BLOCK_STATE);
		if(state.get(AutoLubricatorBlock.SLAVE)){
			return Arrays.asList(ItemStack.EMPTY);
		}
		
		ItemStack stack = new ItemStack(state.getBlock());
		
		TileEntity te = context.get(LootParameters.BLOCK_ENTITY);
		if(te instanceof AutoLubricatorTileEntity){
			AutoLubricatorTileEntity lube = (AutoLubricatorTileEntity) te;
			
			CompoundNBT tag = new CompoundNBT();
			lube.writeTank(tag, true);
			if(!tag.isEmpty()){
				stack.setTag(tag);
			}
		}
		
		return Arrays.asList(stack);
	}
	
	private LazyOptional<IFluidHandler> outputHandler;
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side){
		if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && this.isSlave && (side == null || side == Direction.UP)){
			if(this.outputHandler == null){
				this.outputHandler = LazyOptional.of(() -> {
					TileEntity te = this.world.getTileEntity(getPos().offset(Direction.DOWN));
					if(te != null && te instanceof AutoLubricatorTileEntity){
						return ((AutoLubricatorTileEntity) te).tank;
					}
					return null;
				});
			}
			return this.outputHandler.cast();
		}
		
		return super.getCapability(cap, side);
	}
	
	@Override
	public void remove(){
		super.remove();
		if(this.outputHandler != null)
			this.outputHandler.invalidate();
	}
	
	@Override
	public void markDirty(){
		super.markDirty();
		
		BlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 3);
		world.notifyNeighborsOfStateChange(pos, state.getBlock());
	}
	
	@Override
	protected void invalidateCaps(){
		super.invalidateCaps();
		if(this.outputHandler != null)
			this.outputHandler.invalidate();
	}
	
	public Direction getFacing(){
		return this.facing;
	}
	
	public boolean isMaster(){
		return !this.isSlave;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox(){
		BlockPos pos = getPos();
		int size = 3;
		return new AxisAlignedBB(pos.getX() - size, pos.getY() - size, pos.getZ() - size, pos.getX() + size, pos.getY() + size, pos.getZ() + size);
	}
	
	@Override
	public ITextComponent[] getOverlayText(PlayerEntity player, RayTraceResult mop, boolean hammer){
		if(Utils.isFluidRelatedItemStack(player.getHeldItem(Hand.MAIN_HAND))){
			TileEntity master = this.world.getTileEntity(getPos().add(0, this.isSlave ? -1 : 0, 0));
			if(master != null && master instanceof AutoLubricatorTileEntity){
				AutoLubricatorTileEntity lube = (AutoLubricatorTileEntity) master;
				ITextComponent s = null;
				if(!lube.tank.isEmpty()){
					s = ((IFormattableTextComponent) lube.tank.getFluid().getDisplayName()).appendString(": " + lube.tank.getFluidAmount() + "mB");
				}else{
					s = new TranslationTextComponent(Lib.GUI + "empty");
				}
				return new ITextComponent[]{s};
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
		TileEntity master = this.isSlave ? this.world.getTileEntity(getPos().add(0, -1, 0)) : this;
		if(master != null && master instanceof AutoLubricatorTileEntity){
			if(!getWorld().isRemote && FluidUtil.interactWithFluidHandler(player, hand, ((AutoLubricatorTileEntity) master).tank)){
				markDirty();
			}
			return true;
		}
		return false;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public double getMaxRenderDistanceSquared(){
		return 1024.0D;// super.getMaxRenderDistanceSquared();
	}
	
	int count = 0;
	int lastTank = 0;
	int lastTankUpdate = 0;
	int countClient = 0;
	
	@Override
	public void tick(){
		if(!this.world.isRemote && this.isSlave){
			// See ApiUtils.checkForNeedlessTicking(te);
			EventHandler.REMOVE_FROM_TICKING.add(this);
			return;
		}
		
		if(isMaster()){
			if((this.tank.getFluid() != null && this.tank.getFluid() != FluidStack.EMPTY) && this.tank.getFluidAmount() >= LubricantHandler.getLubeAmount(this.tank.getFluid().getFluid()) && LubricantHandler.isValidLube(this.tank.getFluid().getFluid())){
				BlockPos target = this.pos.offset(this.facing);
				TileEntity te = this.world.getTileEntity(target);
				
				ILubricationHandler<TileEntity> handler = LubricatedHandler.getHandlerForTile(te);
				if(handler != null){
					TileEntity master = handler.isPlacedCorrectly(this.world, this, this.facing);
					if(master != null && handler.isMachineEnabled(this.world, master)){
						this.count++;
						handler.lubricate(this.world, this.count, master);
						
						if(!this.world.isRemote && this.count % 4 == 0){
							this.tank.drain(LubricantHandler.getLubeAmount(this.tank.getFluid().getFluid()), FluidAction.EXECUTE);
							markDirty();
						}
						
						this.countClient++;
						if(this.countClient % 50 == 0){
							this.countClient = this.world.rand.nextInt(40);
							handler.spawnLubricantParticles(this.world, this, this.facing, master);
						}
					}
				}
			}
			
			if(!this.world.isRemote && this.lastTank != this.tank.getFluidAmount()){
				if(this.predictablyDraining && !this.tank.isEmpty() && this.lastTank - this.tank.getFluidAmount() == LubricantHandler.getLubeAmount(this.tank.getFluid().getFluid())){
					this.lastTank = this.tank.getFluidAmount();
				}
				
				if(Math.abs(this.lastTankUpdate - this.tank.getFluidAmount()) > 25){
					this.predictablyDraining = !this.tank.isEmpty() && this.lastTank - this.tank.getFluidAmount() == LubricantHandler.getLubeAmount(this.tank.getFluid().getFluid());
					this.lastTankUpdate = this.tank.getFluidAmount();
				}
				markDirty();
			}
		}
	}
}
