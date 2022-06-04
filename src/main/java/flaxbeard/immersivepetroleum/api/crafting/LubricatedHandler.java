package flaxbeard.immersivepetroleum.api.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;

import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.AutoLubricatorTileEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

public class LubricatedHandler{
	public interface ILubricationHandler<E extends TileEntity> {
		Tuple<BlockPos, Direction> getGhostBlockPosition(World world, E mbte);
		
		Vector3i getStructureDimensions();
		
		boolean isMachineEnabled(World world, E mbte);
		
		TileEntity isPlacedCorrectly(World world, AutoLubricatorTileEntity lubricator, Direction direction);
		
		/**
		 * @deprecated Use {@link #lubricateClient(ClientWorld, Fluid, int, TileEntity)} and {@link #lubricateServer(ServerWorld, Fluid, int, TileEntity)} instead.<br>
		 * Will be removed in 1.18.2!
		 */
		void lubricate(World world, int ticks, E mbte);
		
		/**
		 * @deprecated Use {@link #lubricateClient(ClientWorld, Fluid, int, TileEntity)} and {@link #lubricateServer(ServerWorld, Fluid, int, TileEntity)} instead.<br>
		 * Will be removed in 1.18.2!
		 */
		void lubricate(World world, Fluid lubricant, int ticks, E mbte);
		
		void lubricateClient(ClientWorld world, Fluid lubricant, int ticks, E mbte);
		
		void lubricateServer(ServerWorld world, Fluid lubricant, int ticks, E mbte);
		
		@OnlyIn(Dist.CLIENT)
		void renderPipes(AutoLubricatorTileEntity lubricator, E mbte, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay);
		
		void spawnLubricantParticles(ClientWorld world, AutoLubricatorTileEntity lubricator, Direction direction, E mbte);
	}
	
	static final Map<Class<? extends TileEntity>, ILubricationHandler<? extends TileEntity>> lubricationHandlers = new HashMap<>();
	
	public static <T extends TileEntity> void registerLubricatedTile(Class<T> tileClass, Supplier<ILubricationHandler<T>> handler){
		ILubricationHandler<T> instance = handler.get();
		lubricationHandlers.put(tileClass, instance);
	}
	
	public static <T extends TileEntity> ILubricationHandler<T> getHandlerForTile(T te){
		if(te != null){
			Class<? extends TileEntity> teClass = te.getClass();
			if(lubricationHandlers.containsKey(teClass)){
				@SuppressWarnings("unchecked")
				ILubricationHandler<T> tmp = (ILubricationHandler<T>) lubricationHandlers.get(teClass);
				return tmp;
			}
		}
		return null;
	}
	
	public static class LubricatedTileInfo{
		public RegistryKey<World> world;
		public BlockPos pos;
		public Fluid lubricant = Fluids.EMPTY;
		public int ticks;
		
		public LubricatedTileInfo(RegistryKey<World> registryKey, BlockPos pos, Fluid lubricant, int ticks){
			this.world = registryKey;
			this.pos = pos;
			this.ticks = ticks;
			
			if(lubricant != null && lubricant != Fluids.EMPTY){
				this.lubricant = lubricant;
			}
		}
		
		public LubricatedTileInfo(CompoundNBT tag){
			int ticks = tag.getInt("ticks");
			int x = tag.getInt("x");
			int y = tag.getInt("y");
			int z = tag.getInt("z");
			String worldName = tag.getString("world");
			String lubricantName = tag.getString("lubricant");
			
			this.world = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(worldName));
			this.pos = new BlockPos(x, y, z);
			this.ticks = ticks;
			
			this.lubricant = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(lubricantName));
			if(this.lubricant == null){
				this.lubricant = Fluids.EMPTY;
			}
		}
		
		public CompoundNBT writeToNBT(){
			CompoundNBT tag = new CompoundNBT();
			
			tag.putInt("ticks", this.ticks);
			tag.putInt("x", this.pos.getX());
			tag.putInt("y", this.pos.getY());
			tag.putInt("z", this.pos.getZ());
			tag.putString("world", this.world.getRegistryName().toString());
			tag.putString("lubricant", this.lubricant.getRegistryName().toString());
			
			return tag;
		}
	}
	
	public static List<LubricatedTileInfo> lubricatedTiles = new ArrayList<LubricatedTileInfo>();
	
	public static boolean lubricateTile(TileEntity tile, Fluid lubricant, int ticks){
		return lubricateTile(tile, lubricant, ticks, false, -1);
	}
	
	public static boolean lubricateTile(TileEntity tile, Fluid lubricant, int ticks, boolean additive, int cap){
		if(tile instanceof MultiblockPartTileEntity && ((MultiblockPartTileEntity<?>) tile).offsetToMaster != BlockPos.ZERO){
			tile = ((MultiblockPartTileEntity<?>) tile).master();
		}
		
		if(getHandlerForTile(tile) != null){
			BlockPos pos = tile.getPos();
			
			RegistryKey<World> key = tile.getWorld().getDimensionKey();
			for(LubricatedTileInfo info:lubricatedTiles){
				if(info.pos.equals(pos) && info.world == key){
					if(info.ticks >= ticks){
						if(additive){
							if(cap == -1){
								info.ticks += ticks;
							}else{
								info.ticks = Math.min(cap, info.ticks + ticks);
							}
							return true;
						}else{
							return false;
						}
					}
					
					info.ticks = ticks;
					return true;
				}
			}
			
			LubricatedTileInfo lti = new LubricatedTileInfo(tile.getWorld().getDimensionKey(), tile.getPos(), lubricant, ticks);
			lubricatedTiles.add(lti);
			
			return true;
		}
		
		return false;
	}
	
	public static class LubricantEffect extends ChemthrowerEffect{
		@Override
		public void applyToEntity(LivingEntity target, PlayerEntity shooter, ItemStack thrower, Fluid fluid){
			if(target instanceof IronGolemEntity){
				if(LubricantHandler.isValidLube(fluid)){
					int ticks = (Math.max(1, IEServerConfig.TOOLS.chemthrower_consumption.get() / LubricantHandler.getLubeAmount(fluid)) * 4) / 3;
					
					EffectInstance activeSpeed = target.getActivePotionEffect(Effects.SPEED);
					int ticksSpeed = ticks;
					if(activeSpeed != null && activeSpeed.getAmplifier() <= 1){
						ticksSpeed = Math.min(activeSpeed.getDuration() + ticks, 1200); // 1 Minute
					}
					
					EffectInstance activeStrength = target.getActivePotionEffect(Effects.STRENGTH);
					int ticksStrength = ticks;
					if(activeStrength != null && activeStrength.getAmplifier() <= 1){
						ticksStrength = Math.min(activeStrength.getDuration() + ticks, 1200); // 1 Minute
					}
					
					target.addPotionEffect(new EffectInstance(Effects.SPEED, ticksSpeed, 1));
					target.addPotionEffect(new EffectInstance(Effects.STRENGTH, ticksStrength, 1));
				}
			}
			
		}
		
		@Override
		public void applyToBlock(World world, RayTraceResult mop, PlayerEntity shooter, ItemStack thrower, Fluid fluid){
			if(LubricantHandler.isValidLube(fluid)){
				int ticks = (Math.max(1, IEServerConfig.TOOLS.chemthrower_consumption.get() / LubricantHandler.getLubeAmount(fluid)) * 2) / 3;
				LubricatedHandler.lubricateTile(world.getTileEntity(new BlockPos(mop.getHitVec())), fluid, ticks, true, 1200); // 1 Minute
			}
		}
	}
}
