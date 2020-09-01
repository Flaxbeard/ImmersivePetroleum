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
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.OilWorldInfo;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.ReservoirType;
import flaxbeard.immersivepetroleum.common.IPContent.Fluids;
import flaxbeard.immersivepetroleum.common.blocks.BlockNapalm;
import flaxbeard.immersivepetroleum.common.entity.SpeedboatEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommonEventHandler{
	
	@SubscribeEvent
	public static void onSave(WorldEvent.Save event){
		IPSaveData.setDirty();
	}
	
	@SubscribeEvent
	public static void onUnload(WorldEvent.Unload event){
		IPSaveData.setDirty();
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void handlePickupItem(RightClickBlock event){
		BlockPos pos = event.getPos();
		BlockState state = event.getWorld().getBlockState(pos);
		if(!event.getWorld().isRemote && state.getBlock() == IEBlocks.MetalDevices.sampleDrill){
			TileEntity te = event.getWorld().getTileEntity(pos);
			if(te instanceof SampleDrillTileEntity){
				SampleDrillTileEntity drill = (SampleDrillTileEntity) te;
				if(drill.isDummy()){
					drill = (SampleDrillTileEntity)drill.master();
				}
				
				if(!drill.sample.isEmpty()){
					ColumnPos cPos=CoresampleItem.getCoords(drill.sample);
					if(cPos!=null){
						try{
							World world = event.getWorld();
							DimensionChunkCoords coords=new DimensionChunkCoords(world.getDimension().getType(), cPos.x >> 4, cPos.z >> 4);
							
							OilWorldInfo info = PumpjackHandler.getOilWorldInfo(world, coords.x, coords.z);
							if(info.getType() != null){
								ItemNBTHelper.putString(drill.sample, "resType", info.getType().name);
								ItemNBTHelper.putInt(drill.sample, "oil", info.current);
								
								if(event.getPlayer()!=null){
									int cap=info.capacity;
									int cur=info.current;
									ReservoirType type=info.type;
									ReservoirType override=info.overrideType;
									
									String out=String.format("%.3f/%.3fmB of %s%s", cur/1000D, cap/1000D, (override!=null?override.name:type.name), (override!=null?" [OVERRIDDEN]":""));
									event.getPlayer().sendStatusMessage(new StringTextComponent(out), true);
								}
								
							}else{
								ItemNBTHelper.putInt(drill.sample, "oil", 0);
								
								if(event.getPlayer()!=null)
									event.getPlayer().sendStatusMessage(new StringTextComponent("Found nothing."), true);
							}
						}catch(Exception e){
							ImmersivePetroleum.log.warn(e);
						}
					}
				}
			}
		}
	}
	
//	@SubscribeEvent(priority = EventPriority.HIGH)
//	public static void onLogin(PlayerLoggedInEvent event){
		// ExcavatorHandler.allowPacketsToPlayer = true;
//		if(!event.getPlayer().world.isRemote){
//			HashMap<ReservoirType, Integer> packetMap = new HashMap<ReservoirType, Integer>();
//			for(Entry<ReservoirType, Integer> e:PumpjackHandler.reservoirList.entrySet()){
//				if(e.getKey() != null && e.getValue() != null)
//					packetMap.put(e.getKey(), e.getValue());
//			}
//			
//			//IPPacketHandler.sendToPlayer(event.getPlayer(), new MessageReservoirListSync(packetMap));
//		}
//	}
	
	@SubscribeEvent
	public static void handleBoatImmunity(LivingAttackEvent event){
		if(event.getSource() == DamageSource.LAVA || event.getSource() == DamageSource.ON_FIRE || event.getSource() == DamageSource.IN_FIRE){
			LivingEntity entity = event.getEntityLiving();
			if(entity.getRidingEntity() instanceof SpeedboatEntity){
				SpeedboatEntity boat = (SpeedboatEntity) entity.getRidingEntity();
				if(boat.isFireproof){
					event.setCanceled(true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void handleBoatImmunity(PlayerTickEvent event){
		PlayerEntity entity = event.player;
		if(entity.isBurning() && entity.getRidingEntity() instanceof SpeedboatEntity){
			SpeedboatEntity boat = (SpeedboatEntity) entity.getRidingEntity();
			if(boat.isFireproof){
				entity.extinguish();
				DataParameter<Byte> FLAGS = SpeedboatEntity.getFlags();
				byte b0 = ((Byte) entity.getDataManager().get(FLAGS)).byteValue();
				
				entity.getDataManager().set(FLAGS, Byte.valueOf((byte) (b0 & ~(1 << 0))));
			}
		}
	}
	
	@SubscribeEvent
	public static void handleLubricatingMachinesServer(WorldTickEvent event){
		if(event.phase == Phase.END){
			handleLubricatingMachines(event.world);
		}
	}
	
	static final Random random=new Random();
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void handleLubricatingMachines(World world){
		Set<LubricatedTileInfo> toRemove = new HashSet<LubricatedTileInfo>();
		for(LubricatedTileInfo info:LubricatedHandler.lubricatedTiles){
			if(info.world == world.getDimension().getType() && world.isAreaLoaded(info.pos, 0)){
				TileEntity te = world.getTileEntity(info.pos);
				ILubricationHandler lubeHandler = LubricatedHandler.getHandlerForTile(te);
				if(lubeHandler != null){
					if(lubeHandler.isMachineEnabled(world, te)){
						lubeHandler.lubricate(world, info.ticks, te);
					}
					
					if(world.isRemote){
						if(te instanceof MultiblockPartTileEntity){
							MultiblockPartTileEntity<?> part = (MultiblockPartTileEntity<?>) te;
							BlockState n = Fluids.lubricant.block.getDefaultState();
							Vec3i size=lubeHandler.getStructureDimensions();
							
							int numBlocks = (int)(size.getX()*size.getY()*size.getZ()*0.25F);
							
							for(int i = 0;i < numBlocks;i++){
								BlockPos pos = part.getBlockPosForPos(new BlockPos(size.getX()*random.nextFloat(), size.getY()*random.nextFloat(), size.getZ()*random.nextFloat()));
								if(world.getBlockState(pos)==Blocks.AIR.getDefaultState())
									continue;
								
								TileEntity te2 = world.getTileEntity(info.pos);
								if(te2 != null && te2 instanceof MultiblockPartTileEntity){
									if(((MultiblockPartTileEntity<?>) te2).master() == part.master()){
										for(Direction facing:Direction.Plane.HORIZONTAL){
											if(world.rand.nextInt(30) == 0){// && world.getBlockState(pos.offset(facing)).getBlock().isReplaceable(world, pos.offset(facing))){
												Vec3i direction = facing.getDirectionVec();
												world.addParticle(new BlockParticleData(ParticleTypes.FALLING_DUST, n),
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
					if(info.ticks == 0) toRemove.add(info);
				}
			}
		}
		
		for(LubricatedTileInfo info:toRemove){
			LubricatedHandler.lubricatedTiles.remove(info);
		}
	}
	
	@SubscribeEvent
	public static void onEntityJoiningWorld(EntityJoinWorldEvent event){
		if(IPConfig.MISCELLANEOUS.autounlock_recipes.get() && event.getEntity() instanceof PlayerEntity){
			if(event.getEntity() instanceof FakePlayer){
				return;
			}
			
			List<IRecipe<?>> l = new ArrayList<IRecipe<?>>();
			Collection<IRecipe<?>> recipes=event.getWorld().getRecipeManager().getRecipes();
			recipes.forEach(recipe->{
				ResourceLocation name = recipe.getId();
				if(name.getNamespace()==ImmersivePetroleum.MODID){
					if(recipe.getRecipeOutput().getItem() != null){
						l.add(recipe);
					}
				}
			});
			
			((PlayerEntity) event.getEntity()).unlockRecipes(l);
			
		}
	}
	
	@SubscribeEvent
	public static void test(LivingEvent.LivingUpdateEvent event){
		if(event.getEntityLiving() instanceof PlayerEntity){
			// event.getEntityLiving().setFire(1);
		}
	}
	
	public static Map<Integer, List<BlockPos>> napalmPositions = new HashMap<>();
	public static Map<Integer, List<BlockPos>> toRemove = new HashMap<>();
	
	@SubscribeEvent
	public static void handleNapalm(WorldTickEvent event){
		int d = event.world.getDimension().getType().getId();
		
		if(event.phase == Phase.START){
			toRemove.put(d, new ArrayList<>());
			if(napalmPositions.get(d) != null){
				List<BlockPos> iterate = new ArrayList<>(napalmPositions.get(d));
				for(BlockPos position:iterate){
					BlockState state = event.world.getBlockState(position);
					if(state.getBlock() instanceof FlowingFluidBlock && state.getBlock() == Fluids.napalm.block){
						((BlockNapalm) Fluids.napalm).processFire(event.world, position);
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
