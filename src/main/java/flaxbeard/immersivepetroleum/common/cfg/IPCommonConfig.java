package flaxbeard.immersivepetroleum.common.cfg;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent;

@EventBusSubscriber(modid = ImmersivePetroleum.MODID, bus = Bus.MOD)
public class IPCommonConfig{
	public static final ForgeConfigSpec ALL;
	
	static{
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		
		ALL = builder.build();
	}
	
	@SubscribeEvent
	public static void onCommonReload(ModConfigEvent ev){
		
	}
}
