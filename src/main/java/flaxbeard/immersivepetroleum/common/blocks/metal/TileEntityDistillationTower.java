package flaxbeard.immersivepetroleum.common.blocks.metal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.api.DirectionalBlockPos;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.MultiFluidTank;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.DistillationTowerMultiblock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

@Deprecated
public class TileEntityDistillationTower extends PoweredMultiblockTileEntity<TileEntityDistillationTower, DistillationRecipe> implements IAdvancedSelectionBounds, IAdvancedCollisionBounds, IInteractionObjectIE{
	//public static TileEntityType<TileEntityDistillationTower> TYPE;
	
	@Deprecated
	public static class TileEntityDistillationTowerParent extends TileEntityDistillationTower{
		@Override
		@OnlyIn(Dist.CLIENT)
		public net.minecraft.util.math.AxisAlignedBB getRenderBoundingBox(){
			BlockPos pos = getPos();
			return new AxisAlignedBB(pos.add(-4, -16, -4), pos.add(4, 16, 4));
		}
		
		@Override
		public boolean isDummy(){
			return false;
		}
		
		@Override
		@OnlyIn(Dist.CLIENT)
		public double getMaxRenderDistanceSquared(){
			return super.getMaxRenderDistanceSquared() * IEConfig.GENERAL.increasedTileRenderdistance.get();
		}
	}
	
	public NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
	public MultiFluidTank[] tanks = new MultiFluidTank[]{new MultiFluidTank(24000), new MultiFluidTank(24000)};
	public Fluid lastFluidOut = null;
	private int cooldownTicks = 0;
	private boolean operated = false;
	private boolean wasActive = false;
	
	/** Output Capability Reference*/
	private CapabilityReference<IItemHandler> output_capref = CapabilityReference.forTileEntity(this, () -> new DirectionalBlockPos(pos, getFacing()), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
	
	public TileEntityDistillationTower(){
		// super(MultiblockDistillationTower.instance, new int[]{16, 4, 4}, 16000, true);
		super(DistillationTowerMultiblock.INSTANCE, 16000, true, TYPE);
	}
	
	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket){
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompound("tank0"));
		tanks[1].readFromNBT(nbt.getCompound("tank1"));
		operated = nbt.getBoolean("operated");
		cooldownTicks = nbt.getInt("cooldownTicks");
		
		String lastFluidName = nbt.getString("lastFluidOut");
		if(lastFluidName.length() > 0){
			lastFluidOut = null;
		}else{
			lastFluidOut = null;
		}
		
