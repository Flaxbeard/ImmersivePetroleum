package flaxbeard.immersivepetroleum.common.blocks.metal;

import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.common.Config.IPConfig;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.MultiblockPumpjack;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class TileEntityPumpjack extends TileEntityMultiblockMetal<TileEntityPumpjack, IMultiblockRecipe> implements IAdvancedSelectionBounds, IAdvancedCollisionBounds, IGuiTile
{
	public static class TileEntityPumpjackParent extends TileEntityPumpjack
	{
		@SideOnly(Side.CLIENT)
		@Override
		public AxisAlignedBB getRenderBoundingBox()
		{
			BlockPos nullPos = this.getPos();
			return new AxisAlignedBB(nullPos.offset(facing, -2).offset(mirrored ? facing.rotateYCCW() : facing.rotateY(), -1).down(1), nullPos.offset(facing, 5).offset(mirrored ? facing.rotateYCCW() : facing.rotateY(), 2).up(3));
		}

		@Override
		public boolean isDummy()
		{
			return false;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public double getMaxRenderDistanceSquared()
		{
			return super.getMaxRenderDistanceSquared() * IEConfig.increasedTileRenderdistance;
		}
	}

	public TileEntityPumpjack()
	{
		super(MultiblockPumpjack.instance, new int[]{4, 6, 3}, 16000, true);
	}

	public FluidTank fakeTank = new FluidTank(0);

	public boolean wasActive = false;
	public float activeTicks = 0;
	private int pipeTicks = 0;
	private boolean lastHadPipes = true;
	public IBlockState state = null;

	public boolean canExtract()
	{
		return true;
	}

	public int availableOil()
	{
		return PumpjackHandler.getFluidAmount(world, getPos().getX() >> 4, getPos().getZ() >> 4);
	}

	public Fluid availableFluid()
	{
		return PumpjackHandler.getFluid(world, getPos().getX() >> 4, getPos().getZ() >> 4);
	}

	public int getResidualOil()
	{
		return PumpjackHandler.getResidualFluid(world, getPos().getX() >> 4, getPos().getZ() >> 4);
	}

	public void extractOil(int amount)
	{
		PumpjackHandler.depleteFluid(world, getPos().getX() >> 4, getPos().getZ() >> 4, amount);
	}

	private boolean hasPipes()
	{
		if (!IPConfig.Extraction.req_pipes) return true;
		BlockPos basePos = getPos().offset(this.getFacing(), 4);
		for (int y = basePos.getY() - 2; y > 0; y--)
		{
			BlockPos pos = new BlockPos(basePos.getX(), y, basePos.getZ());
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock() == Blocks.BEDROCK) return true;
			if (!Utils.isBlockAt(world, pos, IEContent.blockMetalDevice1, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta()))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		boolean lastActive = wasActive;
		this.wasActive = nbt.getBoolean("wasActive");
		this.lastHadPipes = nbt.getBoolean("lastHadPipes");
		if (!wasActive && lastActive)
		{
			this.activeTicks++;
		}
		state = null;
		if (nbt.hasKey("comp"))
		{
			ItemStack stack = new ItemStack(nbt.getCompoundTag("comp"));

			if (!stack.isEmpty())
			{
				Block block = Block.getBlockFromItem(stack.getItem());
				state = block.getDefaultState();
			}
		}
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setBoolean("wasActive", wasActive);
		nbt.setBoolean("lastHadPipes", lastHadPipes);
		if (availableFluid() != null)
		{
			if (availableFluid().getBlock() != null)
			{
				ItemStack stack = new ItemStack(availableFluid().getBlock());
				NBTTagCompound comp = new NBTTagCompound();
				stack.writeToNBT(comp);
				nbt.setTag("comp", comp);
			}
		}
	}

	@Override
	public void update()
	{
		update(true);
	}

	public void update(boolean consumePower)
	{
		//System.out.println("TEST");
		super.update();
		if (world.isRemote || isDummy())
		{
			if (world.isRemote && !isDummy() && state != null && wasActive)
			{
				BlockPos particlePos = this.getPos().offset(facing, 4);
				float r1 = (world.rand.nextFloat() - .5F) * 2F;
				float r2 = (world.rand.nextFloat() - .5F) * 2F;

				world.spawnParticle(EnumParticleTypes.BLOCK_DUST, particlePos.getX() + 0.5F, particlePos.getY(), particlePos.getZ() + 0.5F, r1 * 0.04F, 0.25F, r2 * 0.025F, new int[]{Block.getStateId(state)});
			}
			if (wasActive && consumePower)
			{
				activeTicks++;
			}
			return;
		}

		boolean active = false;

		int consumed = IPConfig.Extraction.pumpjack_consumption;
		int extracted = consumePower ? energyStorage.extractEnergy(consumed, true) : consumed;

		if (extracted >= consumed && canExtract() && !this.isRSDisabled())
		{
			if ((getPos().getX() + getPos().getZ()) % IPConfig.Extraction.pipe_check_ticks == pipeTicks)
			{
				lastHadPipes = hasPipes();
			}
			if (lastHadPipes)
			{
				int residual = getResidualOil();
				if (availableOil() > 0 || residual > 0)
				{
					int oilAmnt = availableOil() <= 0 ? residual : availableOil();

					energyStorage.extractEnergy(consumed, false);
					active = true;
					FluidStack out = new FluidStack(availableFluid(), Math.min(IPConfig.Extraction.pumpjack_speed, oilAmnt));
					BlockPos outputPos = this.getPos().offset(facing, 2).offset(facing.rotateY().getOpposite(), 2).offset(EnumFacing.DOWN, 1);
					IFluidHandler output = FluidUtil.getFluidHandler(world, outputPos, facing.rotateY());
					if (output != null)
					{
						int accepted = output.fill(out, false);
						if (accepted > 0)
						{
							int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
							extractOil(drained);
							out = Utils.copyFluidStackWithAmount(out, out.amount - drained, false);
						}
					}


					outputPos = this.getPos().offset(facing, 2).offset(facing.rotateY(), 2).offset(EnumFacing.DOWN, 1);
					output = FluidUtil.getFluidHandler(world, outputPos, facing.rotateYCCW());
					if (output != null)
					{
						int accepted = output.fill(out, false);
						if (accepted > 0)
						{
							int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
							extractOil(drained);

						}
					}


					activeTicks++;
				}
			}
			pipeTicks = (pipeTicks + 1) % IPConfig.Extraction.pipe_check_ticks;
		}

		if (active != wasActive)
		{
			this.markDirty();
			this.markContainingBlockForUpdate(null);
		}

		wasActive = active;

	}

	@Override
	public float[] getBlockBounds()
	{
		/*if(pos==19)
			return new float[]{facing==EnumFacing.WEST?.5f:0,0,facing==EnumFacing.NORTH?.5f:0, facing==EnumFacing.EAST?.5f:1,1,facing==EnumFacing.SOUTH?.5f:1};
		if(pos==17)
			return new float[]{.0625f,0,.0625f, .9375f,1,.9375f};*/

		return new float[]{0, 0, 0, 0, 0, 0};
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		if (mirrored)
			fw = fw.getOpposite();

		int y = pos / 16;
		int x = (pos % 16) / 4;
		int z = pos % 4;
		if (pos == 0)
		{
			List list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			float minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 2F / 16F : ((fl == EnumFacing.WEST) ? 10F / 16F : 2F / 16F);
			float maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 4F / 16F : ((fl == EnumFacing.WEST) ? 14F / 16F : 6F / 16F);
			float minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 2F / 16F : ((fl == EnumFacing.NORTH) ? 10F / 16F : 2F / 16F);
			float maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 4F / 16F : ((fl == EnumFacing.NORTH) ? 14F / 16F : 6F / 16F);
			list.add(new AxisAlignedBB(minX, 0.5, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 12F / 16F : minX;
			maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 14F / 16F : maxX;
			minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 12F / 16F : minZ;
			maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 14F / 16F : maxZ;
			list.add(new AxisAlignedBB(minX, 0.5, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		else if (pos == 18)
		{
			float minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0F : ((fl == EnumFacing.WEST) ? .5F : 0F);
			float maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 1F : ((fl == EnumFacing.WEST) ? 1F : .5F);
			float minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 0F : ((fl == EnumFacing.NORTH) ? .5F : 0F);
			float maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 1F : ((fl == EnumFacing.NORTH) ? 1F : .5F);
			return Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		else if ((pos >= 3 && pos <= 14 && pos != 10 && pos != 13 && pos != 11 && pos != 9) || pos == 1)
		{
			return Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		else if (pos == 13)
		{
			float minX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 0F : .3125F;
			float maxX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 1F : .685F;
			float minZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0F : .3125F;
			float maxZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 1F : .685F;
			List list = Lists.newArrayList(new AxisAlignedBB(minX, 0.5, minZ, maxX, .5 + 3 / 8F, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			list.add(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;

		}
		else if (pos == 10)
		{
			float minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? .3125F : ((fl == EnumFacing.EAST) ? 11F / 16F : 0F / 16F);
			float maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? .685F : ((fl == EnumFacing.EAST) ? 16F / 16F : 5F / 16F);
			float minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? .3125F : ((fl == EnumFacing.SOUTH) ? 11F / 16F : 0F / 16F);
			float maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? .685F : ((fl == EnumFacing.SOUTH) ? 16F / 16F : 5F / 16F);
			List list = Lists.newArrayList(new AxisAlignedBB(minX, 0.5, minZ, maxX, .5 + 3 / 8F, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0F : 5F / 16F;
			maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 1F : 11F / 16F;
			minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 0F : 5F / 16F;
			maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 1F : 11F / 16F;
			list.add(new AxisAlignedBB(minX, 0.5, minZ, maxX, .5 + 3 / 8F, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			list.add(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;

		}
		else if (pos == 16)
		{
			return Lists.newArrayList(new AxisAlignedBB(3 / 16F, 0, 3 / 16F, 13 / 16F, 1, 13 / 16F).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		else if (pos == 22)
		{
			float minX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 0F : .25F;
			float maxX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 1F : .75F;
			float minZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0F : .25F;
			float maxZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 1F : .75F;
			return Lists.newArrayList(new AxisAlignedBB(minX, -0.75, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		else if (pos == 40)
		{
			float minX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 0F : .25F;
			float maxX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 1F : .75F;
			float minZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0F : .25F;
			float maxZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 1F : .75F;
			return Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 0.25, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		else if (pos == 27)
		{
			fl = this.mirrored ? facing.getOpposite() : facing;
			float minX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 0.7F : ((fl == EnumFacing.NORTH) ? 0.6F : -.1F);
			float maxX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 1.4F : ((fl == EnumFacing.NORTH) ? 1.1F : 0.4F);
			float minZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0.7F : ((fl == EnumFacing.EAST) ? .6F : -.1F);
			float maxZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 1.4F : ((fl == EnumFacing.EAST) ? 1.1F : 0.4F);
			List list = Lists.newArrayList(new AxisAlignedBB(minX, -0.5F, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? -.4F : ((fl == EnumFacing.NORTH) ? 0.6F : -.1F);
			maxX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? .3F : ((fl == EnumFacing.NORTH) ? 1.1F : 0.4F);
			minZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? -.4F : ((fl == EnumFacing.EAST) ? .6F : -.1F);
			maxZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? .3F : ((fl == EnumFacing.EAST) ? 1.1F : 0.4F);
			list.add(new AxisAlignedBB(minX, -0.5F, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		else if (pos == 29)
		{
			fl = this.mirrored ? facing.getOpposite() : facing;
			float minX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 0.7F : ((fl == EnumFacing.SOUTH) ? 0.6F : -.1F);
			float maxX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 1.4F : ((fl == EnumFacing.SOUTH) ? 1.1F : 0.4F);
			float minZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0.7F : ((fl == EnumFacing.WEST) ? .6F : -.1F);
			float maxZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 1.4F : ((fl == EnumFacing.WEST) ? 1.1F : 0.4F);
			List list = Lists.newArrayList(new AxisAlignedBB(minX, -0.5F, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? -.4F : ((fl == EnumFacing.SOUTH) ? 0.6F : -.1F);
			maxX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? .3F : ((fl == EnumFacing.SOUTH) ? 1.1F : 0.4F);
			minZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? -.4F : ((fl == EnumFacing.WEST) ? .6F : -.1F);
			maxZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? .3F : ((fl == EnumFacing.WEST) ? 1.1F : 0.4F);
			list.add(new AxisAlignedBB(minX, -0.5F, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		else if (pos == 45)
		{
			fl = this.mirrored ? facing.getOpposite() : facing;
			float minX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 0F : ((fl == EnumFacing.NORTH) ? 0.8F : -0.2F);
			float maxX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 1F : ((fl == EnumFacing.NORTH) ? 1.2F : 0.2F);
			float minZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0F : ((fl == EnumFacing.EAST) ? 0.8F : -0.2F);
			float maxZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 1 : ((fl == EnumFacing.EAST) ? 1.2F : 0.2F);
			return Lists.newArrayList(new AxisAlignedBB(minX, 0F, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		else if (pos == 47)
		{
			fl = this.mirrored ? facing.getOpposite() : facing;
			float minX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 0F : ((fl == EnumFacing.NORTH) ? -0.2F : 0.8F);
			float maxX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 1F : ((fl == EnumFacing.NORTH) ? 0.2F : 1.2F);
			float minZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0F : ((fl == EnumFacing.EAST) ? -0.2F : 0.8F);
			float maxZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 1 : ((fl == EnumFacing.EAST) ? 0.2F : 1.2F);
			return Lists.newArrayList(new AxisAlignedBB(minX, 0F, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		else if (pos == 63 || pos == 65)
		{
			return new ArrayList();
		}
		else if (pos == 58 || pos == 61 || pos == 64 || pos == 67)
		{
			float minX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 0F : 0.25F;
			float maxX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 1F : 0.75F;
			float minZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0F : 0.25F;
			float maxZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 1 : 0.75F;
			return Lists.newArrayList(new AxisAlignedBB(minX, -.25F, minZ, maxX, 0.75F, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		else if (pos == 70)
		{
			float minX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 0F : 0.125F;
			float maxX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 1F : 0.875F;
			float minZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0F : 0.125F;
			float maxZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 1 : 0.875F;
			return Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1.25F, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		else if (pos == 52)
		{
			float minX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 0F : 0.125F;
			float maxX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 1F : 0.875F;
			float minZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0F : 0.125F;
			float maxZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 1 : 0.875F;
			return Lists.newArrayList(new AxisAlignedBB(minX, .25F, minZ, maxX, 1F, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}

		List list = new ArrayList<AxisAlignedBB>();
		list.add(new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		return list;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list)
	{
		return false;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		List list = new ArrayList<AxisAlignedBB>();
		return getAdvancedSelectionBounds();
	}

	@Override
	public int[] getEnergyPos()
	{
		return new int[]{20};
	}

	@Override
	public int[] getRedstonePos()
	{
		return new int[]{18};
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<IMultiblockRecipe> process)
	{
	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 1;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 1;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<IMultiblockRecipe> process)
	{
		return 0;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return true;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public int[] getOutputSlots()
	{
		return null;
	}

	@Override
	public int[] getOutputTanks()
	{
		return new int[]{1};
	}


	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}

	@Override
	public IMultiblockRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected IMultiblockRecipe readRecipeFromNBT(NBTTagCompound tag)
	{
		return null;
	}

	@Override
	public boolean canOpenGui()
	{
		return true;
	}

	@Override
	public int getGuiID()
	{
		return 0;
	}

	@Override
	public TileEntity getGuiMaster()
	{
		return master();
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return null;
	}

	@Override
	public IFluidTank[] getInternalTanks()
	{
		return null;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<IMultiblockRecipe> process)
	{
		return false;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		TileEntityPumpjack master = this.master();
		if (master != null)
		{
			if (pos == 9 && (side == null || side == facing.rotateY() || side == facing.getOpposite().rotateY()))
			{
				return new FluidTank[]{fakeTank};
			}
			else if (pos == 11 && (side == null || side == facing.rotateY() || side == facing.getOpposite().rotateY()))
			{
				return new FluidTank[]{fakeTank};
			}
			else if (pos == 16 && IPConfig.Extraction.req_pipes && (side == null || side == EnumFacing.DOWN))
			{
				return new FluidTank[]{fakeTank};
			}
		}
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return false;
	}

	@Override
	public boolean isDummy()
	{
		return true;
	}

	@Override
	public TileEntityPumpjack master()
	{
		if (offset[0] == 0 && offset[1] == 0 && offset[2] == 0)
		{
			return this;
		}
		TileEntity te = world.getTileEntity(getPos().add(-offset[0], -offset[1], -offset[2]));
		return this.getClass().isInstance(te) ? (TileEntityPumpjack) te : null;
	}

	@Override
	public TileEntityPumpjack getTileForPos(int targetPos)
	{
		BlockPos target = getBlockPosForPos(targetPos);
		TileEntity tile = world.getTileEntity(target);
		return tile instanceof TileEntityPumpjack ? (TileEntityPumpjack) tile : null;
	}
}