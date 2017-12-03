package flaxbeard.immersivepetroleum.common.blocks.metal;

import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class TileEntityFlareStack extends TileEntityIEBase implements IDirectionalTile, ITickable, IBlockBounds
{
	
	private boolean active;
	private boolean wasActive;

	public EnumFacing facing = EnumFacing.UP;
	public FluidTank tank = new FluidTank(8000)
	{
		@Override
		public boolean canFillFluidType(FluidStack fluid)
	    {
	        return fluid != null && LubricantHandler.isValidLube(fluid.getFluid());
	    }
	};
	

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		active = nbt.getBoolean("active");
		wasActive = nbt.getBoolean("wasActive");

		tank.readFromNBT(nbt.getCompoundTag("tank"));

		if (descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing.ordinal());
		nbt.setBoolean("active", active);
		nbt.setBoolean("wasActive", wasActive);

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
		return 0;
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
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && (facing == null || facing == this.facing.getOpposite()))
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
			return (facing == null || facing == this.facing.getOpposite());
		}
		return super.hasCapability(capability, facing);
	}

	private int ticks = 0;
	
	@Override
	public void update()
	{
		if (world.isRemote) {
			for (int i = 0; i < 10; i++) {
				ticks++;
				Random lastRand = new Random((int) Math.floor(ticks / 20f));
				Random thisRand = new Random((int) Math.ceil(ticks / 20f));

				float lastDirection = lastRand.nextFloat() * 360;
				float thisDirection = thisRand.nextFloat() * 360;
				float interpDirection = (thisDirection - lastDirection) * ((ticks % 20f) / 20F) + lastDirection;

				float xPos = pos.getX() + 0.5f + (world.rand.nextFloat() - 0.5f) * .3f;
				float zPos = pos.getZ() + 0.5f + (world.rand.nextFloat() - 0.5f) * .3f;
				float yPos = pos.getY() + 1 + 0.025f * i;
				float xSpeed = (float) Math.sin(interpDirection) * .1f;
				float zSpeed = (float) Math.cos(interpDirection) * .1f;
				world.spawnParticle(EnumParticleTypes.BLOCK_DUST, xPos, yPos, zPos, xSpeed, .5f, zSpeed, Block.getIdFromBlock(Blocks.FIRE));
			}
		}
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
	public float[] getBlockBounds()
	{
		return new float[] { .0625f, 0, .0625f, .9375f, 1, .9375f };
	}
	
	
	public void readTank(NBTTagCompound nbt)
	{
		tank.readFromNBT(nbt.getCompoundTag("tank"));
	}
	
	public void writeTank(NBTTagCompound nbt, boolean toItem)
	{
		boolean write = tank.getFluidAmount()>0;
		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		if(!toItem || write)
			nbt.setTag("tank", tankTag);
	}

}