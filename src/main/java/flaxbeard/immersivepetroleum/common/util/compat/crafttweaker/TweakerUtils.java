package flaxbeard.immersivepetroleum.common.util.compat.crafttweaker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class TweakerUtils{
	public static final Logger log = LogManager.getLogger(ImmersivePetroleum.MODID+"/CT-Compat");
	
	/**
	 * Attempts to get a {@link FluidStack} from a string. Expects: "fluidRL, amountInMilliBuckets"
	 * 
	 * @param str the string to be parsed
	 * @return the parsed {@link FluidStack}. May return null.
	 * 
	 * @throws ResourceLocationException If the {@link ResourceLocation} could not be parsed.
	 * @throws NumberFormatException If the amount provided in the string could not be parsed.
	 * @throws IllegalArgumentException If something is missing.
	 */
	public static FluidStack getFluidStack(String str){
		String[] split=split(str);
		
		ResourceLocation rl=new ResourceLocation(split[0]);
		if(ForgeRegistries.FLUIDS.containsKey(rl)){
			Fluid fluid=ForgeRegistries.FLUIDS.getValue(rl);
			
			int amount=Integer.valueOf(split[1]);
			
			log.debug("getFluidStack(): {} {}", fluid, amount);
			
			return new FluidStack(fluid, amount);
		}
		
		return null;
	}
	
	/**
	 * Attempts to get a {@link FluidTagInput} from a string. Expects: "fluidTagRL, amountInMilliBuckets"
	 * 
	 * @param str the string to be parsed
	 * @return the parsed {@link FluidTagInput}
	 * 
	 * @throws ResourceLocationException If the {@link ResourceLocation} could not be parsed.
	 * @throws NumberFormatException If the amount provided in the string could not be parsed.
	 */
	public static FluidTagInput getFluidTagInput(String str){
		String[] split=split(str);
		
		ResourceLocation rl=new ResourceLocation(split[0]);
		int amount=Integer.valueOf(split[1]);
		
		log.debug("getFluidTagInput(): {} {}", rl, amount);
		
		if(!rl.getNamespace().equals("forge")){
			log.warn("Tags must start with \"forge:\". Got \"{}\" instead.", rl.getNamespace());
		}
		
		return new FluidTagInput(rl, amount);
	}
	
	private static String[] split(String str){
		if(str==null) throw new IllegalArgumentException("String must not be null.");
		if(str.isEmpty()) throw new IllegalArgumentException("String must not be empty.");
		
		// Split the string in two with "," as a spacer, taking any amount of empty spaces into consideration.
		// " {0,}" simply means to include empty spaces as part of the seperator, if there are any.
		String[] split=null;
		if(str.contains(",")){
			split=str.split(", {0,}");
			if(split.length==1){
				split=new String[]{split[0], "1000"};
				log.warn("\"{}\" has seperator, but does not have an amount. Defaulting to 1000mB!");
			}
		}
		
		if(split==null){
			split=new String[]{str, "1000"};
			log.info("\"{}\" has no amount. Defaulting to 1000mB.");
		}
		
		return split;
	}
}
