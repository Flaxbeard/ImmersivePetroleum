package flaxbeard.immersivepetroleum.common.cfg;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent;

@EventBusSubscriber(modid = ImmersivePetroleum.MODID, bus = Bus.MOD)
public class IPClientConfig{
	public static final Miscellaneous MISCELLANEOUS;
	
	public static final ForgeConfigSpec ALL;
	
	static{
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		MISCELLANEOUS = new Miscellaneous(builder);
		ALL = builder.build();
	}
	
	public static class Miscellaneous{
		public final BooleanValue sample_displayBorder;
		Miscellaneous(ForgeConfigSpec.Builder builder){
			builder.push("Miscellaneous");
			
			sample_displayBorder = builder
					.comment("Unused for now!", "Display chunk border while holding Core Samples", "Default: true")
					.define("sample_displayBorder", true);
			
			builder.pop();
		}
	}
	
	@SubscribeEvent
	public static void onConfigChange(ModConfigEvent ev){
		
	}
}
