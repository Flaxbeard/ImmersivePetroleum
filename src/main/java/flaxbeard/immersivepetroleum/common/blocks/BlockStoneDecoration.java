package flaxbeard.immersivepetroleum.common.blocks;

import flaxbeard.immersivepetroleum.common.blocks.stone.EnumStoneDecorationType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;

public class BlockStoneDecoration extends IPBlockBase{
	public static final EnumProperty<EnumStoneDecorationType> TYPE=EnumProperty.create("type", EnumStoneDecorationType.class);
	public BlockStoneDecoration(){
		super("stone_decoration", Block.Properties.create(Material.ROCK).hardnessAndResistance(2.0F, 10.0F));
		
		setDefaultState(getStateContainer().getBaseState()
				.with(TYPE, EnumStoneDecorationType.ASPHALT));
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder){
		builder.add(TYPE);
	}
}
