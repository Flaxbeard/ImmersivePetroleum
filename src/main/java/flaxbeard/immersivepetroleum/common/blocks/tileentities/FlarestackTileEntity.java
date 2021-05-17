package flaxbeard.immersivepetroleum.common.blocks.tileentities;

import java.util.List;
import java.util.Random;

import flaxbeard.immersivepetroleum.api.crafting.FlarestackHandler;
import flaxbeard.immersivepetroleum.common.IPTileTypes;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class FlarestackTileEntity extends IPTileEntityBase implements ITickableTileEntity{
	protected boolean isActive;
	protected FluidTank tank = new FluidTank(1000, fstack -> (fstack != null && FlarestackHandler.isBurnable(fstack)));
	
	public FlarestackTileEntity(){
		super(IPTileTypes.FLARE.get());
	}
	
	public boolean isActive(){
		return this.isActive;
	}
	
	@Override
	public void readCustom(BlockState state, CompoundNBT nbt){
		this.isActive = nbt.getBoolean("active");
		this.tank.readFromNBT(nbt.getCompound("tank"));
	}
	
	@Override
	public void writeCustom(CompoundNBT compound){
		compound.putBoolean("active", this.isActive);
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
			if(this.world.getGameTime() % 4 == 0){
				if(this.isActive){
					Random lastRand = new Random((int) Math.floor(this.world.getGameTime() / 20F));
					Random thisRand = new Random((int) Math.ceil(this.world.getGameTime() / 20F));
					
					float lastDirection = lastRand.nextFloat() * 360;
					float thisDirection = thisRand.nextFloat() * 360;
					float interpDirection = (thisDirection - lastDirection) * ((this.world.getGameTime() % 20) / 20F) + lastDirection;
					float xSpeed = (float) Math.sin(interpDirection) * .1F;
					float zSpeed = (float) Math.cos(interpDirection) * .1F;
					
					for(int i = 0;i < 10;i++){
						float xPos = (this.pos.getX() + 0.50F) + (this.world.rand.nextFloat() - 0.5F) * .4375F;
						float zPos = (this.pos.getZ() + 0.50F) + (this.world.rand.nextFloat() - 0.5F) * .4375F;
						float yPos = (this.pos.getY() + 1.875F) + (this.world.rand.nextFloat() - 0.5F) * 1.0F;
						
						this.world.addParticle(ParticleTypes.FLAME, xPos, yPos, zPos, xSpeed, .2f, zSpeed);
						if(Math.random() <= 0.1){
							this.world.addParticle(ParticleTypes.LARGE_SMOKE, xPos, yPos, zPos, xSpeed, .15f, zSpeed);
						}
					}
				}else{
					float xPos = this.pos.getX() + 0.50F + (this.world.rand.nextFloat() - 0.5F) * .4375F;
					float zPos = this.pos.getZ() + 0.50F + (this.world.rand.nextFloat() - 0.5F) * .4375F;
					float yPos = this.pos.getY() + 1.6F;
					float xa = (this.world.rand.nextFloat() - .5F) * .00625F;
					float ya = (this.world.rand.nextFloat() - .5F) * .00625F;
					
					this.world.addParticle(ParticleTypes.FLAME, xPos, yPos, zPos, xa, .025f, ya);
				}
			}
		}else{
			boolean lastActive = this.isActive;
			this.isActive = false;
			if(!this.world.isBlockPowered(this.pos) && this.tank.getFluidAmount() > 0){
				if(this.tank.drain(100, FluidAction.EXECUTE).getAmount() > 0){
					this.isActive = true;
				}
			}
			
			if(this.isActive && this.world.getGameTime() % 10 == 0){
				// Set anything ablaze that's in the danger zone
				BlockPos min = this.pos.add(-1, 2, -1);
				BlockPos max = min.add(3, 3, 3);
				List<Entity> list = this.getWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(min, max));
				if(!list.isEmpty()){
					list.forEach(e -> {
						if(!e.isImmuneToFire()){
							e.setFire(8);
						}
					});
				}
			}
			
			if(lastActive != this.isActive || (!this.world.isRemote && this.isActive)){
				markDirty();
			}
		}
	}
}
