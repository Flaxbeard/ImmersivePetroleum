package flaxbeard.immersivepetroleum.common.blocks.tileentities;

import java.util.List;

import flaxbeard.immersivepetroleum.api.crafting.FlarestackHandler;
import flaxbeard.immersivepetroleum.common.IPTileTypes;
import flaxbeard.immersivepetroleum.common.particle.IPParticleTypes;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class FlarestackTileEntity extends IPTileEntityBase implements ITickableTileEntity{
	static final DamageSource FLARESTACK = new DamageSource("ipFlarestack").setDamageBypassesArmor().setFireDamage();
	
	protected boolean isRedstoneInverted;
	protected boolean isActive;
	protected short drained;
	protected FluidTank tank = new FluidTank(250, fstack -> (fstack != FluidStack.EMPTY && FlarestackHandler.isBurnable(fstack)));
	
	public FlarestackTileEntity(){
		super(IPTileTypes.FLARE.get());
		this.isRedstoneInverted = true;
	}
	
	public void invertRedstone(){
		this.isRedstoneInverted = !this.isRedstoneInverted;
		this.markDirty();
	}
	
	public boolean isRedstoneInverted(){
		return this.isRedstoneInverted;
	}
	
	public boolean isActive(){
		return this.isActive;
	}
	
	public short getFlow(){
		return this.drained;
	}
	
	@Override
	public void readCustom(BlockState state, CompoundNBT nbt){
		this.isRedstoneInverted = nbt.getBoolean("inverted");
		this.isActive = nbt.getBoolean("active");
		this.drained = nbt.getShort("drained");
		this.tank.readFromNBT(nbt.getCompound("tank"));
	}
	
	@Override
	public void writeCustom(CompoundNBT nbt){
		nbt.putBoolean("inverted", this.isRedstoneInverted);
		nbt.putBoolean("active", this.isActive);
		nbt.putShort("drained", this.drained);
		
		CompoundNBT tank = this.tank.writeToNBT(new CompoundNBT());
		nbt.put("tank", tank);
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
			if(this.isActive){
				if(this.world.getGameTime() % 2 == 0){
					float xPos = (this.pos.getX() + 0.50F) + (this.world.rand.nextFloat() - 0.5F) * .4375F;
					float zPos = (this.pos.getZ() + 0.50F) + (this.world.rand.nextFloat() - 0.5F) * .4375F;
					float yPos = (this.pos.getY() + 1.875F) + (0.2F * this.world.rand.nextFloat());
					
					this.world.addParticle(IPParticleTypes.FLARE_FIRE, xPos, yPos, zPos, 0.0, 0.0625 + (0.5 * (this.drained / 1000F)), 0.0);
				}
				
			}else if(this.world.getGameTime() % 5 == 0){
				float xPos = this.pos.getX() + 0.50F + (this.world.rand.nextFloat() - 0.5F) * .4375F;
				float zPos = this.pos.getZ() + 0.50F + (this.world.rand.nextFloat() - 0.5F) * .4375F;
				float yPos = this.pos.getY() + 1.6F;
				float xa = (this.world.rand.nextFloat() - .5F) * .00625F;
				float ya = (this.world.rand.nextFloat() - .5F) * .00625F;
				
				this.world.addParticle(ParticleTypes.FLAME, xPos, yPos, zPos, xa, 0.025F, ya);
			}
		}else{
			boolean lastActive = this.isActive;
			this.isActive = false;
			
			int redstone = this.world.getRedstonePowerFromNeighbors(this.pos);
			if((this.isRedstoneInverted ? redstone == 0 : redstone > 0) && this.tank.getFluidAmount() > 0){
				float signal = getSignalStrength(redstone);
				FluidStack fs = this.tank.drain((int) (this.tank.getCapacity() * signal), FluidAction.SIMULATE);
				if(fs.getAmount() > 0){
					this.tank.drain(fs.getAmount(), FluidAction.EXECUTE);
					this.drained = (short) fs.getAmount();
					this.isActive = true;
				}
			}
			
			if(this.isActive && this.world.getGameTime() % 10 == 0){
				// Set *anything* ablaze that's in the danger zone
				BlockPos min = this.pos.add(-1, 2, -1);
				BlockPos max = min.add(3, 3, 3);
				List<Entity> list = this.getWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(min, max));
				if(!list.isEmpty()){
					list.forEach(e -> {
						if(!e.isImmuneToFire()){
							e.setFire(15);
							e.attackEntityFrom(FLARESTACK, 6.0F * (this.drained / (float) this.tank.getCapacity()));
						}
					});
				}
			}
			
			if(lastActive != this.isActive || (!this.world.isRemote && this.isActive)){
				markDirty();
			}
		}
	}
	
	private float getSignalStrength(int redstone){
		float signal = redstone / 15F;
		if(this.isRedstoneInverted){
			signal = 1F - signal;
		}
		return signal;
	}
}
