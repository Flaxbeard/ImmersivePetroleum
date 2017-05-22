package flaxbeard.immersivepetroleum.common.blocks.metal;

import java.util.ArrayList;
import java.util.List;

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
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.Utils;

import com.google.common.collect.Lists;

import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.MultiblockDistillationTower;

public class TileEntityDistillationTower extends TileEntityMultiblockMetal<TileEntityDistillationTower, DistillationRecipe> implements IAdvancedSelectionBounds,IAdvancedCollisionBounds, IGuiTile
{
	public static class TileEntityDistillationTowerParent extends TileEntityDistillationTower
	{
		@Override
		@SideOnly(Side.CLIENT)
		public net.minecraft.util.math.AxisAlignedBB getRenderBoundingBox()
		{
			BlockPos pos = getPos();
			return new AxisAlignedBB(pos.add(-4, -16, -4), pos.add(4, 16, 4));
		}
	}
	
	public TileEntityDistillationTower()
	{
		super(MultiblockDistillationTower.instance, new int[]{16, 4, 4}, 16000, true);
	}
	
	public FluidTank[] tanks = new FluidTank[]{new FluidTank(24000),new FluidTank(24000)};
	public ItemStack[] inventory = new ItemStack[4];
	
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
		tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
		if(!descPacket)
			inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 6);
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
		nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
		if(!descPacket)
			nbt.setTag("inventory", Utils.writeInventory(inventory));
	}
	
	private boolean wasActive = false;

	@Override
	public void update()
	{
		super.update();
		if(worldObj.isRemote || isDummy())
			return;
		boolean update = false;
	
		if(energyStorage.getEnergyStored()>0 && processQueue.size()<this.getProcessQueueMaxLength())
		{
			if(tanks[0].getFluidAmount() > 0)
			{
				DistillationRecipe recipe = DistillationRecipe.findRecipe(tanks[0].getFluid());
				if(recipe!=null)
				{
					MultiblockProcessInMachine<DistillationRecipe> process = new MultiblockProcessInMachine(recipe).setInputTanks(new int[] {0});
					if(this.addProcessToQueue(process, true))
					{
						this.addProcessToQueue(process, false);
						update = true;
					}
				}
			}
		}
		
		if (processQueue.size() > 0)
		{
			wasActive = true;
		}
		else if (wasActive)
		{
			wasActive = false;
			update = true;
		}

		if (this.tanks[1].getFluidAmount()>0)
		{

			ItemStack filledContainer = Utils.fillFluidContainer(tanks[1], inventory[2], inventory[3], null);
			if(filledContainer!=null)
			{
				if(inventory[3]!=null && OreDictionary.itemMatches(inventory[3], filledContainer, true))
					inventory[3].stackSize+=filledContainer.stackSize;
				else if(inventory[3]==null)
					inventory[3] = filledContainer.copy();
				if(--inventory[2].stackSize<=0)
					inventory[2]=null;
				update = true;
			}
			if(this.tanks[1].getFluidAmount()>0)
			{
				FluidStack out = Utils.copyFluidStackWithAmount(this.tanks[1].getFluid(), Math.min(this.tanks[1].getFluidAmount(), 80), false);
				BlockPos outputPos = this.getPos().offset(facing.getOpposite(), 1).offset(facing.rotateY().getOpposite(), 1).offset(EnumFacing.DOWN, 1);
				//System.out.println(outputPos);
				IFluidHandler output = FluidUtil.getFluidHandler(worldObj, outputPos, facing);
				if(output!=null)
				{
					int accepted = output.fill(out, false);
					if(accepted>0)
					{
						int drained = output.fill(Utils.copyFluidStackWithAmount(out,Math.min(out.amount, accepted),false), true);
						this.tanks[1].drain(drained, true);
						update=true;
					}
				}
			}
			
		}

		ItemStack emptyContainer = Utils.drainFluidContainer(tanks[0], inventory[0], inventory[1], null);
		if(emptyContainer!=null && emptyContainer.stackSize>0)
		{
			if(inventory[1]!=null && OreDictionary.itemMatches(inventory[1], emptyContainer, true))
				inventory[1].stackSize+=emptyContainer.stackSize;
			else if(inventory[1]==null)
				inventory[1] = emptyContainer.copy();
			if(--inventory[0].stackSize<=0)
				inventory[0]=null;
			update = true;
		}

		if(update)
		{
			this.markDirty();
			this.markContainingBlockForUpdate(null);
		}
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
		
		int y = pos / 16;
		int x = (pos % 16) / 4;
		int z = pos % 4;
		if (pos == 2 || pos == 3 || pos == 4 || pos == 7 || pos == 11 || pos == 13 || pos == 15)
		{
			return Lists.newArrayList(new AxisAlignedBB(0,0,0, 1,.5f,1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
		}
		if (pos == 12)
		{
			List list = Lists.newArrayList(new AxisAlignedBB(0,0,0, 1,.5f,1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			float minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 2F/16F : ((fl == EnumFacing.EAST) ? 10F/16F : 2F/16F);
			float maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 4F/16F : ((fl == EnumFacing.EAST) ? 14F/16F : 6F/16F);
			float minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 2F/16F : ((fl == EnumFacing.SOUTH) ? 10F/16F : 2F/16F);
			float maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 4F/16F : ((fl == EnumFacing.SOUTH) ? 14F/16F : 6F/16F);
			list.add(new AxisAlignedBB(minX, 0.5, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 12F/16F : minX;
			maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 14F/16F : maxX;
			minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 12F/16F : minZ;
			maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 14F/16F : maxZ;
			list.add(new AxisAlignedBB(minX, 0.5, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			return list;
		}
		else if (pos == 12 + 16)
		{
			float minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 0F : ((fl == EnumFacing.EAST) ? .5F : 0F);
			float maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? 1F : ((fl == EnumFacing.EAST) ? 1F : .5F);
			float minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 0F : ((fl == EnumFacing.SOUTH) ? .5F : 0F);
			float maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? 1F : ((fl == EnumFacing.SOUTH) ? 1F : .5F);
			return Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
		}
		else if (pos == 16 + 7 || pos == 32 + 7)
		{
			return null;
		}
		else if (pos == 32 + 1)
		{
			float minX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? .125F : ((fl == EnumFacing.EAST) ? .125F : 0F);
			float maxX = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH) ? .875F : ((fl == EnumFacing.EAST) ? 1F : .875F);
			float minZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? .125F : ((fl == EnumFacing.SOUTH) ? .125F : 0F);
			float maxZ = (fl == EnumFacing.EAST || fl == EnumFacing.WEST) ? .875F : ((fl == EnumFacing.SOUTH) ? 1F : .875F);
			return Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1.125F, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
		}
		else if (pos % 16 == 7)
		{
			List list = new ArrayList<AxisAlignedBB>();
			if (y % 4 > 2 || (fl != EnumFacing.EAST && fl != EnumFacing.WEST))
			{
				float bottom = (fl != EnumFacing.EAST && fl != EnumFacing.WEST) ? 0F : 0.5f;
				list.add(new AxisAlignedBB(0, bottom, 0, 0, 1, 1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				list.add(new AxisAlignedBB(1, bottom, 0, 1, 1, 1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			}
			if (y % 4 > 2 || (fl != EnumFacing.NORTH && fl != EnumFacing.SOUTH))
			{
				float bottom = (fl != EnumFacing.NORTH && fl != EnumFacing.SOUTH) ? 0F : 0.5f;
				list.add(new AxisAlignedBB(0, bottom, 0, 1, 1, 0).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				list.add(new AxisAlignedBB(0, bottom, 1, 1, 1, 1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			}
			return list;
		}
		else if (y > 0 && x == 2 && z == 0)
		{
			List list = Lists.newArrayList(new AxisAlignedBB(0.1875f, 0, 0.1875f, 0.8125f, 1, 0.8125f).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			if (y > 0 && y % 4 == 0)
			{
				list.add(new AxisAlignedBB(0,0.5F,0, 1,1,1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			}
			return list;
		}
		else if (y > 0 && y % 4 == 0 && (x == 0 || z == 0 || x == 3 || z == 3))
		{
			return Lists.newArrayList(new AxisAlignedBB(0,0.5F,0, 1,1,1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
		}
		/*
		if(pos==0||pos==4||pos==10||pos==14)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0,0,0, 1,.5f,1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			if(pos>=10)
				fl = fl.getOpposite();
			if(pos%10==0)
				fw = fw.getOpposite();

			float minX = fl==EnumFacing.WEST?0: fl==EnumFacing.EAST?.75f: fw==EnumFacing.WEST?.5f: .25f;
			float maxX = fl==EnumFacing.EAST?1: fl==EnumFacing.WEST?.25f: fw==EnumFacing.EAST?.5f: .75f;
			float minZ = fl==EnumFacing.NORTH?0: fl==EnumFacing.SOUTH?.75f: fw==EnumFacing.NORTH?.5f: .25f;
			float maxZ = fl==EnumFacing.SOUTH?1: fl==EnumFacing.NORTH?.25f: fw==EnumFacing.SOUTH?.5f: .75f;
			list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1.375f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

			if(pos==4)
			{
				minX = fl==EnumFacing.WEST?.625f: fl==EnumFacing.EAST?.125f: .125f;
				maxX = fl==EnumFacing.EAST?.375f: fl==EnumFacing.WEST?.875f: .25f;
				minZ = fl==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.125f: .125f;
				maxZ = fl==EnumFacing.SOUTH?.375f: fl==EnumFacing.NORTH?.875f: .25f;
				list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

				minX = fl==EnumFacing.WEST?.625f: fl==EnumFacing.EAST?.125f: .75f;
				maxX = fl==EnumFacing.EAST?.375f: fl==EnumFacing.WEST?.875f: .875f;
				minZ = fl==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.125f: .75f;
				maxZ = fl==EnumFacing.SOUTH?.375f: fl==EnumFacing.NORTH?.875f: .875f;
				list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			}

			return list;
		}
		if(pos==1||pos==3||pos==11||pos==13)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0,0,0, 1,.0f,1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			if(pos>=10)
				fl = fl.getOpposite();
			if(pos%10==1)
				fw = fw.getOpposite();

			float minX = fl==EnumFacing.WEST?0: fl==EnumFacing.EAST?.75f: fw==EnumFacing.WEST?.75f: 0;
			float maxX = fl==EnumFacing.EAST?1: fl==EnumFacing.WEST?.25f: fw==EnumFacing.EAST?.25f: 1;
			float minZ = fl==EnumFacing.NORTH?0: fl==EnumFacing.SOUTH?.75f: fw==EnumFacing.NORTH?.75f: 0;
			float maxZ = fl==EnumFacing.SOUTH?1: fl==EnumFacing.NORTH?.25f: fw==EnumFacing.SOUTH?.25f: 1;
			list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1.375f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			return list;
		}

		if((pos==20||pos==24 || pos==25||pos==29)||(pos==35||pos==39 || pos==40||pos==44))
		{
			List<AxisAlignedBB> list = Lists.newArrayList();
			if(pos%5==4)
				fw = fw.getOpposite();
			float minX = fl==EnumFacing.WEST?-.25f: fl==EnumFacing.EAST?-.25f: fw==EnumFacing.WEST?-1f: .5f;
			float maxX = fl==EnumFacing.EAST?1.25f: fl==EnumFacing.WEST?1.25f: fw==EnumFacing.EAST?2: .5f;
			float minZ = fl==EnumFacing.NORTH?-.25f: fl==EnumFacing.SOUTH?-.25f: fw==EnumFacing.NORTH?-1f: .5f;
			float maxZ = fl==EnumFacing.SOUTH?1.25f: fl==EnumFacing.NORTH?1.25f: fw==EnumFacing.SOUTH?2: .5f;
			float minY = pos<35?.5f:-.5f;
			float maxY = pos<35?2f:1f;
			if(pos%15>=10)
			{
				minX += fl==EnumFacing.WEST?1: fl==EnumFacing.EAST?-1: 0;
				maxX += fl==EnumFacing.WEST?1: fl==EnumFacing.EAST?-1: 0;
				minZ += fl==EnumFacing.NORTH?1: fl==EnumFacing.SOUTH?-1: 0;
				maxZ += fl==EnumFacing.NORTH?1: fl==EnumFacing.SOUTH?-1: 0;
			}
			list.add(new AxisAlignedBB(minX,minY,minZ, maxX,maxY,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			return list;
		}
		if((pos==21||pos==23 || pos==26||pos==28)||(pos==36||pos==38 || pos==41||pos==43))
		{
			List<AxisAlignedBB> list = Lists.newArrayList();
			if(pos%5==3)
				fw = fw.getOpposite();
			float minX = fl==EnumFacing.WEST?-.25f: fl==EnumFacing.EAST?-.25f: fw==EnumFacing.WEST?0f:-.5f;
			float maxX = fl==EnumFacing.EAST?1.25f: fl==EnumFacing.WEST?1.25f: fw==EnumFacing.EAST?1f: 1.5f;
			float minZ = fl==EnumFacing.NORTH?-.25f: fl==EnumFacing.SOUTH?-.25f: fw==EnumFacing.NORTH?0:-.5f;
			float maxZ = fl==EnumFacing.SOUTH?1.25f: fl==EnumFacing.NORTH?1.25f: fw==EnumFacing.SOUTH?1f: 1.5f;
			float minY = pos<35?.5f:-.5f;
			float maxY = pos<35?2f:1f;
			if(pos%15>=10)
			{
				minX += fl==EnumFacing.WEST?1: fl==EnumFacing.EAST?-1: 0;
				maxX += fl==EnumFacing.WEST?1: fl==EnumFacing.EAST?-1: 0;
				minZ += fl==EnumFacing.NORTH?1: fl==EnumFacing.SOUTH?-1: 0;
				maxZ += fl==EnumFacing.NORTH?1: fl==EnumFacing.SOUTH?-1: 0;
			}
			list.add(new AxisAlignedBB(minX,minY,minZ, maxX,maxY,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			return list;
		}*/
		
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
		return new int[]{16};
	}
	@Override
	public int[] getRedstonePos()
	{
		return new int[]{28};
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}
	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<DistillationRecipe> process)
	{
		return true;
	}
	@Override
	public void doProcessOutput(ItemStack output)
	{
		BlockPos pos = getPos().offset(facing,1).offset(facing.rotateY(), 2).offset(EnumFacing.DOWN, 1);
		TileEntity inventoryTile = this.worldObj.getTileEntity(pos);
		if(inventoryTile!=null)
			output = Utils.insertStackIntoInventory(inventoryTile, output, facing.getOpposite());
		if(output!=null)
			Utils.dropStackAtPos(worldObj, pos, output, facing);
	}
	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}
	@Override
	public void onProcessFinish(MultiblockProcess<DistillationRecipe> process)
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
	public float getMinProcessDistance(MultiblockProcess<DistillationRecipe> process)
	{
		return 0;
	}


	@Override
	public ItemStack[] getInventory()
	{
		return inventory;
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
	public IFluidTank[] getInternalTanks()
	{
		return tanks;
	}
	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		TileEntityDistillationTower master = this.master();
		if(master != null)
		{
			if(pos == 0 && (side == null || side == facing.getOpposite()))
			{
				return new FluidTank[] {master.tanks[0]};
			}
			else if (pos == 8 && (side == null || side == facing.getOpposite().rotateY()))
			{
				return new FluidTank[] {master.tanks[1]};
			}
		}
		return new FluidTank[0];
	}
	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource)
	{
		if(pos == 0 && (side == null || side == facing.getOpposite()))
		{
			TileEntityDistillationTower master = this.master();
			FluidStack resourceClone = Utils.copyFluidStackWithAmount(resource, 1000, false);
			FluidStack resourceClone2 = Utils.copyFluidStackWithAmount(master.tanks[0].getFluid(), 1000, false);


			if(master==null || master.tanks[iTank].getFluidAmount()>=master.tanks[iTank].getCapacity())
				return false;
			if(master.tanks[0].getFluid()==null)
			{
				DistillationRecipe incompleteRecipes = DistillationRecipe.findRecipe(resourceClone);
				return incompleteRecipes!=null;
			}
			else
			{
				DistillationRecipe incompleteRecipes1 = DistillationRecipe.findRecipe(resourceClone);
				DistillationRecipe incompleteRecipes2 = DistillationRecipe.findRecipe(resourceClone2);
				return incompleteRecipes1 == incompleteRecipes2;
			}
		}
		return false;
	}
	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return (pos == 8 && (side == null || side == facing.getOpposite().rotateY()));
	}
	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}
	@Override
	public DistillationRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}
	@Override
	protected DistillationRecipe readRecipeFromNBT(NBTTagCompound tag)
	{
		return DistillationRecipe.loadFromNBT(tag);
	}
	@Override
	public boolean canOpenGui()
	{
		return formed && (pos / 16) < 4;
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
	public boolean isLadder()
	{
		return pos % 16 == 7;
	}
	
	@Override
	public TileEntityDistillationTower getTileForPos(int targetPos)
	{
		BlockPos target = getBlockPosForPos(targetPos);
		TileEntity tile = worldObj.getTileEntity(target);
		return tile instanceof TileEntityDistillationTower ? (TileEntityDistillationTower) tile : null;
	}
}