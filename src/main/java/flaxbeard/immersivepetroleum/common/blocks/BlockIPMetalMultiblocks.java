package flaxbeard.immersivepetroleum.common.blocks;

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
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import flaxbeard.immersivepetroleum.common.blocks.metal.BlockTypes_IPMetalMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityCoker;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityDistillationTower;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityPumpjack;

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
			case COKER:
				return new TileEntityCoker();
			case COKER_PARENT:
				return new TileEntityCoker.TileEntityCokerParent();
		}
		return null;
	}


	//	@Override
	//	public void breakBlock(World world, int x, int y, int z, Block par5, int par6)
	//	{
	//		TileEntity tileEntity = world.getTileEntity(x, y, z);
	//		if(tileEntity instanceof TileEntityMultiblockPart && world.getGameRules().getGameRuleBooleanValue("doTileDrops"))
	//		{
	//			TileEntityMultiblockPart tile = (TileEntityMultiblockPart)tileEntity;
	//			if(!tile.formed && tile.pos==-1 && tile.getOriginalBlock()!=null)
	//				world.spawnEntityInWorld(new EntityItem(world, x+.5,y+.5,z+.5, tile.getOriginalBlock().copy()));
	//
	//			if(tileEntity instanceof IInventory)
	//			{
	//				if(!world.isRemote && ((TileEntityMultiblockPart)tileEntity).formed)
	//				{
	//					TileEntity master = ((TileEntityMultiblockPart)tileEntity).master();
	//					if(master==null)
	//						master = tileEntity;
	//					for(int i=0; i<((IInventory)master).getSizeInventory(); i++)
	//					{
	//						ItemStack stack = ((IInventory)master).getStackInSlot(i);
	//						if(stack!=null)
	//						{
	//							float fx = world.rand.nextFloat() * 0.8F + 0.1F;
	//							float fz = world.rand.nextFloat() * 0.8F + 0.1F;
	//
	//							EntityItem entityitem = new EntityItem(world, x+fx, y+.5, z+fz, stack);
	//							entityitem.motionX = world.rand.nextGaussian()*.05;
	//							entityitem.motionY = world.rand.nextGaussian()*.05+.2;
	//							entityitem.motionZ = world.rand.nextGaussian()*.05;
	//							if(stack.hasTagCompound())
	//								entityitem.getEntityItem().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
	//							world.spawnEntityInWorld(entityitem);
	//						}
	//					}
	//				}
	//			}
	//		}
	//		super.breakBlock(world, x, y, z, par5, par6);
	//	}
	//	@Override
	//	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	//	{
	//		return new ArrayList<ItemStack>();
	//	}
	//	@Override
	//	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player)
	//	{
	//		return getOriginalBlock(world, x, y, z);
	//	}

	//	public ItemStack getOriginalBlock(World world, int x, int y, int z)
	//	{
	//		TileEntity te = world.getTileEntity(x, y, z);
	//		if(te instanceof TileEntityMultiblockPart)
	//			return ((TileEntityMultiblockPart)te).getOriginalBlock();
	//		return new ItemStack(this, 1, world.getBlockMetadata(x, y, z));
	//	}

	//	@Override
	//	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	//	{
	//		TileEntity curr = world.getTileEntity(x, y, z);
	//		if(curr instanceof TileEntitySqueezer)
	//		{
	//			if(!player.isSneaking() && ((TileEntitySqueezer)curr).formed )
	//			{
	//				TileEntitySqueezer te = ((TileEntitySqueezer)curr).master();
	//				if(te==null)
	//					te = ((TileEntitySqueezer)curr);
	//				if(!world.isRemote)
	//					player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Squeezer, world, te.xCoord, te.yCoord, te.zCoord);
	//				return true;
	//			}
	//		}
	//		else if(curr instanceof TileEntityFermenter)
	//		{
	//			if(!player.isSneaking() && ((TileEntityFermenter)curr).formed )
	//			{
	//				TileEntityFermenter te = ((TileEntityFermenter)curr).master();
	//				if(te==null)
	//					te = ((TileEntityFermenter)curr);
	//				if(!world.isRemote)
	//					player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Fermenter, world, te.xCoord, te.yCoord, te.zCoord);
	//				return true;
	//			}
	//		}
	//		else if(curr instanceof TileEntityRefinery)
	//		{
	//			if(!player.isSneaking() && ((TileEntityRefinery)curr).formed )
	//			{
	//				TileEntityRefinery te = ((TileEntityRefinery)curr).master();
	//				if(te==null)
	//					te = ((TileEntityRefinery)curr);
	//				if(!world.isRemote)
	//					player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Refinery, world, te.xCoord, te.yCoord, te.zCoord);
	//				return true;
	//			}
	//		}
	//		else if(curr instanceof TileEntityDieselGenerator)
	//		{
	//			TileEntityDieselGenerator master = ((TileEntityDieselGenerator)curr).master();
	//			if(master==null)
	//				master = ((TileEntityDieselGenerator)curr);
	//			if(((TileEntityDieselGenerator)curr).pos==40 && Utils.isHammer(player.getCurrentEquippedItem()))
	//			{
	//				master.mirrored = !master.mirrored;
	//				master.markDirty();
	//				world.markBlockForUpdate(master.xCoord, master.yCoord, master.zCoord);
	//			}
	//			else if(!world.isRemote && (((TileEntityDieselGenerator)curr).pos==36 || ((TileEntityDieselGenerator)curr).pos==38))
	//			{
	//				if(Utils.fillFluidHandlerWithPlayerItem(world, master, player))
	//				{
	//					master.markDirty();
	//					world.markBlockForUpdate(master.xCoord,master.yCoord,master.zCoord);
	//					return true;
	//				}
	//				if(player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof IFluidContainerItem)
	//				{
	//					master.markDirty();
	//					world.markBlockForUpdate(master.xCoord,master.yCoord,master.zCoord);
	//					return true;
	//				}
	//			}
	//		}
	//		else if(curr instanceof TileEntityArcFurnace)
	//		{
	//			if(!player.isSneaking() && ((TileEntityArcFurnace)curr).formed )
	//			{
	//				TileEntityArcFurnace te = ((TileEntityArcFurnace)curr);
	//				if(te.pos==2||te.pos==25|| (te.pos>25 && te.pos%5>0 && te.pos%5<4 && te.pos%25/5<4))
	//				{
	//					TileEntityArcFurnace master = te.master();
	//					if(master==null)
	//						master = te;
	//					if(!world.isRemote)
	//						player.openGui(ImmersiveEngineering.instance, Lib.GUIID_ArcFurnace, world, master.xCoord, master.yCoord, master.zCoord);
	//					return true;
	//				}
	//			}
	//		}
	//		else if(!player.isSneaking() && curr instanceof TileEntitySheetmetalTank)
	//		{
	//			TileEntitySheetmetalTank tank = (TileEntitySheetmetalTank)curr;
	//			TileEntitySheetmetalTank master = tank.master();
	//			if(master==null)
	//				master = tank;
	//			if(Utils.fillFluidHandlerWithPlayerItem(world, master, player))
	//			{
	//				master.markDirty();
	//				world.markBlockForUpdate(master.xCoord,master.yCoord,master.zCoord);
	//				return true;
	//			}
	//			if(Utils.fillPlayerItemFromFluidHandler(world, master, player, master.tank.getFluid()))
	//			{
	//				master.markDirty();
	//				world.markBlockForUpdate(master.xCoord,master.yCoord,master.zCoord);
	//				return true;
	//			}
	//			if(player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof IFluidContainerItem)
	//			{
	//				master.markDirty();
	//				world.markBlockForUpdate(master.xCoord,master.yCoord,master.zCoord);
	//				return true;
	//			}
	//		}
	//		else if(curr instanceof TileEntityAssembler)
	//		{
	//			if(!player.isSneaking() && ((TileEntityAssembler)curr).formed)
	//			{
	//				TileEntityAssembler te = ((TileEntityAssembler)curr);
	//				TileEntityAssembler master = te.master();
	//				if(master==null)
	//					master = te;
	//				if(!world.isRemote)
	//					player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Assembler, world, master.xCoord, master.yCoord, master.zCoord);
	//				return true;
	//			}
	//		}
	//		return false;
	//	}


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