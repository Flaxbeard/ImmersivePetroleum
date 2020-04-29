package flaxbeard.immersivepetroleum.api.event;

import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class SchematicPlaceBlockPostEvent extends Event
{
	private IBlockState state;
	private World world;
	private int index;
	private IMultiblock multiblock;
	private int rotate;
	private int l;
	private int h;
	private int w;
	private BlockPos pos;

	public SchematicPlaceBlockPostEvent(IMultiblock multiblock, int index, IBlockState state, BlockPos pos, World world, int rotate, int l, int h, int w)
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

	public IBlockState getBlockState()
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

	public void setBlockState(IBlockState state)
	{
		this.state = state;
	}

	public BlockPos getPos()
	{
		return pos;
	}

	public EnumFacing getRotate()
	{
		switch (rotate)
		{
			case 0:
				return EnumFacing.EAST;
			case 1:
				return EnumFacing.NORTH;
			case 2:
				return EnumFacing.WEST;
			default:
				return EnumFacing.SOUTH;
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
