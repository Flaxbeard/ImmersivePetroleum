package flaxbeard.immersivepetroleum.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import flaxbeard.immersivepetroleum.common.blocks.metal.DistillationTowerTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.metal.EnumIPMetalMultiblockType;
import flaxbeard.immersivepetroleum.common.blocks.metal.PumpjackTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

@Deprecated
public class BlockIPMetalMultiblocks extends BlockIPMultiblock<EnumIPMetalMultiblockType>
{
	public BlockIPMetalMultiblocks()
	{
		super("metal_multiblock", Material.IRON, EnumProperty.create("type", EnumIPMetalMultiblockType.class), ItemBlockIPBase.class, IEProperties.DYNAMICRENDER, IEProperties.BOOLEANS[0], Properties.AnimationProperty, IEProperties.OBJ_TEXTURE_REMAP);
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
		if (EnumIPMetalMultiblockType.values()[meta].needsCustomState())
			return EnumIPMetalMultiblockType.values()[meta].getCustomState();
		return null;
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn){
	{
		switch (EnumIPMetalMultiblockType.values()[meta])
		{
			case DISTILLATION_TOWER:
				return new DistillationTowerTileEntity();
			case DISTILLATION_TOWER_PARENT:
				return new DistillationTowerTileEntity.TileEntityDistillationTowerParent();
			case PUMPJACK:
				return new PumpjackTileEntity();
			case PUMPJACK_PARENT:
				return new PumpjackTileEntity.TileEntityPumpjackParent();
		}
		return null;
	}
	
	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityMultiblockPart)
		{
			TileEntityMultiblockPart tile = (TileEntityMultiblockPart) te;
			if (tile instanceof TileEntityMultiblockMetal && ((TileEntityMultiblockMetal) tile).isRedstonePos())
				return true;

			if (te instanceof DistillationTowerTileEntity)
			{
				return tile.pos <= 1
						|| (tile.pos >= 16 && tile.pos <= 18)
						|| ((tile.pos / 16) > 0 && (tile.pos / 16) % 4 == 0 && side == EnumFacing.UP)
						|| (tile.pos / 16 == 15 && side == EnumFacing.UP);
			}
			else if (te instanceof PumpjackTileEntity)
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
	public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
	{
		super.onEntityCollision(worldIn, pos, state, entityIn);
		if (entityIn instanceof EntityLivingBase && !((EntityLivingBase) entityIn).isOnLadder() && isLadder(state, worldIn, pos, (EntityLivingBase) entityIn))
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

			if (entityIn.motionY < 0 && entityIn instanceof EntityPlayer && entityIn.isSneaking())
			{
				entityIn.motionY = .05;
				return;
			}
			if (entityIn.collidedHorizontally)
				entityIn.motionY = .2;
		}
	}

	@Override
	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity)
	{
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof DistillationTowerTileEntity)
		{
			return ((DistillationTowerTileEntity) te).isLadder();
		}
		return false;
	}

	@Deprecated
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public int getRenderColour(BlockState state, IBlockReader worldIn, BlockPos pos, int tintIndex){
		return 0;
	}

	@Override
	public boolean hasFlavour(){
		return false;
	}

	@Override
	public String getNameForFlavour(){
		return null;
	}
}