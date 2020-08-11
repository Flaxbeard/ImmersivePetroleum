package flaxbeard.immersivepetroleum.common.blocks.metal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.common.IPConfig;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.PumpjackMultiblock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class PumpjackTileEntity extends PoweredMultiblockTileEntity<PumpjackTileEntity, IMultiblockRecipe>  implements IInteractionObjectIE{
	public static class PumpjackParentTileEntity extends PumpjackTileEntity{
		public static TileEntityType<PumpjackParentTileEntity> TYPE;
		
		@Override
		public TileEntityType<?> getType(){
			return super.getType();
		}
		
		@OnlyIn(Dist.CLIENT)
		@Override
		public AxisAlignedBB getRenderBoundingBox(){
			BlockPos nullPos = this.getPos();
			return new AxisAlignedBB(nullPos.offset(getFacing(), -2).offset(getIsMirrored() ? getFacing().rotateYCCW() : getFacing().rotateY(), -1).down(1), nullPos.offset(getFacing(), 5).offset(getIsMirrored() ? getFacing().rotateYCCW() : getFacing().rotateY(), 2).up(3));
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
	
	public static TileEntityType<PumpjackTileEntity> TYPE;
	
	public PumpjackTileEntity(){
		super(PumpjackMultiblock.INSTANCE, 16000, true, null);
	}
	
	public FluidTank fakeTank = new FluidTank(0);
	
	public boolean wasActive = false;
	public float activeTicks = 0;
	private int pipeTicks = 0;
	private boolean lastHadPipes = true;
	public BlockState state = null;
	
	public boolean canExtract(){
		return true;
	}
	
	public int availableOil(){
		return PumpjackHandler.getFluidAmount(world, getPos().getX() >> 4, getPos().getZ() >> 4);
	}
	
	public Fluid availableFluid(){
		return PumpjackHandler.getFluid(world, getPos().getX() >> 4, getPos().getZ() >> 4);
	}
	
	public int getResidualOil(){
		return PumpjackHandler.getResidualFluid(world, getPos().getX() >> 4, getPos().getZ() >> 4);
	}
	
	public void extractOil(int amount){
		PumpjackHandler.depleteFluid(world, getPos().getX() >> 4, getPos().getZ() >> 4, amount);
	}
	
	private boolean hasPipes(){
		if(!IPConfig.EXTRACTION.req_pipes.get()) return true;
		BlockPos basePos = getPos().offset(this.getFacing(), 4);
		for(int y = basePos.getY() - 2;y > 0;y--){
			BlockPos pos = new BlockPos(basePos.getX(), y, basePos.getZ());
			BlockState state = world.getBlockState(pos);
			if(state.getBlock() == Blocks.BEDROCK) return true;
			if(!Utils.isBlockAt(world, pos, IEBlocks.MetalDevices.fluidPipe)){
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket){
		super.readCustomNBT(nbt, descPacket);
		boolean lastActive = wasActive;
		this.wasActive = nbt.getBoolean("wasActive");
		this.lastHadPipes = nbt.getBoolean("lastHadPipes");
		if(!wasActive && lastActive){
			this.activeTicks++;
		}
		state = null;
		if(nbt.hasUniqueId("comp")){
			ItemStack stack = ItemStack.read(nbt.getCompound("comp"));
			
			if(!stack.isEmpty()){
				Block block = Block.getBlockFromItem(stack.getItem());
				state = block.getDefaultState();
			}
		}
	}
	
	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket){
		super.writeCustomNBT(nbt, descPacket);
		nbt.putBoolean("wasActive", wasActive);
		nbt.putBoolean("lastHadPipes", lastHadPipes);
		if(availableFluid() != null){
			FluidStack stack = new FluidStack(availableFluid(), 0);
			CompoundNBT comp = new CompoundNBT();
			stack.writeToNBT(comp);
			nbt.put("comp", comp);
		}
	}
	
	@Override
	public void updateMasterBlock(BlockState state, boolean blockUpdate){
		update(true);
		super.updateMasterBlock(state, blockUpdate);
	}
	
	public void update(boolean consumePower){
		// System.out.println("TEST");
		if(world.isRemote || isDummy()){
			if(world.isRemote && !isDummy() && state != null && wasActive){
				BlockPos particlePos = this.getPos().offset(getFacing(), 4);
				float r1 = (world.rand.nextFloat() - .5F) * 2F;
				float r2 = (world.rand.nextFloat() - .5F) * 2F;
				
				world.addParticle(ParticleTypes.SMOKE, particlePos.getX() + 0.5D, particlePos.getY(), particlePos.getZ() + 0.5D, r1 * 0.04D, 0.25D, r2 * 0.025D);
			}
			if(wasActive && consumePower){
				activeTicks++;
			}
			return;
		}
		
		boolean active = false;
		
		int consumed = IPConfig.EXTRACTION.pumpjack_consumption.get();
		int extracted = consumePower ? energyStorage.extractEnergy(consumed, true) : consumed;
		
		if(extracted >= consumed && canExtract() && !this.isRSDisabled()){
			if((getPos().getX() + getPos().getZ()) % IPConfig.EXTRACTION.pipe_check_ticks.get() == pipeTicks){
				lastHadPipes = hasPipes();
			}
			if(lastHadPipes){
				int residual = getResidualOil();
				if(availableOil() > 0 || residual > 0){
					int oilAmnt = availableOil() <= 0 ? residual : availableOil();
					
					energyStorage.extractEnergy(consumed, false);
					active = true;
					FluidStack out = new FluidStack(availableFluid(), Math.min(IPConfig.EXTRACTION.pumpjack_speed.get(), oilAmnt));
					BlockPos outputPos = this.getPos().offset(getFacing(), 2).offset(getFacing().rotateY().getOpposite(), 2).offset(Direction.DOWN, 1);
					IFluidHandler output = FluidUtil.getFluidHandler(world, outputPos, getFacing().rotateY()).orElse(null);
					if(output != null){
						int accepted = output.fill(out, FluidAction.SIMULATE);
						if(accepted > 0){
							int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), FluidAction.EXECUTE);
							extractOil(drained);
							out = Utils.copyFluidStackWithAmount(out, out.getAmount() - drained, false);
						}
					}
					
					outputPos = this.getPos().offset(getFacing(), 2).offset(getFacing().rotateY(), 2).offset(Direction.DOWN, 1);
					output = FluidUtil.getFluidHandler(world, outputPos, getFacing().rotateYCCW()).orElse(null);
					if(output != null){
						int accepted = output.fill(out, FluidAction.SIMULATE);
						if(accepted > 0){
							int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), FluidAction.EXECUTE);
							extractOil(drained);
						}
					}
					
					activeTicks++;
				}
			}
			pipeTicks = (pipeTicks + 1) % IPConfig.EXTRACTION.pipe_check_ticks.get();
		}
		
		if(active != wasActive){
			this.markDirty();
			this.markContainingBlockForUpdate(null);
		}
		
		wasActive = active;
		
	}
	
	
