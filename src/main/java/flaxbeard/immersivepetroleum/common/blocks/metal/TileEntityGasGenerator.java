package flaxbeard.immersivepetroleum.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorLV;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxConnector;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.api.energy.FuelHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TileEntityGasGenerator extends TileEntityImmersiveConnectable implements IDirectionalTile, ITickable, IPlayerInteraction, IBlockOverlayText, IIEInternalFluxConnector, ITileDrop, IIEInternalFluxHandler, IEBlockInterfaces.ISoundTile
{
	public boolean active;
	public EnumFacing facing = EnumFacing.NORTH;
	public FluidTank tank = new FluidTank(8000)
	{
		@Override
		public boolean canFillFluidType(FluidStack fluid)
		{
			return fluid != null && FuelHandler.isValidFuel(fluid.getFluid());
		}
	};

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = EnumFacing.byIndex(nbt.getInteger("facing"));
		active = nbt.getBoolean("active");
		tank.readFromNBT(nbt.getCompoundTag("tank"));
		energyStorage.readFromNBT(nbt);

		if (descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing.ordinal());
		nbt.setBoolean("active", active);

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
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing == EnumFacing.UP)
		{
			return (T) tank;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
		{
			return facing == EnumFacing.UP;
		}
		return super.hasCapability(capability, facing);
	}

	int lastTank = 0;

	@Override
	public void update()
	{

		if (!world.isRemote && lastTank != this.tank.getFluidAmount())
		{
			markContainingBlockForUpdate(null);
			lastTank = this.tank.getFluidAmount();
		}
		active = false;
		if (!world.isBlockPowered(pos) && this.tank.getFluid() != null)
		{
			Fluid fuel = this.tank.getFluid().getFluid();
			if (FuelHandler.isValidFuel(fuel))
			{
				int amount = FuelHandler.getFuelUsedPerTick(fuel);
				if (this.tank.getFluidAmount() >= amount)
				{
					if (!this.world.isRemote)
					{
						BlockPos outputPos = getPos().offset(facing);
						TileEntity te = Utils.getExistingTileEntity(world, outputPos);
						if (this.energyStorage.receiveEnergy(FuelHandler.getFluxGeneratedPerTick(fuel), false) > 0)
						{
							this.tank.drain(new FluidStack(fuel, amount), true);
						}
					}
					else
					{
						BlockPos outputPos = getPos().offset(facing);
						TileEntity te = Utils.getExistingTileEntity(world, outputPos);
						if (this.energyStorage.receiveEnergy(FuelHandler.getFluxGeneratedPerTick(fuel), true) > 0)
						{
							active = true;
						}
					}
				}
			}
		}

		if (!world.isRemote)
		{
			//				if(Lib.IC2 && !this.inICNet)
			//				{
			//					IC2Helper.loadIC2Tile(this);
			//					this.inICNet = true;
			//				}
			if (energyStorage.getEnergyStored() > 0)
			{
				int temp = this.transferEnergy(energyStorage.getEnergyStored(), true, 0);
				if (temp > 0)
				{
					energyStorage.modifyEnergyStored(-this.transferEnergy(temp, false, 0));
					markDirty();
				}
			}
			currentTickAccepted = 0;
		}
		else if (firstTick)
		{
			Set<Connection> conns = ImmersiveNetHandler.INSTANCE.getConnections(world, pos);
			if (conns != null)
				for (Connection conn : conns)
				{
					if (pos.compareTo(conn.end) < 0 && world.isBlockLoaded(conn.end))
						this.markContainingBlockForUpdate(null);
				}
			firstTick = false;
		}

		if (this.world.isRemote)
		{
			ImmersiveEngineering.proxy.handleTileSound(IESounds.dieselGenerator, this, active, .3f, .5f);
			if (active && world.getTotalWorldTime() % 4 == 0)
			{
				BlockPos exhaust = pos;
				EnumFacing fl = facing;
				EnumFacing fw = facing.rotateY();
				world.spawnParticle(
						world.rand.nextInt(10) == 0 ? EnumParticleTypes.SMOKE_LARGE : EnumParticleTypes.SMOKE_NORMAL,
						exhaust.getX() + .5 + (fl.getXOffset() * 2 / 16F) + (-fw.getXOffset() * .6125f),
						exhaust.getY() + .4,
						exhaust.getZ() + .5 + (fl.getZOffset() * 2 / 16F) + (-fw.getZOffset() * .6125f), 0, 0, 0);
			}
		}

	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		FluidStack f = FluidUtil.getFluidContained(heldItem);
		if (FluidUtil.interactWithFluidHandler(player, hand, tank))
		{
			markContainingBlockForUpdate(null);
			return true;
		}
		else if (player.isSneaking())
		{
			boolean added = false;
			if (player.inventory.getCurrentItem().isEmpty())
			{
				added = true;
				player.inventory.setInventorySlotContents(player.inventory.currentItem, getTileDrop(player, world.getBlockState(pos)));
			}
			else
			{
				added = player.inventory.addItemStackToInventory(getTileDrop(player, world.getBlockState(pos)));
			}
			if (added)
			{
				world.setBlockToAir(pos);
			}
		}
		return false;
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

			String s = null;
			if (tank.getFluid() != null)
				s = tank.getFluid().getLocalizedName() + ": " + tank.getFluidAmount() + "mB";
			else
				s = I18n.format(Lib.GUI + "empty");
			return new String[]{s};

		}
		return null;
	}

	@Override
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop)
	{
		return false;
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
			if (ItemNBTHelper.hasKey(stack, "energyStorage"))
				energyStorage.setEnergy(ItemNBTHelper.getInt(stack, "energyStorage"));
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
		if (energyStorage.getEnergyStored() > 0)
			ItemNBTHelper.setInt(stack, "energyStorage", energyStorage.getEnergyStored());
		return stack;
	}

	@Override
	public Vec3d getRaytraceOffset(IImmersiveConnectable link)
	{
		float xo = facing.getDirectionVec().getX() * .6f + .5f;
		float zo = facing.getDirectionVec().getZ() * .6f + .5f;
		return new Vec3d(xo, .5f, zo);
	}

	@Override
	public Vec3d getConnectionOffset(Connection con)
	{
		float xo = facing.getDirectionVec().getX() * .5f + .5f;
		float zo = facing.getDirectionVec().getZ() * .5f + .5f;
		return new Vec3d(xo, .5f, zo);
	}


	boolean inICNet = false;
	private long lastTransfer = -1;
	public int currentTickAccepted = 0;
	private FluxStorage energyStorage = new FluxStorage(getMaxStorage(), getMaxInput(), 0);

	boolean firstTick = true;

	@Override
	public boolean isEnergyOutput()
	{
		return false;
	}

	@Override
	public int outputEnergy(int amount, boolean simulate, int energyType)
	{
		return 0;
	}

	@Override
	protected boolean canTakeLV()
	{
		return true;
	}

	@Override
	protected boolean canTakeMV()
	{
		return true;
	}


	IEForgeEnergyWrapper energyWrapper;

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing)
	{
		if (facing != this.facing || isRelay())
			return null;
		if (energyWrapper == null || energyWrapper.side != this.facing)
			energyWrapper = new IEForgeEnergyWrapper(this, this.facing);
		return energyWrapper;
	}

	@Override
	public FluxStorage getFluxStorage()
	{
		return energyStorage;
	}

	@Override
	public SideConfig getEnergySideConfig(EnumFacing facing)
	{
		return SideConfig.OUTPUT;
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from)
	{
		return false;
	}

	@Override
	public int getEnergyStored(EnumFacing from)
	{
		return energyStorage.getEnergyStored();
	}

	private int getMaxStorage()
	{
		return IEConfig.Machines.capacitorLV_storage;
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from)
	{
		return getMaxStorage();
	}

	@Override
	public int extractEnergy(EnumFacing from, int energy, boolean simulate)
	{
		return 0;
	}

	public int transferEnergy(int energy, boolean simulate, final int energyType)
	{
		int received = 0;
		if (!world.isRemote)
		{
			Set<AbstractConnection> outputs = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(Utils.toCC(this), world);
			int powerLeft = Math.min(Math.min(getMaxOutput(), getMaxInput()), energy);
			final int powerForSort = powerLeft;

			if (outputs.size() < 1)
				return 0;

			int sum = 0;
			HashMap<AbstractConnection, Integer> powerSorting = new HashMap<AbstractConnection, Integer>();
			for (AbstractConnection con : outputs)
			{
				IImmersiveConnectable end = ApiUtils.toIIC(con.end, world);
				if (con.cableType != null && end != null)
				{
					int atmOut = Math.min(powerForSort, con.cableType.getTransferRate());
					int tempR = end.outputEnergy(atmOut, true, energyType);
					if (tempR > 0)
					{
						powerSorting.put(con, tempR);
						sum += tempR;
					}
				}
			}

			if (sum > 0)
				for (AbstractConnection con : powerSorting.keySet())
				{
					IImmersiveConnectable end = ApiUtils.toIIC(con.end, world);
					if (con.cableType != null && end != null)
					{
						float prio = powerSorting.get(con) / (float) sum;
						int output = (int) (powerForSort * prio);

						int tempR = end.outputEnergy(Math.min(output, con.cableType.getTransferRate()), true, energyType);
						int r = tempR;
						int maxInput = getMaxInput();
						tempR -= (int) Math.max(0, Math.floor(tempR * con.getPreciseLossRate(tempR, maxInput)));
						end.outputEnergy(tempR, simulate, energyType);
						HashSet<IImmersiveConnectable> passedConnectors = new HashSet<IImmersiveConnectable>();
						float intermediaryLoss = 0;
						for (Connection sub : con.subConnections)
						{
							float length = sub.length / (float) sub.cableType.getMaxLength();
							float baseLoss = (float) sub.cableType.getLossRatio();
							float mod = (((maxInput - tempR) / (float) maxInput) / .25f) * .1f;
							intermediaryLoss = MathHelper.clamp(intermediaryLoss + length * (baseLoss + baseLoss * mod), 0, 1);

							int transferredPerCon = ImmersiveNetHandler.INSTANCE.getTransferedRates(world.provider.getDimension()).containsKey(sub) ? ImmersiveNetHandler.INSTANCE.getTransferedRates(world.provider.getDimension()).get(sub) : 0;
							transferredPerCon += r;
							if (!simulate)
							{
								ImmersiveNetHandler.INSTANCE.getTransferedRates(world.provider.getDimension()).put(sub, transferredPerCon);
								IImmersiveConnectable subStart = ApiUtils.toIIC(sub.start, world);
								IImmersiveConnectable subEnd = ApiUtils.toIIC(sub.end, world);
								if (subStart != null && passedConnectors.add(subStart))
									subStart.onEnergyPassthrough((int) (r - r * intermediaryLoss));
								if (subEnd != null && passedConnectors.add(subEnd))
									subEnd.onEnergyPassthrough((int) (r - r * intermediaryLoss));
							}
						}
						received += r;
						powerLeft -= r;
						if (powerLeft <= 0)
							break;
					}
				}
		}
		return received;
	}

	public int getMaxInput()
	{
		return TileEntityConnectorLV.connectorInputValues[1];
	}

	public int getMaxOutput()
	{
		return TileEntityConnectorLV.connectorInputValues[1];
	}

	@Override
	public boolean shoudlPlaySound(String sound)
	{
		return active;
	}
}