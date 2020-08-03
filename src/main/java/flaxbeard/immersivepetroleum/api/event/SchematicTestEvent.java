package flaxbeard.immersivepetroleum.api.event;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Event;

public class SchematicTestEvent extends Event{
	private ItemStack stack;
	private World world;
	private BlockPos pos;
	private boolean isEqual;
	private int index;
	private IMultiblock multiblock;
	private int rotate;
	private int l;
	private int h;
	private int w;
	
	public SchematicTestEvent(boolean isEqual, IMultiblock multiblock, int index, ItemStack stack, World world, BlockPos pos, int rotate, int l, int h, int w){
		super();
		this.stack = stack;
		this.world = world;
		this.pos = pos;
		this.isEqual = isEqual;
		this.multiblock = multiblock;
		this.index = index;
		this.l = l;
		this.h = h;
		this.w = w;
		this.rotate = rotate;
	}
	
	public World getWorld(){
		return world;
	}
	
	public ItemStack getItemStack(){
		return stack;
	}
	
	public BlockPos getPos(){
		return pos;
	}
	
	public void setIsEqual(boolean equal){
		this.isEqual = equal;
	}
	
	public boolean isEqual(){
		return isEqual;
	}
	
	public int getIndex(){
		return index;
	}
	
	public IMultiblock getMultiblock(){
		return multiblock;
	}
	
	public Direction getRotate(){
		switch(rotate){
			case 0:
				return Direction.EAST;
			case 1:
				return Direction.NORTH;
			case 2:
				return Direction.WEST;
			default:
				return Direction.SOUTH;
		}
	}
	
	public int getL(){
		return l;
	}
	
	public int getH(){
		return h;
	}
	
	public int getW(){
		return w;
	}
}
