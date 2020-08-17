package flaxbeard.immersivepetroleum.common.blocks.metal;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import com.mojang.blaze3d.platform.GlStateManager;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.metal.BucketWheelTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.CrusherTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.ExcavatorTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.api.energy.FuelHandler;
import flaxbeard.immersivepetroleum.client.model.ModelLubricantPipes;
import flaxbeard.immersivepetroleum.common.IPContent.Fluids;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Hand;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class AutoLubricatorTileEntity extends IEBaseTileEntity implements IDirectionalTile, IHasDummyBlocks, ITickable, IPlayerInteraction, IBlockOverlayText, IBlockBounds, ITileDrop{
	public static TileEntityType<AutoLubricatorTileEntity> TYPE;
	
	public static class PumpjackLubricationHandler implements ILubricationHandler<PumpjackTileEntity>{
		@Override
		public TileEntity isPlacedCorrectly(World world, AutoLubricatorTileEntity tile, Direction facing){
			BlockPos target = tile.getPos().offset(facing, 2).up();
			TileEntity te = world.getTileEntity(target);
			
			if(te instanceof PumpjackTileEntity){
				PumpjackTileEntity jack = (PumpjackTileEntity) te;
				PumpjackTileEntity master = jack.master();
				
				Direction f = master.getIsMirrored() ? facing : facing.getOpposite();
				if(jack == master && jack.getFacing().rotateY() == f){
					return master;
				}
			}
			
			return null;
		}
		
		@Override
		public boolean isMachineEnabled(World world, PumpjackTileEntity mbte){
			return mbte.wasActive;
		}
		
		@Override
		public void lubricate(World world, int ticks, PumpjackTileEntity mbte){
			if(!world.isRemote){
				if(ticks % 4 == 0){
					mbte.update(true);
				}
			}else{
				mbte.activeTicks += 1F / 4F;
			}
		}
		
		@Override
		public void spawnLubricantParticles(World world, AutoLubricatorTileEntity tile, Direction facing, PumpjackTileEntity mbte){
			Direction f = mbte.getIsMirrored() ? facing : facing.getOpposite();
			float location = world.rand.nextFloat();
			
			boolean flip = f.getAxis() == Axis.Z ^ facing.getAxisDirection() == AxisDirection.POSITIVE ^ !mbte.getIsMirrored();
			float xO = 2.5F;
			float zO = -.15F;
			float yO = 2.25F;
			
			if(location > .5F){
				xO = 1.7F;
				yO = 2.9F;
				zO = -1.5F;
				
			}
			
			if(facing.getAxisDirection() == AxisDirection.NEGATIVE) xO = -xO + 1;
			if(!flip) zO = -zO + 1;
			
			float x = tile.pos.getX() + (f.getAxis() == Axis.X ? xO : zO);
			float y = tile.pos.getY() + yO;
			float z = tile.pos.getZ() + (f.getAxis() == Axis.X ? zO : xO);
			
			for(int i = 0;i < 3;i++){
				float r1 = (world.rand.nextFloat() - .5F) * 2F;
				float r2 = (world.rand.nextFloat() - .5F) * 2F;
				float r3 = world.rand.nextFloat();
				BlockState n = Fluids.fluidLubricant.block.getDefaultState();
				world.addParticle(new BlockParticleData(ParticleTypes.FALLING_DUST, n), x, y, z, r1 * 0.04F, r3 * 0.0125F, r2 * 0.025F);
			}
		}
		
		private static Object pumpjackM;
		private static Object pumpjack;
		
		@Override
		@OnlyIn(Dist.CLIENT)
		public void renderPipes(World world, AutoLubricatorTileEntity tile, Direction facing, PumpjackTileEntity mbte){
			if(pumpjackM == null){
				pumpjackM = new ModelLubricantPipes.Pumpjack(true);
				pumpjack = new ModelLubricantPipes.Pumpjack(false);
			}
			
			GlStateManager.translatef(0, -1, 0);
			Vec3i offset = mbte.getPos().subtract(tile.getPos());
			GlStateManager.translatef(offset.getX(), offset.getY(), offset.getZ());
			
			Direction rotation = mbte.getFacing();
			if(rotation == Direction.NORTH){
				GlStateManager.rotatef(90F, 0, 1, 0);
				GlStateManager.translatef(-1, 0, 0);
			}else if(rotation == Direction.WEST){
				GlStateManager.rotatef(180F, 0, 1, 0);
				GlStateManager.translatef(-1, 0, -1);
			}else if(rotation == Direction.SOUTH){
				GlStateManager.rotatef(270F, 0, 1, 0);
				GlStateManager.translatef(0, 0, -1);
			}
			GlStateManager.translatef(-1, 0, -1);
			ClientUtils.bindTexture("immersivepetroleum:textures/blocks/lube_pipe12.png");
			if(mbte.getIsMirrored()){
				((ModelLubricantPipes.Pumpjack) pumpjackM).render(null, 0, 0, 0, 0, 0, 0.0625F);
			}else{
				((ModelLubricantPipes.Pumpjack) pumpjack).render(null, 0, 0, 0, 0, 0, 0.0625F);
			}
		}
		
		@Override
		public Tuple<BlockPos, Direction> getGhostBlockPosition(World world, PumpjackTileEntity mbte){
			if(!mbte.isDummy()){
				BlockPos pos = mbte.getPos().offset(mbte.getIsMirrored() ? mbte.getFacing().rotateYCCW() : mbte.getFacing().rotateY(), 2);
				Direction f = mbte.getIsMirrored() ? mbte.getFacing().rotateY() : mbte.getFacing().rotateYCCW();
				return new Tuple<BlockPos, Direction>(pos, f);
			}
			return null;
		}
		
		private static Vec3i size;
		@Override
		public Vec3i getStructureDimensions(){
			if(size==null)
				size=new Vec3i(4, 6, 3);
			return size;
		}
	}
	
	public static class ExcavatorLubricationHandler implements ILubricationHandler<ExcavatorTileEntity>{
		
		@Override
		public TileEntity isPlacedCorrectly(World world, AutoLubricatorTileEntity tile, Direction facing){
			BlockPos initialTarget = tile.getPos().offset(facing);
			ExcavatorTileEntity adjacent = (ExcavatorTileEntity) world.getTileEntity(initialTarget);
			Direction f = adjacent.getIsMirrored() ? facing : facing.getOpposite();
			
			BlockPos target = tile.getPos().offset(facing, 2).offset(f.rotateY(), 4).up();
			TileEntity te = world.getTileEntity(target);
			
			if(te instanceof ExcavatorTileEntity){
				ExcavatorTileEntity excavator = (ExcavatorTileEntity) te;
				ExcavatorTileEntity master = excavator.master();
				
				if(excavator == master && excavator.getFacing().rotateY() == f){
					return master;
				}
			}
			
			return null;
		}
		
		@Override
		public boolean isMachineEnabled(World world, ExcavatorTileEntity mbte){
			BlockPos wheelPos = mbte.master().getOrigin();
			TileEntity center = world.getTileEntity(wheelPos);
			
			if(center instanceof BucketWheelTileEntity){
				BucketWheelTileEntity wheel = (BucketWheelTileEntity) center;
				
				return wheel.active;
			}
			return false;
		}
		
		@Override
		public void lubricate(World world, int ticks, ExcavatorTileEntity mbte){
			BlockPos wheelPos = mbte.master().getOrigin();// getOrigin();//BlockPosForPos(31);
			TileEntity center = world.getTileEntity(wheelPos);
			
			if(center instanceof BucketWheelTileEntity){
				BucketWheelTileEntity wheel = (BucketWheelTileEntity) center;
				
				if(!world.isRemote && ticks % 4 == 0){
					int consumed = IEConfig.MACHINES.excavator_consumption.get();
					int extracted = mbte.energyStorage.extractEnergy(consumed, true);
					if(extracted >= consumed){
						mbte.energyStorage.extractEnergy(extracted, false);
						wheel.rotation += IEConfig.MACHINES.excavator_speed.get() / 4F;
					}
				}else{
					wheel.rotation += IEConfig.MACHINES.excavator_speed.get() / 4F;
				}
				
			}
		}
		
		@Override
		public void spawnLubricantParticles(World world, AutoLubricatorTileEntity tile, Direction facing, ExcavatorTileEntity mbte){
			Direction f = mbte.getIsMirrored() ? facing : facing.getOpposite();
			
			float location = world.rand.nextFloat();
			
			boolean flip = f.getAxis() == Axis.Z ^ facing.getAxisDirection() == AxisDirection.POSITIVE ^ !mbte.getIsMirrored();
			float xO = 1.2F;
			float zO = -.5F;
			float yO = .5F;
			
			if(location > .5F){
				xO = 0.9F;
				yO = 0.8F;
				zO = 1.75F;
			}
			
			if(facing.getAxisDirection() == AxisDirection.NEGATIVE) xO = -xO + 1;
			if(!flip) zO = -zO + 1;
			
			float x = tile.pos.getX() + (f.getAxis() == Axis.X ? xO : zO);
			float y = tile.pos.getY() + yO;
			float z = tile.pos.getZ() + (f.getAxis() == Axis.X ? zO : xO);
			
			for(int i = 0;i < 3;i++){
				float r1 = (world.rand.nextFloat() - .5F) * 2F;
				float r2 = (world.rand.nextFloat() - .5F) * 2F;
				float r3 = world.rand.nextFloat();
				BlockState n = Fluids.fluidLubricant.block.getDefaultState();
				world.addParticle(new BlockParticleData(ParticleTypes.FALLING_DUST, n), x, y, z, r1 * 0.04F, r3 * 0.0125F, r2 * 0.025F);
			}
		}
		
		private static Object excavator;
		private static Object excavatorM;
		
		@Override
		@OnlyIn(Dist.CLIENT)
		public void renderPipes(World world, AutoLubricatorTileEntity tile, Direction facing, ExcavatorTileEntity mbte){
			if(excavator == null){
				excavatorM = new ModelLubricantPipes.Excavator(true);
				excavator = new ModelLubricantPipes.Excavator(false);
			}
			
			GlStateManager.translatef(0, -1, 0);
			Vec3i offset = mbte.getPos().subtract(tile.getPos());
			GlStateManager.translatef(offset.getX(), offset.getY(), offset.getZ());
			
			Direction rotation = mbte.getFacing();
			if(rotation == Direction.NORTH){
				GlStateManager.rotatef(90F, 0, 1, 0);
				
			}else if(rotation == Direction.WEST){
				GlStateManager.rotatef(180F, 0, 1, 0);
				GlStateManager.translatef(0, 0, -1);
				
			}else if(rotation == Direction.SOUTH){
				GlStateManager.rotatef(270F, 0, 1, 0);
				GlStateManager.translatef(1, 0, -1);
			}else if(rotation == Direction.EAST){
				GlStateManager.translatef(1, 0, 0);
				
			}
			
			GlStateManager.translatef(-1, 0, -1);
			ClientUtils.bindTexture("immersivepetroleum:textures/blocks/lube_pipe12.png");
			if(mbte.getIsMirrored()){
				((ModelLubricantPipes.Excavator) excavatorM).render(null, 0, 0, 0, 0, 0, 0.0625F);
			}else{
				((ModelLubricantPipes.Excavator) excavator).render(null, 0, 0, 0, 0, 0, 0.0625F);
			}
		}
		
		@Override
		public Tuple<BlockPos, Direction> getGhostBlockPosition(World world, ExcavatorTileEntity mbte){
			if(!mbte.isDummy()){
				BlockPos pos = mbte.getPos().offset(mbte.getFacing(), 4).offset(mbte.getIsMirrored() ? mbte.getFacing().rotateYCCW() : mbte.getFacing().rotateY(), 2);
				Direction f = mbte.getIsMirrored() ? mbte.getFacing().rotateY() : mbte.getFacing().rotateYCCW();
				return new Tuple<BlockPos, Direction>(pos, f);
			}
			return null;
		}
		
		private static Vec3i size;
		@Override
		public Vec3i getStructureDimensions(){
			if(size==null)
				size=new Vec3i(3, 6, 3);
			return size;
		}
	}
	
	public static class CrusherLubricationHandler implements ILubricationHandler<CrusherTileEntity>{
		
		@Override
		public TileEntity isPlacedCorrectly(World world, AutoLubricatorTileEntity tile, Direction facing){
			
			BlockPos target = tile.getPos().offset(facing, 2).up();
			TileEntity te = world.getTileEntity(target);
			
			if(te instanceof CrusherTileEntity){
				CrusherTileEntity excavator = (CrusherTileEntity) te;
				CrusherTileEntity master = excavator.master();
				
				Direction f = facing;
				if(excavator == master && excavator.getFacing().getOpposite() == f){
					
					return master;
				}
			}
			
			return null;
		}
		
		@Override
		public boolean isMachineEnabled(World world, CrusherTileEntity mbte){
			return mbte.shouldRenderAsActive();
		}
		
		@Override
		public void lubricate(World world, int ticks, CrusherTileEntity mbte){
			Iterator<MultiblockProcess<CrusherRecipe>> processIterator = mbte.processQueue.iterator();
			MultiblockProcess<CrusherRecipe> process = processIterator.next();
			
			if(!world.isRemote){
				if(ticks % 4 == 0){
					int consume = mbte.energyStorage.extractEnergy(process.energyPerTick, true);
					if(consume >= process.energyPerTick){
						mbte.energyStorage.extractEnergy(process.energyPerTick, false);
						if(process.processTick < process.maxTicks) process.processTick++;
						if(process.processTick >= process.maxTicks && mbte.processQueue.size() > 1){
							process = processIterator.next();
							if(process.processTick < process.maxTicks) process.processTick++;
						}
					}
				}
			}else{
				mbte.animation_barrelRotation += 18f / 4f;
				mbte.animation_barrelRotation %= 360f;
			}
		}
		
		@Override
		public void spawnLubricantParticles(World world, AutoLubricatorTileEntity tile, Direction facing, CrusherTileEntity mbte){
			
			Direction f = mbte.getIsMirrored() ? facing : facing.getOpposite();
			
			float location = world.rand.nextFloat();
			
			boolean flip = f.getAxis() == Axis.Z ^ facing.getAxisDirection() == AxisDirection.POSITIVE ^ !mbte.getIsMirrored();
			float xO = 2.5F;
			float zO = -0.1F;
			float yO = 1.3F;
			
			if(location > .5F){
				xO = 1.0F;
				yO = 3.0F;
				zO = 1.65F;
			}
			
			if(facing.getAxisDirection() == AxisDirection.NEGATIVE) xO = -xO + 1;
			if(!flip) zO = -zO + 1;
			
			float x = tile.pos.getX() + (f.getAxis() == Axis.X ? xO : zO);
			float y = tile.pos.getY() + yO;
			float z = tile.pos.getZ() + (f.getAxis() == Axis.X ? zO : xO);
			
			for(int i = 0;i < 3;i++){
				float r1 = (world.rand.nextFloat() - .5F) * 2F;
				float r2 = (world.rand.nextFloat() - .5F) * 2F;
				float r3 = world.rand.nextFloat();
				BlockState n = Fluids.fluidLubricant.block.getDefaultState();
				world.addParticle(new BlockParticleData(ParticleTypes.FALLING_DUST, n), x, y, z, r1 * 0.04F, r3 * 0.0125F, r2 * 0.025F);
			}
		}
		
		private static Object excavator;
		
		@Override
		@OnlyIn(Dist.CLIENT)
		public void renderPipes(World world, AutoLubricatorTileEntity tile, Direction facing, CrusherTileEntity mbte){
			if(excavator == null){
				excavator = new ModelLubricantPipes.Crusher(false);
			}
			
			GlStateManager.translatef(0, -1, 0);
			Vec3i offset = mbte.getPos().subtract(tile.getPos());
			GlStateManager.translatef(offset.getX(), offset.getY(), offset.getZ());
			
			Direction rotation = mbte.getFacing();
			if(rotation == Direction.NORTH){
				GlStateManager.rotatef(90F, 0, 1, 0);
				GlStateManager.translatef(-1, 0, 0);
			}else if(rotation == Direction.WEST){
				GlStateManager.rotatef(180F, 0, 1, 0);
				GlStateManager.translatef(-1, 0, -1);
				
			}else if(rotation == Direction.SOUTH){
				GlStateManager.rotatef(270F, 0, 1, 0);
				GlStateManager.translatef(0, 0, -1);
				
			}
			
			ClientUtils.bindTexture("immersivepetroleum:textures/blocks/lube_pipe12.png");
			((ModelLubricantPipes.Crusher) excavator).render(null, 0, 0, 0, 0, 0, 0.0625F);
		}
		
		@Override
		public Tuple<BlockPos, Direction> getGhostBlockPosition(World world, CrusherTileEntity mbte){
			if(!mbte.isDummy()){
				BlockPos pos = mbte.getPos().offset(mbte.getFacing(), 2);
				Direction f = mbte.getFacing().getOpposite();
				return new Tuple<BlockPos, Direction>(pos, f);
			}
			return null;
		}
		
		private static Vec3i size;
		@Override
		public Vec3i getStructureDimensions(){
			if(size==null)
				size=new Vec3i(3,3,5);
			return size;
		}
	}
	
	public boolean active;
	public int dummy = 0;
	public Direction facing = Direction.NORTH;
	public FluidTank tank = new FluidTank(8000, new Predicate<FluidStack>(){
		@Override
		public boolean test(FluidStack fluid){
			return fluid != null && FuelHandler.isValidFuel(fluid.getFluid());
		}
	});
	
	public boolean predictablyDraining = false;
	
	public AutoLubricatorTileEntity(TileEntityType<? extends TileEntity> type){
		super(type);
	}
	
	public boolean shouldRenderInPass(int pass){
		return pass <= 2;
	}
	
	@Override
	public boolean isDummy(){
		return dummy > 0;
	}
	
	public void placeDummies(BlockItemUseContext ctx, BlockState state){
		world.setBlockState(pos.add(0, 1, 0), state);
		AutoLubricatorTileEntity tile = (AutoLubricatorTileEntity) world.getTileEntity(pos.add(0, 1, 0));
		
		tile.dummy = 1;
		tile.facing = this.facing;
	}
	
	@Override
	public void breakDummies(BlockPos pos, BlockState state){
		for(int i = 0;i <= 1;i++){
			if(world.getTileEntity(getPos().add(0, -dummy, 0).add(0, i, 0)) instanceof AutoLubricatorTileEntity)
				world.setBlockState(getPos().add(0, -dummy, 0).add(0, i, 0), Blocks.AIR.getDefaultState());
		}
	}
	
	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket){
		dummy = nbt.getInt("dummy");
		facing = Direction.byIndex(nbt.getInt("facing"));
		if(facing == Direction.DOWN || facing == Direction.UP){
			facing = Direction.NORTH;
		}
		active = nbt.getBoolean("active");
		tank.readFromNBT(nbt.getCompound("tank"));
		count = nbt.getInt("count");
		predictablyDraining = nbt.getBoolean("predictablyDraining");
		
		if(descPacket) this.markContainingBlockForUpdate(null);
	}
	
	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket){
		nbt.putInt("dummy", dummy);
		nbt.putInt("facing", facing.ordinal());
		nbt.putInt("count", count);
		nbt.putBoolean("active", active);
		nbt.putBoolean("predictablyDraining", predictablyDraining);
		
		CompoundNBT tankTag = tank.writeToNBT(new CompoundNBT());
		nbt.put("tank", tankTag);
	}
	
	@Override
	public Direction getFacing(){
		return facing;
	}
	
	@Override
	public void setFacing(Direction facing){
		if(facing == Direction.DOWN || facing == Direction.UP){
			facing = Direction.NORTH;
		}
		this.facing = facing;
	}
	
	@Override
	public PlacementLimitation getFacingLimitation(){
		return PlacementLimitation.HORIZONTAL;
	}
	
	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer){
		return false;
	}
	
	@Override
	public boolean canHammerRotate(Direction side, Vec3d hit, LivingEntity entity){
		return false;
	}
	
	@Override
	public boolean canRotate(Direction axis){
		return true;
	}
	
	@Override
	public void afterRotation(Direction oldDir, Direction newDir){
		for(int i = 0;i <= 1;i++){
			TileEntity te = world.getTileEntity(getPos().add(0, -dummy + i, 0));
			if(te instanceof AutoLubricatorTileEntity){
				((AutoLubricatorTileEntity) te).setFacing(newDir);
				te.markDirty();
				((AutoLubricatorTileEntity) te).markContainingBlockForUpdate(null);
			}
		}
	}
	
	private LazyOptional<IFluidHandler> outputHandler = registerConstantCap(this.tank);
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing){
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && dummy == 1 && (facing == null || facing == Direction.UP)){
			return this.outputHandler.cast();
		}
		return super.getCapability(capability, facing);
	}
	
	int count = 0;
	int lastTank = 0;
	int lastTankUpdate = 0;
	int countClient = 0;
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void tick(){
		if(dummy == 0){
			if(tank.getFluid() != null && tank.getFluid().getFluid() != null && tank.getFluidAmount() >= LubricantHandler.getLubeAmount(tank.getFluid().getFluid()) && LubricantHandler.isValidLube(tank.getFluid().getFluid())){
				BlockPos target = pos.offset(facing);
				TileEntity te = world.getTileEntity(target);
				
				ILubricationHandler handler = LubricatedHandler.getHandlerForTile(te);
				if(handler != null){
					TileEntity master = handler.isPlacedCorrectly(world, this, facing);
					if(master != null){
						if(handler.isMachineEnabled(world, master)){
							count++;
							handler.lubricate(world, count, master);
							
							if(!world.isRemote && count % 4 == 0){
								tank.drain(LubricantHandler.getLubeAmount(tank.getFluid().getFluid()), FluidAction.EXECUTE);
							}
							
							if(world.isRemote){
								countClient++;
								if(countClient % 50 == 0){
									countClient = world.rand.nextInt(40);
									handler.spawnLubricantParticles(world, this, facing, master);
								}
							}
						}
					}
				}
			}
			
			if(!world.isRemote && lastTank != this.tank.getFluidAmount()){
				if(predictablyDraining && tank.getFluid() != null && lastTank - this.tank.getFluidAmount() == LubricantHandler.getLubeAmount(tank.getFluid().getFluid())){
					lastTank = this.tank.getFluidAmount();
					return;
				}
				if(Math.abs(lastTankUpdate - this.tank.getFluidAmount()) > 25){
					markContainingBlockForUpdate(null);
					predictablyDraining = tank.getFluid() != null && lastTank - this.tank.getFluidAmount() == LubricantHandler.getLubeAmount(tank.getFluid().getFluid());
					lastTankUpdate = this.tank.getFluidAmount();
				}
				lastTank = this.tank.getFluidAmount();
			}
		}
	}
	
	@Override
	public boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ){
		TileEntity master = world.getTileEntity(getPos().add(0, -dummy, 0));
		if(master != null && master instanceof AutoLubricatorTileEntity){
			if(FluidUtil.interactWithFluidHandler(player, hand, ((AutoLubricatorTileEntity) master).tank)){
				((AutoLubricatorTileEntity) master).markContainingBlockForUpdate(null);
				return true;
			}
		}
		return false;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox(){
		BlockPos nullPos = this.getPos();
		return new AxisAlignedBB(nullPos.offset(facing, -5).offset(facing.rotateY(), -5).down(1), nullPos.offset(facing, 5).offset(facing.rotateY(), 5).up(3));
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public double getMaxRenderDistanceSquared(){
		return super.getMaxRenderDistanceSquared() * IEConfig.GENERAL.increasedTileRenderdistance.get();
	}
	
	@Override
	public String[] getOverlayText(PlayerEntity player, RayTraceResult mop, boolean hammer){
		if(Utils.isFluidRelatedItemStack(player.getHeldItem(Hand.MAIN_HAND))){
			TileEntity master = world.getTileEntity(getPos().add(0, -dummy, 0));
			if(master != null && master instanceof AutoLubricatorTileEntity){
				AutoLubricatorTileEntity lube = (AutoLubricatorTileEntity) master;
				String s = null;
				if(lube.tank.getFluid() != null)
					s = lube.tank.getFluid().getDisplayName() + ": " + lube.tank.getFluidAmount() + "mB";
				else
					s = I18n.format(Lib.GUI + "empty");
				return new String[]{s};
			}
		}
		return null;
	}
	
	@Override
	public boolean useNixieFont(PlayerEntity player, RayTraceResult mop){
		return false;
	}

	@Override
	public IGeneralMultiblock master(){
		return master();
	}

	@Override
	public VoxelShape getBlockBounds(ISelectionContext ctx){
		if(dummy==1){
			return VoxelShapes.create(.1875F, 0, .1875F, .8125f, 1, .8125f);
		}else{
			return VoxelShapes.create(.0625f, 0, .0625f, .9375f, 1, .9375f);
		}
	}
	
	public void readTank(CompoundNBT nbt){
		tank.readFromNBT(nbt.getCompound("tank"));
	}
	
	public void writeTank(CompoundNBT nbt, boolean toItem){
		boolean write = tank.getFluidAmount() > 0;
		CompoundNBT tankTag = tank.writeToNBT(new CompoundNBT());
		if(!toItem || write) nbt.put("tank", tankTag);
	}
	
	@Override
	public void readOnPlacement(LivingEntity placer, ItemStack stack){
		if(stack.hasTag()){
			readTank(stack.getTag());
		}
	}
	
	public List<ItemStack> getTileDrop(PlayerEntity player, BlockState state){
		ItemStack stack = new ItemStack(state.getBlock(), 1);
		CompoundNBT tag = new CompoundNBT();
		writeTank(tag, true);
		if(!tag.isEmpty()) stack.setTag(tag);
		return Arrays.asList(stack);
	}
	
	@Override
	public List<ItemStack> getTileDrops(LootContext context){
		ItemStack stack = new ItemStack(getState().getBlock(), 1);
		CompoundNBT tag = new CompoundNBT();
		writeTank(tag, true);
		if(!tag.isEmpty()) stack.setTag(tag);
		return Arrays.asList(stack);
	}
}
