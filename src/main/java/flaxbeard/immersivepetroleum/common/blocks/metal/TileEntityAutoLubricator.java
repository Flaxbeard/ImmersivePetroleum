package flaxbeard.immersivepetroleum.common.blocks.metal;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBucketWheel;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.client.model.ModelLubricantPipes;
import flaxbeard.immersivepetroleum.client.model.ModelLubricantPipes.Pumpjack;
import flaxbeard.immersivepetroleum.common.IPContent;

public class TileEntityAutoLubricator extends TileEntityIEBase implements IDirectionalTile, IHasDummyBlocks, ITickable, IPlayerInteraction, IBlockOverlayText
{
	
	public static class PumpjackLubricationHandler implements ILubricationHandler
	{

		@Override
		public boolean isPlacedCorrectly(World world, TileEntityAutoLubricator tile, EnumFacing facing)
		{
			BlockPos target = tile.getPos().offset(facing, 2).up();
			TileEntity te = world.getTileEntity(target);
			
			if (te instanceof TileEntityPumpjack)
			{
				TileEntityPumpjack jack = (TileEntityPumpjack) te;
				TileEntityPumpjack master = jack.master();
				
				EnumFacing f = master.mirrored ? facing : facing.getOpposite() ;
				if (jack == master && jack.getFacing().rotateY() == f)
				{
					return true;
				}
			}
			
			return false;
		}

		@Override
		public boolean isMachineEnabled(World world, TileEntityAutoLubricator tile, EnumFacing facing)
		{
			BlockPos target = tile.getPos().offset(facing, 2).up();
			TileEntity te = world.getTileEntity(target);
			
			if (te instanceof TileEntityPumpjack)
			{
				TileEntityPumpjack jack = (TileEntityPumpjack) te;
				TileEntityPumpjack master = jack.master();
				
				return master.wasActive;
			}
			
			return false;
		}

		@Override
		public void lubricate(World world, TileEntityAutoLubricator tile, EnumFacing facing, int ticks)
		{
			if (ticks % 4 == 0)
			{
				BlockPos target = tile.getPos().offset(facing, 2).up();
				TileEntity te = world.getTileEntity(target);
				
				if (te instanceof TileEntityPumpjack)
				{
					tile.tank.drainInternal(LubricantHandler.getLubeAmount(tile.tank.getFluid().getFluid()), true);
					((TileEntityPumpjack) te).update();
					tile.markContainingBlockForUpdate(null);
				}
			}			
		}

		@Override
		public void spawnLubricantParticles(World world, TileEntityAutoLubricator tile, EnumFacing facing)
		{
			BlockPos target = tile.pos.offset(facing, 2).up();
			TileEntity te = world.getTileEntity(target);
			
			if (te instanceof TileEntityPumpjack)
			{
				TileEntityPumpjack master = (TileEntityPumpjack) te;
					
				EnumFacing f = master.mirrored ? facing : facing.getOpposite() ;
				float location = world.rand.nextFloat();
				
				boolean flip = f.getAxis() == Axis.Z ^ facing.getAxisDirection() == AxisDirection.POSITIVE ^ !master.mirrored;
				float xO = 2.5F;
				float zO = -.15F;
				float yO = 2.25F;
	
				
				if (location > .5F)
				{
					xO = 1.7F;
					yO = 2.9F;
					zO = -1.5F;
	
				}
				
				if (facing.getAxisDirection() == AxisDirection.NEGATIVE) xO = -xO + 1;
				if (!flip) zO = -zO + 1;
	
				
				
				float x = tile.pos.getX() + (f.getAxis() == Axis.X ? xO : zO);
				float y = tile.pos.getY() + yO;
				float z = tile.pos.getZ() + (f.getAxis() == Axis.X ? zO : xO);
				
				for (int i = 0; i < 3; i++)
				{
	
					float r1 = (world.rand.nextFloat() - .5F) * 2F;
					float r2 = (world.rand.nextFloat() - .5F) * 2F;
					float r3 = world.rand.nextFloat();
					int n = Block.getStateId(IPContent.blockFluidLubricant.getDefaultState());
					world.spawnParticle(EnumParticleTypes.BLOCK_DUST, x, y, z, r1 * 0.04F, r3 * 0.0125F, r2 * 0.025F, new int[] {n});
				}			
			}
		}
		
		private static Object pumpjackM;
		private static Object pumpjack;

