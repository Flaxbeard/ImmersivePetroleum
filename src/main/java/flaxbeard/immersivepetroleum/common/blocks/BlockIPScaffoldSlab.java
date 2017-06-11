package flaxbeard.immersivepetroleum.common.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import blusunrize.immersiveengineering.common.blocks.TileEntityIESlab;


public class BlockIPScaffoldSlab extends BlockIPSlab
{

	public BlockIPScaffoldSlab(String name, Material material, PropertyEnum property)
	{
		super(name, material, property);
		this.setAllNotNormalBlock();
		setBlockLayer(BlockRenderLayer.CUTOUT_MIPPED);
		lightOpacity = 0;
	}
	

	@Override
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		int meta = this.getMetaFromState(state);
		
		IBlockState state2 = world.getBlockState(pos.offset(side));
		if (this.equals(state2.getBlock()))
		{
			TileEntityIESlab slab1 = (TileEntityIESlab) world.getTileEntity(pos);
			TileEntityIESlab slab2 = (TileEntityIESlab) world.getTileEntity(pos.offset(side));

			return this.getMetaFromState(state2) != meta || slab1.slabType != slab2.slabType;
		}
		
		return super.shouldSideBeRendered(state, world, pos, side);
	}
	
	
	@Override
	public boolean allowHammerHarvest(IBlockState state)
	{
		return true;
	}

	
}