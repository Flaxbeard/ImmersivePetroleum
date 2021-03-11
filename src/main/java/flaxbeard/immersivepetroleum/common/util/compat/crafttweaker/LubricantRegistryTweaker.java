package flaxbeard.immersivepetroleum.common.util.compat.crafttweaker;

import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.impl.tag.MCTag;

import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.ITag;

@ZenRegister
@Name("mods.immersivepetroleum.Lubricant")
public class LubricantRegistryTweaker{
	
	@Method
	public static void register(IFluidStack fluid){
		CraftTweakerAPI.logError("§ccLubricantRegistry: Please use the Tag based version please.§r");
	}
	
	@SuppressWarnings("unchecked")
	@Method
	public static void register(MCTag<Fluid> tag, int amount){
		if(tag == null){
			CraftTweakerAPI.logError("§cLubricantRegistry: Expected fluidtag as input fluid!§r");
		}else if(amount <= 0){
			CraftTweakerAPI.logError("§cLubricantRegistry: Amount must atleast be 1mB!§r");
		}else{
			LubricantHandler.register((ITag<Fluid>) tag.getInternal(), amount);
		}
	}
}
