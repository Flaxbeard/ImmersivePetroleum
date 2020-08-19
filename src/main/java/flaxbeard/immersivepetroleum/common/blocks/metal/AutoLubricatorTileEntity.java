package flaxbeard.immersivepetroleum.common.blocks.metal;

import java.util.Arrays;
import java.util.List;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.api.energy.FuelHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

// TODO Delete this and rename AutoLubricatorNewTileEntity to AutoLubricatorTileEntity instead
public class AutoLubricatorTileEntity extends IEBaseTileEntity implements IDirectionalTile, IHasDummyBlocks, ITickable, IPlayerInteraction, IBlockOverlayText, IBlockBounds, ITileDrop{
	public static TileEntityType<AutoLubricatorTileEntity> TYPE;
	
	public boolean active;
	public int dummy = 0;
	public Direction facing = Direction.NORTH;
	public FluidTank tank = new FluidTank(8000, fluid->(fluid != null && FuelHandler.isValidFuel(fluid.getFluid())));
	
	public boolean predictablyDraining = false;
	
	public AutoLubricatorTileEntity(){
		super(TYPE);
	}
	
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
		return true;
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
	
	@Override
	public List<ItemStack> getTileDrops(LootContext context){
		ItemStack stack = new ItemStack(getState().getBlock(), 1);
		CompoundNBT tag = new CompoundNBT();
		writeTank(tag, true);
		if(!tag.isEmpty()) stack.setTag(tag);
		return Arrays.asList(stack);
	}
	
	@Deprecated
	public List<ItemStack> getTileDrop(PlayerEntity player, BlockState state){
		ItemStack stack = new ItemStack(state.getBlock(), 1);
		CompoundNBT tag = new CompoundNBT();
		writeTank(tag, true);
		if(!tag.isEmpty()) stack.setTag(tag);
		return Arrays.asList(stack);
	}
}
