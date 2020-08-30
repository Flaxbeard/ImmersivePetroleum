package flaxbeard.immersivepetroleum.api.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.metal.AutoLubricatorTileEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LubricatedHandler{
	public interface ILubricationHandler<E extends TileEntity> {
		TileEntity isPlacedCorrectly(World world, AutoLubricatorTileEntity lubricator, Direction direction);
		
		boolean isMachineEnabled(World world, E mbte);
		
		void lubricate(World world, int ticks, E mbte);
		
		void spawnLubricantParticles(World world, AutoLubricatorTileEntity lubricator, Direction direction, E mbte);
		
		@OnlyIn(Dist.CLIENT)
		void renderPipes(World world, AutoLubricatorTileEntity lubricator, Direction direction, E mbte);
		
		Tuple<BlockPos, Direction> getGhostBlockPosition(World world, E mbte);
		
		Vec3i getStructureDimensions();
	}
	
	static final Map<Class<? extends TileEntity>, ILubricationHandler<?>> lubricationHandlers = new HashMap<>();
	
	public static void registerLubricatedTile(Class<? extends TileEntity> tileClass, ILubricationHandler<?> handler){
		lubricationHandlers.put(tileClass, handler);
	}
	
	public static ILubricationHandler<?> getHandlerForTile(TileEntity tile){
		if(tile != null){
			Class<? extends TileEntity> teClass = tile.getClass();
			if(lubricationHandlers.containsKey(teClass))
				return lubricationHandlers.get(teClass);
		}
		return null;
	}
	
	public static class LubricatedTileInfo{
		public BlockPos pos;
		// TODO Use a more general way? (ie Combined use of ModDimension and DimensionType)
		public DimensionType world;
		public int ticks;
		
		public LubricatedTileInfo(Dimension dimension, BlockPos pos, int ticks){
			this.world = dimension.getType();
			this.pos = pos;
			this.ticks = ticks;
		}
		
		public LubricatedTileInfo(CompoundNBT tag){
			int ticks = tag.getInt("ticks");
			int x = tag.getInt("x");
			int y = tag.getInt("y");
			int z = tag.getInt("z");
			String name = tag.getString("world");
			
			this.world = DimensionType.byName(new ResourceLocation(name));
			this.pos = new BlockPos(x, y, z);
			this.ticks = ticks;
		}
		
		public CompoundNBT writeToNBT(){
			CompoundNBT tag = new CompoundNBT();
			tag.putInt("ticks", ticks);
			tag.putInt("x", pos.getX());
			tag.putInt("y", pos.getY());
			tag.putInt("z", pos.getZ());
			tag.putString("world", world.getRegistryName().toString());
			
			return tag;
		}
	}
	
	public static List<LubricatedTileInfo> lubricatedTiles = new ArrayList<LubricatedTileInfo>();
	
	public static boolean lubricateTile(TileEntity tile, int ticks){
		return lubricateTile(tile, ticks, false, -1);
	}
	
	public static boolean lubricateTile(TileEntity tile, int ticks, boolean additive, int cap){
		if(tile instanceof MultiblockPartTileEntity){
			tile = ((MultiblockPartTileEntity<?>) tile).master();
		}
		if(getHandlerForTile(tile) != null){
			BlockPos pos = tile.getPos();
			for(int i = 0;i < lubricatedTiles.size();i++){
				LubricatedTileInfo info = lubricatedTiles.get(i);
				if(info.pos.equals(pos) && info.world == tile.getWorld().getDimension().getType()){
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
			LubricatedTileInfo lti = new LubricatedTileInfo(tile.getWorld().getDimension(), tile.getPos(), ticks);
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
					int amount = (Math.max(1, IEConfig.TOOLS.chemthrower_consumption.get() / LubricantHandler.getLubeAmount(fluid)) * 4) / 3;
					
					EffectInstance activeSpeed = target.getActivePotionEffect(Effects.SPEED);
					int ticksSpeed = amount;
					if(activeSpeed != null && activeSpeed.getAmplifier() <= 1){
						ticksSpeed = Math.min(activeSpeed.getDuration() + amount, 60 * 20);
					}
					
					EffectInstance activeStrength = target.getActivePotionEffect(Effects.STRENGTH);
					int ticksStrength = amount;
					if(activeStrength != null && activeStrength.getAmplifier() <= 1){
						ticksStrength = Math.min(activeStrength.getDuration() + amount, 60 * 20);
					}
					
					target.addPotionEffect(new EffectInstance(Effects.SPEED, ticksSpeed, 1));
					target.addPotionEffect(new EffectInstance(Effects.STRENGTH, ticksStrength, 1));
				}
			}
			
		}
		
		@Override
		public void applyToBlock(World world, RayTraceResult mop, PlayerEntity shooter, ItemStack thrower, Fluid fluid){
			if(LubricantHandler.isValidLube(fluid)){
				int amount = (Math.max(1, IEConfig.TOOLS.chemthrower_consumption.get() / LubricantHandler.getLubeAmount(fluid)) * 2) / 3;
				LubricatedHandler.lubricateTile(world.getTileEntity(new BlockPos(mop.getHitVec())), amount, true, 20 * 60);
			}
		}
	}
}
