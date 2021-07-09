package flaxbeard.immersivepetroleum.common.blocks.tileentities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableSet;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.api.crafting.pumpjack.PumpjackHandler;
import flaxbeard.immersivepetroleum.common.IPTileTypes;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.multiblocks.PumpjackMultiblock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class PumpjackTileEntity extends PoweredMultiblockTileEntity<PumpjackTileEntity, MultiblockRecipe> implements IBlockBounds{
	/** Template-Location of the Energy Input Port. (0, 1, 5) */
	public static final Set<BlockPos> Redstone_IN = ImmutableSet.of(new BlockPos(0, 1, 5));
	
	/** Template-Location of the Redstone Input Port. (2, 1, 5) */
	public static final Set<BlockPos> Energy_IN = ImmutableSet.of(new BlockPos(2, 1, 5));
	
	/** Template-Location of the Eastern Fluid Output Port. (2, 0, 2) */
	public static final BlockPos East_Port = new BlockPos(2, 0, 2);
	
	/** Template-Location of the Western Fluid Output Port. (0, 0, 2) */
	public static final BlockPos West_Port = new BlockPos(0, 0, 2);
	
	/**
	 * Template-Location of the Bottom Fluid Output Port. (1, 0, 0) <b>(Also
	 * Master Block)</b>
	 */
	public static final BlockPos Down_Port = new BlockPos(1, 0, 0);
	
	public FluidTank fakeTank = new FluidTank(0);
	public boolean wasActive = false;
	public float activeTicks = 0;
	private int pipeTicks = 0;
	private boolean lastHadPipes = true;
	public BlockState state = null;
	public PumpjackTileEntity(){
		super(PumpjackMultiblock.INSTANCE, 16000, true, null);
	}
	
	@Override
	public TileEntityType<?> getType(){
		return IPTileTypes.PUMP.get();
	}
	
	public boolean canExtract(){
		return true;
	}
	
	public int getFluidAmount(){
		return PumpjackHandler.getFluidAmount(this.world, getPos().getX() >> 4, getPos().getZ() >> 4);
	}
	
	public Fluid getFluidType(){
		return PumpjackHandler.getFluid(this.world, getPos().getX() >> 4, getPos().getZ() >> 4);
	}
	
	public int getResidualFluid(){
		return PumpjackHandler.getResidualFluid(this.world, getPos().getX() >> 4, getPos().getZ() >> 4);
	}
	
	public void extractFluid(int amount){
		PumpjackHandler.depleteFluid(this.world, getPos().getX() >> 4, getPos().getZ() >> 4, amount);
	}
	
	private boolean hasPipes(){
		if(IPServerConfig.EXTRACTION.required_pipes.get()){
			BlockPos basePos = getBlockPosForPos(Down_Port);
			for(int y = basePos.getY() - 1;y > 0;y--){
				BlockPos pos = new BlockPos(basePos.getX(), y, basePos.getZ());
				BlockState state = this.world.getBlockState(pos);
				
				if(state.getBlock() == Blocks.BEDROCK)
					return true;
				
				if(state.getBlock() != IEBlocks.MetalDevices.fluidPipe)
					return false;
			}
		}
		
		return true;
	}
	
	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket){
		super.readCustomNBT(nbt, descPacket);
		boolean lastActive = this.wasActive;
		this.wasActive = nbt.getBoolean("wasActive");
		this.lastHadPipes = nbt.getBoolean("lastHadPipes");
		if(!wasActive && lastActive){
			this.activeTicks++;
		}
		this.state = null;
		if(nbt.hasUniqueId("comp")){
			ItemStack stack = ItemStack.read(nbt.getCompound("comp"));
			
			if(!stack.isEmpty()){
				Block block = Block.getBlockFromItem(stack.getItem());
				if(block != Blocks.AIR){
					this.state = block.getDefaultState();
				}
			}
		}
	}
	
	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket){
		super.writeCustomNBT(nbt, descPacket);
		nbt.putBoolean("wasActive", this.wasActive);
		nbt.putBoolean("lastHadPipes", this.lastHadPipes);
		if(getFluidType() != null){
			FluidStack stack = new FluidStack(getFluidType(), 0);
			CompoundNBT comp = new CompoundNBT();
			stack.writeToNBT(comp);
			nbt.put("comp", comp);
		}
	}
	
	@Override
	public void tick(){
		super.tick();
		
		if((this.world.isRemote || isDummy()) && this.wasActive){
			this.activeTicks++;
			
			if(this.state != null){
				// What is this whole thing even for? I've never seen the
				// pumpjack spawning particles anywhere.
				float r1 = (this.world.rand.nextFloat() - .5F) * 2F;
				float r2 = (this.world.rand.nextFloat() - .5F) * 2F;
				
				this.world.addParticle(ParticleTypes.SMOKE, this.pos.getX() + .5, this.pos.getY() + .5, this.pos.getZ() + .5, r1 * 0.04D, 0.25D, r2 * 0.025D);
			}
			
			return;
		}
		
		boolean active = false;
		
		int consumption = IPServerConfig.EXTRACTION.pumpjack_consumption.get();
		int extracted = this.energyStorage.extractEnergy(IPServerConfig.EXTRACTION.pumpjack_consumption.get(), true);
		
		if(extracted >= consumption && canExtract()){
			if((getPos().getX() + getPos().getZ()) % IPServerConfig.EXTRACTION.pipe_check_ticks.get() == this.pipeTicks){
				this.lastHadPipes = hasPipes();
			}
			
			if(!isRSDisabled() && this.lastHadPipes){
				int available = getFluidAmount();
				int residual = getResidualFluid();
				if(available > 0 || residual > 0){
					int oilAmnt = getFluidAmount() <= 0 ? residual : getFluidAmount();
					
					this.energyStorage.extractEnergy(consumption, false);
					FluidStack out = new FluidStack(getFluidType(), Math.min(IPServerConfig.EXTRACTION.pumpjack_speed.get(), oilAmnt));
					Direction facing = getIsMirrored() ? getFacing().rotateYCCW() : getFacing().rotateY();
					BlockPos outputPos = master().getBlockPosForPos(East_Port).offset(facing);
					IFluidHandler output = FluidUtil.getFluidHandler(this.world, outputPos, facing).orElse(null);
					if(output != null){
						int accepted = output.fill(out, FluidAction.SIMULATE);
						if(accepted > 0){
							int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), FluidAction.EXECUTE);
							extractFluid(drained);
							active = true;
							out = Utils.copyFluidStackWithAmount(out, out.getAmount() - drained, false);
						}
					}
					
					facing = getIsMirrored() ? getFacing().rotateY() : getFacing().rotateYCCW();
					outputPos = master().getBlockPosForPos(West_Port).offset(facing);
					output = FluidUtil.getFluidHandler(this.world, outputPos, facing).orElse(null);
					if(output != null){
						int accepted = output.fill(out, FluidAction.SIMULATE);
						if(accepted > 0){
							int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), FluidAction.EXECUTE);
							extractFluid(drained);
							active = true;
						}
					}
					
					this.activeTicks++;
				}
			}
			this.pipeTicks = (this.pipeTicks + 1) % IPServerConfig.EXTRACTION.pipe_check_ticks.get();
		}
		
		if(active != this.wasActive){
			this.markDirty();
			this.markContainingBlockForUpdate(null);
		}
		
		this.wasActive = active;
	}
	
	@Override
	public Set<BlockPos> getEnergyPos(){
		return Energy_IN;
	}
	
	@Override
	public IOSideConfig getEnergySideConfig(Direction facing){
		if(this.formed && this.isEnergyPos() && (facing == null || facing == Direction.UP))
			return IOSideConfig.INPUT;
		
		return IOSideConfig.NONE;
	}
	
	@Override
	public Set<BlockPos> getRedstonePos(){
		return Redstone_IN;
	}
	
	@Override
	public boolean isInWorldProcessingMachine(){
		return false;
	}
	
	@Override
	public void doProcessOutput(ItemStack output){
	}
	
	@Override
	public void doProcessFluidOutput(FluidStack output){
	}
	
	@Override
	public void onProcessFinish(MultiblockProcess<MultiblockRecipe> process){
	}
	
	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<MultiblockRecipe> process){
		return false;
	}
	
	@Override
	public float getMinProcessDistance(MultiblockProcess<MultiblockRecipe> process){
		return 0;
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
	public void doGraphicalUpdates(int slot){
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}
	
	@Override
	public MultiblockRecipe findRecipeForInsertion(ItemStack inserting){
		return null;
	}
	
	@Override
	protected MultiblockRecipe getRecipeForId(ResourceLocation id){
		return null;
	}
	
	@Override
	public NonNullList<ItemStack> getInventory(){
		return null;
	}
	
	@Override
	public IFluidTank[] getInternalTanks(){
		return null;
	}
	
	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side){
		PumpjackTileEntity master = master();
		if(master != null){
			// East Port
			if(this.posInMultiblock.equals(East_Port)){
				if(side == null || (getIsMirrored() ? (side == getFacing().rotateYCCW()) : (side == getFacing().rotateY()))){
					return new FluidTank[]{master.fakeTank};
				}
			}
			
			// West Port
			if(this.posInMultiblock.equals(West_Port)){
				if(side == null || (getIsMirrored() ? (side == getFacing().rotateY()) : (side == getFacing().rotateYCCW()))){
					return new FluidTank[]{master.fakeTank};
				}
			}
			
			// Below Head
			if(IPServerConfig.EXTRACTION.required_pipes.get() && this.posInMultiblock.equals(Down_Port) && (side == null || side == Direction.DOWN)){
				return new FluidTank[]{master.fakeTank};
			}
		}
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
	
	private static CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES = CachedShapesWithTransform.createForMultiblock(PumpjackTileEntity::getShape);
	@Override
	public VoxelShape getBlockBounds(ISelectionContext ctx){
		return SHAPES.get(this.posInMultiblock, Pair.of(getFacing(), getIsMirrored()));
	}
	
	private static List<AxisAlignedBB> getShape(BlockPos posInMultiblock){
		final int bX = posInMultiblock.getX();
		final int bY = posInMultiblock.getY();
		final int bZ = posInMultiblock.getZ();
		
		// Most of the arm doesnt need collision. Dumb anyway.
		if((bY == 3 && bX == 1 && bZ != 2) || (bX == 1 && bY == 2 && bZ == 0)){
			return new ArrayList<>();
		}
		
		// Motor
		if(bY < 3 && bX == 1 && bZ == 4){
			List<AxisAlignedBB> list = new ArrayList<>();
			if(bY == 2){
				list.add(new AxisAlignedBB(0.25, 0.0, 0.0, 0.75, 0.25, 1.0));
			}else{
				list.add(new AxisAlignedBB(0.25, 0.0, 0.0, 0.75, 1.0, 1.0));
			}
			if(bY == 0){
				list.add(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0));
			}
			return list;
		}
		
		// Support
		if(bZ == 2 && bY > 0){
			if(bX == 0){
				if(bY == 1){
					List<AxisAlignedBB> list = new ArrayList<>();
					list.add(new AxisAlignedBB(0.6875, 0.0, 0.0, 1.0, 1.0, 0.25));
					list.add(new AxisAlignedBB(0.6875, 0.0, 0.75, 1.0, 1.0, 1.0));
					return list;
				}
				if(bY == 2){
					List<AxisAlignedBB> list = new ArrayList<>();
					list.add(new AxisAlignedBB(0.8125, 0.0, 0.0, 1.0, 0.5, 1.0));
					list.add(new AxisAlignedBB(0.8125, 0.5, 0.25, 1.0, 1.0, 0.75));
					return list;
				}
				if(bY == 3){
					return Arrays.asList(new AxisAlignedBB(0.9375, 0.0, 0.375, 1.0, 0.125, 0.625));
				}
			}
			if(bX == 1 && bY == 3){
				return Arrays.asList(new AxisAlignedBB(0.0, -0.125, 0.375, 1.0, 0.125, 0.625));
			}
			if(bX == 2){
				if(bY == 1){
					List<AxisAlignedBB> list = new ArrayList<>();
					list.add(new AxisAlignedBB(0.0, 0.0, 0.0, 0.3125, 1.0, 0.25));
					list.add(new AxisAlignedBB(0.0, 0.0, 0.75, 0.3125, 1.0, 1.0));
					return list;
				}
				if(bY == 2){
					List<AxisAlignedBB> list = new ArrayList<>();
					list.add(new AxisAlignedBB(0.0, 0.0, 0.0, 0.1875, 0.5, 1.0));
					list.add(new AxisAlignedBB(0.0, 0.5, 0.25, 0.1875, 1.0, 0.75));
					return list;
				}
				if(bY == 3){
					return Arrays.asList(new AxisAlignedBB(0.0, 0.0, 0.375, 0.0625, 0.125, 0.625));
				}
			}
		}
		
		// Redstone Controller
		if(bX == 0 && bZ == 5){
			if(bY == 0){ // Bottom
				return Arrays.asList(
						new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0),
						new AxisAlignedBB(0.75, 0.0, 0.625, 0.875, 1.0, 0.875),
						new AxisAlignedBB(0.125, 0.0, 0.625, 0.25, 1.0, 0.875)
				);
			}
			if(bY == 1){ // Top
				return Arrays.asList(new AxisAlignedBB(0.0, 0.0, 0.5, 1.0, 1.0, 1.0));
			}
		}
		
		// Below the power-in block, base height
		if(bX == 2 && bY == 0 && bZ == 5){
			return Arrays.asList(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
		}
		
		// Misc
		if(bY == 0){
			
			// Legs Bottom Front
			if(bZ == 1 && (bX == 0 || bX == 2)){
				List<AxisAlignedBB> list = new ArrayList<>();
				
				list.add(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0));
				
				if(bX == 0){
					list.add(new AxisAlignedBB(0.5, 0.5, 0.5, 1.0, 1.0, 1.0));
				}
				if(bX == 2){
					list.add(new AxisAlignedBB(0.0, 0.5, 0.5, 0.5, 1.0, 1.0));
				}
				
				return list;
			}
			
			// Legs Bottom Back
			if(bZ == 3 && (bX == 0 || bX == 2)){
				List<AxisAlignedBB> list = new ArrayList<>();
				
				list.add(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0));
				
				if(bX == 0){
					list.add(new AxisAlignedBB(0.5, 0.5, 0.0, 1.0, 1.0, 0.5));
				}
				if(bX == 2){
					list.add(new AxisAlignedBB(0.0, 0.5, 0.0, 0.5, 1.0, 0.5));
				}
				
				return list;
			}
			
			// Fluid Outputs
			if(bZ == 2 && (bX == 0 || bX == 2)){
				return Arrays.asList(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
			}
			
			if(bX == 1){
				// Well
				if(bZ == 0){
					return Arrays.asList(new AxisAlignedBB(0.3125, 0.5, 0.8125, 0.6875, 0.875, 1.0), new AxisAlignedBB(0.1875, 0, 0.1875, 0.8125, 1.0, 0.8125));
				}
				
				// Pipes
				if(bZ == 1){
					return Arrays.asList(
							new AxisAlignedBB(0.3125, 0.5, 0.0, 0.6875, 0.875, 1.0),
							new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0)
					);
				}
				if(bZ == 2){
					return Arrays.asList(
							new AxisAlignedBB(0.3125, 0.5, 0.0, 0.6875, 0.875, 0.6875),
							new AxisAlignedBB(0.0, 0.5, 0.3125, 1.0, 0.875, 0.6875),
							new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0)
					);
				}
			}
			
			return Arrays.asList(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0));
		}
		
		return Arrays.asList(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
	}
}
