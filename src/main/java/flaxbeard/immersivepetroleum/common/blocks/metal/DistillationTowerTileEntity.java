package flaxbeard.immersivepetroleum.common.blocks.metal;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableSet;

import blusunrize.immersiveengineering.api.DirectionalBlockPos;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.MultiFluidTank;
import blusunrize.immersiveengineering.common.util.shapes.CachedShapesWithTransform;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

public class DistillationTowerTileEntity extends PoweredMultiblockTileEntity<DistillationTowerTileEntity, DistillationRecipe> implements IInteractionObjectIE, IBlockBounds{
	public static class DistillationTowerParentTileEntity extends DistillationTowerTileEntity{
		public static TileEntityType<DistillationTowerParentTileEntity> TYPE;
		
		@Override
		public TileEntityType<?> getType(){
			return TYPE;
		}
		
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
	
	public DistillationTowerTileEntity(){
		super(DistillationTowerMultiblock.INSTANCE, 16000, true, null);
	}
	
	@Override
	public TileEntityType<?> getType(){
		return TYPE;
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
			lastFluidOut = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(lastFluidName));
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
		
		if(list.size()==0) // Incase it loaded none
			list=NonNullList.withSize(4, ItemStack.EMPTY);
		else if(list.size()<4) // Padding incase it loaded less than 4
			while(list.size()<4)
				list.add(ItemStack.EMPTY);
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
//				BlockPos pos=getBlockPosForPos(new BlockPos(0,0,0)); // Perhaps with this?
				
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
		
		ItemStack emptyContainer=Utils.fillFluidContainer(this.tanks[TANK_OUTPUT],
				this.inventory.get(INV_2),
				this.inventory.get(INV_3), null);
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
		return stack==ItemStack.EMPTY || stack.getItem() instanceof BucketItem;
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
		return DistillationRecipe.recipes.get(id);
	}
	
	@Override
	public Set<BlockPos> getEnergyPos(){
		return ImmutableSet.of(new BlockPos(3,1,3));
	}
	
	@Override
	public Set<BlockPos> getRedstonePos(){
		return ImmutableSet.of(new BlockPos(0, 1, 3));
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
	
	/** Output Capability Reference */
	private CapabilityReference<IItemHandler> output_capref = CapabilityReference.forTileEntity(this, () -> new DirectionalBlockPos(pos, getFacing()), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
	
	@Override
	public void doProcessOutput(ItemStack output){
		this.output_capref=CapabilityReference.forTileEntity(this, () -> new DirectionalBlockPos(getPos(), getFacing().getOpposite()), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		
		//output = Utils.insertStackIntoInventory(this.output_capref, output, false);
		if(!output.isEmpty())
			Utils.dropStackAtPos(this.world, getPos(), output, getFacing().getOpposite());
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
		return 1;
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
		DistillationTowerTileEntity master=master();
		if(master!=null){
			if(this.posInMultiblock.equals(new BlockPos(1,0,3)) && (side==null || side==getFacing().getOpposite())){ // OUTPUT
				return new IFluidTank[]{master.tanks[TANK_OUTPUT]};
			}
			if(this.posInMultiblock.equals(new BlockPos(3,0,3)) && (side==null || side==getFacing().rotateYCCW().getOpposite())){ // INPUT
				return new IFluidTank[]{master.tanks[TANK_INPUT]};
			}
		}
		return new FluidTank[0];
	}
	
	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resource){
		if(this.posInMultiblock.equals(new BlockPos(1,0,3)) && (side==null || side==getFacing().getOpposite())){
			DistillationTowerTileEntity master=master();
			if(iTank!=TANK_INPUT || master==null || master.tanks[TANK_INPUT].getFluidAmount()>=master.tanks[TANK_INPUT].getCapacity())
				return false;
			
			if(master!=null){
				if(master.tanks[iTank].getFluid().isEmpty()){
					return DistillationRecipe.findRecipe(resource)!=null;
				}else{
					return master.tanks[iTank].getFluid().equals(resource);
				}
			}
		}
		return false;
	}
	
	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side){
		return false;
	}
	
	
	private static final CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES =
			CachedShapesWithTransform.createForMultiblock(DistillationTowerTileEntity::getShape);
	@Override
	public VoxelShape getBlockBounds(ISelectionContext ctx){
		return SHAPES.get(this.posInMultiblock, Pair.of(getFacing(), getIsMirrored()));
	}
	
	private static List<AxisAlignedBB> getShape(BlockPos posInMultiblock){
		return Arrays.asList(new AxisAlignedBB(0, 0, 0, 1, 1, 1));
	}
}
