package flaxbeard.immersivepetroleum.common.blocks.metal;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBucketWheel;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal.MultiblockProcess;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.client.model.ModelLubricantPipes;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
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

import java.util.Iterator;

public class TileEntityAutoLubricator extends TileEntityIEBase implements IDirectionalTile, IHasDummyBlocks, ITickable, IPlayerInteraction, IBlockOverlayText, IBlockBounds, ITileDrop
{

	public static class PumpjackLubricationHandler implements ILubricationHandler<TileEntityPumpjack>
	{

		@Override
		public TileEntity isPlacedCorrectly(World world, TileEntityAutoLubricator tile, EnumFacing facing)
		{
			BlockPos target = tile.getPos().offset(facing, 2).up();
			TileEntity te = world.getTileEntity(target);

			if (te instanceof TileEntityPumpjack)
			{
				TileEntityPumpjack jack = (TileEntityPumpjack) te;
				TileEntityPumpjack master = jack.master();

				EnumFacing f = master.mirrored ? facing : facing.getOpposite();
				if (jack == master && jack.getFacing().rotateY() == f)
				{
					return master;
				}
			}

			return null;
		}

		@Override
		public boolean isMachineEnabled(World world, TileEntityPumpjack master)
		{
			return master.wasActive;
		}

		@Override
		public void lubricate(World world, int ticks, TileEntityPumpjack master)
		{
			if (!world.isRemote)
			{
				if (ticks % 4 == 0)
				{
					master.update(true);
				}
			}
			else
			{
				master.activeTicks += 1F / 4F;
			}
		}

		@Override
		public void spawnLubricantParticles(World world, TileEntityAutoLubricator tile, EnumFacing facing, TileEntityPumpjack master)
		{
			EnumFacing f = master.mirrored ? facing : facing.getOpposite();
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
				world.spawnParticle(EnumParticleTypes.BLOCK_DUST, x, y, z, r1 * 0.04F, r3 * 0.0125F, r2 * 0.025F, new int[]{n});
			}
		}

		private static Object pumpjackM;
		private static Object pumpjack;

		@Override
		@SideOnly(Side.CLIENT)
		public void renderPipes(World world, TileEntityAutoLubricator tile, EnumFacing facing, TileEntityPumpjack master)
		{
			if (pumpjackM == null)
			{
				pumpjackM = new ModelLubricantPipes.Pumpjack(true);
				pumpjack = new ModelLubricantPipes.Pumpjack(false);
			}


			GlStateManager.translate(0, -1, 0);
			Vec3i offset = master.getPos().subtract(tile.getPos());
			GlStateManager.translate(offset.getX(), offset.getY(), offset.getZ());

			EnumFacing rotation = master.facing;
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
			if (master.mirrored)
			{
				((ModelLubricantPipes.Pumpjack) pumpjackM).render(null, 0, 0, 0, 0, 0, 0.0625F);
			}
			else
			{
				((ModelLubricantPipes.Pumpjack) pumpjack).render(null, 0, 0, 0, 0, 0, 0.0625F);
			}
		}


		@Override
		public Tuple<BlockPos, EnumFacing> getGhostBlockPosition(World world, TileEntityPumpjack tile)
		{
			if (!tile.isDummy())
			{
				BlockPos pos = tile.getPos().offset(tile.mirrored ? tile.facing.rotateYCCW() : tile.facing.rotateY(), 2);
				EnumFacing f = tile.mirrored ? tile.facing.rotateY() : tile.facing.rotateYCCW();
				return new Tuple(pos, f);
			}
			return null;
		}

