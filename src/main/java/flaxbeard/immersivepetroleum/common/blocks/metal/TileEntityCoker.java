package flaxbeard.immersivepetroleum.common.blocks.metal;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.MultiblockCoker;

public class TileEntityCoker extends TileEntityMultiblockMetal<TileEntityCoker, IMultiblockRecipe> implements IAdvancedSelectionBounds,IAdvancedCollisionBounds, IGuiTile
{
	public static class TileEntityCokerParent extends TileEntityCoker
	{
		
		@Override
		public boolean isDummy()
		{
			return false;
		}
	}
	
	public TileEntityCoker()
	{
		super(MultiblockCoker.instance, new int[]{23, 5, 9}, 16000, true);
	}
	
	public FluidTank fakeTank = new FluidTank(0);

	public boolean wasActive = false;
	public int activeTicks = 0;
	

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		boolean lastActive = wasActive;
		this.wasActive = nbt.getBoolean("wasActive");
		if (!wasActive && lastActive)
		{
			this.activeTicks++;
		}
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setBoolean("wasActive", wasActive);

	}

	@Override
	public void update()
	{
		//System.out.println("TEST");
		super.update();
		if(worldObj.isRemote || isDummy())
		{
			return;
		}
		
		/*
		boolean active = false;
		
		int consumed = IPConfig.Machines.pumpjack_consumption;
		int extracted = energyStorage.extractEnergy(consumed, true);
		
		if(extracted >= consumed && canExtract() && !this.isRSDisabled() && (availableOil() > 0 || canGetResidualOil()))
		{
			int oilAmnt = availableOil() == 0 ? IPConfig.Machines.oil_replenish : availableOil();
			
			energyStorage.extractEnergy(consumed, false);
			active = true;
			FluidStack out = new FluidStack(IPContent.fluidCrudeOil, Math.min(IPConfig.Machines.pumpjack_speed, oilAmnt));
			BlockPos outputPos = this.getPos().offset(facing, 2).offset(facing.rotateY().getOpposite(), 2).offset(EnumFacing.DOWN, 1);
			IFluidHandler output = FluidUtil.getFluidHandler(worldObj, outputPos, facing);
			if(output != null)
			{
				int accepted = output.fill(out, false);
				if(accepted > 0)
				{
					int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
					extractOil(drained);
					out = Utils.copyFluidStackWithAmount(out, out.amount - drained, false);
				}
			}
			
			outputPos = this.getPos().offset(facing, 2).offset(facing.rotateY(), 2).offset(EnumFacing.DOWN, 1);
			output = FluidUtil.getFluidHandler(worldObj, outputPos, facing);
			if(output != null)
			{
				int accepted = output.fill(out, false);
				if(accepted > 0)
				{
					int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
					extractOil(drained);

				}
			}
						
			
			activeTicks++;
		}

		if(active != wasActive)
		{
			this.markDirty();
			this.markContainingBlockForUpdate(null);
		}
		
		wasActive = active;
		*/

	}

	@Override
	public float[] getBlockBounds()
	{
		/*if(pos==19)
			return new float[]{facing==EnumFacing.WEST?.5f:0,0,facing==EnumFacing.NORTH?.5f:0, facing==EnumFacing.EAST?.5f:1,1,facing==EnumFacing.SOUTH?.5f:1};
		if(pos==17)
			return new float[]{.0625f,0,.0625f, .9375f,1,.9375f};*/

		return new float[]{0,0,0, 0,0,0};
	}
	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		if(mirrored)
			fw = fw.getOpposite();
		
		int y = pos / (5  * 9);
		int x = (pos / 9)  % 5;
		int z = pos % 9;
		
		if (y <= 12)
		{
			if (x == 0 && z == 4 && y >= 3)
			{			
				float minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 6F/16F : ((fl == EnumFacing.EAST) ? 2F/16F : 10F/16F);
				float maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 10F/16F : ((fl == EnumFacing.EAST) ? 6F/16F : 14F/16F);
				float minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 6F/16F : ((fl == EnumFacing.SOUTH) ? 2F/16F : 10F/16F);
				float maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 10F/16F : ((fl == EnumFacing.SOUTH) ? 6F/16F : 14F/16F);
				List list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 0.99, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
				if (y == 7 || y == 12)
				{
					list.add(new AxisAlignedBB(0, 0.5, 0, 1, 1, 1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				}
				
				if (y == 5 || y == 10)
				{
					minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0F : ((fl == EnumFacing.EAST) ? 2F/16F : 10F/16F);
					maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 1F : ((fl == EnumFacing.EAST) ? 6F/16F : 14F/16F);
					minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 0F : ((fl == EnumFacing.SOUTH) ? 2F/16F : 10F/16F);
					maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 1F : ((fl == EnumFacing.SOUTH) ? 6F/16F : 14F/16F);
					list.add(new AxisAlignedBB(minX, 2F/16F, minZ, maxX, 6F/16F, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
					
				}
				
				return list;
			}
			else if (x == 4 && z == 4 && y >= 3)
			{			
				float minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 6F/16F : ((fl == EnumFacing.WEST) ? 2F/16F : 10F/16F);
				float maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 10F/16F : ((fl == EnumFacing.WEST) ? 6F/16F : 14F/16F);
				float minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 6F/16F : ((fl == EnumFacing.NORTH) ? 2F/16F : 10F/16F);
				float maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 10F/16F : ((fl == EnumFacing.NORTH) ? 6F/16F : 14F/16F);
				List list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 0.99, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
				if (y == 7 || y == 12)
				{
					list.add(new AxisAlignedBB(0, 0.5, 0, 1, 1, 1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				}
				
				if (y == 5 || y == 10)
				{
					minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0F : ((fl == EnumFacing.WEST) ? 2F/16F : 10F/16F);
					maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 1F : ((fl == EnumFacing.WEST) ? 6F/16F : 14F/16F);
					minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 0F : ((fl == EnumFacing.NORTH) ? 2F/16F : 10F/16F);
					maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 1F : ((fl == EnumFacing.NORTH) ? 6F/16F : 14F/16F);
					list.add(new AxisAlignedBB(minX, 2F/16F, minZ, maxX, 6F/16F, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
					
				}
				
				return list;
			}
			
			if (x == 3 && (z == 2 || z == 6) && y >= 3)
			{			
				float minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0F : ((fl == EnumFacing.WEST) ? 2F/16F : 0F);
				float maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 1F : ((fl == EnumFacing.WEST) ? 1F : 14F/16F);
				float minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 0F : ((fl == EnumFacing.NORTH) ? 2F/16F : 0F);
				float maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 1F : ((fl == EnumFacing.NORTH) ? 1F : 14F/16F);
				List list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
	
				
				return list;
			}
			else if (x == 3 && (z == 3 || z == 7) && y >= 3)
			{			
				float minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? ((fl == EnumFacing.SOUTH) ? 0F : 2F/16F) : ((fl == EnumFacing.WEST) ? 2F/16F : 0F);
				float maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? ((fl == EnumFacing.SOUTH) ? 14F/16F : 1F)  : ((fl == EnumFacing.WEST) ? 1F : 14F/16F);
				float minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? ((fl == EnumFacing.EAST) ? 0F : 2F/16F) : ((fl == EnumFacing.NORTH) ? 2F/16F : 0F);
				float maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? ((fl == EnumFacing.EAST) ? 14F/16F : 1F) : ((fl == EnumFacing.NORTH) ? 1F : 14F/16F);
				List list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
				return list;
			}
			else if (x == 3 && (z == 1 || z == 5) && y >= 3)
			{			
				float minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? ((fl == EnumFacing.NORTH) ? 0F : 2F/16F) : ((fl == EnumFacing.WEST) ? 2F/16F : 0F);
				float maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? ((fl == EnumFacing.NORTH) ? 14F/16F : 1F)  : ((fl == EnumFacing.WEST) ? 1F : 14F/16F);
				float minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? ((fl == EnumFacing.WEST) ? 0F : 2F/16F) : ((fl == EnumFacing.NORTH) ? 2F/16F : 0F);
				float maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? ((fl == EnumFacing.WEST) ? 14F/16F : 1F) : ((fl == EnumFacing.NORTH) ? 1F : 14F/16F);
				List list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
				return list;
			}
			else if (x == 1 && (z == 2 || z == 6) && y >= 3)
			{			
				float minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0F : ((fl == EnumFacing.EAST) ? 2F/16F : 0F);
				float maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 1F : ((fl == EnumFacing.EAST) ? 1F : 14F/16F);
				float minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 0F : ((fl == EnumFacing.SOUTH) ? 2F/16F : 0F);
				float maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 1F : ((fl == EnumFacing.SOUTH) ? 1F : 14F/16F);
				List list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
				return list;
			}
			else if (x == 1 && (z == 3 || z == 7) && y >= 3)
			{			
				float minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? ((fl == EnumFacing.SOUTH) ? 0F : 2F/16F) : ((fl == EnumFacing.EAST) ? 2F/16F : 0F);
				float maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? ((fl == EnumFacing.SOUTH) ? 14F/16F : 1F)  : ((fl == EnumFacing.EAST) ? 1F : 14F/16F);
				float minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? ((fl == EnumFacing.EAST) ? 0F : 2F/16F) : ((fl == EnumFacing.SOUTH) ? 2F/16F : 0F);
				float maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? ((fl == EnumFacing.EAST) ? 14F/16F : 1F) : ((fl == EnumFacing.SOUTH) ? 1F : 14F/16F);
				List list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
				return list;
			}
			else if (x == 1 && (z == 1 || z == 5) && y >= 3)
			{			
				float minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? ((fl == EnumFacing.NORTH) ? 0F : 2F/16F) : ((fl == EnumFacing.EAST) ? 2F/16F : 0F);
				float maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? ((fl == EnumFacing.NORTH) ? 14F/16F : 1F)  : ((fl == EnumFacing.EAST) ? 1F : 14F/16F);
				float minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? ((fl == EnumFacing.WEST) ? 0F : 2F/16F) : ((fl == EnumFacing.SOUTH) ? 2F/16F : 0F);
				float maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? ((fl == EnumFacing.WEST) ? 14F/16F : 1F) : ((fl == EnumFacing.SOUTH) ? 1F : 14F/16F);
				List list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
				return list;
			}
			else if (x == 2 && (z == 1 || z == 5) && y >= 3)
			{			
				float minX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 0F : ((fl == EnumFacing.SOUTH) ? 2F/16F : 0F);
				float maxX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 1F : ((fl == EnumFacing.SOUTH) ? 1F : 14F/16F);
				float minZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0F : ((fl == EnumFacing.EAST) ? 2F/16F : 0F);
				float maxZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 1F : ((fl == EnumFacing.EAST) ? 1F : 14F/16F);
				List list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
				return list;
			}
			else if (x == 2 && (z == 3 || z == 7) && y >= 3)
			{			
				float minX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 0F : ((fl == EnumFacing.NORTH) ? 2F/16F : 0F);
				float maxX = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 1F : ((fl == EnumFacing.NORTH) ? 1F : 14F/16F);
				float minZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0F : ((fl == EnumFacing.WEST) ? 2F/16F : 0F);
				float maxZ = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 1F : ((fl == EnumFacing.WEST) ? 1F : 14F/16F);
				List list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
				
				return list;
			}
	
			if (x == 0 && z == 8 && y >= 3)
			{			
				float minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? ((fl == EnumFacing.SOUTH) ? 10F/16F : 2F/16F) : ((fl == EnumFacing.EAST) ? 2F/16F : 10F/16F);
				float maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? ((fl == EnumFacing.SOUTH) ? 14F/16F : 6F/16F)  : ((fl == EnumFacing.EAST) ? 6F/16F : 14F/16F);
				float minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? ((fl == EnumFacing.EAST) ? 10F/16F : 2F/16F) : ((fl == EnumFacing.SOUTH) ? 2F/16F : 10F/16F);
				float maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? ((fl == EnumFacing.EAST) ? 14F/16F : 6F/16F) : ((fl == EnumFacing.SOUTH) ? 6F/16F : 14F/16F);
				List list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
				
				return list;
			}
			else if (x == 4 && z == 8 && y >= 3)
			{			
				float minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? ((fl == EnumFacing.SOUTH) ? 10F/16F : 2F/16F) : ((fl == EnumFacing.WEST) ? 2F/16F : 10F/16F);
				float maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? ((fl == EnumFacing.SOUTH) ? 14F/16F : 6F/16F)  : ((fl == EnumFacing.WEST) ? 6F/16F : 14F/16F);
				float minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? ((fl == EnumFacing.EAST) ? 10F/16F : 2F/16F) : ((fl == EnumFacing.NORTH) ? 2F/16F : 10F/16F);
				float maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? ((fl == EnumFacing.EAST) ? 14F/16F : 6F/16F) : ((fl == EnumFacing.NORTH) ? 6F/16F : 14F/16F);
				List list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
	
				
				return list;
			}
			else if (x == 0 && z == 0 && y >= 3)
			{			
				float minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? ((fl == EnumFacing.NORTH) ? 10F/16F : 2F/16F) : ((fl == EnumFacing.EAST) ? 2F/16F : 10F/16F);
				float maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? ((fl == EnumFacing.NORTH) ? 14F/16F : 6F/16F)  : ((fl == EnumFacing.EAST) ? 6F/16F : 14F/16F);
				float minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? ((fl == EnumFacing.WEST) ? 10F/16F : 2F/16F) : ((fl == EnumFacing.SOUTH) ? 2F/16F : 10F/16F);
				float maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? ((fl == EnumFacing.WEST) ? 14F/16F : 6F/16F) : ((fl == EnumFacing.SOUTH) ? 6F/16F : 14F/16F);
				List list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
				return list;
			}
			else if (x == 4 && z == 0 && y >= 3)
			{			
				float minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? ((fl == EnumFacing.NORTH) ? 10F/16F : 2F/16F) : ((fl == EnumFacing.WEST) ? 2F/16F : 10F/16F);
				float maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? ((fl == EnumFacing.NORTH) ? 14F/16F : 6F/16F)  : ((fl == EnumFacing.WEST) ? 6F/16F : 14F/16F);
				float minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? ((fl == EnumFacing.WEST) ? 10F/16F : 2F/16F) : ((fl == EnumFacing.NORTH) ? 2F/16F : 10F/16F);
				float maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? ((fl == EnumFacing.WEST) ? 14F/16F : 6F/16F) : ((fl == EnumFacing.NORTH) ? 6F/16F : 14F/16F);
				List list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
				
				return list;
			}
			
			
			else if (y == 7 || y == 12)
			{
				return Lists.newArrayList(new AxisAlignedBB(0, 0.5, 0, 1, 1, 1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			}
		}
		
		
		List list = new ArrayList<AxisAlignedBB>();
		list.add(new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
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
		return null ;
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
		int y = pos / (5  * 9);
		int x = (pos / 9)  % 5;
		int z = pos % 9;
		
		System.out.println(x + " " + y + " " + z);
		return false;
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
	public ItemStack[] getInventory()
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
		TileEntityCoker master = this.master();
		if(master != null)
		{
			if (pos == 9 && (side == null || side == facing.getOpposite().rotateY()))
			{
				return new FluidTank[] { fakeTank };
			}
			else if (pos == 11 && (side == null || side == facing.rotateY()))
			{
				return new FluidTank[] { fakeTank };
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
	public TileEntityCoker master()
	{
		if(offset[0]==0 && offset[1]==0 && offset[2]==0)
		{
			return this;
		}
		TileEntity te = worldObj.getTileEntity(getPos().add(-offset[0],-offset[1],-offset[2]));
		return this.getClass().isInstance(te) ? (TileEntityCoker) te : null;
	}
	
	@Override
	public TileEntityCoker getTileForPos(int targetPos)
	{
		BlockPos target = getBlockPosForPos(targetPos);
		TileEntity tile = worldObj.getTileEntity(target);
		return tile instanceof TileEntityCoker ? (TileEntityCoker) tile : null;
	}
}