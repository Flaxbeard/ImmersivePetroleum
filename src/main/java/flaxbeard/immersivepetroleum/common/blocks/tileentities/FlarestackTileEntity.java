package flaxbeard.immersivepetroleum.common.blocks.tileentities;

import java.util.Random;

import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class FlarestackTileEntity extends TileEntity implements ITickableTileEntity{
	public static TileEntityType<FlarestackTileEntity> TYPE;
	
	protected boolean isActive;
	protected FluidTank tank = new FluidTank(8000, fluid -> (fluid != null && LubricantHandler.isValidLube(fluid.getFluid())));
	public FlarestackTileEntity(){
		this(TYPE);
	}
	
	public FlarestackTileEntity(TileEntityType<?> tileEntityTypeIn){
		super(tileEntityTypeIn);
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt){
		super.read(state, nbt);
		
		this.isActive = nbt.getBoolean("active");
		this.tank.readFromNBT(nbt.getCompound("tank"));
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound){
		super.write(compound);
		
		compound.putBoolean("active", this.isActive);
		CompoundNBT tank = this.tank.writeToNBT(new CompoundNBT());
		compound.put("tank", tank);
		
		return compound;
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
	
	public void readTank(CompoundNBT nbt){
		this.tank.readFromNBT(nbt.getCompound("tank"));
	}
	
	public void writeTank(CompoundNBT nbt, boolean toItem){
		boolean write = this.tank.getFluidAmount() > 0;
		CompoundNBT tankTag = this.tank.writeToNBT(new CompoundNBT());
		if(!toItem || write) nbt.put("tank", tankTag);
	}
	
	private LazyOptional<IFluidHandler> inputHandler;
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side){
		if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && (side == null || side == Direction.DOWN)){
			if(this.inputHandler == null){
				this.inputHandler = LazyOptional.of(() -> {
					TileEntity te = this.world.getTileEntity(getPos());
					if(te != null && te instanceof FlarestackTileEntity){
						return ((FlarestackTileEntity) te).tank;
					}
					return null;
				});
			}
			return this.inputHandler.cast();
		}
		return super.getCapability(cap, side);
	}
	
	@Override
	public void remove(){
		super.remove();
		if(this.inputHandler != null){
			this.inputHandler.invalidate();
		}
	}
	
	@Override
	protected void invalidateCaps(){
		super.invalidateCaps();
		if(this.inputHandler != null){
			this.inputHandler.invalidate();
		}
	}
	
	@Override
	public void markDirty(){
		super.markDirty();
		
		BlockState state = this.world.getBlockState(this.pos);
		this.world.notifyBlockUpdate(this.pos, state, state, 3);
		this.world.notifyNeighborsOfStateChange(this.pos, state.getBlock());
	}
	
	@Override
	public void tick(){
		if(this.world.isRemote){
			if(this.world.getGameTime() % 3 == 0){
				if(this.isActive){
					for(int i = 0;i < 10;i++){
						Random lastRand = new Random((int) Math.floor(this.world.getGameTime() / 20F));
						Random thisRand = new Random((int) Math.ceil(this.world.getGameTime() / 20F));
						
						float lastDirection = lastRand.nextFloat() * 360;
						float thisDirection = thisRand.nextFloat() * 360;
						float interpDirection = (thisDirection - lastDirection) * ((this.world.getGameTime() % 20) / 20F) + lastDirection;
						
						float xPos = (this.pos.getX() + 0.50F) + (this.world.rand.nextFloat() - 0.5F) * .4375F;
						float zPos = (this.pos.getZ() + 0.50F) + (this.world.rand.nextFloat() - 0.5F) * .4375F;
						float yPos = (this.pos.getY() + 1.75F) + (this.world.rand.nextFloat() - 0.5F) * .4375F;
						float xSpeed = (float) Math.sin(interpDirection) * .1F;
						float zSpeed = (float) Math.cos(interpDirection) * .1F;
						
						this.world.addParticle(ParticleTypes.FLAME, xPos, yPos, zPos, xSpeed, .2f, zSpeed);
						if(Math.random()<=0.05){
							this.world.addParticle(ParticleTypes.LARGE_SMOKE, xPos, yPos, zPos, xSpeed, .15f, zSpeed);
						}
					}
				}else{
					for(int i = 0;i < 3;i++){
						float xPos = this.pos.getX() + 0.50F + (this.world.rand.nextFloat() - 0.5F) * .4375F;
						float zPos = this.pos.getZ() + 0.50F + (this.world.rand.nextFloat() - 0.5F) * .4375F;
						float yPos = this.pos.getY() + 1.6F;
						double xa = (Math.random() - .5) * .00625;
						double ya = (Math.random() - .5) * .00625;
						
						this.world.addParticle(ParticleTypes.FLAME, xPos, yPos, zPos, xa, .025f, ya);
					}
				}
			}
		}else{
			boolean lastActive = this.isActive;
			
			if(!this.tank.getFluid().isEmpty() && this.tank.getFluidAmount() > 0){
				if(this.tank.drain(75, FluidAction.EXECUTE).getAmount() > 0 && !this.isActive){
					this.isActive = true;
				}
			}else if(this.isActive){
				this.isActive = false;
			}
			
			if(lastActive != this.isActive){
				markDirty();
			}
		}
	}
}
