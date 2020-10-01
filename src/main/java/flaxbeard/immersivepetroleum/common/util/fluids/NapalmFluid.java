package flaxbeard.immersivepetroleum.common.util.fluids;

import java.util.ArrayList;

import flaxbeard.immersivepetroleum.common.CommonEventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NapalmFluid extends IPFluid{
	public NapalmFluid(){
		super("napalm", 1000, 4000);
	}
	
	@Override
	protected IPFluidBlock createFluidBlock(){
		IPFluidBlock block = new IPFluidBlock(this.source, this.fluidName){
			@Override
			public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving){
				for(Direction facing:Direction.values()){
					BlockPos notifyPos = pos.offset(facing);
					if(worldIn.getBlockState(notifyPos).getBlock() instanceof FireBlock || worldIn.getBlockState(notifyPos).getMaterial() == Material.FIRE){
						worldIn.setBlockState(pos, Blocks.FIRE.getDefaultState());
						break;
					}
				}
				super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
			}
			
			@Override
			public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
				if(worldIn.getBlockState(fromPos).getBlock() instanceof FireBlock || worldIn.getBlockState(fromPos).getMaterial() == Material.FIRE){
					ResourceLocation d = worldIn.getDimensionKey().func_240901_a_();
					if(!CommonEventHandler.napalmPositions.containsKey(d) || !CommonEventHandler.napalmPositions.get(d).contains(fromPos)){
						processFire(worldIn, pos);
					}
				}
				
				super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
			}
		};
		return block;
	}
	
	public void processFire(World world, BlockPos pos){
		ResourceLocation d = world.getDimensionKey().func_240901_a_();
		if(!CommonEventHandler.napalmPositions.containsKey(d)){
			CommonEventHandler.napalmPositions.put(d, new ArrayList<>());
		}
		CommonEventHandler.napalmPositions.get(d).add(pos);
		
		world.setBlockState(pos, Blocks.FIRE.getDefaultState(), 1 | 2);
		
		for(Direction facing:Direction.values()){
			BlockPos notifyPos = pos.offset(facing);
			Block block = world.getBlockState(notifyPos).getBlock();
			if(block == this.block){
				CommonEventHandler.napalmPositions.get(d).add(notifyPos);
				// world.neighborChanged(notifyPos, block, neighbourPos);
			}
		}
	}
}
