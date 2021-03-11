package flaxbeard.immersivepetroleum.common.util.compat.crafttweaker;

import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.tag.MCTag;

import flaxbeard.immersivepetroleum.api.crafting.FlarestackHandler;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.ITag;

@ZenRegister
@Name("mods.immersivepetroleum.Flarestack")
public class FlarestackRegistryTweaker{
	
	@SuppressWarnings("unchecked")
	@Method
	public static void register(MCTag<Fluid> tag, int amount){
		if(tag == null){
			CraftTweakerAPI.logError("§cFlarestackHandler: Expected fluidtag as input fluid!§r");
		}else if(amount <= 0){
			CraftTweakerAPI.logError("§cFlarestackHandler: Amount must atleast be 1mB!§r");
		}else{
			FlarestackHandler.register((ITag<Fluid>) tag.getInternal());
		}
	}
}
