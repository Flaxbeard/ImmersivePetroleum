package flaxbeard.immersivepetroleum.common.util.compat.crafttweaker;

import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;

import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import net.minecraftforge.fluids.FluidStack;

@ZenRegister
@Name("mods.immersivepetroleum.Lubricant")
public class LubricantRegistryTweaker{
	
	@Method
	public static void register(IFluidStack fluid){
		boolean isValid = true;
		if(fluid == null){
			CraftTweakerAPI.logError("§cLubricant fluid can not be null!§r");
			isValid = false;
		}
		
		if(isValid){
			FluidStack fstack = fluid.getInternal();
			LubricantHandler.registerLubricant(fstack.getFluid(), fstack.getAmount());
		}
	}
}
