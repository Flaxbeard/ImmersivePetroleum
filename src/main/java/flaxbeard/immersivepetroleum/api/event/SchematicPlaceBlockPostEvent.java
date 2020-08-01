package flaxbeard.immersivepetroleum.api.event;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class SchematicPlaceBlockPostEvent extends Event
{
	private BlockState state;
	private World world;
	private int index;
	private IMultiblock multiblock;
	private int rotate;
	private int l;
	private int h;
	private int w;
	private BlockPos pos;

	public SchematicPlaceBlockPostEvent(IMultiblock multiblock, int index, BlockState state, BlockPos pos, World world, int rotate, int l, int h, int w)
	{
		super();
		this.state = state;
		this.world = world;
		this.multiblock = multiblock;
		this.index = index;
		this.rotate = rotate;
		this.l = l;
		this.h = h;
		this.w = w;
		this.pos = pos;
	}

	public World getWorld()
	{
		return world;
	}

	public BlockState getBlockState()
	{
		return state;
	}

	public int getIndex()
	{
		return index;
	}

	public IMultiblock getMultiblock()
	{
		return multiblock;
	}

	public void setBlockState(BlockState state)
	{
		this.state = state;
	}

	public BlockPos getPos()
	{
		return pos;
	}

	public Direction getRotate()
	{
		switch (rotate)
		{
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


	public int getL()
	{
		return l;
	}

	public int getH()
	{
		return h;
	}

	public int getW()
	{
		return w;
	}
}
