package flaxbeard.immersivepetroleum.common.blocks;

import java.util.ArrayList;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import flaxbeard.immersivepetroleum.common.CommonEventHandler;
import flaxbeard.immersivepetroleum.common.util.fluids.IPFluid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.StateHolder;
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
				builder.add(this.getStateContainer().getProperties().toArray(new Property[0]));
			}
			
			@Override
			public FluidState getFluidState(BlockState state){
				FluidState baseState=super.getFluidState(state);
				for(Property<?> prop: this.getStateContainer().getProperties())
					if(prop!=FlowingFluidBlock.LEVEL)
						baseState = withCopiedValue(prop, baseState, state);
				return baseState;
			}
			
			private <T extends StateHolder<?, T>, S extends Comparable<S>> T withCopiedValue(Property<S> prop, T oldState, StateHolder<?, ?> copyFrom){
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
					ResourceLocation d = worldIn.getDimensionKey().getRegistryName();
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
		ResourceLocation d = world.getDimensionKey().getRegistryName();
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
