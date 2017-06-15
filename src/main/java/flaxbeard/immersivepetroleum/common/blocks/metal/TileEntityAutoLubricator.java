package flaxbeard.immersivepetroleum.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBlastFurnacePreheater;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;

public class TileEntityAutoLubricator extends TileEntityIEBase implements IDirectionalTile, IHasDummyBlocks
{
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

		if(descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("dummy", dummy);
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
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			return dummy == 1 && (facing==null || facing == EnumFacing.UP);
		return super.hasCapability(capability, facing);
	}
}