		@Override
		public int[] getStructureDimensions()
		{
			return new int[]{4, 6, 3};
		}

	}

	public static class ExcavatorLubricationHandler implements ILubricationHandler<TileEntityExcavator>
	{

		@Override
		public TileEntity isPlacedCorrectly(World world, TileEntityAutoLubricator tile, EnumFacing facing)
		{
			BlockPos initialTarget = tile.getPos().offset(facing);
			TileEntityExcavator adjacent = (TileEntityExcavator) world.getTileEntity(initialTarget);
			EnumFacing f = adjacent.mirrored ? facing : facing.getOpposite();

			BlockPos target = tile.getPos().offset(facing, 2).offset(f.rotateY(), 4).up();
			TileEntity te = world.getTileEntity(target);

			if (te instanceof TileEntityExcavator)
			{
				TileEntityExcavator excavator = (TileEntityExcavator) te;
				TileEntityExcavator master = excavator.master();

				if (excavator == master && excavator.getFacing().rotateY() == f)
				{
					return master;
				}
			}

			return null;
		}

		@Override
		public boolean isMachineEnabled(World world, TileEntityExcavator master)
		{
			BlockPos wheelPos = master.getBlockPosForPos(31);
			TileEntity center = world.getTileEntity(wheelPos);

			if (center instanceof TileEntityBucketWheel)
			{
				TileEntityBucketWheel wheel = (TileEntityBucketWheel) center;

				return wheel.active;
			}
			return false;
		}

		@Override
		public void lubricate(World world, int ticks, TileEntityExcavator master)
		{
			BlockPos wheelPos = master.getBlockPosForPos(31);
			TileEntity center = world.getTileEntity(wheelPos);

			if (center instanceof TileEntityBucketWheel)
			{
				TileEntityBucketWheel wheel = (TileEntityBucketWheel) center;

				if (!world.isRemote && ticks % 4 == 0)
				{
					int consumed = IEConfig.Machines.excavator_consumption;
					int extracted = master.energyStorage.extractEnergy(consumed, true);
					if (extracted >= consumed)
					{
						master.energyStorage.extractEnergy(extracted, false);
						wheel.rotation += IEConfig.Machines.excavator_speed / 4F;
					}
				}
				else
				{
					wheel.rotation += IEConfig.Machines.excavator_speed / 4F;
				}

			}
		}

		@Override
		public void spawnLubricantParticles(World world, TileEntityAutoLubricator tile, EnumFacing facing, TileEntityExcavator master)
		{
			EnumFacing f = master.mirrored ? facing : facing.getOpposite();

			float location = world.rand.nextFloat();

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

			float x = tile.pos.getX() + (f.getAxis() == Axis.X ? xO : zO);
			float y = tile.pos.getY() + yO;
			float z = tile.pos.getZ() + (f.getAxis() == Axis.X ? zO : xO);

			for (int i = 0; i < 3; i++)
			{
				float r1 = (world.rand.nextFloat() - .5F) * 2F;
				float r2 = (world.rand.nextFloat() - .5F) * 2F;
				float r3 = world.rand.nextFloat();
				int n = Block.getStateId(IPContent.blockFluidLubricant.getDefaultState());
				world.spawnParticle(EnumParticleTypes.BLOCK_DUST, x, y, z, r1 * 0.04F, r3 * 0.0125F, r2 * 0.025F, new int[]{n});
			}
		}

		private static Object excavator;
		private static Object excavatorM;

		@Override
		@SideOnly(Side.CLIENT)
		public void renderPipes(World world, TileEntityAutoLubricator tile, EnumFacing facing, TileEntityExcavator master)
		{
			if (excavator == null)
			{
				excavatorM = new ModelLubricantPipes.Excavator(true);
				excavator = new ModelLubricantPipes.Excavator(false);
			}


			GlStateManager.translate(0, -1, 0);
			Vec3i offset = master.getPos().subtract(tile.getPos());
			GlStateManager.translate(offset.getX(), offset.getY(), offset.getZ());

			EnumFacing rotation = master.facing;
			if (rotation == EnumFacing.NORTH)
			{
				GlStateManager.rotate(90F, 0, 1, 0);

			}
			else if (rotation == EnumFacing.WEST)
			{
				GlStateManager.rotate(180F, 0, 1, 0);
				GlStateManager.translate(0, 0, -1);

			}
			else if (rotation == EnumFacing.SOUTH)
			{
				GlStateManager.rotate(270F, 0, 1, 0);
				GlStateManager.translate(1, 0, -1);
			}
			else if (rotation == EnumFacing.EAST)
			{
				GlStateManager.translate(1, 0, 0);

			}

			GlStateManager.translate(-1, 0, -1);
			ClientUtils.bindTexture("immersivepetroleum:textures/blocks/lube_pipe12.png");
			if (master.mirrored)
			{
				((ModelLubricantPipes.Excavator) excavatorM).render(null, 0, 0, 0, 0, 0, 0.0625F);
			}
			else
			{
				((ModelLubricantPipes.Excavator) excavator).render(null, 0, 0, 0, 0, 0, 0.0625F);
			}
		}


		@Override
		public Tuple<BlockPos, EnumFacing> getGhostBlockPosition(World world, TileEntityExcavator tile)
		{
			if (!tile.isDummy())
			{
				BlockPos pos = tile.getPos().offset(tile.facing, 4).offset(tile.mirrored ? tile.facing.rotateYCCW() : tile.facing.rotateY(), 2);
				EnumFacing f = tile.mirrored ? tile.facing.rotateY() : tile.facing.rotateYCCW();
				return new Tuple(pos, f);
			}
			return null;
		}

		@Override
		public int[] getStructureDimensions()
		{
			return new int[]{3, 6, 3};
		}
	}

	public static class CrusherLubricationHandler implements ILubricationHandler<TileEntityCrusher>
	{

		@Override
		public TileEntity isPlacedCorrectly(World world, TileEntityAutoLubricator tile, EnumFacing facing)
		{

			BlockPos target = tile.getPos().offset(facing, 2).up();
			TileEntity te = world.getTileEntity(target);

			if (te instanceof TileEntityCrusher)
			{
				TileEntityCrusher excavator = (TileEntityCrusher) te;
				TileEntityCrusher master = excavator.master();

				EnumFacing f = facing;
				if (excavator == master && excavator.getFacing().getOpposite() == f)
				{

					return master;
				}
			}

			return null;
		}

		@Override
		public boolean isMachineEnabled(World world, TileEntityCrusher master)
		{
			return master.shouldRenderAsActive();
		}

		@Override
		public void lubricate(World world, int ticks, TileEntityCrusher master)
		{
			Iterator<MultiblockProcess<CrusherRecipe>> processIterator = master.processQueue.iterator();
			MultiblockProcess<CrusherRecipe> process = processIterator.next();

			if (!world.isRemote)
			{
				if (ticks % 4 == 0)
				{
					int consume = master.energyStorage.extractEnergy(process.energyPerTick, true);
					if (consume >= process.energyPerTick)
					{
						master.energyStorage.extractEnergy(process.energyPerTick, false);
						if (process.processTick < process.maxTicks) process.processTick++;
						if (process.processTick >= process.maxTicks && master.processQueue.size() > 1)
						{
							process = processIterator.next();
							if (process.processTick < process.maxTicks) process.processTick++;
						}
					}
				}
			}
			else
			{
				master.animation_barrelRotation += 18f / 4f;
				master.animation_barrelRotation %= 360f;
			}
		}

		@Override
		public void spawnLubricantParticles(World world, TileEntityAutoLubricator tile, EnumFacing facing, TileEntityCrusher master)
		{

			EnumFacing f = master.mirrored ? facing : facing.getOpposite();

			float location = world.rand.nextFloat();

			boolean flip = f.getAxis() == Axis.Z ^ facing.getAxisDirection() == AxisDirection.POSITIVE ^ !master.mirrored;
			float xO = 2.5F;
			float zO = -0.1F;
			float yO = 1.3F;

			if (location > .5F)
			{
				xO = 1.0F;
				yO = 3.0F;
				zO = 1.65F;
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
				world.spawnParticle(EnumParticleTypes.BLOCK_DUST, x, y, z, r1 * 0.04F, r3 * 0.0125F, r2 * 0.025F, new int[]{n});
			}
		}

		private static Object excavator;

		@Override
		@SideOnly(Side.CLIENT)
		public void renderPipes(World world, TileEntityAutoLubricator tile, EnumFacing facing, TileEntityCrusher master)
		{
			if (excavator == null)
			{
				excavator = new ModelLubricantPipes.Crusher(false);
			}


			GlStateManager.translate(0, -1, 0);
			Vec3i offset = master.getPos().subtract(tile.getPos());
			GlStateManager.translate(offset.getX(), offset.getY(), offset.getZ());

			EnumFacing rotation = master.facing;
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

			ClientUtils.bindTexture("immersivepetroleum:textures/blocks/lube_pipe12.png");
			((ModelLubricantPipes.Crusher) excavator).render(null, 0, 0, 0, 0, 0, 0.0625F);
		}

		@Override
		public Tuple<BlockPos, EnumFacing> getGhostBlockPosition(World world, TileEntityCrusher tile)
		{
			if (!tile.isDummy())
			{
				BlockPos pos = tile.getPos().offset(tile.facing, 2);
				EnumFacing f = tile.getFacing().getOpposite();
				return new Tuple(pos, f);
			}
			return null;
		}

		@Override
		public int[] getStructureDimensions()
		{
			return new int[]{3, 3, 5};
		}
	}

	public boolean active;
	public int dummy = 0;
	public EnumFacing facing = EnumFacing.NORTH;
	public FluidTank tank = new FluidTank(8000)
	{
		@Override
		public boolean canFillFluidType(FluidStack fluid)
		{
			return fluid != null && LubricantHandler.isValidLube(fluid.getFluid());
		}
	};

	public boolean predictablyDraining = false;

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
		return dummy > 0;
	}

	@Override
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		world.setBlockState(pos.add(0, 1, 0), state);
		((TileEntityAutoLubricator) world.getTileEntity(pos.add(0, 1, 0))).dummy = 1;
		((TileEntityAutoLubricator) world.getTileEntity(pos.add(0, 1, 0))).facing = this.facing;

	}

	@Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		for (int i = 0; i <= 1; i++)
		{
			if (world.getTileEntity(getPos().add(0, -dummy, 0).add(0, i, 0)) instanceof TileEntityAutoLubricator)
				world.setBlockToAir(getPos().add(0, -dummy, 0).add(0, i, 0));
		}
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		dummy = nbt.getInteger("dummy");
		facing = EnumFacing.byIndex(nbt.getInteger("facing"));
		if (facing == EnumFacing.DOWN || facing == EnumFacing.UP)
		{
			facing = EnumFacing.NORTH;
		}
		active = nbt.getBoolean("active");
		tank.readFromNBT(nbt.getCompoundTag("tank"));
		count = nbt.getInteger("count");
		predictablyDraining = nbt.getBoolean("predictablyDraining");

		if (descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("dummy", dummy);
		nbt.setInteger("facing", facing.ordinal());
		nbt.setBoolean("active", active);
		nbt.setInteger("count", count);
		nbt.setBoolean("predictablyDraining", predictablyDraining);

		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		nbt.setTag("tank", tankTag);
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		if (facing == EnumFacing.DOWN || facing == EnumFacing.UP)
		{
			facing = EnumFacing.NORTH;
		}
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
	public void afterRotation(EnumFacing oldDir, EnumFacing newDir)
	{
		for (int i = 0; i <= 1; i++)
		{
			TileEntity te = world.getTileEntity(getPos().add(0, -dummy + i, 0));
			if (te instanceof TileEntityAutoLubricator)
			{
				((TileEntityAutoLubricator) te).setFacing(newDir);
				te.markDirty();
				((TileEntityAutoLubricator) te).markContainingBlockForUpdate(null);
			}
		}
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && dummy == 1 && (facing == null || facing == EnumFacing.UP))
		{
			TileEntity te = world.getTileEntity(getPos().add(0, -dummy, 0));
			if (te instanceof TileEntityAutoLubricator)
			{
				return (T) ((TileEntityAutoLubricator) te).tank;
			}
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if (dummy == 1 && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
		{
			TileEntity te = world.getTileEntity(getPos().add(0, -dummy, 0));
			if (te instanceof TileEntityAutoLubricator)
			{
				return (facing == null || facing == EnumFacing.UP);
			}
		}
		return super.hasCapability(capability, facing);
	}

	int count = 0;
	int lastTank = 0;
	int lastTankUpdate = 0;
	int countClient = 0;

	@Override
	public void update()
	{
		if (dummy == 0)
		{
			if (tank.getFluid() != null && tank.getFluid().getFluid() != null && tank.getFluidAmount() >= LubricantHandler.getLubeAmount(tank.getFluid().getFluid()) && LubricantHandler.isValidLube(tank.getFluid().getFluid()))
			{
				BlockPos target = pos.offset(facing);
				TileEntity te = world.getTileEntity(target);

				ILubricationHandler handler = LubricatedHandler.getHandlerForTile(te);
				if (handler != null)
				{
					TileEntity master = handler.isPlacedCorrectly(world, this, facing);
					if (master != null)
					{
						if (handler.isMachineEnabled(world, master))
						{
							count++;
							handler.lubricate(world, count, master);

							if (!world.isRemote && count % 4 == 0)
							{
								tank.drainInternal(LubricantHandler.getLubeAmount(tank.getFluid().getFluid()), true);
							}

							if (world.isRemote)
							{
								countClient++;
								if (countClient % 50 == 0)
								{
									countClient = world.rand.nextInt(40);
									handler.spawnLubricantParticles(world, this, facing, master);
								}
							}
						}
					}
				}

			}

			if (!world.isRemote && lastTank != this.tank.getFluidAmount())
			{
				if (predictablyDraining && tank.getFluid() != null && lastTank - this.tank.getFluidAmount() == LubricantHandler.getLubeAmount(tank.getFluid().getFluid()))
				{
					lastTank = this.tank.getFluidAmount();
					return;
				}
				if (Math.abs(lastTankUpdate - this.tank.getFluidAmount()) > 25)
				{
					markContainingBlockForUpdate(null);
					predictablyDraining = tank.getFluid() != null && lastTank - this.tank.getFluidAmount() == LubricantHandler.getLubeAmount(tank.getFluid().getFluid());
					lastTankUpdate = this.tank.getFluidAmount();
				}
				lastTank = this.tank.getFluidAmount();
			}
		}


	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		TileEntity master = world.getTileEntity(getPos().add(0, -dummy, 0));
		if (master != null && master instanceof TileEntityAutoLubricator)
		{
			FluidStack f = FluidUtil.getFluidContained(heldItem);
			if (FluidUtil.interactWithFluidHandler(player, hand, ((TileEntityAutoLubricator) master).tank))
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
		return super.getMaxRenderDistanceSquared() * IEConfig.increasedTileRenderdistance;
	}


	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer)
	{
		if (Utils.isFluidRelatedItemStack(player.getHeldItem(EnumHand.MAIN_HAND)))
		{
			TileEntity master = world.getTileEntity(getPos().add(0, -dummy, 0));
			if (master != null && master instanceof TileEntityAutoLubricator)
			{
				TileEntityAutoLubricator lube = (TileEntityAutoLubricator) master;
				String s = null;
				if (lube.tank.getFluid() != null)
					s = lube.tank.getFluid().getLocalizedName() + ": " + lube.tank.getFluidAmount() + "mB";
				else
					s = I18n.format(Lib.GUI + "empty");
				return new String[]{s};
			}
		}
		return null;
	}

	@Override
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop)
	{
		return false;
	}

	@Override
	public float[] getBlockBounds()
	{
		if (dummy == 1)
			return new float[]{.1875F, 0, .1875F, .8125f, 1, .8125f};
		else
			return new float[]{.0625f, 0, .0625f, .9375f, 1, .9375f};
	}


	public void readTank(NBTTagCompound nbt)
	{
		tank.readFromNBT(nbt.getCompoundTag("tank"));
	}

	public void writeTank(NBTTagCompound nbt, boolean toItem)
	{
		boolean write = tank.getFluidAmount() > 0;
		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		if (!toItem || write)
			nbt.setTag("tank", tankTag);
	}

	@Override
	public void readOnPlacement(EntityLivingBase placer, ItemStack stack)
	{
		if (stack.hasTagCompound())
		{
			readTank(stack.getTagCompound());
		}
	}

	@Override
	public ItemStack getTileDrop(EntityPlayer player, IBlockState state)
	{
		ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
		NBTTagCompound tag = new NBTTagCompound();
		writeTank(tag, true);
		if (!tag.isEmpty())
			stack.setTagCompound(tag);
		return stack;
	}

}