package flaxbeard.immersivepetroleum.common.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorCreative;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorLV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorMV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPump;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMetalBarrel;
import flaxbeard.immersivepetroleum.common.blocks.metal.BlockTypes_IPMetalDevice;

public class BlockIPMetalDevice extends BlockIETileProvider<BlockTypes_IPMetalDevice>
{
	public BlockIPMetalDevice()
	{
		super("metalDevice0",Material.IRON, PropertyEnum.create("type", BlockTypes_IPMetalDevice.class), ItemBlockIEBase.class, IEProperties.MULTIBLOCKSLAVE,IEProperties.SIDECONFIG[0],IEProperties.SIDECONFIG[1],IEProperties.SIDECONFIG[2],IEProperties.SIDECONFIG[3],IEProperties.SIDECONFIG[4],IEProperties.SIDECONFIG[5]);
		setHardness(3.0F);
		setResistance(15.0F);
	}

	@Override
	public boolean useCustomStateMapper()
	{
		return true;
	}
	@Override
	public String getCustomStateMapping(int meta, boolean itemBlock)
	{
		return null;
	}

	@Override
	public boolean canIEBlockBePlaced(World world, BlockPos pos, IBlockState newState, EnumFacing side, float hitX, float hitY, float hitZ, EntityPlayer player, ItemStack stack)
	{
		return true;
	}


	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		state = super.getActualState(state, world, pos);
		return state;
	}


	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		switch(BlockTypes_IPMetalDevice.values()[meta])
		{
			case AUTOMATIC_LUBRICATOR:
				return new TileEntityCapacitorLV();
		}
		return null;
	}

	@Override
	public boolean allowHammerHarvest(IBlockState state)
	{
		return true;
	}
}