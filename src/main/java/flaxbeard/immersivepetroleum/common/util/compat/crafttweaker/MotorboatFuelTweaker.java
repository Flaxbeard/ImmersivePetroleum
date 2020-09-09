package flaxbeard.immersivepetroleum.common.util.compat.crafttweaker;

import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;

import flaxbeard.immersivepetroleum.api.energy.FuelHandler;
import net.minecraftforge.fluids.FluidStack;

@ZenRegister
@Name("mods.immersivepetroleum.Motorboat")
public class MotorboatFuelTweaker{
	
	@Method
	public static void register(String strFluidName){
		if(strFluidName == null || strFluidName.isEmpty()){
			CraftTweakerAPI.logError("§cFound null/empty fluid name in motorboat fuel entry§r");
		}else{
			FluidStack fstack = TweakerUtils.getFluidStack(strFluidName);
			if(fstack != null){
				FuelHandler.registerMotorboatFuel(fstack.getFluid(), fstack.getAmount());
			}else{
				CraftTweakerAPI.logError("§c\"%s\" is not a valid fluid§r", strFluidName);
			}
		}
	}
}
