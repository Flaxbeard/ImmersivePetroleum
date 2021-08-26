package flaxbeard.immersivepetroleum.common.util.compat.crafttweaker;

import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.tag.MCTagWithAmount;

import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.ITag;

@ZenRegister
@Name("mods.immersivepetroleum.Lubricant")
public class LubricantRegistryTweaker{
	
	@SuppressWarnings("unchecked")
	@Method
	public static void register(MCTagWithAmount<Fluid> tag){
		if(tag == null){
			CraftTweakerAPI.logError("§cLubricantRegistry: Expected fluidtag as input fluid!§r");
			return;
		}
		
		if(tag.getAmount() < 1){
			CraftTweakerAPI.logError("§cLubricantRegistry: Amount must atleast be 1mB!§r");
			return;
		}
		
		LubricantHandler.register((ITag<Fluid>) tag.getTag().getInternal(), tag.getAmount());
	}
}
