package flaxbeard.immersivepetroleum.common.blocks;

import java.util.ArrayList;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import flaxbeard.immersivepetroleum.common.EventHandler;
import flaxbeard.immersivepetroleum.common.util.fluids.IPFluid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.IFluidState;
import net.minecraft.state.IProperty;
import net.minecraft.state.IStateHolder;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;

public class BlockNapalm extends IPFluid{
	public BlockNapalm(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, @Nullable Consumer<FluidAttributes.Builder> buildAttributes){
		super(name, stillTexture, flowingTexture, buildAttributes);
	}
	
	@Override
	protected FlowingFluidBlock createBlock(){
		FlowingFluidBlock block=new FlowingFluidBlock(()->this.source, Block.Properties.create(Material.WATER)){
			@Override
			protected void fillStateContainer(Builder<Block, BlockState> builder){
				super.fillStateContainer(builder);
				builder.add(this.getStateContainer().getProperties().toArray(new IProperty[0]));
			}
			
			@Override
			public IFluidState getFluidState(BlockState state){
				IFluidState baseState=super.getFluidState(state);
				for(IProperty<?> prop: this.getStateContainer().getProperties())
					if(prop!=FlowingFluidBlock.LEVEL)
						baseState = withCopiedValue(prop, baseState, state);
				return baseState;
			}
			
			private <T extends IStateHolder<T>, S extends Comparable<S>> T withCopiedValue(IProperty<S> prop, T oldState, IStateHolder<?> copyFrom){
				return oldState.with(prop, copyFrom.get(prop));
			}
			
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
					int d = worldIn.getDimension().getType().getId();
					if(!EventHandler.napalmPositions.containsKey(d) || !EventHandler.napalmPositions.get(d).contains(fromPos)){
						processFire(worldIn, pos);
					}
				}
				
				super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
			}
		};
		return block;
	}
	
	public void processFire(World world, BlockPos pos){
		int d = world.getDimension().getType().getId();
		if(!EventHandler.napalmPositions.containsKey(d)){
			EventHandler.napalmPositions.put(d, new ArrayList<>());
		}
		EventHandler.napalmPositions.get(d).add(pos);
		
		world.setBlockState(pos, Blocks.FIRE.getDefaultState(), 1 | 2);
		
		for(Direction facing:Direction.values()){
			BlockPos notifyPos = pos.offset(facing);
			Block block = world.getBlockState(notifyPos).getBlock();
			if(block == this.block){
				EventHandler.napalmPositions.get(d).add(notifyPos);
				// world.neighborChanged(notifyPos, block, neighbourPos);
			}
		}
	}
}
