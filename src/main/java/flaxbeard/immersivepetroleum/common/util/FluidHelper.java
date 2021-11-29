package flaxbeard.immersivepetroleum.common.util;

import net.minecraftforge.fluids.FluidStack;

public class FluidHelper{
	public static FluidStack copyFluid(FluidStack fluid, int amount){
		FluidStack fs = new FluidStack(fluid.getFluid(), amount);
		return fs;
	}
}
