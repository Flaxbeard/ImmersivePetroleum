package flaxbeard.immersivepetroleum.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.SampleDrillTileEntity;
import blusunrize.immersiveengineering.common.items.CoresampleItem;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.LubricatedTileInfo;
import flaxbeard.immersivepetroleum.api.crafting.pumpjack.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.pumpjack.ReservoirWorldInfo;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import flaxbeard.immersivepetroleum.common.fluids.NapalmFluid;
import flaxbeard.immersivepetroleum.common.util.IPEffects;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommonEventHandler{
	
	@SubscribeEvent
	public void onSave(WorldEvent.Save event){
		if(!event.getWorld().isRemote()){
			IPSaveData.setDirty();
		}
	}
	
	@SubscribeEvent
	public void onUnload(WorldEvent.Unload event){
		if(!event.getWorld().isRemote()){
			IPSaveData.setDirty();
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void handlePickupItem(RightClickBlock event){
		BlockPos pos = event.getPos();
		BlockState state = event.getWorld().getBlockState(pos);
		if(state.getBlock() == IEBlocks.MetalDevices.sampleDrill){
			TileEntity te = event.getWorld().getTileEntity(pos);
			if(te instanceof SampleDrillTileEntity){
				SampleDrillTileEntity drill = (SampleDrillTileEntity) te;
				if(drill.isDummy()){
					drill = (SampleDrillTileEntity) drill.master();
				}
				
				if(!drill.sample.isEmpty()){
					ColumnPos cPos = CoresampleItem.getCoords(drill.sample);
					if(cPos != null){
						try{
							World world = event.getWorld();
							DimensionChunkCoords coords = new DimensionChunkCoords(world.getDimensionKey(), cPos.x >> 4, cPos.z >> 4);
							
							ReservoirWorldInfo info = PumpjackHandler.getOrCreateOilWorldInfo(world, coords, false);
							if(info != null && info.getType() != null){
								ItemNBTHelper.putString(drill.sample, "resType", info.getType().name);
								ItemNBTHelper.putInt(drill.sample, "resAmount", info.current);
							}else{
								ItemNBTHelper.putInt(drill.sample, "resAmount", 0);
							}
							
						}catch(Exception e){
							ImmersivePetroleum.log.warn("This aint good!", e);
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void handleBoatImmunity(LivingAttackEvent event){
		if(event.getSource() == DamageSource.LAVA || event.getSource() == DamageSource.ON_FIRE || event.getSource() == DamageSource.IN_FIRE){
			LivingEntity entity = event.getEntityLiving();
			if(entity.getRidingEntity() instanceof MotorboatEntity){
				MotorboatEntity boat = (MotorboatEntity) entity.getRidingEntity();
				if(boat.isFireproof){
					event.setCanceled(true);
					return;
				}
			}
			
			if(entity.getFireTimer() > 0 && entity.getActivePotionEffect(IPEffects.ANTI_DISMOUNT_FIRE) != null){
				entity.extinguish();
				entity.removePotionEffect(IPEffects.ANTI_DISMOUNT_FIRE);
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public void handleBoatImmunity(PlayerTickEvent event){
		PlayerEntity entity = event.player;
		if(entity.isBurning() && entity.getRidingEntity() instanceof MotorboatEntity){
			MotorboatEntity boat = (MotorboatEntity) entity.getRidingEntity();
			if(boat.isFireproof){
				entity.extinguish();
				boat.setFlag(0, false);
			}
		}
	}
	
	/**
	 * Handles dismounting the Speedboat while in lava to trying avoid getting
	 * burned
	 */
	@SubscribeEvent
	public void handleDismountingBoat(EntityMountEvent event){
		if(event.getEntityMounting() == null){
			return;
		}
		
		if(event.getEntityMounting() instanceof LivingEntity && event.getEntityBeingMounted() instanceof MotorboatEntity){
			if(event.isDismounting()){
				MotorboatEntity boat = (MotorboatEntity) event.getEntityBeingMounted();
				
				if(boat.isFireproof){
					FluidState fluidstate = event.getWorldObj().getBlockState(new BlockPos(boat.getPositionVec().add(0.5, 0, 0.5))).getFluidState();
					if(fluidstate != Fluids.EMPTY.getDefaultState() && fluidstate.isTagged(FluidTags.LAVA)){
						LivingEntity living = (LivingEntity) event.getEntityMounting();
						
						living.addPotionEffect(new EffectInstance(IPEffects.ANTI_DISMOUNT_FIRE, 1, 0, false, false));
						return;
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void handleLubricatingMachinesServer(WorldTickEvent event){
		if(event.phase == Phase.END){
			handleLubricatingMachines(event.world);
		}
	}
	
	static final Random random = new Random();
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void handleLubricatingMachines(World world){
		Set<LubricatedTileInfo> toRemove = new HashSet<LubricatedTileInfo>();
		for(LubricatedTileInfo info:LubricatedHandler.lubricatedTiles){
			if(info.world == world.getDimensionKey() && world.isAreaLoaded(info.pos, 0)){
				TileEntity te = world.getTileEntity(info.pos);
				ILubricationHandler lubeHandler = LubricatedHandler.getHandlerForTile(te);
				if(lubeHandler != null){
					if(lubeHandler.isMachineEnabled(world, te)){
						lubeHandler.lubricate(world, info.ticks, te);
					}
					
					if(world.isRemote){
						if(te instanceof MultiblockPartTileEntity){
							MultiblockPartTileEntity<?> part = (MultiblockPartTileEntity<?>) te;
							
							BlockParticleData lubeParticle = new BlockParticleData(ParticleTypes.FALLING_DUST, IPContent.Fluids.lubricant.block.getDefaultState());
							Vector3i size = lubeHandler.getStructureDimensions();
							
							int numBlocks = (int) (size.getX() * size.getY() * size.getZ() * 0.25F);
							
							for(int i = 0;i < numBlocks;i++){
								BlockPos pos = part.getBlockPosForPos(new BlockPos(size.getX() * random.nextFloat(), size.getY() * random.nextFloat(), size.getZ() * random.nextFloat()));
								if(world.getBlockState(pos) == Blocks.AIR.getDefaultState())
									continue;
								
								TileEntity te2 = world.getTileEntity(info.pos);
								if(te2 != null && te2 instanceof MultiblockPartTileEntity){
									if(((MultiblockPartTileEntity<?>) te2).master() == part.master()){
										for(Direction facing:Direction.Plane.HORIZONTAL){
											if(world.rand.nextInt(30) == 0){// && world.getBlockState(pos.offset(facing)).getBlock().isReplaceable(world, pos.offset(facing))){
												Vector3i direction = facing.getDirectionVec();
												world.addParticle(lubeParticle,
														pos.getX() + .5f + direction.getX() * .65f,
														pos.getY() + 1,
														pos.getZ() + .5f + direction.getZ() * .65f,
														0, 0, 0);
											}
										}
									}
								}
							}
						}
					}
					
					info.ticks--;
					if(info.ticks == 0)
						toRemove.add(info);
				}
			}
		}
		
		for(LubricatedTileInfo info:toRemove){
			LubricatedHandler.lubricatedTiles.remove(info);
		}
	}
	
	@SubscribeEvent
	public void onEntityJoiningWorld(EntityJoinWorldEvent event){
		if(event.getEntity() instanceof PlayerEntity){
			if(event.getEntity() instanceof FakePlayer){
				return;
			}
			
			if(IPServerConfig.MISCELLANEOUS.autounlock_recipes.get()){
				List<IRecipe<?>> l = new ArrayList<IRecipe<?>>();
				Collection<IRecipe<?>> recipes = event.getWorld().getRecipeManager().getRecipes();
				recipes.forEach(recipe -> {
					ResourceLocation name = recipe.getId();
					if(name.getNamespace() == ImmersivePetroleum.MODID){
						if(recipe.getRecipeOutput().getItem() != null){
							l.add(recipe);
						}
					}
				});
				
				((PlayerEntity) event.getEntity()).unlockRecipes(l);
			}
		}
	}
	
	@SubscribeEvent
	public void test(LivingEvent.LivingUpdateEvent event){
		if(event.getEntityLiving() instanceof PlayerEntity){
			// event.getEntityLiving().setFire(1);
		}
	}
	
	public static Map<ResourceLocation, List<BlockPos>> napalmPositions = new HashMap<>();
	public static Map<ResourceLocation, List<BlockPos>> toRemove = new HashMap<>();
	
	@SubscribeEvent
	public void handleNapalm(WorldTickEvent event){
		ResourceLocation d = event.world.getDimensionKey().getRegistryName();
		
		if(event.phase == Phase.START){
			toRemove.put(d, new ArrayList<>());
			if(napalmPositions.get(d) != null){
				List<BlockPos> iterate = new ArrayList<>(napalmPositions.get(d));
				for(BlockPos position:iterate){
					BlockState state = event.world.getBlockState(position);
					if(state.getBlock() instanceof FlowingFluidBlock && state.getBlock() == IPContent.Fluids.napalm.block){
						((NapalmFluid) IPContent.Fluids.napalm).processFire(event.world, position);
					}
					toRemove.get(d).add(position);
				}
			}
		}else if(event.phase == Phase.END){
			if(toRemove.get(d) != null && napalmPositions.get(d) != null){
				for(BlockPos position:toRemove.get(d)){
					napalmPositions.get(d).remove(position);
				}
			}
		}
	}
}
