package flaxbeard.immersivepetroleum.common.util;

import net.minecraftforge.fluids.FluidStack;

public class FluidHelper{
	
	/** No pressure variant of {@link #copyFluid(FluidStack, int, boolean)} */
	public static FluidStack copyFluid(FluidStack fluid, int amount){
		FluidStack fs = new FluidStack(fluid.getFluid(), amount);
		return fs;
	}
	
	public static FluidStack copyFluid(FluidStack fluid, int amount, boolean pressurize){
		FluidStack fs = new FluidStack(fluid.getFluid(), amount);
		if(pressurize && amount > 50){
			fs.getOrCreateTag().putBoolean("pressurized", true);
		}
		return fs;
	}
}
