package flaxbeard.immersivepetroleum.common.util.compat.crafttweaker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.util.ResourceLocation;

public class TweakerUtils{
	public static final Logger log = LogManager.getLogger(ImmersivePetroleum.MODID+"/CT-Compat");
	
	public static ResourceLocation ctLoc(String name){
		return new ResourceLocation("crafttweaker", name);
	}
	
	public static ResourceLocation ipLoc(String name){
		return new ResourceLocation(ImmersivePetroleum.MODID, name);
	}
	
	public static ResourceLocation mcLoc(String name){
		return new ResourceLocation("minecraft", name);
	}
}
