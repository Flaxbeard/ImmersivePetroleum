package flaxbeard.immersivepetroleum.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IReadOnPlacement;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockBase;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.GasGeneratorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class GasGeneratorBlock extends IPBlockBase{
	private static final Material material = new Material(MaterialColor.IRON, false, false, true, true, false, false, PushReaction.BLOCK);
	
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
	
	public GasGeneratorBlock(){
		super("gas_generator", Block.Properties.create(material)
				.hardnessAndResistance(5.0F, 6.0F)
				.harvestTool(ToolType.PICKAXE)
				.sound(SoundType.METAL)
				.notSolid());
		
		setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.NORTH));
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder){
		builder.add(FACING);
	}
	
	@Override
	public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos){
		return 0;
	}
	
	@Override
	public float getAmbientOcclusionLightValue(BlockState state, IBlockReader worldIn, BlockPos pos){
		return 1.0F;
	}
	
	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos){
		return true;
	}
	
	// Fixes black faces apearing when a solid block is placed next to the generator
	// at the cost of not being able to put a lever on the generator anymore.
	static final VoxelShape SHAPE = VoxelShapes.create(0.0001, 0.0001, 0.0001, 0.9999, 0.9999, 0.9999);
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context){
		return SHAPE;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context){
		return SHAPE;
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit){
		TileEntity te = worldIn.getTileEntity(pos);
		if(te instanceof IPlayerInteraction){
			if(((IPlayerInteraction) te).interact(hit.getFace(), player, handIn, player.getHeldItem(handIn), (float) hit.getHitVec().x, (float) hit.getHitVec().y, (float) hit.getHitVec().z)){
				return ActionResultType.SUCCESS;
			}
		}
		return ActionResultType.FAIL;
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
		if(!worldIn.isRemote){
			TileEntity te = worldIn.getTileEntity(pos);
			if(te instanceof IReadOnPlacement){
				((IReadOnPlacement) te).readOnPlacement(placer, stack);
			}
		}
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context){
		return getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
	}
	
	@Override
	public boolean hasTileEntity(BlockState state){
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world){
		GasGeneratorTileEntity te = new GasGeneratorTileEntity();
		te.setFacing(state.get(FACING));
		return te;
	}
}
