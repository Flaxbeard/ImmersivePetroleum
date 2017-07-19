package flaxbeard.immersivepetroleum.common.blocks.metal;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxConnector;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.common.IPContent;

public class TileEntityGasGenerator extends TileEntityIEBase implements IDirectionalTile, ITickable, IPlayerInteraction, IBlockOverlayText, IBlockBounds, IIEInternalFluxConnector
{
	public boolean active;
	public EnumFacing facing = EnumFacing.NORTH;
	public FluidTank tank = new FluidTank(8000)
	{
		@Override
		public boolean canFillFluidType(FluidStack fluid)
	    {
	        return fluid != null && fluid.getFluid() == IPContent.fluidGasoline;
	    }
	};

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		active = nbt.getBoolean("active");
		tank.readFromNBT(nbt.getCompoundTag("tank"));

		if(descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing.ordinal());
		nbt.setBoolean("active", active);
		
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
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing.getAxis() != this.facing.getAxis() && facing.getAxis() != Axis.Y)
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
			return (facing.getAxis() != this.facing.getAxis() && facing.getAxis() != Axis.Y);
		}
		return super.hasCapability(capability, facing);
	}
	
	int lastTank = 0;
	
	@Override
	public void update()
	{
		
			
			if (lastTank != this.tank.getFluidAmount())
			{
				markContainingBlockForUpdate(null);
				lastTank = this.tank.getFluidAmount();
				
				
			}
	
	}
	
	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		FluidStack f = FluidUtil.getFluidContained(heldItem);
		if (FluidUtil.interactWithFluidHandler(heldItem, tank, player))
		{
			markContainingBlockForUpdate(null);
			return true;
		}
	
		return false;
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

			String s = null;
			if (tank.getFluid() != null)
				s = tank.getFluid().getLocalizedName() + ": " + tank.getFluidAmount() + "mB";
			else
				s = I18n.format(Lib.GUI + "empty");
			return new String[] {s};
		
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
		return new float[] { .0625f, 0, .0625f, .9375f, 1, .9375f };
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from)
	{
		return from == EnumFacing.UP;
	}

	@Override
	public SideConfig getEnergySideConfig(EnumFacing facing)
	{
		return facing == EnumFacing.UP ? SideConfig.NONE : SideConfig.OUTPUT;
	}

	IEForgeEnergyWrapper wrapper = new IEForgeEnergyWrapper(this,null);
	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing)
	{
		return wrapper;
	}
}