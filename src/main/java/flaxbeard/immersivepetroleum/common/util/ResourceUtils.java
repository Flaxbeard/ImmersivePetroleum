package flaxbeard.immersivepetroleum.common.util;

import com.blamejared.crafttweaker.CraftTweaker;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.versions.forge.ForgeVersion;

public class ResourceUtils{
	public static final ResourceLocation ip(String str){
		return new ResourceLocation(ImmersivePetroleum.MODID, str);
	}
	
	public static ResourceLocation ct(String str){
		return new ResourceLocation(CraftTweaker.MODID, str);
	}
	
	public static ResourceLocation ie(String str){
		return new ResourceLocation(ImmersiveEngineering.MODID, str);
	}
	
	public static ResourceLocation forge(String str){
		return new ResourceLocation(ForgeVersion.MOD_ID, str);
	}
	
	public static final ResourceLocation mc(String str){
		return new ResourceLocation("minecraft", str);
	}
}
