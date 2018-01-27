package flaxbeard.immersivepetroleum.common.blocks.metal;

import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySqueezer;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.SulfurRecoveryRecipe;
import flaxbeard.immersivepetroleum.common.Config.IPConfig;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.MultiblockHydrotreater;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.MultiblockPumpjack;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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

public class TileEntityHydrotreater extends TileEntityMultiblockMetal<TileEntityHydrotreater, SulfurRecoveryRecipe> implements IAdvancedSelectionBounds, IAdvancedCollisionBounds, IGuiTile, IEBlockInterfaces.IPlayerInteraction
{

	public static class TileEntityHydrotreaterParent extends TileEntityHydrotreater
	{
		@SideOnly(Side.CLIENT)
		@Override
		public AxisAlignedBB getRenderBoundingBox()
		{
			BlockPos nullPos = this.getPos();
			return new AxisAlignedBB(nullPos.offset(mirrored?facing.rotateYCCW():facing.rotateY(), -1).down(1), nullPos.offset(facing, 4).offset(mirrored?facing.rotateYCCW():facing.rotateY(), 2).up(3));
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

	private boolean wasActive = false;
	private int cooldownTicks = 0;
	private boolean operated = false;

	public TileEntityHydrotreater()
	{
		super(MultiblockHydrotreater.instance, new int[]{4, 4, 3}, 16000, true);
	}

	public FluidTank[] tanks = new FluidTank[]{new FluidTank(24000), new FluidTank(24000)};

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
		tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
		operated = nbt.getBoolean("operated");
		cooldownTicks = nbt.getInteger("cooldownTicks");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
		nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
		nbt.setBoolean("operated", operated);
		nbt.setInteger("cooldownTicks", cooldownTicks);
	}

	@Override
	protected SulfurRecoveryRecipe readRecipeFromNBT(NBTTagCompound tag)
	{
		return SulfurRecoveryRecipe.loadFromNBT(tag);
	}

	@Override
	public void update()
	{
		super.update();
		if (cooldownTicks > 0) cooldownTicks--;
		if (world.isRemote || isDummy())
			return;
		boolean update = false;

		if (!operated)
		{
			operated = true;
		}
		if (energyStorage.getEnergyStored() > 0 && processQueue.size() < this.getProcessQueueMaxLength())
		{
			if (tanks[0].getFluidAmount() > 0)
			{
				SulfurRecoveryRecipe recipe = SulfurRecoveryRecipe.findRecipe(tanks[0].getFluid());
				if (recipe != null)
				{
					MultiblockProcessInMachine<SulfurRecoveryRecipe> process = new MultiblockProcessInMachine(recipe).setInputTanks(new int[]{0});
					if (this.addProcessToQueue(process, true))
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
			cooldownTicks = 6;
		}
		else if (wasActive)
		{
			wasActive = false;
			update = true;
		}

		EnumFacing fw = mirrored ? facing.rotateY() : facing.rotateYCCW();
		if (this.tanks[1].getFluidAmount() > 0)
		{
			FluidStack out = Utils.copyFluidStackWithAmount(this.tanks[1].getFluid(), Math.min(this.tanks[1].getFluidAmount(), 80), false);
			BlockPos outputPos = this.getPos().add(0, -1, 0).offset(fw, 2).offset(facing, 2);
			IFluidHandler output = FluidUtil.getFluidHandler(world, outputPos, fw.getOpposite());
			if (output != null)
			{
				int accepted = output.fill(out, false);
				if (accepted > 0)
				{
					int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
					this.tanks[1].drain(drained, true);
					update = true;
				}
			}
		}

		if (update)
		{
			this.markDirty();
			this.markContainingBlockForUpdate(null);
		}
	}

	@Override
	public float[] getBlockBounds()
	{
		return new float[]{0, 0, 0, 0, 0, 0};
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
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
		return new int[]{25};
	}

	@Override
	public int[] getRedstonePos()
	{
		return new int[]{12};
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{
		BlockPos pos = getPos().offset(facing, 4);
		TileEntity inventoryTile = this.world.getTileEntity(pos);
		if (inventoryTile != null)
			output = Utils.insertStackIntoInventory(inventoryTile, output, facing.getOpposite());
		if (!output.isEmpty())
			Utils.dropStackAtPos(world, pos, output, facing);
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<SulfurRecoveryRecipe> process)
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
	public float getMinProcessDistance(MultiblockProcess<SulfurRecoveryRecipe> process)
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
	public boolean additionalCanProcessCheck(MultiblockProcess<SulfurRecoveryRecipe> process)
	{
		return true;
	}


	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
		this.markContainingBlockForUpdate(null);
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
		return tanks;
	}

	@Override
	public SulfurRecoveryRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		TileEntityHydrotreater master = master();
		if (master != null && pos == 2 && (side == null || side == (mirrored ? facing.rotateYCCW() : facing.rotateY())))
		{
			return new FluidTank[]{master.tanks[0]};
		}
		else if (master != null && pos == 6 && (side == null || side == (mirrored ? facing.rotateY() : facing.rotateYCCW())))
		{
			return new FluidTank[]{master.tanks[1]};
		}
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource)
	{
		if (iTank == 0)
		{
			TileEntityHydrotreater master = this.master();
			FluidStack resourceClone = Utils.copyFluidStackWithAmount(resource, 1000, false);
			FluidStack resourceClone2 = Utils.copyFluidStackWithAmount(master.tanks[0].getFluid(), 1000, false);


			if (master == null || master.tanks[iTank].getFluidAmount() >= master.tanks[iTank].getCapacity())
				return false;
			if (master.tanks[0].getFluid() == null)
			{
				SulfurRecoveryRecipe incompleteRecipes = SulfurRecoveryRecipe.findRecipe(resourceClone);
				return incompleteRecipes != null;
			}
			else
			{
				SulfurRecoveryRecipe incompleteRecipes1 = SulfurRecoveryRecipe.findRecipe(resourceClone);
				SulfurRecoveryRecipe incompleteRecipes2 = SulfurRecoveryRecipe.findRecipe(resourceClone2);
				return incompleteRecipes1 == incompleteRecipes2;
			}
		}

		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return iTank == 1;
	}

	@Override
	public boolean isDummy()
	{
		return true;
	}

	@Override
	public TileEntityHydrotreater master()
	{
		if (offset[0] == 0 && offset[1] == 0 && offset[2] == 0)
		{
			return this;
		}
		TileEntity te = world.getTileEntity(getPos().add(-offset[0], -offset[1], -offset[2]));
		return this.getClass().isInstance(te) ? (TileEntityHydrotreater) te : null;
	}

	@Override
	public TileEntityHydrotreater getTileForPos(int targetPos)
	{
		BlockPos target = getBlockPosForPos(targetPos);
		TileEntity tile = world.getTileEntity(target);
		return tile instanceof TileEntityHydrotreater ? (TileEntityHydrotreater) tile : null;
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		System.out.println(pos);
		return false;
	}
}