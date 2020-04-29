package flaxbeard.immersivepetroleum.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import flaxbeard.immersivepetroleum.common.blocks.metal.BlockTypes_IPMetalDevice;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityAutoLubricator;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityGasGenerator;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockIPMetalDevice extends BlockIPTileProvider<BlockTypes_IPMetalDevice>
{
	public BlockIPMetalDevice()
	{
		super("metal_device", Material.IRON, PropertyEnum.create("type", BlockTypes_IPMetalDevice.class), ItemBlockIPBase.class, IEProperties.FACING_HORIZONTAL, IEProperties.MULTIBLOCKSLAVE);
		setHardness(3.0F);
		setResistance(15.0F);
		lightOpacity = 0;
		this.setNotNormalBlock(BlockTypes_IPMetalDevice.GAS_GENERATOR.getMeta());
		this.setNotNormalBlock(BlockTypes_IPMetalDevice.AUTOMATIC_LUBRICATOR.getMeta());
		this.setMetaBlockLayer(BlockTypes_IPMetalDevice.AUTOMATIC_LUBRICATOR.getMeta(), BlockRenderLayer.CUTOUT);

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
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		state = super.getActualState(state, world, pos);
		return state;
	}


	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		switch (BlockTypes_IPMetalDevice.values()[meta])
		{
			case AUTOMATIC_LUBRICATOR:
				return new TileEntityAutoLubricator();
			case GAS_GENERATOR:
				return new TileEntityGasGenerator();
		}
		return null;
	}

	@Override
	public boolean allowHammerHarvest(IBlockState state)
	{
		return true;
	}

	@Override
	public boolean canIEBlockBePlaced(World world, BlockPos pos, IBlockState newState, EnumFacing side, float hitX, float hitY, float hitZ, EntityPlayer player, ItemStack stack)
	{
		if (stack.getItemDamage() == BlockTypes_IPMetalDevice.AUTOMATIC_LUBRICATOR.getMeta())
		{
			if (!world.getBlockState(pos.add(0, 1, 0)).getBlock().isReplaceable(world, pos.add(0, 1, 0)))
				return false;
		}
		return true;
	}

	@Deprecated
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		if (getMetaFromState(state) == BlockTypes_IPMetalDevice.GAS_GENERATOR.getMeta())
		{
			return EnumBlockRenderType.MODEL;
		}
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		if (getMetaFromState(state) == BlockTypes_IPMetalDevice.GAS_GENERATOR.getMeta())
		{
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityGasGenerator)
			{
				return side == EnumFacing.UP || side == ((TileEntityGasGenerator) te).facing.getOpposite();
			}
			return side == EnumFacing.UP;
		}
		else
		{
			return false;
		}
	}
}