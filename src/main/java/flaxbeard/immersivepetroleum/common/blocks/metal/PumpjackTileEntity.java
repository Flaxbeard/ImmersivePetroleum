package flaxbeard.immersivepetroleum.common.blocks.metal;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableSet;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.shapes.CachedShapesWithTransform;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.common.IPConfig;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.PumpjackMultiblock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
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
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class PumpjackTileEntity extends PoweredMultiblockTileEntity<PumpjackTileEntity, MultiblockRecipe>  implements IBlockBounds{
	/** Do not Touch! Taken care of by {@link IPContent#registerTile(RegistryEvent.Register, Class, Block...)} */
	public static TileEntityType<PumpjackTileEntity> TYPE;
	
	/** Template-Location of the Energy Input Port. (0, 1, 5) */
	public static final Set<BlockPos> Redstone_IN=ImmutableSet.of(new BlockPos(0, 1, 5));
	
	/** Template-Location of the Redstone Input Port. (2, 1, 5) */
	public static final Set<BlockPos> Energy_IN=ImmutableSet.of(new BlockPos(2, 1, 5));
	
	/** Template-Location of the Eastern Fluid Output Port. (2, 0, 2) */
	public static final BlockPos East_Port=new BlockPos(2, 0, 2);
	
	/** Template-Location of the Western Fluid Output Port. (0, 0, 2) */
	public static final BlockPos West_Port=new BlockPos(0, 0, 2);
	
	/** Template-Location of the Bottom Fluid Output Port. (1, 0, 0) <b>(Also Master Block)</b> */
	public static final BlockPos Down_Port=new BlockPos(1, 0, 0);
	
	
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
		return TYPE;
	}
	
	public boolean canExtract(){
		return true;
	}
	
	public int availableOil(){
		return PumpjackHandler.getFluidAmount(this.world, getPos().getX() >> 4, getPos().getZ() >> 4);
	}
	
	public Fluid availableFluid(){
		return PumpjackHandler.getFluid(this.world, getPos().getX() >> 4, getPos().getZ() >> 4);
	}
	
	public int getResidualOil(){
		return PumpjackHandler.getResidualFluid(this.world, getPos().getX() >> 4, getPos().getZ() >> 4);
	}
	
	public void extractOil(int amount){
		PumpjackHandler.depleteFluid(this.world, getPos().getX() >> 4, getPos().getZ() >> 4, amount);
	}
	
	private boolean hasPipes(){
		if(!IPConfig.EXTRACTION.required_pipes.get()) return true;
		
		BlockPos basePos = getBlockPosForPos(Down_Port);
		for(int y = basePos.getY() - 2;y > 0;y--){
			BlockPos pos = new BlockPos(basePos.getX(), y, basePos.getZ());
			BlockState state = this.world.getBlockState(pos);
			
			if(state.getBlock() == Blocks.BEDROCK)
				return true;
			
			if(!Utils.isBlockAt(this.world, pos, IEBlocks.MetalDevices.fluidPipe))
				return false;
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
				this.state = block.getDefaultState();
			}
		}
	}
	
	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket){
		super.writeCustomNBT(nbt, descPacket);
		nbt.putBoolean("wasActive", this.wasActive);
		nbt.putBoolean("lastHadPipes", this.lastHadPipes);
		if(availableFluid() != null){
			FluidStack stack = new FluidStack(availableFluid(), 0);
			CompoundNBT comp = new CompoundNBT();
			stack.writeToNBT(comp);
			nbt.put("comp", comp);
		}
	}
	
	@Override
	public void tick(){
		super.tick();
		
		if(this.world.isRemote || isDummy()){
//			if(this.world.isRemote && !isDummy() && this.state != null && this.wasActive){
//				// What is this whole thing even for? I've never seen the pumpjack spawning particles anywhere.
//				BlockPos particlePos = getPos();
//				float r1 = (this.world.rand.nextFloat() - .5F) * 2F;
//				float r2 = (this.world.rand.nextFloat() - .5F) * 2F;
//				
//				this.world.addParticle(ParticleTypes.SMOKE,
//						particlePos.getX() + 0.5D, particlePos.getY(), particlePos.getZ() + 0.5D,
//						r1 * 0.04D, 0.25D, r2 * 0.025D);
//			}
			if(this.wasActive){
				this.activeTicks++;
			}
			return;
		}
		
		boolean active=false;
		
		int consumption=IPConfig.EXTRACTION.pumpjack_consumption.get();
		int extracted=this.energyStorage.extractEnergy(IPConfig.EXTRACTION.pumpjack_consumption.get(), true);
		
		if(extracted>=consumption && canExtract()){
			if((getPos().getX() + getPos().getZ()) % IPConfig.EXTRACTION.pipe_check_ticks.get() == this.pipeTicks){
				this.lastHadPipes = hasPipes();
			}
			
			if(!isRSDisabled() && this.lastHadPipes){
				int available = availableOil();
				int residual = getResidualOil();
				if(available > 0 || residual > 0){
					int oilAmnt = availableOil() <= 0 ? residual : availableOil();
					
					this.energyStorage.extractEnergy(consumption, false);
					FluidStack out = new FluidStack(availableFluid(), Math.min(IPConfig.EXTRACTION.pumpjack_speed.get(), oilAmnt));
					BlockPos outputPos = master().getBlockPosForPos(East_Port).offset(getFacing().rotateY());
					IFluidHandler output = FluidUtil.getFluidHandler(this.world, outputPos, getFacing().rotateY()).orElse(null);
					if(output != null){
						int accepted = output.fill(out, FluidAction.SIMULATE);
						if(accepted > 0){
							int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), FluidAction.EXECUTE);
							extractOil(drained);
							active = true;
							out = Utils.copyFluidStackWithAmount(out, out.getAmount() - drained, false);
						}
					}
					
					outputPos = master().getBlockPosForPos(West_Port).offset(getFacing().rotateYCCW());
					output = FluidUtil.getFluidHandler(this.world, outputPos, getFacing().rotateYCCW()).orElse(null);
					if(output != null){
						int accepted = output.fill(out, FluidAction.SIMULATE);
						if(accepted > 0){
							int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), FluidAction.EXECUTE);
							extractOil(drained);
							active = true;
						}
					}
					
					this.activeTicks++;
				}
			}
			this.pipeTicks = (this.pipeTicks + 1) % IPConfig.EXTRACTION.pipe_check_ticks.get();
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
		if(this.formed && this.isEnergyPos() && (facing==null || facing==Direction.UP))
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
		if(master!=null){
			// East Port
			if(this.posInMultiblock.equals(East_Port) && (side==null || side==getFacing().rotateY())){
				return new FluidTank[]{master.fakeTank};
			}
			
			// West Port
			if(this.posInMultiblock.equals(West_Port) && (side==null || side==getFacing().rotateYCCW())){
				return new FluidTank[]{master.fakeTank};
			}
			
			// Below Head
			if(IPConfig.EXTRACTION.required_pipes.get() && this.posInMultiblock.equals(Down_Port) && (side==null || side==Direction.DOWN)){
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
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox(){ // TODO Needs rewrite probably
		BlockPos nullPos = this.getPos();
		
		BlockPos a=nullPos.offset(getFacing(), -2).offset(getIsMirrored() ? getFacing().rotateYCCW() : getFacing().rotateY(), -1).down(1);
		BlockPos b=nullPos.offset(getFacing(), 5).offset(getIsMirrored() ? getFacing().rotateYCCW() : getFacing().rotateY(), 2).up(3);
		
		return new AxisAlignedBB(a, b);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public double getMaxRenderDistanceSquared(){
		return super.getMaxRenderDistanceSquared() * IEConfig.GENERAL.increasedTileRenderdistance.get();
	}
	
	private static CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES = CachedShapesWithTransform.createForMultiblock(PumpjackTileEntity::getShape);
	@Override
	public VoxelShape getBlockBounds(ISelectionContext ctx){
		return SHAPES.get(this.posInMultiblock, Pair.of(getFacing(), getIsMirrored()));
	}
	
	@SuppressWarnings("unused")
	private static List<AxisAlignedBB> getShape(BlockPos posInMultiblock){
		final int bX=posInMultiblock.getX();
		final int bY=posInMultiblock.getY();
		final int bZ=posInMultiblock.getZ();
		
		
		return Arrays.asList(new AxisAlignedBB(0, 0, 0, 1, 1, 1));
		
//		Direction fl = getFacing();
//		Direction fw = getFacing().rotateY();
//		if(getIsMirrored()) fw = fw.getOpposite();
//		
//		int pos = -1;
//		
//		if(pos == 0){
//			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//			
//			float minX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 2F / 16F : ((fl == Direction.WEST) ? 10F / 16F : 2F / 16F);
//			float maxX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 4F / 16F : ((fl == Direction.WEST) ? 14F / 16F : 6F / 16F);
//			float minZ = (fl == Direction.EAST || fl == Direction.WEST) ? 2F / 16F : ((fl == Direction.NORTH) ? 10F / 16F : 2F / 16F);
//			float maxZ = (fl == Direction.EAST || fl == Direction.WEST) ? 4F / 16F : ((fl == Direction.NORTH) ? 14F / 16F : 6F / 16F);
//			list.add(new AxisAlignedBB(minX, 0.5, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//			
//			minX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 12F / 16F : minX;
//			maxX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 14F / 16F : maxX;
//			minZ = (fl == Direction.EAST || fl == Direction.WEST) ? 12F / 16F : minZ;
//			maxZ = (fl == Direction.EAST || fl == Direction.WEST) ? 14F / 16F : maxZ;
//			list.add(new AxisAlignedBB(minX, 0.5, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//			return list;
//		}else if(pos == 18){
//			float minX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0F : ((fl == Direction.WEST) ? .5F : 0F);
//			float maxX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1F : ((fl == Direction.WEST) ? 1F : .5F);
//			float minZ = (fl == Direction.EAST || fl == Direction.WEST) ? 0F : ((fl == Direction.NORTH) ? .5F : 0F);
//			float maxZ = (fl == Direction.EAST || fl == Direction.WEST) ? 1F : ((fl == Direction.NORTH) ? 1F : .5F);
//			return Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//		}else if((pos >= 3 && pos <= 14 && pos != 10 && pos != 13 && pos != 11 && pos != 9) || pos == 1){
//			return Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//		}else if(pos == 13){
//			float minX = (fl == Direction.EAST || fl == Direction.WEST) ? 0F : .3125F;
//			float maxX = (fl == Direction.EAST || fl == Direction.WEST) ? 1F : .685F;
//			float minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0F : .3125F;
//			float maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1F : .685F;
//			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, 0.5, minZ, maxX, .5 + 3 / 8F, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//			list.add(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//			return list;
//			
//		}else if(pos == 10){
//			float minX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? .3125F : ((fl == Direction.EAST) ? 11F / 16F : 0F / 16F);
//			float maxX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? .685F : ((fl == Direction.EAST) ? 16F / 16F : 5F / 16F);
//			float minZ = (fl == Direction.EAST || fl == Direction.WEST) ? .3125F : ((fl == Direction.SOUTH) ? 11F / 16F : 0F / 16F);
//			float maxZ = (fl == Direction.EAST || fl == Direction.WEST) ? .685F : ((fl == Direction.SOUTH) ? 16F / 16F : 5F / 16F);
//			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, 0.5, minZ, maxX, .5 + 3 / 8F, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//			
//			minX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0F : 5F / 16F;
//			maxX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1F : 11F / 16F;
//			minZ = (fl == Direction.EAST || fl == Direction.WEST) ? 0F : 5F / 16F;
//			maxZ = (fl == Direction.EAST || fl == Direction.WEST) ? 1F : 11F / 16F;
//			list.add(new AxisAlignedBB(minX, 0.5, minZ, maxX, .5 + 3 / 8F, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//			
//			list.add(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//			return list;
//			
//		}else if(pos == 16){
//			return Lists.newArrayList(new AxisAlignedBB(3 / 16F, 0, 3 / 16F, 13 / 16F, 1, 13 / 16F).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//		}else if(pos == 22){
//			float minX = (fl == Direction.EAST || fl == Direction.WEST) ? 0F : .25F;
//			float maxX = (fl == Direction.EAST || fl == Direction.WEST) ? 1F : .75F;
//			float minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0F : .25F;
//			float maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1F : .75F;
//			return Lists.newArrayList(new AxisAlignedBB(minX, -0.75, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//		}else if(pos == 40){
//			float minX = (fl == Direction.EAST || fl == Direction.WEST) ? 0F : .25F;
//			float maxX = (fl == Direction.EAST || fl == Direction.WEST) ? 1F : .75F;
//			float minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0F : .25F;
//			float maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1F : .75F;
//			return Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 0.25, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//		}else if(pos == 27){
//			fl = this.getIsMirrored() ? getFacing().getOpposite() : getFacing();
//			float minX = (fl == Direction.EAST || fl == Direction.WEST) ? 0.7F : ((fl == Direction.NORTH) ? 0.6F : -.1F);
//			float maxX = (fl == Direction.EAST || fl == Direction.WEST) ? 1.4F : ((fl == Direction.NORTH) ? 1.1F : 0.4F);
//			float minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0.7F : ((fl == Direction.EAST) ? .6F : -.1F);
//			float maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1.4F : ((fl == Direction.EAST) ? 1.1F : 0.4F);
//			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, -0.5F, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//			
//			minX = (fl == Direction.EAST || fl == Direction.WEST) ? -.4F : ((fl == Direction.NORTH) ? 0.6F : -.1F);
//			maxX = (fl == Direction.EAST || fl == Direction.WEST) ? .3F : ((fl == Direction.NORTH) ? 1.1F : 0.4F);
//			minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? -.4F : ((fl == Direction.EAST) ? .6F : -.1F);
//			maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? .3F : ((fl == Direction.EAST) ? 1.1F : 0.4F);
//			list.add(new AxisAlignedBB(minX, -0.5F, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//			return list;
//		}else if(pos == 29){
//			fl = this.getIsMirrored() ? getFacing().getOpposite() : getFacing();
//			float minX = (fl == Direction.EAST || fl == Direction.WEST) ? 0.7F : ((fl == Direction.SOUTH) ? 0.6F : -.1F);
//			float maxX = (fl == Direction.EAST || fl == Direction.WEST) ? 1.4F : ((fl == Direction.SOUTH) ? 1.1F : 0.4F);
//			float minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0.7F : ((fl == Direction.WEST) ? .6F : -.1F);
//			float maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1.4F : ((fl == Direction.WEST) ? 1.1F : 0.4F);
//			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, -0.5F, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//			
//			minX = (fl == Direction.EAST || fl == Direction.WEST) ? -.4F : ((fl == Direction.SOUTH) ? 0.6F : -.1F);
//			maxX = (fl == Direction.EAST || fl == Direction.WEST) ? .3F : ((fl == Direction.SOUTH) ? 1.1F : 0.4F);
//			minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? -.4F : ((fl == Direction.WEST) ? .6F : -.1F);
//			maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? .3F : ((fl == Direction.WEST) ? 1.1F : 0.4F);
//			list.add(new AxisAlignedBB(minX, -0.5F, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//			return list;
//		}else if(pos == 45){
//			fl = this.getIsMirrored() ? getFacing().getOpposite() : getFacing();
//			float minX = (fl == Direction.EAST || fl == Direction.WEST) ? 0F : ((fl == Direction.NORTH) ? 0.8F : -0.2F);
//			float maxX = (fl == Direction.EAST || fl == Direction.WEST) ? 1F : ((fl == Direction.NORTH) ? 1.2F : 0.2F);
//			float minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0F : ((fl == Direction.EAST) ? 0.8F : -0.2F);
//			float maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1 : ((fl == Direction.EAST) ? 1.2F : 0.2F);
//			return Lists.newArrayList(new AxisAlignedBB(minX, 0F, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//		}else if(pos == 47){
//			fl = this.getIsMirrored() ? getFacing().getOpposite() : getFacing();
//			float minX = (fl == Direction.EAST || fl == Direction.WEST) ? 0F : ((fl == Direction.NORTH) ? -0.2F : 0.8F);
//			float maxX = (fl == Direction.EAST || fl == Direction.WEST) ? 1F : ((fl == Direction.NORTH) ? 0.2F : 1.2F);
//			float minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0F : ((fl == Direction.EAST) ? -0.2F : 0.8F);
//			float maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1 : ((fl == Direction.EAST) ? 0.2F : 1.2F);
//			return Lists.newArrayList(new AxisAlignedBB(minX, 0F, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//		}else if(pos == 63 || pos == 65){
//			return new ArrayList<AxisAlignedBB>();
//		}else if(pos == 58 || pos == 61 || pos == 64 || pos == 67){
//			float minX = (fl == Direction.EAST || fl == Direction.WEST) ? 0F : 0.25F;
//			float maxX = (fl == Direction.EAST || fl == Direction.WEST) ? 1F : 0.75F;
//			float minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0F : 0.25F;
//			float maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1 : 0.75F;
//			return Lists.newArrayList(new AxisAlignedBB(minX, -.25F, minZ, maxX, 0.75F, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//		}else if(pos == 70){
//			float minX = (fl == Direction.EAST || fl == Direction.WEST) ? 0F : 0.125F;
//			float maxX = (fl == Direction.EAST || fl == Direction.WEST) ? 1F : 0.875F;
//			float minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0F : 0.125F;
//			float maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1 : 0.875F;
//			return Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1.25F, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//		}else if(pos == 52){
//			float minX = (fl == Direction.EAST || fl == Direction.WEST) ? 0F : 0.125F;
//			float maxX = (fl == Direction.EAST || fl == Direction.WEST) ? 1F : 0.875F;
//			float minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0F : 0.125F;
//			float maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1 : 0.875F;
//			return Lists.newArrayList(new AxisAlignedBB(minX, .25F, minZ, maxX, 1F, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
//		}
//		
//		List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
//		list.add(new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
	}
}