//	public float[] getBlockBounds(){
//		if(pos == 19)
//			return new float[]{
//					getFacing() == Direction.WEST ? .5f : 0, 0,
//					getFacing() == Direction.NORTH ? .5f : 0,
//					getFacing() == Direction.EAST ? .5f : 1, 1,
//					getFacing() == Direction.SOUTH ? .5f : 1};
//		if(pos == 17)
//			return new float[]{.0625f, 0, .0625f, .9375f, 1, .9375f};
//		
//		return new float[]{0, 0, 0, 0, 0, 0};
//	}
	
	
	public List<AxisAlignedBB> getAdvancedSelectionBounds(){
		Direction fl = getFacing();
		Direction fw = getFacing().rotateY();
		if(getIsMirrored()) fw = fw.getOpposite();
		
		int pos = 0; // FIXME Temporary Fix
		
		if(pos == 0){
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			
			float minX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 2F / 16F : ((fl == Direction.WEST) ? 10F / 16F : 2F / 16F);
			float maxX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 4F / 16F : ((fl == Direction.WEST) ? 14F / 16F : 6F / 16F);
			float minZ = (fl == Direction.EAST || fl == Direction.WEST) ? 2F / 16F : ((fl == Direction.NORTH) ? 10F / 16F : 2F / 16F);
			float maxZ = (fl == Direction.EAST || fl == Direction.WEST) ? 4F / 16F : ((fl == Direction.NORTH) ? 14F / 16F : 6F / 16F);
			list.add(new AxisAlignedBB(minX, 0.5, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			
			minX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 12F / 16F : minX;
			maxX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 14F / 16F : maxX;
			minZ = (fl == Direction.EAST || fl == Direction.WEST) ? 12F / 16F : minZ;
			maxZ = (fl == Direction.EAST || fl == Direction.WEST) ? 14F / 16F : maxZ;
			list.add(new AxisAlignedBB(minX, 0.5, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}else if(pos == 18){
			float minX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0F : ((fl == Direction.WEST) ? .5F : 0F);
			float maxX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1F : ((fl == Direction.WEST) ? 1F : .5F);
			float minZ = (fl == Direction.EAST || fl == Direction.WEST) ? 0F : ((fl == Direction.NORTH) ? .5F : 0F);
			float maxZ = (fl == Direction.EAST || fl == Direction.WEST) ? 1F : ((fl == Direction.NORTH) ? 1F : .5F);
			return Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}else if((pos >= 3 && pos <= 14 && pos != 10 && pos != 13 && pos != 11 && pos != 9) || pos == 1){
			return Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}else if(pos == 13){
			float minX = (fl == Direction.EAST || fl == Direction.WEST) ? 0F : .3125F;
			float maxX = (fl == Direction.EAST || fl == Direction.WEST) ? 1F : .685F;
			float minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0F : .3125F;
			float maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1F : .685F;
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, 0.5, minZ, maxX, .5 + 3 / 8F, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			list.add(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
			
		}else if(pos == 10){
			float minX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? .3125F : ((fl == Direction.EAST) ? 11F / 16F : 0F / 16F);
			float maxX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? .685F : ((fl == Direction.EAST) ? 16F / 16F : 5F / 16F);
			float minZ = (fl == Direction.EAST || fl == Direction.WEST) ? .3125F : ((fl == Direction.SOUTH) ? 11F / 16F : 0F / 16F);
			float maxZ = (fl == Direction.EAST || fl == Direction.WEST) ? .685F : ((fl == Direction.SOUTH) ? 16F / 16F : 5F / 16F);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, 0.5, minZ, maxX, .5 + 3 / 8F, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			
			minX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0F : 5F / 16F;
			maxX = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1F : 11F / 16F;
			minZ = (fl == Direction.EAST || fl == Direction.WEST) ? 0F : 5F / 16F;
			maxZ = (fl == Direction.EAST || fl == Direction.WEST) ? 1F : 11F / 16F;
			list.add(new AxisAlignedBB(minX, 0.5, minZ, maxX, .5 + 3 / 8F, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			
			list.add(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
			
		}else if(pos == 16){
			return Lists.newArrayList(new AxisAlignedBB(3 / 16F, 0, 3 / 16F, 13 / 16F, 1, 13 / 16F).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}else if(pos == 22){
			float minX = (fl == Direction.EAST || fl == Direction.WEST) ? 0F : .25F;
			float maxX = (fl == Direction.EAST || fl == Direction.WEST) ? 1F : .75F;
			float minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0F : .25F;
			float maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1F : .75F;
			return Lists.newArrayList(new AxisAlignedBB(minX, -0.75, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}else if(pos == 40){
			float minX = (fl == Direction.EAST || fl == Direction.WEST) ? 0F : .25F;
			float maxX = (fl == Direction.EAST || fl == Direction.WEST) ? 1F : .75F;
			float minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0F : .25F;
			float maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1F : .75F;
			return Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 0.25, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}else if(pos == 27){
			fl = this.getIsMirrored() ? getFacing().getOpposite() : getFacing();
			float minX = (fl == Direction.EAST || fl == Direction.WEST) ? 0.7F : ((fl == Direction.NORTH) ? 0.6F : -.1F);
			float maxX = (fl == Direction.EAST || fl == Direction.WEST) ? 1.4F : ((fl == Direction.NORTH) ? 1.1F : 0.4F);
			float minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0.7F : ((fl == Direction.EAST) ? .6F : -.1F);
			float maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1.4F : ((fl == Direction.EAST) ? 1.1F : 0.4F);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, -0.5F, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			
			minX = (fl == Direction.EAST || fl == Direction.WEST) ? -.4F : ((fl == Direction.NORTH) ? 0.6F : -.1F);
			maxX = (fl == Direction.EAST || fl == Direction.WEST) ? .3F : ((fl == Direction.NORTH) ? 1.1F : 0.4F);
			minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? -.4F : ((fl == Direction.EAST) ? .6F : -.1F);
			maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? .3F : ((fl == Direction.EAST) ? 1.1F : 0.4F);
			list.add(new AxisAlignedBB(minX, -0.5F, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}else if(pos == 29){
			fl = this.getIsMirrored() ? getFacing().getOpposite() : getFacing();
			float minX = (fl == Direction.EAST || fl == Direction.WEST) ? 0.7F : ((fl == Direction.SOUTH) ? 0.6F : -.1F);
			float maxX = (fl == Direction.EAST || fl == Direction.WEST) ? 1.4F : ((fl == Direction.SOUTH) ? 1.1F : 0.4F);
			float minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0.7F : ((fl == Direction.WEST) ? .6F : -.1F);
			float maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1.4F : ((fl == Direction.WEST) ? 1.1F : 0.4F);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, -0.5F, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			
			minX = (fl == Direction.EAST || fl == Direction.WEST) ? -.4F : ((fl == Direction.SOUTH) ? 0.6F : -.1F);
			maxX = (fl == Direction.EAST || fl == Direction.WEST) ? .3F : ((fl == Direction.SOUTH) ? 1.1F : 0.4F);
			minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? -.4F : ((fl == Direction.WEST) ? .6F : -.1F);
			maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? .3F : ((fl == Direction.WEST) ? 1.1F : 0.4F);
			list.add(new AxisAlignedBB(minX, -0.5F, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}else if(pos == 45){
			fl = this.getIsMirrored() ? getFacing().getOpposite() : getFacing();
			float minX = (fl == Direction.EAST || fl == Direction.WEST) ? 0F : ((fl == Direction.NORTH) ? 0.8F : -0.2F);
			float maxX = (fl == Direction.EAST || fl == Direction.WEST) ? 1F : ((fl == Direction.NORTH) ? 1.2F : 0.2F);
			float minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0F : ((fl == Direction.EAST) ? 0.8F : -0.2F);
			float maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1 : ((fl == Direction.EAST) ? 1.2F : 0.2F);
			return Lists.newArrayList(new AxisAlignedBB(minX, 0F, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}else if(pos == 47){
			fl = this.getIsMirrored() ? getFacing().getOpposite() : getFacing();
			float minX = (fl == Direction.EAST || fl == Direction.WEST) ? 0F : ((fl == Direction.NORTH) ? -0.2F : 0.8F);
			float maxX = (fl == Direction.EAST || fl == Direction.WEST) ? 1F : ((fl == Direction.NORTH) ? 0.2F : 1.2F);
			float minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0F : ((fl == Direction.EAST) ? -0.2F : 0.8F);
			float maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1 : ((fl == Direction.EAST) ? 0.2F : 1.2F);
			return Lists.newArrayList(new AxisAlignedBB(minX, 0F, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}else if(pos == 63 || pos == 65){
			return new ArrayList<AxisAlignedBB>();
		}else if(pos == 58 || pos == 61 || pos == 64 || pos == 67){
			float minX = (fl == Direction.EAST || fl == Direction.WEST) ? 0F : 0.25F;
			float maxX = (fl == Direction.EAST || fl == Direction.WEST) ? 1F : 0.75F;
			float minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0F : 0.25F;
			float maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1 : 0.75F;
			return Lists.newArrayList(new AxisAlignedBB(minX, -.25F, minZ, maxX, 0.75F, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}else if(pos == 70){
			float minX = (fl == Direction.EAST || fl == Direction.WEST) ? 0F : 0.125F;
			float maxX = (fl == Direction.EAST || fl == Direction.WEST) ? 1F : 0.875F;
			float minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0F : 0.125F;
			float maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1 : 0.875F;
			return Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1.25F, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}else if(pos == 52){
			float minX = (fl == Direction.EAST || fl == Direction.WEST) ? 0F : 0.125F;
			float maxX = (fl == Direction.EAST || fl == Direction.WEST) ? 1F : 0.875F;
			float minZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 0F : 0.125F;
			float maxZ = (fl == Direction.NORTH || fl == Direction.SOUTH) ? 1 : 0.875F;
			return Lists.newArrayList(new AxisAlignedBB(minX, .25F, minZ, maxX, 1F, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		
		List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
		list.add(new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		return list;
	}
	
	@Override
	public List<AxisAlignedBB> getAdvancedCollisionBounds(){
		// List list = new ArrayList<AxisAlignedBB>(); // Waste of ram
		return getAdvancedSelectionBounds();
	}
	
	@Override
	public Set<BlockPos> getEnergyPos(){
		return ImmutableSet.of(new BlockPos(2,1,0));
	}
	
	@Override
	public Set<BlockPos> getRedstonePos(){
		return ImmutableSet.of(new BlockPos(0,1,0));
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
	public void onProcessFinish(MultiblockProcess<IMultiblockRecipe> process){
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
	public float getMinProcessDistance(MultiblockProcess<IMultiblockRecipe> process){
		return 0;
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
	public IMultiblockRecipe findRecipeForInsertion(ItemStack inserting){
		return null;
	}
	
	@Override
	protected IMultiblockRecipe readRecipeFromNBT(CompoundNBT tag){
		return null;
	}
	
	@Override
	public boolean canUseGui(PlayerEntity player){
		return true;
	}
	
	@Override
	public IInteractionObjectIE getGuiMaster(){
		return master();
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
	public boolean additionalCanProcessCheck(MultiblockProcess<IMultiblockRecipe> process){
		return false;
	}
	
	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side){
		PumpjackTileEntity master = this.master();
		int pos = 0; // FIXME Temporary Fix
		if(master != null){
			if(pos == 9 && (side == null || side == getFacing().rotateY() || side == getFacing().getOpposite().rotateY())){
				return new FluidTank[]{fakeTank};
			}else if(pos == 11 && (side == null || side == getFacing().rotateY() || side == getFacing().getOpposite().rotateY())){
				return new FluidTank[]{fakeTank};
			}else if(pos == 16 && IPConfig.EXTRACTION.req_pipes.get() && (side == null || side == Direction.DOWN)){
				return new FluidTank[]{fakeTank};
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
	
	@Override
	public boolean isDummy(){
		return true;
	}
	
	@Override
	public PumpjackTileEntity master(){
		int[] offset = {0, 0, 0}; // FIXME Temporary Fix
		
		if(offset[0] == 0 && offset[1] == 0 && offset[2] == 0){
			return this;
		}
		TileEntity te = world.getTileEntity(getPos().add(-offset[0], -offset[1], -offset[2]));
		return this.getClass().isInstance(te) ? (PumpjackTileEntity) te : null;
	}
	
	@Override
	public PumpjackTileEntity getTileForPos(BlockPos targetPosInMB){
		return super.getTileForPos(targetPosInMB);
	}
	
	/*
	@Override
	public TileEntityPumpjack getTileForPos(int targetPos){
		BlockPos target = getBlockPosForPos(targetPos);
		TileEntity tile = world.getTileEntity(target);
		return tile instanceof TileEntityPumpjack ? (TileEntityPumpjack) tile : null;
	}
	*/
}
