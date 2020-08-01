package flaxbeard.immersivepetroleum.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import flaxbeard.immersivepetroleum.common.blocks.metal.AutoLubricatorTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.metal.EnumIPMetalDevice;
import flaxbeard.immersivepetroleum.common.blocks.metal.GasGeneratorTileEntity;
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

@Deprecated
public class BlockIPMetalDevice extends BlockIPTileProvider<EnumIPMetalDevice>
{
	public BlockIPMetalDevice()
	{
		super("metal_device", Material.IRON, PropertyEnum.create("type", EnumIPMetalDevice.class), ItemBlockIPBase.class, IEProperties.FACING_HORIZONTAL, IEProperties.MULTIBLOCKSLAVE);
		setHardness(3.0F);
		setResistance(15.0F);
		lightOpacity = 0;
		this.setNotNormalBlock(EnumIPMetalDevice.GAS_GENERATOR.getMeta());
		this.setNotNormalBlock(EnumIPMetalDevice.AUTOMATIC_LUBRICATOR.getMeta());
		this.setMetaBlockLayer(EnumIPMetalDevice.AUTOMATIC_LUBRICATOR.getMeta(), BlockRenderLayer.CUTOUT);

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
		switch (EnumIPMetalDevice.values()[meta])
		{
			case AUTOMATIC_LUBRICATOR:
				return new AutoLubricatorTileEntity();
			case GAS_GENERATOR:
				return new GasGeneratorTileEntity();
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
		if (stack.getItemDamage() == EnumIPMetalDevice.AUTOMATIC_LUBRICATOR.getMeta())
		{
			if (!world.getBlockState(pos.add(0, 1, 0)).getBlock().isReplaceable(world, pos.add(0, 1, 0)))
				return false;
		}
		return true;
	}

	@Deprecated
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		if (getMetaFromState(state) == EnumIPMetalDevice.GAS_GENERATOR.getMeta())
		{
			return EnumBlockRenderType.MODEL;
		}
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		if (getMetaFromState(state) == EnumIPMetalDevice.GAS_GENERATOR.getMeta())
		{
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof GasGeneratorTileEntity)
			{
				return side == EnumFacing.UP || side == ((GasGeneratorTileEntity) te).facing.getOpposite();
			}
			return side == EnumFacing.UP;
		}
		else
		{
			return false;
		}
	}
}