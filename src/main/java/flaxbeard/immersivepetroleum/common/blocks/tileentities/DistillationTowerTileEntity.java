package flaxbeard.immersivepetroleum.common.blocks.tileentities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableSet;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.MultiFluidTank;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.common.IPTileTypes;
import flaxbeard.immersivepetroleum.common.multiblocks.DistillationTowerMultiblock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class DistillationTowerTileEntity extends PoweredMultiblockTileEntity<DistillationTowerTileEntity, DistillationRecipe> implements IInteractionObjectIE, IBlockBounds{
	/** Input Tank ID */
	public static final int TANK_INPUT = 0;
	
	/** Output Tank ID */
	public static final int TANK_OUTPUT = 1;
	
	/** Inventory Fluid Input (Filled Bucket) */
	public static final int INV_0 = 0;
	
	/** Inventory Fluid Input (Empty Bucket) */
	public static final int INV_1 = 1;
	
	/** Inventory Fluid Output (Empty Bucket) */
	public static final int INV_2 = 2;
	
	/** Inventory Fluid Output (Filled Bucket) */
	public static final int INV_3 = 3;
	
	/** Template-Location of the Fluid Input Port. (3 0 3) */
	public static final BlockPos Fluid_IN = new BlockPos(3, 0, 3);
	
	/** Template-Location of the Fluid Output Port. (1 0 3) */
	public static final BlockPos Fluid_OUT = new BlockPos(1, 0, 3);
	
	/** Template-Location of the Item Output Port. (0 0 1) */
	public static final BlockPos Item_OUT = new BlockPos(0, 0, 1);
	
	/** Template-Location of the Energy Input Port. (3 1 3) */
	public static final Set<BlockPos> Energy_IN = ImmutableSet.of(new BlockPos(3, 1, 3));
	
	/** Template-Location of the Redstone Input Port. (0 1 3) */
	public static final Set<BlockPos> Redstone_IN = ImmutableSet.of(new BlockPos(0, 1, 3));
	
	public NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
	public MultiFluidTank[] tanks = new MultiFluidTank[]{new MultiFluidTank(24000), new MultiFluidTank(24000)};
	private int cooldownTicks = 0;
	private boolean wasActive = false;
	
	public DistillationTowerTileEntity(){
		super(DistillationTowerMultiblock.INSTANCE, 16000, true, null);
	}
	
	@Override
	public TileEntityType<?> getType(){
		return IPTileTypes.TOWER.get();
	}
	
	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket){
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompound("tank0"));
		tanks[1].readFromNBT(nbt.getCompound("tank1"));
		cooldownTicks = nbt.getInt("cooldownTicks");
		
		if(!descPacket){
			this.inventory = readInventory(nbt.getCompound("inventory"));
		}
	}
	
	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket){
		super.writeCustomNBT(nbt, descPacket);
		nbt.put("tank0", tanks[TANK_INPUT].writeToNBT(new CompoundNBT()));
		nbt.put("tank1", tanks[TANK_OUTPUT].writeToNBT(new CompoundNBT()));
		nbt.putInt("cooldownTicks", cooldownTicks);
		if(!descPacket){
			nbt.put("inventory", writeInventory(this.inventory));
		}
	}
	
	protected NonNullList<ItemStack> readInventory(CompoundNBT nbt){
		NonNullList<ItemStack> list = NonNullList.create();
		ItemStackHelper.loadAllItems(nbt, list);
		
		if(list.size() == 0){ // Incase it loaded none
			list = this.inventory.size() == 4 ? this.inventory : NonNullList.withSize(4, ItemStack.EMPTY);
		}else if(list.size() < 4){ // Padding incase it loaded less than 4
			while(list.size() < 4)
				list.add(ItemStack.EMPTY);
		}
		return list;
	}
	
	protected CompoundNBT writeInventory(NonNullList<ItemStack> list){
		return ItemStackHelper.saveAllItems(new CompoundNBT(), list);
	}
	
	// DEBUGGING
	public boolean enableStepping = false;
	public int step = 0;
	public boolean step(){
		if(this.step > 0){
			this.step--;
			return true;
		}
		return false;
	}
	
	@Override
	public void tick(){
		if(this.enableStepping && !step()){
			return;
		}
		
		if(this.cooldownTicks > 0){
			this.cooldownTicks--;
		}
		
		checkForNeedlessTicking();
		
		if(this.world.isRemote || isDummy() || isRSDisabled()){
			return;
		}
		
		boolean update = false;
		if(this.energyStorage.getEnergyStored() > 0 && this.processQueue.size() < getProcessQueueMaxLength()){
			if(this.tanks[TANK_INPUT].getFluidAmount() > 0){
				DistillationRecipe recipe = DistillationRecipe.findRecipe(this.tanks[TANK_INPUT].getFluid());
				if(recipe != null && this.tanks[TANK_INPUT].getFluidAmount() >= recipe.getInputFluid().getAmount() && this.energyStorage.getEnergyStored() >= recipe.getTotalProcessEnergy()){
					MultiblockProcessInMachine<DistillationRecipe> process = new MultiblockProcessInMachine<DistillationRecipe>(recipe).setInputTanks(TANK_INPUT);
					if(addProcessToQueue(process, true)){
						addProcessToQueue(process, false);
						update = true;
					}
				}
			}
		}
		
		if(!this.processQueue.isEmpty()){
			this.wasActive = true;
			this.cooldownTicks = 6;
			update = true;
		}else if(this.wasActive){
			this.wasActive = false;
			update = true;
		}
		
		super.tick();
		
		if(this.inventory.get(INV_0) != ItemStack.EMPTY && this.tanks[TANK_INPUT].getFluidAmount() < this.tanks[TANK_INPUT].getCapacity()){
			ItemStack emptyContainer = Utils.drainFluidContainer(this.tanks[TANK_INPUT], this.inventory.get(INV_0), this.inventory.get(INV_1));
			if(!emptyContainer.isEmpty()){
				if(!this.inventory.get(INV_1).isEmpty() && ItemHandlerHelper.canItemStacksStack(this.inventory.get(INV_1), emptyContainer)){
					this.inventory.get(INV_1).grow(emptyContainer.getCount());
				}else if(this.inventory.get(INV_1).isEmpty()){
					this.inventory.set(INV_1, emptyContainer.copy());
				}
				
				this.inventory.get(INV_0).shrink(1);
				if(this.inventory.get(INV_0).getCount() <= 0){
					this.inventory.set(INV_0, ItemStack.EMPTY);
				}
				update = true;
			}
		}
		
		if(this.tanks[TANK_OUTPUT].getFluidAmount() > 0){
			if(this.inventory.get(INV_2) != ItemStack.EMPTY){
				
				if(this.tanks[TANK_OUTPUT].getFluidTypes() > 0){
					MultiFluidTank outTank = this.tanks[TANK_OUTPUT];
					
					for(int i = outTank.getFluidTypes() - 1;i >= 0;i--){
						FluidStack fs = outTank.getFluidInTank(i);
						
						if(fs.getAmount() > 0){
							ItemStack filledContainer = fillFluidContainer(outTank, fs, this.inventory.get(INV_2), this.inventory.get(INV_3));
							if(!filledContainer.isEmpty()){
								if(this.inventory.get(INV_3).getCount() == 1 && !Utils.isFluidContainerFull(filledContainer)){
									this.inventory.set(INV_3, filledContainer.copy());
								}else{
									if(!this.inventory.get(INV_3).isEmpty() && ItemHandlerHelper.canItemStacksStack(this.inventory.get(INV_3), filledContainer)){
										this.inventory.get(INV_3).grow(filledContainer.getCount());
									}else if(this.inventory.get(INV_3).isEmpty()){
										this.inventory.set(INV_3, filledContainer.copy());
									}
									
									this.inventory.get(INV_2).shrink(1);
									if(this.inventory.get(INV_2).getCount() <= 0){
										this.inventory.set(INV_2, ItemStack.EMPTY);
									}
								}
								
								update = true;
								break;
							}
						}
					}
				}
			}
			
			update |= FluidUtil.getFluidHandler(this.world, getBlockPosForPos(Fluid_OUT).offset(getFacing().getOpposite()), getFacing()).map(output -> {
				boolean ret = false;
				if(this.tanks[TANK_OUTPUT].fluids.size() > 0){
					List<FluidStack> toDrain = new ArrayList<>();
					
					// Tries to Output the output-fluids in parallel
					for(FluidStack target:this.tanks[TANK_OUTPUT].fluids){
						FluidStack outStack = Utils.copyFluidStackWithAmount(target, Math.min(target.getAmount(), 100), false);
						
						int accepted = output.fill(outStack, FluidAction.SIMULATE);
						if(accepted > 0){
							int drained = output.fill(Utils.copyFluidStackWithAmount(outStack, Math.min(outStack.getAmount(), accepted), false), FluidAction.EXECUTE);
							
							toDrain.add(new FluidStack(target.getFluid(), drained));
							ret |= true;
						}
					}
					
					// If this were to be done in the for-loop it would throw a concurrent exception
					toDrain.forEach(fluid -> this.tanks[TANK_OUTPUT].drain(fluid, FluidAction.EXECUTE));
				}
				
				return ret;
			}).orElse(false);
		}
		
		if(update){
			updateMasterBlock(null, true);
		}
	}
	
	/**
	 * FluidStack based version of {@link blusunrize.immersiveengineering.common.util.Utils#fillFluidContainer(IFluidHandler, ItemStack, ItemStack, PlayerEntity)}
	 * minus the useless bits :D
	 */
	static ItemStack fillFluidContainer(IFluidTank tank, FluidStack fluid, ItemStack containerIn, ItemStack containerOut){
		if(containerIn == null || containerIn.isEmpty())
			return ItemStack.EMPTY;
		
		FluidActionResult result = tryFillContainer(tank, fluid, containerIn, false);
		if(result.isSuccess()){
			final ItemStack full = result.getResult();
			if((containerOut.isEmpty() || ItemHandlerHelper.canItemStacksStack(containerOut, full))){
				if(!containerOut.isEmpty() && containerOut.getCount() + full.getCount() > containerOut.getMaxStackSize()){
					return ItemStack.EMPTY;
				}
				
				result = tryFillContainer(tank, fluid, containerIn, true);
				if(result.isSuccess()){
					return result.getResult();
				}
			}
		}
		
		return ItemStack.EMPTY;
	}
	
	/** 
	 * FluidStack based version of {@link net.minecraftforge.fluids.FluidUtil#tryFillContainer(ItemStack, IFluidHandler, int, PlayerEntity, boolean)}
	 * minus the useless bits :D
	 */
	static FluidActionResult tryFillContainer(IFluidTank tank, FluidStack fluidSource, @Nonnull ItemStack container, boolean doFill){
		ItemStack containerCopy = ItemHandlerHelper.copyStackWithSize(container, 1);
		return FluidUtil.getFluidHandler(containerCopy).map(containerFluidHandler -> {
			
			int fillableAmount = containerFluidHandler.fill(fluidSource, FluidAction.SIMULATE);
			if(fillableAmount > 0){
				if(doFill){
					FluidStack fs = new FluidStack(fluidSource, Math.min(fluidSource.getAmount(), fillableAmount));
					containerFluidHandler.fill(fs, FluidAction.EXECUTE);
					tank.drain(fs, FluidAction.EXECUTE);
				}
				
				ItemStack resultContainer = containerFluidHandler.getContainer();
				return new FluidActionResult(resultContainer);
			}
			
			return FluidActionResult.FAILURE;
		}).orElse(FluidActionResult.FAILURE);
	}
	
	@Override
	public NonNullList<ItemStack> getInventory(){
		return this.inventory;
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
	public void doGraphicalUpdates(int slot){
		updateMasterBlock(null, true);
	}
	
	@Override
	public IInteractionObjectIE getGuiMaster(){
		return master();
	}
	
	@Override
	public boolean canUseGui(PlayerEntity player){
		return this.formed;
	}
	
	@Override
	protected DistillationRecipe getRecipeForId(ResourceLocation id){
		return DistillationRecipe.recipes.get(id);
	}
	
	@Override
	public Set<BlockPos> getEnergyPos(){
		return Energy_IN;
	}
	
	@Override
	public IOSideConfig getEnergySideConfig(Direction facing){
		if(this.formed && this.isEnergyPos() && (facing == null || facing == Direction.UP)){
			return IOSideConfig.INPUT;
		}
		
		return IOSideConfig.NONE;
	}
	
	@Override
	public Set<BlockPos> getRedstonePos(){
		return Redstone_IN;
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
		int outputAmount = 0;
		for (FluidStack outputFluid : process.recipe.getFluidOutputs()){
			outputAmount += outputFluid.getAmount();
		}

		return (this.tanks[TANK_OUTPUT].getCapacity() >= this.tanks[TANK_OUTPUT].getFluidAmount() + outputAmount);
	}
	
	@Override
	public void doProcessOutput(ItemStack output){
		Direction outputdir = (getIsMirrored() ? getFacing().rotateY() : getFacing().rotateYCCW());
		BlockPos outputpos = getBlockPosForPos(Item_OUT).offset(outputdir);
		
		TileEntity te = world.getTileEntity(outputpos);
		if(te!=null){
			IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
			if(handler!=null){
				output = ItemHandlerHelper.insertItem(handler, output, false);
			}
		}
		
		if(!output.isEmpty()){
			double x = outputpos.getX() + 0.5;
			double y = outputpos.getY() + 0.25;
			double z = outputpos.getZ() + 0.5;
			
			Direction facing = getIsMirrored() ? getFacing().getOpposite() : getFacing();
			if(facing != Direction.EAST && facing != Direction.WEST){
				x = outputpos.getX() + (facing == Direction.SOUTH ? 0.15 : 0.85);
			}
			if(facing != Direction.NORTH && facing != Direction.SOUTH){
				z = outputpos.getZ() + (facing == Direction.WEST ? 0.15 : 0.85);
			}
			
			ItemEntity ei = new ItemEntity(world, x, y, z, output.copy());
			ei.setMotion(0.075 * outputdir.getXOffset(), 0.025, 0.075 * outputdir.getZOffset());
			world.addEntity(ei);
		}
	}
	
	@Override
	public void doProcessFluidOutput(FluidStack output){
	}
	
	@Override
	public void onProcessFinish(MultiblockProcess<DistillationRecipe> process){
	}
	
	@Override
	public float getMinProcessDistance(MultiblockProcess<DistillationRecipe> process){
		return 1.0F;
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
	public boolean isInWorldProcessingMachine(){
		return false;
	}
	
	@Override
	public boolean shouldRenderAsActive(){
		return this.cooldownTicks > 0 || super.shouldRenderAsActive();
	}
	
	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side){
		DistillationTowerTileEntity master = master();
		if(master != null){
			// Fluid Input
			if(this.posInMultiblock.equals(Fluid_IN)){
				if(side == null || (getIsMirrored() ? (side == getFacing().rotateYCCW()) : (side == getFacing().rotateY()))){
					return new IFluidTank[]{master.tanks[TANK_INPUT]};
				}
			}
			
			// Fluid Output
			if(this.posInMultiblock.equals(Fluid_OUT) && (side == null || side == getFacing().getOpposite())){
				return new IFluidTank[]{master.tanks[TANK_OUTPUT]};
			}
		}
		return new FluidTank[0];
	}
	
	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resource){
		if(this.posInMultiblock.equals(Fluid_IN)){
			if(getIsMirrored() ? (side == null || side == getFacing().rotateYCCW()) : (side == null || side == getFacing().rotateY())){
				DistillationTowerTileEntity master = master();
				
				if(master == null || master.tanks[TANK_INPUT].getFluidAmount() >= master.tanks[TANK_INPUT].getCapacity())
					return false;
				
				FluidStack copy0 = Utils.copyFluidStackWithAmount(resource, 1000, false);
				FluidStack copy1 = Utils.copyFluidStackWithAmount(master.tanks[TANK_INPUT].getFluid(), 1000, false);
				
				if(master.tanks[TANK_INPUT].getFluid() == FluidStack.EMPTY){
					DistillationRecipe r = DistillationRecipe.findRecipe(copy0);
					return r != null;
				}else{
					DistillationRecipe r0 = DistillationRecipe.findRecipe(copy0);
					DistillationRecipe r1 = DistillationRecipe.findRecipe(copy1);
					return r0 == r1;
				}
			}
		}
		return false;
	}
	
	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side){
		if(this.posInMultiblock.equals(Fluid_OUT) && (side == null || side == getFacing().getOpposite())){
			DistillationTowerTileEntity master = master();
			
			return master != null && master.tanks[TANK_OUTPUT].getFluidAmount() > 0;
		}
		return false;
	}
	
	public boolean isLadder(){
		return this.posInMultiblock.getY() > 0 && (this.posInMultiblock.getX() == 2 && this.posInMultiblock.getZ() == 0);
	}
	
	private static CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES = CachedShapesWithTransform.createForMultiblock(DistillationTowerTileEntity::getShape);
	
	@Override
	public VoxelShape getBlockBounds(ISelectionContext ctx){
		return SHAPES.get(this.posInMultiblock, Pair.of(getFacing(), getIsMirrored()));
	}
	
	private static List<AxisAlignedBB> getShape(BlockPos posInMultiblock){
		final int bX = posInMultiblock.getX();
		final int bY = posInMultiblock.getY();
		final int bZ = posInMultiblock.getZ();
		
		// Redstone Input
		if(bY < 2){
			if(bX == 0 && bZ == 3){
				if(bY == 1){ // Actual Input
					return Arrays.asList(new AxisAlignedBB(0.0, 0.0, 0.0, 0.5, 1.0, 1.0));
				}else{ // Input Legs
					return Arrays.asList(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0), new AxisAlignedBB(0.125, 0.0, 0.75, 0.375, 1.0, 0.875), new AxisAlignedBB(0.125, 0.0, 0.125, 0.375, 1.0, 0.25));
				}
			}
		}
		
		// Pipe over Furnace
		if(bY == 2 && bX == 3 && bZ == 2){
			return Arrays.asList(new AxisAlignedBB(-0.0625, 0.375, 0.125, 0.0625, 1.125, 0.875), new AxisAlignedBB(0.125, 0, 0.125, 0.875, 0.125, 0.875), new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 1.0, 0.75), new AxisAlignedBB(0.0, 0.5, 0.25, 0.75, 1.0, 0.75));
		}
		
		// Long Pipe
		if(bY > 0 && bX == 1 && bZ == 3){
			if(bY != 15){
				List<AxisAlignedBB> list = new ArrayList<>();
				list.add(new AxisAlignedBB(0.1875, 0.0, 0.1875, 0.8125, 1.0, 0.8125));
				if(bY > 0 && bY % 4 == 0){ // For pipe passing a platform
					list.add(new AxisAlignedBB(0.0, 0.5, 0.0, 1.0, 1.0, 1.0));
				}
				return list;
			}else{ // Pipe Top Bend
				return Arrays.asList(new AxisAlignedBB(0.1875, 0.0, -0.0625, 0.8125, 0.625, 0.8125));
			}
		}
		
		// Ladder
		if(bY > 0 && bX == 2 && bZ == 0){
			List<AxisAlignedBB> list = new ArrayList<>();
			list.add(new AxisAlignedBB(0.0625, bY == 1 ? 0.125 : 0.0, 0.875, 0.9375, 1.0, 1.0625));
			if(bY > 0 && bY % 4 == 0){
				list.add(new AxisAlignedBB(0.0, 0.5, 0.875, 1.0, 1.0, 1.0625));
				list.add(new AxisAlignedBB(0.0, 0.5, 0.0, 1.0, 1.0, 0.0625));
			}
			return list;
		}
		
		// Center
		if(bX > 0 && bX < 3 && bZ > 0 && bZ < 3){
			if(bY > 0){
				// Boiler
				AxisAlignedBB bb = new AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 1.0, 0.9375);
				if(bZ == 1){
					if(bX == 1) bb = new AxisAlignedBB(0.0625, 0.0, 0.0625, 1.0, 1.0, 1.0);
					if(bX == 2) bb = new AxisAlignedBB(0.0, 0.0, 0.0625, 0.9375, 1.0, 1.0);
				}else if(bZ == 2){
					if(bX == 1) bb = new AxisAlignedBB(0.0625, 0.0, 0.0, 1.0, 1.0, 0.9375);
					if(bX == 2) bb = new AxisAlignedBB(0.0, 0.0, 0.0, 0.9375, 1.0, 0.9375);
				}
				return Arrays.asList(bb);
			}else{
				// Below Boiler
				return Arrays.asList(
						new AxisAlignedBB(-0.125, 0.5, -0.125, 1.125, 1.125, 1.125),
						new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0)
				);
			}
		}
		
		// Platforms
		if(bY > 0 && bY % 4 == 0){
			return Arrays.asList(new AxisAlignedBB(0.0, 0.5, 0.0, 1.0, 1.0, 1.0));
		}
		
		// Base
		if(bY == 0){
			List<AxisAlignedBB> list = new ArrayList<>();
			if((bX == 0 && bZ == 1) || (bX == 1 && bZ == 3) || (bX == 3 && bZ == 2) || (bX == 3 && bZ == 3)){
				list.add(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
			}else{
				list.add(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0));
			}
			return list;
		}
		
		return Arrays.asList(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
	}
}
