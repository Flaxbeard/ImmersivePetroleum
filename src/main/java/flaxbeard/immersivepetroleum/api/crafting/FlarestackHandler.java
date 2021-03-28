package flaxbeard.immersivepetroleum.api.crafting;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.fluid.Fluid;
import net.minecraft.tags.ITag;
import net.minecraftforge.fluids.FluidStack;

/**
 * Simple handler for the Flarestack
 * @author TwistedGate
 */
public class FlarestackHandler{
	// Can contain both Tags and Direct Fluids
	static final Set<ITag<Fluid>> burnables = new HashSet<>();
	
	/**
	 * Registers a fluidtag as being burnable in the Flarestack
	 * 
	 * @param fluidTag that should be burned
	 */
	public static void register(@Nonnull ITag<Fluid> fluidTag){
		if(fluidTag != null && !burnables.contains(fluidTag)){
			burnables.add(fluidTag);
		}
	}
	
	/**
	 * Tests wether the given fluid is burnable by the flarestack or not
	 * 
	 * @param fluid to test
	 * @return true if the given fluid is infact burnable, false otherwise
	 */
	public static boolean isBurnable(@Nonnull Fluid fluid){
		return fluid != null && burnables.stream().anyMatch(tag -> tag.contains(fluid));
	}
	
	/**
	 * Tests wether the given fluidstack is burnable by the flarestack or not
	 * 
	 * @param fluidstack to test
	 * @return true if the given fluid is infact burnable, false otherwise
	 */
	public static boolean isBurnable(@Nonnull FluidStack fluidstack){
		return !fluidstack.isEmpty() && isBurnable(fluidstack.getFluid());
	}
	
	public static Set<ITag<Fluid>> getSet(){
		return Collections.unmodifiableSet(burnables);
	}
}