		@Override
		@SideOnly(Side.CLIENT)
		public void renderPipes(World world, TileEntityAutoLubricator tile, EnumFacing facing)
		{
			if (pumpjackM == null)
			{
				pumpjackM = new ModelLubricantPipes.Pumpjack(true);
				pumpjack = new ModelLubricantPipes.Pumpjack(false);
			}
			BlockPos pos = tile.getPos().offset(tile.getFacing());
			TileEntity pj = tile.getWorld().getTileEntity(pos);
			if (pj instanceof TileEntityPumpjack)
			{
				BlockPos masterPos = tile.getPos().offset(tile.getFacing(), 2).up();
				TileEntityPumpjack base = ((TileEntityPumpjack)pj).master();
				TileEntity target = tile.getWorld().getTileEntity(masterPos);
				
				EnumFacing f = base.mirrored ? tile.getFacing() : tile.getFacing().getOpposite() ;
				if (base != null && base == target && base.getFacing().rotateY() == f)
				{
	
					GlStateManager.translate(0, -1, 0);
					Vec3i offset = base.getPos().subtract(tile.getPos());
					GlStateManager.translate(offset.getX(), offset.getY(), offset.getZ());
	
					EnumFacing rotation = base.facing;
					if (rotation == EnumFacing.NORTH)
					{
						GlStateManager.rotate(90F, 0, 1, 0);
						GlStateManager.translate(-1, 0, 0);
					}
					else if (rotation == EnumFacing.WEST)
					{
						GlStateManager.rotate(180F, 0, 1, 0);
						GlStateManager.translate(-1, 0, -1);
					}
					else if (rotation == EnumFacing.SOUTH)
					{
						GlStateManager.rotate(270F, 0, 1, 0);
						GlStateManager.translate(0, 0, -1);
					}
					GlStateManager.translate(-1, 0, -1);
					ClientUtils.bindTexture("immersivepetroleum:textures/blocks/lube_pipe12.png");
					if (base.mirrored)
					{
						((ModelLubricantPipes.Pumpjack) pumpjackM).render(null, 0, 0, 0, 0, 0, 0.0625F);
					}
					else
					{
						((ModelLubricantPipes.Pumpjack) pumpjack).render(null, 0, 0, 0, 0, 0, 0.0625F);
					}					
				}
			}
		}
		
	}
	
	public boolean active;
	public int dummy = 0;
	public FluxStorage energyStorage = new FluxStorage(8000);
	public EnumFacing facing = EnumFacing.NORTH;
	public FluidTank tank = new FluidTank(8000);

	public int doSpeedup()
	{
		int consumed = IEConfig.Machines.preheater_consumption;
		/*if(this.energyStorage.extractEnergy(consumed, true)==consumed)
		{
			if (!active)
			{
				active = true;
				this.markContainingBlockForUpdate(null);
			}
			this.energyStorage.extractEnergy(consumed, false);
			return 1;
		}
		else if(active)
		{
			active = false;
			this.markContainingBlockForUpdate(null);
		}*/
		return 0;
	}
	
	@Override
	public boolean shouldRenderInPass(int pass)
	{
		return pass <= 2;
	}

