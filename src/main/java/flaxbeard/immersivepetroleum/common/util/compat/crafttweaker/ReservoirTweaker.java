package flaxbeard.immersivepetroleum.common.util.compat.crafttweaker;

import java.util.ArrayList;
import java.util.List;

import org.openzen.zencode.java.ZenCodeType.Constructor;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;

import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.ReservoirType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;

@ZenRegister
@Name("mods.immersivepetroleum.ReservoirRegistry")
public class ReservoirTweaker{
	
	@Method
	public static boolean register(ReservoirTypeWrapper wrapper){
		if(wrapper.isInvalid){
			return false;
		}
		
		PumpjackHandler.addReservoir(wrapper.internal.getId(), wrapper.internal);
		return true;
	}
	
	@Method
	public static boolean remove(String name){
		ResourceLocation loc = new ResourceLocation(name);
		
		if(!PumpjackHandler.reservoirs.containsKey(loc)){
			loc = TweakerUtils.ctLoc(name);
		}
		
		PumpjackHandler.reservoirs.remove(loc);
		
		return false;
	}
	
	
	@ZenRegister
	@Name("mods.immersivepetroleum.Reservoir")
	public static class ReservoirTypeWrapper{
		
		@Method
		public static ReservoirTypeWrapper create(String name, String strFluidLocation, int minSize, int maxSize, int traceAmount, int weight){
			return new ReservoirTypeWrapper(name, strFluidLocation, minSize, maxSize, traceAmount, weight);
		}
		
		protected ReservoirType internal;
		protected boolean isInvalid = false;
		
		@Constructor
		public ReservoirTypeWrapper(String name, String strFluidLocation, int minSize, int maxSize, int traceAmount, int weight){
			this.isInvalid = false;
			if(name.isEmpty()){
				CraftTweakerAPI.logError("§cReservoir name can not be empty string!§r");
				this.isInvalid = true;
			}
			if(strFluidLocation.isEmpty()){
				CraftTweakerAPI.logError("§cReservoir fluidLocation can not be empty string!§r");
				this.isInvalid = true;
			}
			if(minSize <= 0){
				CraftTweakerAPI.logError("§cReservoir minSize has to be at least 1mb!§r");
				this.isInvalid = true;
			}
			if(maxSize < minSize){
				CraftTweakerAPI.logError("§cReservoir maxSize can not be smaller than minSize!§r");
				this.isInvalid = true;
			}
			if(weight <= 1){
				CraftTweakerAPI.logError("§cReservoir weight has to be greater than or equal to 1!§r");
				this.isInvalid = true;
			}
			
			ResourceLocation id = TweakerUtils.ctLoc(name);
			ResourceLocation fluidLocation = new ResourceLocation(strFluidLocation);
			
			this.internal = new ReservoirType(name, id, fluidLocation, minSize, maxSize, traceAmount, weight);
		}
		
		@Method
		public boolean addDimension(boolean blacklist, String[] names){
			List<ResourceLocation> list = new ArrayList<>();
			for(int i = 0;i < names.length;i++){
				try{
					list.add(new ResourceLocation(names[i]));
				}catch(ResourceLocationException e){
					CraftTweakerAPI.logError("§caddDimension: %s§r", e.getMessage());
				}
			}
			
			return this.internal.addDimension(blacklist, list);
		}
		
		@Method
		public boolean addBiome(boolean blacklist, String[] names){
			List<ResourceLocation> list = new ArrayList<>();
			for(int i = 0;i < names.length;i++){
				try{
					list.add(new ResourceLocation(names[i]));
				}catch(ResourceLocationException e){
					CraftTweakerAPI.logError("§caddBiome: %s§r", e.getMessage());
				}
			}
			
			return this.internal.addBiome(blacklist, list);
		}
	}
}