		if(!descPacket){
			inventory = readInventory(nbt.getCompound("inventory"));
		}
	}
	
	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket){
		super.writeCustomNBT(nbt, descPacket);
		nbt.put("tank0", tanks[0].writeToNBT(new CompoundNBT()));
		nbt.put("tank1", tanks[1].writeToNBT(new CompoundNBT()));
		nbt.putBoolean("operated", operated);
		nbt.putInt("cooldownTicks", cooldownTicks);
		nbt.putString("lastFluidOut", lastFluidOut == null ? "" : lastFluidOut.getRegistryName().toString());
		if(!descPacket){
			nbt.put("inventory", writeInventory(inventory));
		}
	}
	
	protected NonNullList<ItemStack> readInventory(CompoundNBT nbt){
		NonNullList<ItemStack> list=NonNullList.create();
		ItemStackHelper.loadAllItems(nbt, list);
		return list;
	}
	
	protected CompoundNBT writeInventory(NonNullList<ItemStack> list){
		return ItemStackHelper.saveAllItems(new CompoundNBT(), list);
	}
	
	public boolean shouldRenderAsActive(){
		return cooldownTicks > 0 || super.shouldRenderAsActive();
	}
	
	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, Vec3d hitVec){
		if(operated){
			return super.hammerUseSide(side, player, hitVec);
		}
		return true;
	}

	@Override
	public void tick()
	{
		super.tick();
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
				DistillationRecipe recipe = DistillationRecipe.findRecipe(tanks[0].getFluid());
				if (recipe != null)
				{
					MultiblockProcessInMachine<DistillationRecipe> process = new MultiblockProcessInMachine<DistillationRecipe>(recipe).setInputTanks(new int[]{0});
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


		if (this.tanks[1].getFluidAmount() > 0)
		{

			ItemStack filledContainer = Utils.fillFluidContainer(tanks[1], inventory.get(2), inventory.get(3), null);
			if (!filledContainer.isEmpty())
			{
				// TODO Convert to using Tags
				if (!inventory.get(3).isEmpty())// && OreDictionary.itemMatches(inventory.get(3), filledContainer, true))
					inventory.get(3).grow(filledContainer.getCount());
				else if (inventory.get(3).isEmpty())
					inventory.set(3, filledContainer.copy());
				inventory.get(2).shrink(1);
				if (inventory.get(2).getCount() <= 0)
					inventory.set(2, ItemStack.EMPTY);
				update = true;
			}
			
			IFluidHandler output;
			if(getIsMirrored()){
				BlockPos outputPos = this.getPos().offset(getFacing().getOpposite(), 1).offset(getFacing().rotateY(), 1).offset(Direction.DOWN, 1);
				output = FluidUtil.getFluidHandler(world, outputPos, getFacing().rotateYCCW()).orElse(null);
			}else{
				BlockPos outputPos = this.getPos().offset(getFacing().getOpposite(), 1).offset(getFacing().rotateY().getOpposite(), 1).offset(Direction.DOWN, 1);
				output = FluidUtil.getFluidHandler(world, outputPos, getFacing().rotateY()).orElse(null);
			}
			
			if(output != null){
				FluidStack targetFluidStack = null;
				if(lastFluidOut != null){
					for(FluidStack stack:this.tanks[1].fluids){
						if(stack.getFluid() == lastFluidOut){
							targetFluidStack = stack;
						}
					}
				}
				if(targetFluidStack == null){
					int max = 0;
					for(FluidStack stack:this.tanks[1].fluids){
						if(stack.getAmount() > max){
							max = stack.getAmount();
							targetFluidStack = stack;
						}
					}
				}
				
				Iterator<FluidStack> iterator = this.tanks[1].fluids.iterator();
				
				lastFluidOut = null;
				if(targetFluidStack != null)
				{
					FluidStack out = Utils.copyFluidStackWithAmount(targetFluidStack, Math.min(targetFluidStack.getAmount(), 80), false);
					int accepted = output.fill(out, FluidAction.SIMULATE);
					if(accepted > 0){
						lastFluidOut = targetFluidStack.getFluid();
						int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), FluidAction.EXECUTE);
						this.tanks[1].drain(new FluidStack(targetFluidStack.getFluid(), drained), FluidAction.SIMULATE);
						update = true;
					}else{
						while(iterator.hasNext()){
							targetFluidStack = iterator.next();
							out = Utils.copyFluidStackWithAmount(targetFluidStack, Math.min(targetFluidStack.getAmount(), 80), false);
							accepted = output.fill(out, FluidAction.SIMULATE);
							if(accepted > 0){
								lastFluidOut = targetFluidStack.getFluid();
								int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), FluidAction.EXECUTE);
								this.tanks[1].drain(new FluidStack(targetFluidStack.getFluid(), drained), FluidAction.EXECUTE);
								update = true;
								break;
							}
						}
					}
				}
			}
		}

		ItemStack emptyContainer = Utils.drainFluidContainer(tanks[0], inventory.get(0), inventory.get(1), null);
		if (!emptyContainer.isEmpty() && emptyContainer.getCount() > 0)
		{
			// TODO Convert to using Tags
			if (!inventory.get(1).isEmpty()) // && OreDictionary.itemMatches(inventory.get(1), emptyContainer, true))
				inventory.get(1).grow(emptyContainer.getCount());
			else if (inventory.get(1).isEmpty())
				inventory.set(1, emptyContainer.copy());
			inventory.get(0).shrink(1);
			if (inventory.get(0).getCount() <= 0)
				inventory.set(0, ItemStack.EMPTY);
			update = true;
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
		/*if(pos==19)
			return new float[]{facing==Direction.WEST?.5f:0,0,facing==Direction.NORTH?.5f:0, facing==Direction.EAST?.5f:1,1,facing==Direction.SOUTH?.5f:1};
		if(pos==17)
			return new float[]{.0625f,0,.0625f, .9375f,1,.9375f};*/

		return new float[]{0, 0, 0, 0, 0, 0};
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		Direction fl = getFacing();
		Direction fw = getFacing().rotateY();
		if (getIsMirrored())
			fw = fw.getOpposite();

		int pos = 0; // FIXME Temporary Fix

		int y = pos / 16;
		int x = (pos % 16) / 4;
		int z = pos % 4;
		if (pos == 2 || pos == 3 || pos == 4 || pos == 7 || pos == 11 || pos == 13 || pos == 15)
		{
			return Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if (pos == 12)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			float minX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 2F / 16F : ((fl == Direction.EAST) ? 10F / 16F : 2F / 16F);
			float maxX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 4F / 16F : ((fl == Direction.EAST) ? 14F / 16F : 6F / 16F);
			float minZ = (fl == Direction.EAST || fl == Direction.WEST) ? 2F / 16F : ((fl == Direction.SOUTH) ? 10F / 16F : 2F / 16F);
			float maxZ = (fl == Direction.EAST || fl == Direction.WEST) ? 4F / 16F : ((fl == Direction.SOUTH) ? 14F / 16F : 6F / 16F);
			list.add(new AxisAlignedBB(minX, 0.5, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 12F / 16F : minX;
			maxX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 14F / 16F : maxX;
			minZ = (fl == Direction.EAST || fl == Direction.WEST) ? 12F / 16F : minZ;
			maxZ = (fl == Direction.EAST || fl == Direction.WEST) ? 14F / 16F : maxZ;
			list.add(new AxisAlignedBB(minX, 0.5, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		else if (pos == 12 + 16)
		{
			float minX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0F : ((fl == Direction.EAST) ? .5F : 0F);
			float maxX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1F : ((fl == Direction.EAST) ? 1F : .5F);
			float minZ = (fl == Direction.EAST || fl == Direction.WEST) ? 0F : ((fl == Direction.SOUTH) ? .5F : 0F);
			float maxZ = (fl == Direction.EAST || fl == Direction.WEST) ? 1F : ((fl == Direction.SOUTH) ? 1F : .5F);
			return Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		else if (pos == 16 + 7 || pos == 32 + 7)
		{
			return null;
		}
		else if (pos == 32 + 1)
		{
			float minX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? .125F : ((fl == Direction.EAST) ? .125F : 0F);
			float maxX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? .875F : ((fl == Direction.EAST) ? 1F : .875F);
			float minZ = (fl == Direction.EAST || fl == Direction.WEST) ? .125F : ((fl == Direction.SOUTH) ? .125F : 0F);
			float maxZ = (fl == Direction.EAST || fl == Direction.WEST) ? .875F : ((fl == Direction.SOUTH) ? 1F : .875F);
			return Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1.125F, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		else if (pos % 16 == 7)
		{
			List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
			if (y % 4 > 2 || (fl != Direction.EAST && fl != Direction.WEST))
			{
				float bottom = (fl != Direction.EAST && fl != Direction.WEST) ? 0F : 0.5f;
				list.add(new AxisAlignedBB(0, bottom, 0, 0, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				list.add(new AxisAlignedBB(1, bottom, 0, 1, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			if (y % 4 > 2 || (fl != Direction.NORTH && fl != Direction.SOUTH))
			{
				float bottom = (fl != Direction.NORTH && fl != Direction.SOUTH) ? 0F : 0.5f;
				list.add(new AxisAlignedBB(0, bottom, 0, 1, 1, 0).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				list.add(new AxisAlignedBB(0, bottom, 1, 1, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			return list;
		}
		else if (y > 0 && x == 2 && z == 0)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0.1875f, 0, 0.1875f, 0.8125f, 1, 0.8125f).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if (y > 0 && y % 4 == 0)
			{
				list.add(new AxisAlignedBB(0, 0.5F, 0, 1, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			return list;
		}
		else if (y > 0 && y % 4 == 0 && (x == 0 || z == 0 || x == 3 || z == 3))
		{
			return Lists.newArrayList(new AxisAlignedBB(0, 0.5F, 0, 1, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		/*
		if(pos==0||pos==4||pos==10||pos==14)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0,0,0, 1,.5f,1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			if(pos>=10)
				fl = fl.getOpposite();
			if(pos%10==0)
				fw = fw.getOpposite();

			float minX = fl==Direction.WEST?0: fl==Direction.EAST?.75f: fw==Direction.WEST?.5f: .25f;
			float maxX = fl==Direction.EAST?1: fl==Direction.WEST?.25f: fw==Direction.EAST?.5f: .75f;
			float minZ = fl==Direction.NORTH?0: fl==Direction.SOUTH?.75f: fw==Direction.NORTH?.5f: .25f;
			float maxZ = fl==Direction.SOUTH?1: fl==Direction.NORTH?.25f: fw==Direction.SOUTH?.5f: .75f;
			list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1.375f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

			if(pos==4)
			{
				minX = fl==Direction.WEST?.625f: fl==Direction.EAST?.125f: .125f;
				maxX = fl==Direction.EAST?.375f: fl==Direction.WEST?.875f: .25f;
				minZ = fl==Direction.NORTH?.625f: fl==Direction.SOUTH?.125f: .125f;
				maxZ = fl==Direction.SOUTH?.375f: fl==Direction.NORTH?.875f: .25f;
				list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

				minX = fl==Direction.WEST?.625f: fl==Direction.EAST?.125f: .75f;
				maxX = fl==Direction.EAST?.375f: fl==Direction.WEST?.875f: .875f;
				minZ = fl==Direction.NORTH?.625f: fl==Direction.SOUTH?.125f: .75f;
				maxZ = fl==Direction.SOUTH?.375f: fl==Direction.NORTH?.875f: .875f;
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

			float minX = fl==Direction.WEST?0: fl==Direction.EAST?.75f: fw==Direction.WEST?.75f: 0;
			float maxX = fl==Direction.EAST?1: fl==Direction.WEST?.25f: fw==Direction.EAST?.25f: 1;
			float minZ = fl==Direction.NORTH?0: fl==Direction.SOUTH?.75f: fw==Direction.NORTH?.75f: 0;
			float maxZ = fl==Direction.SOUTH?1: fl==Direction.NORTH?.25f: fw==Direction.SOUTH?.25f: 1;
			list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1.375f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			return list;
		}

		if((pos==20||pos==24 || pos==25||pos==29)||(pos==35||pos==39 || pos==40||pos==44))
		{
			List<AxisAlignedBB> list = Lists.newArrayList();
			if(pos%5==4)
				fw = fw.getOpposite();
			float minX = fl==Direction.WEST?-.25f: fl==Direction.EAST?-.25f: fw==Direction.WEST?-1f: .5f;
			float maxX = fl==Direction.EAST?1.25f: fl==Direction.WEST?1.25f: fw==Direction.EAST?2: .5f;
			float minZ = fl==Direction.NORTH?-.25f: fl==Direction.SOUTH?-.25f: fw==Direction.NORTH?-1f: .5f;
			float maxZ = fl==Direction.SOUTH?1.25f: fl==Direction.NORTH?1.25f: fw==Direction.SOUTH?2: .5f;
			float minY = pos<35?.5f:-.5f;
			float maxY = pos<35?2f:1f;
			if(pos%15>=10)
			{
				minX += fl==Direction.WEST?1: fl==Direction.EAST?-1: 0;
				maxX += fl==Direction.WEST?1: fl==Direction.EAST?-1: 0;
				minZ += fl==Direction.NORTH?1: fl==Direction.SOUTH?-1: 0;
				maxZ += fl==Direction.NORTH?1: fl==Direction.SOUTH?-1: 0;
			}
			list.add(new AxisAlignedBB(minX,minY,minZ, maxX,maxY,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			return list;
		}
		if((pos==21||pos==23 || pos==26||pos==28)||(pos==36||pos==38 || pos==41||pos==43))
		{
			List<AxisAlignedBB> list = Lists.newArrayList();
			if(pos%5==3)
				fw = fw.getOpposite();
			float minX = fl==Direction.WEST?-.25f: fl==Direction.EAST?-.25f: fw==Direction.WEST?0f:-.5f;
			float maxX = fl==Direction.EAST?1.25f: fl==Direction.WEST?1.25f: fw==Direction.EAST?1f: 1.5f;
			float minZ = fl==Direction.NORTH?-.25f: fl==Direction.SOUTH?-.25f: fw==Direction.NORTH?0:-.5f;
			float maxZ = fl==Direction.SOUTH?1.25f: fl==Direction.NORTH?1.25f: fw==Direction.SOUTH?1f: 1.5f;
			float minY = pos<35?.5f:-.5f;
			float maxY = pos<35?2f:1f;
			if(pos%15>=10)
			{
				minX += fl==Direction.WEST?1: fl==Direction.EAST?-1: 0;
				maxX += fl==Direction.WEST?1: fl==Direction.EAST?-1: 0;
				minZ += fl==Direction.NORTH?1: fl==Direction.SOUTH?-1: 0;
				maxZ += fl==Direction.NORTH?1: fl==Direction.SOUTH?-1: 0;
			}
			list.add(new AxisAlignedBB(minX,minY,minZ, maxX,maxY,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			return list;
		}*/

		List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
		list.add(new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		return list;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedCollisionBounds()
	{
		//List list = new ArrayList<AxisAlignedBB>(); // Waste of ram
		return getAdvancedSelectionBounds();
	}

	@Override
	public Set<BlockPos> getEnergyPos(){
		return null; // TODO TBD
		// return new int[]{16};
	}
	
	@Override
	public Set<BlockPos> getRedstonePos(){
		return null; // TODO TBD
		// return new int[]{28};
	}
	
	@Override
	public boolean isInWorldProcessingMachine(){
		return false;
	}
	
	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<DistillationRecipe> process){
		return true;
	}
	
	@Override
	public void doProcessOutput(ItemStack output)
	{
		BlockPos pos = getPos().offset(getFacing(), 1).offset(this.getIsMirrored() ? getFacing().getOpposite().rotateY() : getFacing().rotateY(), 2).offset(Direction.DOWN, 1);
		TileEntity inventoryTile = this.world.getTileEntity(pos);
		if (inventoryTile != null)
			output = Utils.insertStackIntoInventory(output_capref, output, false);
			//output = Utils.insertStackIntoInventory(inventoryTile, output, getFacing().getOpposite());
		if (!output.isEmpty())
			Utils.dropStackAtPos(world, pos, output, getFacing());
	}

	@Override
	public void doProcessFluidOutput(FluidStack output){
	}

	@Override
	public void onProcessFinish(MultiblockProcess<DistillationRecipe> process){
	}

	@Override
	public int getMaxProcessPerTick(){
		return 1;
	}

	@Override
	public int getProcessQueueMaxLength(){
		return 1;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<DistillationRecipe> process){
		return 0;
	}

	@Override
	public NonNullList<ItemStack> getInventory(){
		return inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack){
		return true;
	}

	@Override
	public int getSlotLimit(int slot){
		return 64;
	}

	@Override
	public int[] getOutputSlots(){
		return null;
	}

	@Override
	public int[] getOutputTanks(){
		return new int[]{1};
	}

	@Override
	public IFluidTank[] getInternalTanks(){
		return tanks;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side){
		TileEntityDistillationTower master = this.master();
		int pos = 0; // FIXME Temporary Fix
		if(master != null){
			if(pos == 0 && (side == null || side == getFacing().getOpposite())){
				return new MultiFluidTank[]{master.tanks[0]};
			}else if(pos == 8 && (side == null || side == getFacing().getOpposite().rotateY() || side == getFacing().rotateY())){
				return new IFluidTank[]{master.tanks[1]};
			}
		}
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resource)
	{
		int pos = 0; // FIXME Temporary Fix
		if (pos == 0 && (side == null || side == getFacing().getOpposite()))
		{
			TileEntityDistillationTower master = this.master();
			FluidStack resourceClone = Utils.copyFluidStackWithAmount(resource, 1000, false);
			FluidStack resourceClone2 = Utils.copyFluidStackWithAmount(master.tanks[0].getFluid(), 1000, false);


			if (master == null || master.tanks[iTank].getFluidAmount() >= master.tanks[iTank].getCapacity())
				return false;
			if (master.tanks[0].getFluid() == null)
			{
				DistillationRecipe incompleteRecipes = DistillationRecipe.findRecipe(resourceClone);
				return incompleteRecipes != null;
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
	protected boolean canDrainTankFrom(int iTank, Direction side){
		int pos = 0; // FIXME Temporary Fix
		return(pos == 8 && (side == null || side == getFacing().getOpposite().rotateY() || side == getFacing().rotateY()));
	}
	
	@Override
	public void doGraphicalUpdates(int slot){
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}
	
	@Override
	public DistillationRecipe findRecipeForInsertion(ItemStack inserting){
		return null;
	}
	
	protected DistillationRecipe readRecipeFromNBT(CompoundNBT tag){
		return DistillationRecipe.loadFromNBT(tag);
	}
	
	@Override
	protected DistillationRecipe getRecipeForId(ResourceLocation id){
		return null; // TODO
	}

	@Override
	public boolean canUseGui(PlayerEntity player){
		return false;
	}

	@Override
	public IInteractionObjectIE getGuiMaster(){
		return master();
	}

	public boolean isLadder()
	{
		int pos = 0; // FIXME Temporary Fix
		return pos % 16 == 7;
	}

	@Override
	public TileEntityDistillationTower getTileForPos(BlockPos targetPosInMB){
		return super.getTileForPos(targetPosInMB);
	}
	
	/*
	@Override
	public TileEntityDistillationTower getTileForPos(int targetPos)
	{
		BlockPos target = getBlockPosForPos(targetPos);
		TileEntity tile = world.getTileEntity(target);
		return tile instanceof TileEntityDistillationTower ? (TileEntityDistillationTower) tile : null;
	}
	*/
}