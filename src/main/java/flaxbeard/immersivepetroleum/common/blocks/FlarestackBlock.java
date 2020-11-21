package flaxbeard.immersivepetroleum.common.blocks;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.FlarestackTileEntity;
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
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.LazyOptional;

public class FlarestackBlock extends IPBlockBase{
	private static final Material material = new Material(MaterialColor.IRON, false, false, true, true, false, false, PushReaction.BLOCK);
	
	public static final BooleanProperty SLAVE = BooleanProperty.create("slave");
	
	public FlarestackBlock(){
		super("flarestack", Block.Properties.create(material)
				.hardnessAndResistance(3.0F, 15.0F)
				.harvestTool(ToolType.PICKAXE)
				.sound(SoundType.METAL)
				.notSolid());
		
		setDefaultState(getStateContainer().getBaseState()
				.with(SLAVE, false));
	}
	
	@Override
	protected BlockItem createBlockItem(){
		return new FlarestackBlockItem(this);
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder){
		builder.add(SLAVE);
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
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
		if(!worldIn.isRemote){
			worldIn.setBlockState(pos.offset(Direction.UP), state.with(SLAVE, true));
		}
	}
	
	@Override
	public List<ItemStack> getDrops(BlockState state, net.minecraft.loot.LootContext.Builder builder){
		if(!state.get(SLAVE)){
			return Arrays.asList(new ItemStack(this, 1));
		}else{
			return Collections.emptyList();
		}
	}
	
	static final LazyOptional<VoxelShape> SHAPE_SLAVE = LazyOptional.of(()->{
		VoxelShape s0 = VoxelShapes.create(0.125, 0.0001, 0.125, 0.875, 0.75, 0.875);
		VoxelShape s1 = VoxelShapes.create(0.0625, 0.0001, 0.0625, 0.9375, 0.375, 0.9375);
		return VoxelShapes.combineAndSimplify(s0, s1, IBooleanFunction.OR);
	});
	
	static final LazyOptional<VoxelShape> SHAPE_MASTER = LazyOptional.of(()->{
		VoxelShape s0 = VoxelShapes.create(0.125, 0.0001, 0.125, 0.875, 0.75, 0.875);
		VoxelShape s1 = VoxelShapes.create(0.0625, 0.5, 0.0625, 0.9375, 0.9999, 0.9375);
		return VoxelShapes.combineAndSimplify(s0, s1, IBooleanFunction.OR);
	});
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context){
		if(state.get(SLAVE)){
			return SHAPE_SLAVE.orElse(VoxelShapes.fullCube());
		}else{
			return SHAPE_MASTER.orElse(VoxelShapes.fullCube());
		}
	}
	
	@Override
	public boolean hasTileEntity(BlockState state){
		return !state.get(SLAVE);
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world){
		return FlarestackTileEntity.TYPE.create();
	}
	
	public static class FlarestackBlockItem extends IPBlockItemBase{
		public FlarestackBlockItem(Block blockIn){
			super(blockIn, new Item.Properties().group(ImmersivePetroleum.creativeTab));
		}
		
		@Override
		protected boolean canPlace(BlockItemUseContext con, BlockState state){
			if(super.canPlace(con, state)){
				BlockPos otherPos = con.getPos().offset(Direction.UP);
				BlockState otherState = con.getWorld().getBlockState(otherPos);
				
				return otherState.getBlock().isAir(otherState, con.getWorld(), otherPos);
			}
			return false;
		}
	}
}
