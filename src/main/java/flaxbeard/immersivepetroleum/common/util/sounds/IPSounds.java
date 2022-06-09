package flaxbeard.immersivepetroleum.common.util.sounds;

import java.util.HashSet;
import java.util.Set;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = ImmersivePetroleum.MODID, bus = Bus.MOD)
public class IPSounds{
	static Set<SoundEvent> soundEvents = new HashSet<>();
	
	public final static SoundEvent FLARESTACK = register("flarestack_fire");
	public final static SoundEvent PROJECTOR = register("projector");
	
	static SoundEvent register(String name){
		ResourceLocation rl = ResourceUtils.ip(name);
		SoundEvent event = new SoundEvent(rl);
		soundEvents.add(event.setRegistryName(rl));
		return event;
	}
	
	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event){
		ImmersivePetroleum.log.info("Loading sounds.");
		for(SoundEvent sound:soundEvents){
			event.getRegistry().register(sound);
		}
		soundEvents.clear();
	}
}
