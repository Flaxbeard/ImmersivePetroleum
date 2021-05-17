package flaxbeard.immersivepetroleum.common.blocks;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class IPBlockSlab<B extends IPBlockBase> extends SlabBlock{
	private final B base;
	
	public IPBlockSlab(B base){
		super(Properties.from(base).setSuffocates(causesSuffocation(base)).setOpaque(isNormalCube(base)));
		setRegistryName(new ResourceLocation(ImmersivePetroleum.MODID, base.getRegistryName().getPath() + "_slab"));
		
		IPContent.registeredIPBlocks.add(this);
		
		BlockItem bItem = createBlockItem();
		if(bItem != null){
			IPContent.registeredIPItems.add(bItem.setRegistryName(getRegistryName()));
		}
		
		this.base = base;
	}
	
	protected BlockItem createBlockItem(){
		return new IPBlockItemBase(this, new Item.Properties().group(ImmersivePetroleum.creativeTab));
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos){
		return Math.min(base.getOpacity(state, worldIn, pos), super.getOpacity(state, worldIn, pos));
	}
	
	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos){
		return super.propagatesSkylightDown(state, reader, pos) || base.propagatesSkylightDown(state, reader, pos);
	}
	
	public static AbstractBlock.IPositionPredicate causesSuffocation(Block base){
		return (state, world, pos) -> base.getDefaultState().isSuffocating(world, pos) && state.get(TYPE) == SlabType.DOUBLE;
	}
	
	public static AbstractBlock.IPositionPredicate isNormalCube(Block base){
		return (state, world, pos) -> base.getDefaultState().isNormalCube(world, pos) && state.get(TYPE) == SlabType.DOUBLE;
	}
}
