package flaxbeard.immersivepetroleum.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import flaxbeard.immersivepetroleum.common.blocks.metal.BlockTypes_IPMetalMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityDistillationTower;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityHydrotreater;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityPumpjack;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.Properties;

public class BlockIPMetalMultiblocks extends BlockIPMultiblock<BlockTypes_IPMetalMultiblock>
{
	public BlockIPMetalMultiblocks()
	{
		super("metal_multiblock",Material.IRON, PropertyEnum.create("type", BlockTypes_IPMetalMultiblock.class), ItemBlockIPBase.class, IEProperties.DYNAMICRENDER,IEProperties.BOOLEANS[0],Properties.AnimationProperty,IEProperties.OBJ_TEXTURE_REMAP);
		setHardness(3.0F);
		setResistance(15.0F);
		this.setAllNotNormalBlock();
		lightOpacity = 0;
	}

	@Override
	public boolean useCustomStateMapper()
	{
		return true;
	}
	@Override
	public String getCustomStateMapping(int meta, boolean itemBlock)
	{
		if(BlockTypes_IPMetalMultiblock.values()[meta].needsCustomState())
			return BlockTypes_IPMetalMultiblock.values()[meta].getCustomState();
		return null;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		switch(BlockTypes_IPMetalMultiblock.values()[meta])
		{
			case DISTILLATION_TOWER:
				return new TileEntityDistillationTower();
			case DISTILLATION_TOWER_PARENT:
				return new TileEntityDistillationTower.TileEntityDistillationTowerParent();
			case PUMPJACK:
				return new TileEntityPumpjack();
			case PUMPJACK_PARENT:
				return new TileEntityPumpjack.TileEntityPumpjackParent();
			case HYDROTREATER:
				return new TileEntityHydrotreater();
			case HYDROTREATER_PARENT:
				return new TileEntityHydrotreater.TileEntityHydrotreaterParent();
		}
		return null;
	}


	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityMultiblockPart)
		{
			TileEntityMultiblockPart tile = (TileEntityMultiblockPart)te;
			if(tile instanceof TileEntityMultiblockMetal && ((TileEntityMultiblockMetal) tile).isRedstonePos())
				return true;
			
			if (te instanceof TileEntityDistillationTower)
			{
				return tile.pos <= 1
						|| (tile.pos >= 16 && tile.pos <= 18)
						|| ((tile.pos / 16) > 0 && (tile.pos / 16) % 4 == 0 && side == EnumFacing.UP)
						|| (tile.pos / 16 == 15 && side == EnumFacing.UP);
			}
			else if(te instanceof TileEntityPumpjack)
			{
				return tile.pos == 2 || tile.pos == 20 || tile.pos == 11 || tile.pos == 9;
			}
			
		}
		return super.isSideSolid(state, world, pos, side);
	}



	@Override
	public boolean allowHammerHarvest(IBlockState state)
	{
		return true;
	}
	
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
	{
		super.onEntityCollidedWithBlock(worldIn, pos, state, entityIn);
		if (entityIn instanceof EntityLivingBase&&!((EntityLivingBase) entityIn).isOnLadder() && isLadder(state, worldIn, pos, (EntityLivingBase)entityIn))
		{
			float f5 = 0.15F;
			if (entityIn.motionX < -f5)
				entityIn.motionX = -f5;
			if (entityIn.motionX > f5)
				entityIn.motionX = f5;
			if (entityIn.motionZ < -f5)
				entityIn.motionZ = -f5;
			if (entityIn.motionZ > f5)
				entityIn.motionZ = f5;

			entityIn.fallDistance = 0.0F;
			if (entityIn.motionY < -0.15D)
				entityIn.motionY = -0.15D;

			if(entityIn.motionY<0 && entityIn instanceof EntityPlayer && entityIn.isSneaking())
			{
				entityIn.motionY=.05;
				return;
			}
			if(entityIn.isCollidedHorizontally)
				entityIn.motionY=.2;
		}
	}
	
	@Override
	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityDistillationTower)
		{
			return ((TileEntityDistillationTower) te).isLadder();
		}
		return false;
	}
	
	@Deprecated
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}
}