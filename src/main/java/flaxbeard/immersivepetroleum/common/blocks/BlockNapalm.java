package flaxbeard.immersivepetroleum.common.blocks;

import flaxbeard.immersivepetroleum.common.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class BlockNapalm extends BlockIPFluid
{

	public BlockNapalm(String name, Fluid fluid, Material material)
	{
		super(name, fluid, material);
	}

	@Override
	public void onBlockAdded(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state)
	{
		for (EnumFacing facing : EnumFacing.VALUES)
		{
			BlockPos notifyPos = pos.offset(facing);
			if (world.getBlockState(notifyPos).getBlock() instanceof BlockFire
					|| world.getBlockState(notifyPos).getMaterial() == Material.FIRE)
			{
				world.setBlockState(pos, Blocks.FIRE.getDefaultState(), 1 | 2);
				break;
			}
		}
		super.onBlockAdded(world, pos, state);
	}

	@Override
	public void neighborChanged(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Block neighborBlock, @Nonnull BlockPos neighbourPos)
	{
		if (world.getBlockState(neighbourPos).getBlock() instanceof BlockFire
				|| world.getBlockState(neighbourPos).getMaterial() == Material.FIRE)
		{
			int d = world.provider.getDimension();
			if (!EventHandler.napalmPositions.containsKey(d)
					|| !EventHandler.napalmPositions.get(d).contains(neighbourPos))
			{
				processFire(world, pos);
			}
		}
		super.neighborChanged(state, world, pos, neighborBlock, neighbourPos);
	}

	public void processFire(World world, BlockPos pos)
	{
		int d = world.provider.getDimension();
		if (!EventHandler.napalmPositions.containsKey(d))
		{
			EventHandler.napalmPositions.put(d, new ArrayList<>());
		}
		EventHandler.napalmPositions.get(d).add(pos);

		world.setBlockState(pos, Blocks.FIRE.getDefaultState(), 1 | 2);

		for (EnumFacing facing : EnumFacing.VALUES)
		{
			BlockPos notifyPos = pos.offset(facing);
			Block block = world.getBlockState(notifyPos).getBlock();
			if (block instanceof BlockNapalm)
			{
				EventHandler.napalmPositions.get(d).add(notifyPos);
				//world.neighborChanged(notifyPos, block, neighbourPos);
			}
		}
	}
}
