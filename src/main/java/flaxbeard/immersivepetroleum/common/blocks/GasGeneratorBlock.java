package flaxbeard.immersivepetroleum.common.blocks;

import flaxbeard.immersivepetroleum.common.blocks.metal.GasGeneratorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class GasGeneratorBlock extends IPBlockBase{
	private static final Material material=new Material(MaterialColor.IRON, false, true, false, false, false, false, false, PushReaction.BLOCK);
	
	public static final DirectionProperty FACING=DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
	
	public GasGeneratorBlock(){
		super("gas_generator", Block.Properties.create(material).hardnessAndResistance(3.0F, 15.0F));
		
		setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.NORTH));
	}
	
	@Override
	public BlockRenderLayer getRenderLayer(){
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
//		TileEntity tmp=worldIn.getTileEntity(pos);
//		
//		worldIn.setBlockState(pos, state.with(FACING, placer.getHorizontalFacing().rotateYCCW()), 2);
//		worldIn.removeTileEntity(pos);
//		
//		worldIn.setTileEntity(pos, tmp);
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context){
		return getDefaultState().with(FACING, context.getPlacementHorizontalFacing().rotateYCCW());
	}
	
	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos){
		return true;
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder){
		builder.add(FACING);
	}
	
	@Override
	public boolean hasTileEntity(BlockState state){
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world){
		return new GasGeneratorTileEntity();
	}
}
