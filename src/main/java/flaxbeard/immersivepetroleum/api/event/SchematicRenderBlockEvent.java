package flaxbeard.immersivepetroleum.api.event;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class SchematicRenderBlockEvent extends Event{
	private World world;
	private Direction rotate;
	private IMultiblock multiblock;
	private BlockPos pos;
	private BlockState state;
	private CompoundNBT nbt;
	
	public SchematicRenderBlockEvent(IMultiblock multiblock, World world, BlockPos pos, BlockState state, CompoundNBT nbt, Direction rotate){
		super();
		this.world = world;
		this.multiblock = multiblock;
		this.pos=pos;
		this.state=state;
		this.nbt=nbt;
		
		if(Direction.Plane.HORIZONTAL.test(rotate)){
			this.rotate = rotate;
		}else{
			this.rotate = Direction.NORTH;
		}
	}
	
	public World getWorld(){
		return world;
	}
	
	public IMultiblock getMultiblock(){
		return multiblock;
	}
	
	public Direction getRotate(){
		return this.rotate;
	}
	
	public BlockPos getPos(){
		return this.pos;
	}
	
	public BlockState getState(){
		return this.state;
	}
	
	public CompoundNBT getNBT(){
		return this.nbt;
	}
	
	@Deprecated
	public ItemStack getItemStack(){
		return ItemStack.EMPTY;
	}
	
	@Deprecated
	public int getIndex(){
		return 0;
	}
	
	@Deprecated
	public void setItemStack(ItemStack itemStack){}
	
	@Deprecated
	public int getL(){
		return 0;
	}
	
	@Deprecated
	public int getH(){
		return 0;
	}
	
	@Deprecated
	public int getW(){
		return 0;
	}
}
