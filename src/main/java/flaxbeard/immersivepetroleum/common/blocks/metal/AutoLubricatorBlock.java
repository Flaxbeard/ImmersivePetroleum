package flaxbeard.immersivepetroleum.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IReadOnPlacement;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockBase;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockItemBase;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.AutoLubricatorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
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

public class AutoLubricatorBlock extends IPBlockBase{
	private static final Material material = new Material(MaterialColor.IRON, false, false, true, true, false, false, PushReaction.BLOCK);
	
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
	public static final BooleanProperty SLAVE = BooleanProperty.create("slave");
	
	public AutoLubricatorBlock(String name){
		super(name, Block.Properties.create(material)
				.hardnessAndResistance(5.0F, 6.0F)
				.harvestTool(ToolType.AXE)
				.sound(SoundType.METAL)
				.notSolid());
		
		setDefaultState(getStateContainer().getBaseState()
				.with(FACING, Direction.NORTH)
				.with(SLAVE, false));
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder){
		builder.add(FACING, SLAVE);
	}
	
	@Override
	protected BlockItem createBlockItem(){
		return new AutoLubricatorBlockItem(this);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context){
		return getDefaultState().with(FACING, context.getPlacementHorizontalFacing());
	}
	
	@Override
	public boolean hasTileEntity(BlockState state){
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world){
		AutoLubricatorTileEntity te = new AutoLubricatorTileEntity();
		te.isSlave = state.get(SLAVE);
		te.facing = state.get(FACING);
		return te;
	}
	
	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player){
		if(state.get(SLAVE)){
			worldIn.destroyBlock(pos.add(0, -1, 0), true);
		}else{
			worldIn.destroyBlock(pos.add(0, 1, 0), false);
		}
		
		super.onBlockHarvested(worldIn, pos, state, player);
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
			worldIn.setBlockState(pos.add(0, 1, 0), state.with(SLAVE, true));
			TileEntity te = worldIn.getTileEntity(pos);
			if(te instanceof IReadOnPlacement){
				((IReadOnPlacement) te).readOnPlacement(placer, stack);
			}
		}
	}
	
	static final VoxelShape SHAPE_SLAVE = VoxelShapes.create(.1875F, 0, .1875F, .8125f, 1, .8125f);
	static final VoxelShape SHAPE_MASTER = VoxelShapes.create(.0625f, 0, .0625f, .9375f, 1, .9375f);
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context){
		return state.get(SLAVE) ? SHAPE_SLAVE : SHAPE_MASTER;
	}
	
	public static class AutoLubricatorBlockItem extends IPBlockItemBase{
		public AutoLubricatorBlockItem(Block blockIn){
			super(blockIn, new Item.Properties().group(ImmersivePetroleum.creativeTab));
		}
		
		@Override
		protected boolean canPlace(BlockItemUseContext con, BlockState state){
			// No point in checking if the second block above is empty if it can't even place on the first one
			if(super.canPlace(con, state)){
				BlockPos pos=con.getPos().add(0, 1, 0);
				BlockState otherState=con.getWorld().getBlockState(pos);
				otherState.getBlock().isAir(otherState, con.getWorld(), pos);
				return otherState.getBlock().isAir(otherState, con.getWorld(), pos);
			}
			return false;
		}
	}
}
