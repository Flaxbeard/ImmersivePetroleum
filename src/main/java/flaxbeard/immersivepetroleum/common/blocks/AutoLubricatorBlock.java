package flaxbeard.immersivepetroleum.common.blocks;

import java.util.HashMap;
import java.util.Map;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.blocks.metal.AutoLubricatorNewTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class AutoLubricatorBlock extends IPBlockBase{
	private static final Material material=new Material(MaterialColor.IRON, false, true, true, false, false, false, false, PushReaction.BLOCK);
	
	public static final DirectionProperty FACING=DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
	public static final BooleanProperty SLAVE=BooleanProperty.create("slave");
	
	public AutoLubricatorBlock(String name){
		super(name, Block.Properties.create(material).hardnessAndResistance(3.0F, 15.0F));
		
		setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.NORTH).with(SLAVE, false));
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
		AutoLubricatorNewTileEntity te=new AutoLubricatorNewTileEntity();
		te.isSlave=state.get(SLAVE);
		te.facing=state.get(FACING);
		return te;
	}
	
	@Override
	public BlockRenderLayer getRenderLayer(){
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
		worldIn.setBlockState(pos.add(0, 1, 0), state.with(SLAVE, true));
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
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit){
		TileEntity te=worldIn.getTileEntity(pos);
		if(te instanceof AutoLubricatorNewTileEntity){
			return ((AutoLubricatorNewTileEntity)te).interact(hit.getFace(), player, handIn, player.getHeldItem(handIn), 0.0F, 0.0F, 0.0F);
		}
		
		return false;
	}
	
	static final Map<Direction, VoxelShape> SHAPE_CACHE=new HashMap<>();
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context){
		if(!state.get(SLAVE)){
			
			// Bottom Half
			
			Direction facing=state.get(FACING);
			VoxelShape shape=SHAPE_CACHE.get(facing);
			if(shape==null){
				
				VoxelShape vs0=VoxelShapes.create(0.0, 0.0, 0.0, 1.0, 0.1875, 1.0); // Plate at the very bottom
				
				VoxelShape vs1=VoxelShapes.empty();
				{
					VoxelShape vs1_0=VoxelShapes.create(0.0625, 0.1875, 0.0625, 0.25, 0.75, 0.25); // North-West Leg
					VoxelShape vs1_1=VoxelShapes.create(0.0625, 0.1875, 0.75, 0.25, 0.75, 0.9375); // West-South Leg
					VoxelShape vs1_2=VoxelShapes.create(0.75, 0.1875, 0.0625, 0.9375, 0.75, 0.25); // North-East Leg
					VoxelShape vs1_3=VoxelShapes.create(0.75, 0.1875, 0.75, 0.9375, 0.75, 0.9375); // East-South Leg
					
					VoxelShape vs1_4=VoxelShapes.create(0.1875, 0.375, 0.1875, 0.8125, 1.0, 0.8125); // Glass in the center
					
					vs1=VoxelShapes.or(vs1_0, vs1_1, vs1_2, vs1_3, vs1_4);
				}
				
				VoxelShape vs2=VoxelShapes.create(0.0, 0.6875, 0.0, 1.0, 0.875, 1.0); // Plate at the top
				
				VoxelShape vs3=VoxelShapes.empty();
				{
					// These two little grey thingies
					switch(facing){
						case NORTH:{
							VoxelShape vs4_0=VoxelShapes.create(0.375, 0.4375, 0.0, 0.5, 0.5625, 0.1875);
							VoxelShape vs4_1=VoxelShapes.create(0.5625, 0.4375, 0.0, 0.6875, 0.5625, 0.1875);
							
							vs3=VoxelShapes.or(vs4_0, vs4_1);
							break;
						}
						case SOUTH:{
							VoxelShape vs4_0=VoxelShapes.create(0.3125, 0.4375, 0.8125, 0.4375, 0.5625, 1.0);
							VoxelShape vs4_1=VoxelShapes.create(0.5, 0.4375, 0.8125, 0.625, 0.5625, 1.0);
							
							vs3=VoxelShapes.or(vs4_0, vs4_1);
							break;
						}
						case EAST:{
							VoxelShape vs4_0=VoxelShapes.create(0.8125, 0.4375, 0.5625, 1.0, 0.5625, 0.6875);
							VoxelShape vs4_1=VoxelShapes.create(0.8125, 0.4375, 0.375, 1.0, 0.5625, 0.5);
							
							vs3=VoxelShapes.or(vs4_0, vs4_1);
							break;
						}
						case WEST:{
							VoxelShape vs4_0=VoxelShapes.create(0.0, 0.4375, 0.5, 0.1875, 0.5625, 0.625);
							VoxelShape vs4_1=VoxelShapes.create(0.0, 0.4375, 0.3125, 0.1875, 0.5625, 0.4375);
							
							vs3=VoxelShapes.or(vs4_0, vs4_1);
							break;
						}
						default: vs3=VoxelShapes.empty(); break;
					}
				}
				
				shape=VoxelShapes.or(vs0, vs1, vs2, vs3);
				SHAPE_CACHE.put(facing, shape);
			}
			
			return shape;
		}else{
			
			// Top Half
			
			VoxelShape vs0=VoxelShapes.create(0.1875, 0.0, 0.1875, 0.8125, 0.9375, 0.8125); // Glass Tube
			VoxelShape vs1=VoxelShapes.create(0.25, 0.9375, 0.25, 0.75, 1.0, 0.75); // IO Port at Top
			return VoxelShapes.or(vs0, vs1);
		}
	}
	
	public static class AutoLubricatorBlockItem extends IPBlockItemBase{
		public AutoLubricatorBlockItem(Block blockIn){
			super(blockIn, new Item.Properties().group(ImmersivePetroleum.creativeTab));
		}
		
		@Override
		protected boolean canPlace(BlockItemUseContext con, BlockState state){
			if(super.canPlace(con, state)){ // No point in checking if the second block above is empty if it can't even place on the first one
				BlockState stateAbove=con.getWorld().getBlockState(con.getPos().add(0, 1, 0));
				return stateAbove==Blocks.AIR.getDefaultState() || stateAbove.getMaterial()==Material.AIR;
			}
			return false;
		}
	}
}
