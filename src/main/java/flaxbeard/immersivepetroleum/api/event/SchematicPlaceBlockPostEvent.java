package flaxbeard.immersivepetroleum.api.event;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import net.minecraft.block.BlockState;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class SchematicPlaceBlockPostEvent extends Event{
	private BlockState state;
	private World world;
	private IMultiblock multiblock;
	private Rotation rotate;
	private BlockPos pos;
	
	public SchematicPlaceBlockPostEvent(IMultiblock multiblock, World world, BlockPos pos, BlockState state, Rotation rotate){
		super();
		this.state = state;
		this.world = world;
		this.multiblock = multiblock;
		this.rotate = rotate;
		this.pos = pos;
	}
	
	public void setBlockState(BlockState state){
		this.state = state;
	}
	
	public World getWorld(){
		return world;
	}
	
	public BlockState getBlockState(){
		return state;
	}
	
	public IMultiblock getMultiblock(){
		return multiblock;
	}
	
	public BlockPos getPos(){
		return pos;
	}
	
	public Rotation getRotate(){
		return this.rotate;
	}
	
	@Deprecated
	public int getIndex(){
		return 0;
	}
	
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