	@Override
	public boolean isDummy()
	{
		return dummy>0;
	}
	@Override
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		worldObj.setBlockState(pos.add(0, 1, 0), state);
		((TileEntityAutoLubricator)worldObj.getTileEntity(pos.add(0, 1, 0))).dummy = 1;
		((TileEntityAutoLubricator)worldObj.getTileEntity(pos.add(0, 1, 0))).facing = this.facing;
	
	}
	@Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		for (int i = 0; i <= 1; i++)
			if (worldObj.getTileEntity(getPos().add(0,-dummy,0).add(0,i,0)) instanceof TileEntityAutoLubricator)
				worldObj.setBlockToAir(getPos().add(0,-dummy,0).add(0,i,0));
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		dummy = nbt.getInteger("dummy");
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		energyStorage.readFromNBT(nbt);
		active = nbt.getBoolean("active");
		tank.readFromNBT(nbt.getCompoundTag("tank"));
		count = nbt.getInteger("count");

		if(descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("dummy", dummy);
		nbt.setInteger("facing", facing.ordinal());
		nbt.setBoolean("active", active);
		nbt.setInteger("count", count);
		
		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		nbt.setTag("tank", tankTag);
		
		energyStorage.writeToNBT(nbt);
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}
	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}
	@Override
	public int getFacingLimitation()
	{
		return 2;
	}
	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
	}
	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return true;
	}
	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return true;
	}
	@Override
	public  void afterRotation(EnumFacing oldDir, EnumFacing newDir)
	{
		for(int i=0; i<=1; i++)
		{
			TileEntity te = worldObj.getTileEntity(getPos().add(0,-dummy+i,0));
			if(te instanceof TileEntityAutoLubricator)
			{
				((TileEntityAutoLubricator)te).setFacing(newDir);
				te.markDirty();
				((TileEntityAutoLubricator)te).markContainingBlockForUpdate(null);
			}
		}
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && dummy == 1 && (facing==null || facing == EnumFacing.UP))
		{
			TileEntity te = worldObj.getTileEntity(getPos().add(0,-dummy,0));
			if (te instanceof TileEntityAutoLubricator)
			{
				return (T) ((TileEntityAutoLubricator)te).tank;
			}
		}
		return super.getCapability(capability, facing);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if (dummy == 1 && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
		{
			TileEntity te = worldObj.getTileEntity(getPos().add(0,-dummy,0));
			if (te instanceof TileEntityAutoLubricator)
			{
				return (facing==null || facing == EnumFacing.UP);
			}
		}
		return super.hasCapability(capability, facing);
	}
	
	int count = 0;
	int lastTank = 0;
	int countClient = 0;
	
	@Override
	public void update()
	{
		if (dummy == 0)
		{
			if (tank.getFluid() != null && tank.getFluid().getFluid() != null && tank.getFluidAmount() > LubricantHandler.getLubeAmount(tank.getFluid().getFluid()) && LubricantHandler.isValidLube(tank.getFluid().getFluid()))
			{
				BlockPos target = pos.offset(facing, 2).up();
				TileEntity te = worldObj.getTileEntity(target);
				
				if (te instanceof TileEntityPumpjack)
				{
					TileEntityPumpjack jack = (TileEntityPumpjack) te;
					TileEntityPumpjack master = jack.master();
					
					EnumFacing f = master.mirrored ? facing : facing.getOpposite() ;
					if (jack == master && master.wasActive && jack.getFacing().rotateY() == f)
					{
						if (worldObj.isRemote)
						{
							master.activeTicks += 1F/4F;
							countClient++;
							if (countClient % 50 == 0)
							{
								countClient = worldObj.rand.nextInt(40);
								
								float location = worldObj.rand.nextFloat();
								
								boolean flip = f.getAxis() == Axis.Z ^ facing.getAxisDirection() == AxisDirection.POSITIVE ^ !master.mirrored;
								float xO = 2.5F;
								float zO = -.15F;
								float yO = 2.25F;
				
								
								if (location > .5F)
								{
									xO = 1.7F;
									yO = 2.9F;
									zO = -1.5F;

								}
								
								if (facing.getAxisDirection() == AxisDirection.NEGATIVE) xO = -xO + 1;
								if (!flip) zO = -zO + 1;

								
								
								float x = pos.getX() + (f.getAxis() == Axis.X ? xO : zO);
								float y = pos.getY() + yO;
								float z = pos.getZ() + (f.getAxis() == Axis.X ? zO : xO);
								
								for (int i = 0; i < 3; i++)
								{
					
									float r1 = (worldObj.rand.nextFloat() - .5F) * 2F;
									float r2 = (worldObj.rand.nextFloat() - .5F) * 2F;
									float r3 = worldObj.rand.nextFloat();
									int n = Block.getStateId(IPContent.blockFluidLubricant.getDefaultState());
									worldObj.spawnParticle(EnumParticleTypes.BLOCK_DUST, x, y, z, r1 * 0.04F, r3 * 0.0125F, r2 * 0.025F, new int[] {n});
								}
							}
						}
						else
						{
							count++;
							if (count % 4 == 0)
							{
								tank.drainInternal(LubricantHandler.getLubeAmount(tank.getFluid().getFluid()), true);
								master.update();
								markContainingBlockForUpdate(null);
	
								count = 0;
							}
						}
					}
				}
				
				target = pos.offset(facing, 1).up();
				te = worldObj.getTileEntity(target);
				if (te instanceof TileEntityExcavator)
				{
					TileEntityExcavator ex = (TileEntityExcavator) te;
					
					EnumFacing f = ex.mirrored ? facing : facing.getOpposite() ;

					target = pos.offset(facing, 2).offset(f.rotateY(), 4).up();
					te = worldObj.getTileEntity(target);
					ex = (TileEntityExcavator) te;
					
					TileEntityExcavator master = ex.master();
					if (ex == master && ex.getFacing().rotateY() == f)
					{
						BlockPos wheelPos = master.getBlockPosForPos(31);
						TileEntity center = worldObj.getTileEntity(wheelPos);

						if (center instanceof TileEntityBucketWheel)
						{
							TileEntityBucketWheel wheel = (TileEntityBucketWheel) center;
							
							if (wheel.active)
							{
								wheel.rotation += IEConfig.Machines.excavator_speed / 4F;
								
								if (!worldObj.isRemote)
								{
									count++;
									if (count % 4 == 0)
									{
										tank.drainInternal(LubricantHandler.getLubeAmount(tank.getFluid().getFluid()), true);
										master.update();
										markContainingBlockForUpdate(null);
			
										count = 0;
									}
								}
								else
								{
									countClient++;
									if (countClient % 50 == 0)
									{
										countClient = worldObj.rand.nextInt(40);
										
										float location = worldObj.rand.nextFloat();
										
										boolean flip = f.getAxis() == Axis.Z ^ facing.getAxisDirection() == AxisDirection.POSITIVE ^ !master.mirrored;
										float xO = 1.2F;
										float zO = -.5F;
										float yO = .5F;
						
										
										if (location > .5F)
										{
											xO = 0.9F;
											yO = 0.8F;
											zO = 1.75F;

										}
										
										if (facing.getAxisDirection() == AxisDirection.NEGATIVE) xO = -xO + 1;
										if (!flip) zO = -zO + 1;

										
										float x = pos.getX() + (f.getAxis() == Axis.X ? xO : zO);
										float y = pos.getY() + yO;
										float z = pos.getZ() + (f.getAxis() == Axis.X ? zO : xO);
										
										for (int i = 0; i < 3; i++)
										{
							
											float r1 = (worldObj.rand.nextFloat() - .5F) * 2F;
											float r2 = (worldObj.rand.nextFloat() - .5F) * 2F;
											float r3 = worldObj.rand.nextFloat();
											int n = Block.getStateId(IPContent.blockFluidLubricant.getDefaultState());
											worldObj.spawnParticle(EnumParticleTypes.BLOCK_DUST, x, y, z, r1 * 0.04F, r3 * 0.0125F, r2 * 0.025F, new int[] {n});
										}
									}
								}
							}

						}
					}
				}
			}
			
			if (lastTank != this.tank.getFluidAmount())
			{
				markContainingBlockForUpdate(null);
				lastTank = this.tank.getFluidAmount();
				
				
			}
		}
		
		
	
	}
	
	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		TileEntity master = worldObj.getTileEntity(getPos().add(0,-dummy,0));
		if (master != null && master instanceof TileEntityAutoLubricator)
		{
			FluidStack f = FluidUtil.getFluidContained(heldItem);
			if (FluidUtil.interactWithFluidHandler(heldItem, ((TileEntityAutoLubricator) master).tank, player))
			{
				((TileEntityAutoLubricator) master).markContainingBlockForUpdate(null);
				return true;
			}
		}
		return false;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		BlockPos nullPos = this.getPos();
		return new AxisAlignedBB(nullPos.offset(facing, -5).offset(facing.rotateY(), -5).down(1), nullPos.offset(facing, 5).offset(facing.rotateY(), 5).up(3));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return super.getMaxRenderDistanceSquared()* IEConfig.increasedTileRenderdistance;
	}


	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer)
	{
		if (Utils.isFluidRelatedItemStack(player.getHeldItem(EnumHand.MAIN_HAND)))
		{
			TileEntity master = worldObj.getTileEntity(getPos().add(0,-dummy,0));
			if (master != null && master instanceof TileEntityAutoLubricator)
			{
				TileEntityAutoLubricator lube = (TileEntityAutoLubricator) master;
				String s = null;
				if (lube.tank.getFluid() != null)
					s = lube.tank.getFluid().getLocalizedName() + ": " +lube.tank.getFluidAmount() + "mB";
				else
					s = I18n.format(Lib.GUI + "empty");
				return new String[] {s};
			}
		}
		return null;
	}

	@Override
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop)
	{
		return false;
	}
}