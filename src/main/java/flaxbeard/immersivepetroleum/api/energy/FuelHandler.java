package flaxbeard.immersivepetroleum.api.energy;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.cfg.ConfigUtils;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent;

public class FuelHandler{
	protected static final Logger log = LogManager.getLogger(ImmersivePetroleum.MODID + "/FuelHandler");
	
	static final Map<ResourceLocation, Values> portablegen = new HashMap<>();
	
	static final Map<ResourceLocation, Integer> motorboatAmountTick = new HashMap<>();
	
	public static void registerPortableGeneratorFuel(Fluid fuel, int fluxPerTick, int mbPerTick){
		if(fuel != null){
			registerPortableGeneratorFuel(fuel.getRegistryName(), mbPerTick, fluxPerTick);
		}
	}
	
	public static void registerMotorboatFuel(Fluid fuel, int mbPerTick){
		if(fuel != null){
			registerMotorboatFuel(fuel.getRegistryName(), mbPerTick);
		}
	}
	
	public static void registerPortableGeneratorFuel(ResourceLocation fuelRL, int fluxPerTick, int mbPerTick){
		if(fuelRL != null && !fuelRL.toString().isEmpty()){
			portablegen.put(fuelRL, new Values(fluxPerTick, mbPerTick));
			
			log.info("Added {} as Portable Generator Fuel. ({}RF/t {}mB/t)", fuelRL, fluxPerTick, mbPerTick);
		}
	}
	
	public static void registerMotorboatFuel(ResourceLocation fuelRL, int mbPerTick){
		if(fuelRL != null && !fuelRL.toString().isEmpty()){
			motorboatAmountTick.put(fuelRL, mbPerTick);
			
			log.info("Added {} as Motorboat Fuel. ({} mB/t)", fuelRL, mbPerTick);
		}
	}
	
	public static boolean isValidBoatFuel(Fluid fuel){
		return fuel != null ? motorboatAmountTick.containsKey(fuel.getRegistryName()) : false;
	}
	
	public static boolean isValidFuel(Fluid fuel){
		return fuel != null ? portablegen.containsKey(fuel.getRegistryName()) : false;
	}
	
	public static int getBoatFuelUsedPerTick(Fluid fuel){
		if(!isValidBoatFuel(fuel))
			return 0;
		
		return motorboatAmountTick.get(fuel.getRegistryName());
	}
	
	public static int getFuelUsedPerTick(Fluid fuel){
		if(!isValidFuel(fuel))
			return 0;
		
		return portablegen.get(fuel.getRegistryName()).mbPerTick;
	}
	
	public static int getFluxGeneratedPerTick(Fluid fuel){
		if(!isValidFuel(fuel))
			return 0;
		
		return portablegen.get(fuel.getRegistryName()).fluxPerTick;
	}
	
	public static void onConfigReload(ModConfigEvent ev){
		if(ev.getConfig().getSpec() != IPServerConfig.ALL){
			return;
		}
		
		portablegen.clear();
		motorboatAmountTick.clear();
		
		ConfigUtils.addFuel(IPServerConfig.GENERATION.fuels.get());
		ConfigUtils.addBoatFuel(IPServerConfig.MISCELLANEOUS.boat_fuels.get());
	}
	
	private static class Values{
		final int fluxPerTick;
		final int mbPerTick;
		Values(int fluxPerTick, int mbPerTick){
			this.fluxPerTick = fluxPerTick;
			this.mbPerTick = mbPerTick;
		}
	}
}
