package flaxbeard.immersivepetroleum.common.cfg;

import java.util.List;
import java.util.Locale;

import flaxbeard.immersivepetroleum.api.energy.FuelHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class ConfigUtils{
	
	public static void addFuel(List<? extends String> list){
		for(int i = 0;i < list.size();i++){
			String str = list.get(i);
			
			if(str.isEmpty())
				continue;
			
			String fluid = null;
			int amount = 0;
			int production = 0;
			
			String remain = str;
			
			int index = 0;
			
			while(remain.indexOf(",") != -1){
				int endPos = remain.indexOf(",");
				
				String current = remain.substring(0, endPos).trim();
				
				if(index == 0)
					fluid = current;
				else if(index == 1){
					try{
						amount = Integer.parseInt(current);
						if(amount <= 0){
							throw new RuntimeException("Negative value for fuel mB/tick for generator fuel " + (i + 1));
						}
					}catch(NumberFormatException e){
						throw new RuntimeException("Invalid value for fuel mB/tick for generator fuel " + (i + 1));
					}
				}
				
				remain = remain.substring(endPos + 1);
				index++;
			}
			String current = remain.trim();
			
			try{
				production = Integer.parseInt(current);
				if(production < 0){
					throw new RuntimeException("Negative value for fuel RF/tick for generator fuel " + (i + 1));
				}
			}catch(NumberFormatException e){
				throw new RuntimeException("Invalid value for fuel RF/tick for generator fuel " + (i + 1));
			}
			
			fluid = fluid.toLowerCase(Locale.ENGLISH);
			
			ResourceLocation fluidRL = new ResourceLocation(fluid);
			if(!ForgeRegistries.FLUIDS.containsKey(fluidRL)){
				throw new RuntimeException("\"" + fluid + "\" did not resolve into a valid fluid. (" + fluidRL + ")");
			}
			
			FuelHandler.registerPortableGeneratorFuel(fluidRL, production, amount);
		}
	}
	
	public static void addBoatFuel(List<? extends String> list){
		for(int i = 0;i < list.size();i++){
			String str = list.get(i);
			
			if(str.isEmpty())
				continue;
			
			String fluid = null;
			int amount = 0;
			
			String remain = str;
			int index = 0;
			while(remain.indexOf(",") != -1){
				int endPos = remain.indexOf(",");
				
				String current = remain.substring(0, endPos).trim();
				
				if(index == 0)
					fluid = current;
				
				remain = remain.substring(endPos + 1);
				index++;
			}
			String current = remain.trim();
			
			fluid = fluid.toLowerCase(Locale.ENGLISH);
			
			try{
				amount = Integer.parseInt(current);
				if(amount <= 0){
					throw new RuntimeException("Negative value for fuel mB/tick for boat fuel " + (i + 1));
				}
			}catch(NumberFormatException e){
				throw new RuntimeException("Invalid value for fuel mB/tick for boat fuel " + (i + 1));
			}
			
			ResourceLocation fluidRL = new ResourceLocation(fluid);
			if(!ForgeRegistries.FLUIDS.containsKey(fluidRL)){
				throw new RuntimeException("\"" + fluid + "\" did not resolve into a valid fluid. (" + fluidRL + ")");
			}
			
			FuelHandler.registerMotorboatFuel(fluidRL, amount);
		}
	}
}