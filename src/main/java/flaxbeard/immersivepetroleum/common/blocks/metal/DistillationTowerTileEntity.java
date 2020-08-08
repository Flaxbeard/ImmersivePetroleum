package flaxbeard.immersivepetroleum.common.blocks.metal;

import java.util.Iterator;
import java.util.Set;

import blusunrize.immersiveengineering.api.DirectionalBlockPos;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.MultiFluidTank;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.DistillationTowerMultiblock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class DistillationTowerTileEntity extends PoweredMultiblockTileEntity<DistillationTowerTileEntity, DistillationRecipe> implements IInteractionObjectIE{
	public static TileEntityType<DistillationTowerTileEntity> TYPE;
	
	protected static final int TANK_INPUT=0;
	protected static final int TANK_OUTPUT=1;
	protected static final int INV_0=0;
	protected static final int INV_1=1;
	protected static final int INV_2=2;
	protected static final int INV_3=3;
	
	public NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
	public MultiFluidTank[] tanks = new MultiFluidTank[]{new MultiFluidTank(24000), new MultiFluidTank(24000)};
	public Fluid lastFluidOut = null;
	private int cooldownTicks = 0;
	private boolean operated = false;
	private boolean wasActive = false;
	
	/** Output Capability Reference */
	private CapabilityReference<IItemHandler> output_capref = CapabilityReference.forTileEntity(this, () -> new DirectionalBlockPos(pos, getFacing()), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
	
	public DistillationTowerTileEntity(IETemplateMultiblock multiblockInstance, int energyCapacity, boolean redstoneControl, TileEntityType<? extends DistillationTowerTileEntity> type){
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
			// lastFluidOut = FluidRegistry.getFluid(lastFluidName);
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
		nbt.put("tank0", tanks[TANK_INPUT].writeToNBT(new CompoundNBT()));
		nbt.put("tank1", tanks[TANK_OUTPUT].writeToNBT(new CompoundNBT()));
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
	
	@Override
	public void tick(){
		super.tick();
		if(this.cooldownTicks > 0)
			this.cooldownTicks--;
		if(this.world.isRemote || isDummy())
			return;
		
		boolean update=false;
		if(!this.operated)
			this.operated=true;
		
		if(this.energyStorage.getEnergyStored() > 0 && this.processQueue.size() < getProcessQueueMaxLength()){
			if(this.tanks[TANK_INPUT].getFluidAmount()>0){
				DistillationRecipe recipe=DistillationRecipe.findRecipe(this.tanks[TANK_INPUT].getFluid());
				if(recipe!=null){
					MultiblockProcessInMachine<DistillationRecipe> process=new MultiblockProcessInMachine<DistillationRecipe>(recipe).setInputTanks(0);
					if(addProcessToQueue(process, true)){
						addProcessToQueue(process, false);
						update=true;
					}
				}
			}
		}
		
		if(this.processQueue.size()>0){
			this.wasActive=true;
			this.cooldownTicks=6;
		}else if(this.wasActive){
			this.wasActive=false;
			update=true;
		}
		
		if(this.tanks[1].getFluidAmount()>0){
			ItemStack filledContainer=Utils.fillFluidContainer(this.tanks[TANK_OUTPUT], this.inventory.get(INV_2), this.inventory.get(INV_3), null);
			if(!filledContainer.isEmpty()){
				if (!inventory.get(INV_3).isEmpty())
					inventory.get(INV_3).grow(filledContainer.getCount());
				else if (inventory.get(INV_3).isEmpty())
					inventory.set(INV_3, filledContainer.copy());
				inventory.get(INV_2).shrink(1);
				if (inventory.get(INV_2).getCount() <= 0)
					inventory.set(INV_2, ItemStack.EMPTY);
				update = true;
			}
			
			LazyOptional<IFluidHandler> lazy;
			if(getIsMirrored()){ // TODO When multiblocks have been completed.
				BlockPos outPos=getPos().offset(getFacing().getOpposite());
				lazy=FluidUtil.getFluidHandler(this.world, BlockPos.ZERO, getFacing().getOpposite());
			}else{
				BlockPos outPos=getPos().offset(getFacing());
				lazy=FluidUtil.getFluidHandler(this.world, BlockPos.ZERO, getFacing());
			}
			
			update|=lazy.map(handler->{
				FluidStack targetFluidStack=null;
				if(this.lastFluidOut!=null){
					for(FluidStack f:this.tanks[TANK_OUTPUT].fluids){
						if(f.getFluid()==this.lastFluidOut){
							targetFluidStack=f;
						}
					}
				}
				
				if(targetFluidStack==null){
					int max=0;
					for(FluidStack f:this.tanks[TANK_OUTPUT].fluids){
						if(f.getAmount()>max){
							max=f.getAmount();
							targetFluidStack=f;
						}
					}
				}
				
				this.lastFluidOut=null;
				if(targetFluidStack!=null){
					FluidStack outStack = Utils.copyFluidStackWithAmount(targetFluidStack, Math.min(targetFluidStack.getAmount(), 80), false);
					int accepted=handler.fill(outStack, FluidAction.SIMULATE);
					if(accepted>0){
						this.lastFluidOut=targetFluidStack.getFluid();
						int drained=handler.fill(Utils.copyFluidStackWithAmount(outStack, Math.min(outStack.getAmount(), accepted), false), FluidAction.EXECUTE);
						this.tanks[TANK_OUTPUT].drain(new FluidStack(targetFluidStack.getFluid(), drained), FluidAction.EXECUTE);
						return true;
					}else{
						Iterator<FluidStack> it=this.tanks[TANK_OUTPUT].fluids.iterator();
						while(it.hasNext()){
							targetFluidStack = it.next();
							outStack = Utils.copyFluidStackWithAmount(targetFluidStack, Math.min(targetFluidStack.getAmount(), 80), false);
							accepted = handler.fill(outStack, FluidAction.SIMULATE);
							if(accepted>0){
								this.lastFluidOut=targetFluidStack.getFluid();
								int drained=handler.fill(Utils.copyFluidStackWithAmount(outStack, Math.min(outStack.getAmount(), accepted), false), FluidAction.EXECUTE);
								this.tanks[TANK_OUTPUT].drain(new FluidStack(targetFluidStack.getFluid(), drained), FluidAction.EXECUTE);
								return true;
							}
						}
					}
				}
				
				return false;
			}).orElse(false);
		}
		
		ItemStack emptyContainer=Utils.fillFluidContainer(this.tanks[TANK_OUTPUT], this.inventory.get(INV_2), this.inventory.get(INV_3), null);
		if(!emptyContainer.isEmpty()){
			if (!inventory.get(INV_3).isEmpty())
				inventory.get(INV_3).grow(emptyContainer.getCount());
			else if (inventory.get(INV_3).isEmpty())
				inventory.set(INV_3, emptyContainer.copy());
			inventory.get(INV_2).shrink(1);
			if (inventory.get(INV_2).getCount() <= 0)
				inventory.set(INV_2, ItemStack.EMPTY);
			update = true;
		}
		
		if(update){
			markDirty();
			markContainingBlockForUpdate(null);
		}
	}
	
	@Override
	public NonNullList<ItemStack> getInventory(){
		return this.inventory;
	}
	
	@Override
	public boolean isStackValid(int slot, ItemStack stack){
		if(stack.getItem() instanceof BucketItem)
			return true;
		return false;
	}
	
	@Override
	public int getSlotLimit(int slot){
		return 64;
	}
	
	@Override
	public void doGraphicalUpdates(int slot){
		markDirty();
		markContainingBlockForUpdate(null);
	}
	
	@Override
	public IInteractionObjectIE getGuiMaster(){
		return master();
	}
	
	@Override
	public boolean canUseGui(PlayerEntity player){
		return false;
	}
	
	@Override
	protected DistillationRecipe getRecipeForId(ResourceLocation id){
		return null;
	}
	
	@Override
	public Set<BlockPos> getEnergyPos(){
		return null; // TODO TBD
	}
	
	@Override
	public Set<BlockPos> getRedstonePos(){
		return null; // TODO TBD
	}
	
	@Override
	public IFluidTank[] getInternalTanks(){
		return this.tanks;
	}
	
	@Override
	public DistillationRecipe findRecipeForInsertion(ItemStack inserting){
		return null;
	}
	
	@Override
	public int[] getOutputSlots(){
		return null;
	}
	
	@Override
	public int[] getOutputTanks(){
		return new int[]{TANK_OUTPUT};
	}
	
	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<DistillationRecipe> process){
		return true;
	}
	
	@Override
	public void doProcessOutput(ItemStack output){
	}
	
	@Override
	public void doProcessFluidOutput(FluidStack output){
	}
	
	@Override
	public void onProcessFinish(MultiblockProcess<DistillationRecipe> process){
	}
	
	@Override
	public int getMaxProcessPerTick(){
		return 0;
	}
	
	@Override
	public int getProcessQueueMaxLength(){
		return 0;
	}
	
	@Override
	public float getMinProcessDistance(MultiblockProcess<DistillationRecipe> process){
		return 0;
	}
	
	@Override
	public boolean isInWorldProcessingMachine(){
		return false;
	}
	
	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side){
		return new FluidTank[0];
	}
	
	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resource){
		return false;
	}
	
	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side){
		return false;
	}
	
}
