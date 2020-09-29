package flaxbeard.immersivepetroleum.client.model;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@SuppressWarnings("deprecation")
@Mod.EventBusSubscriber(modid = ImmersivePetroleum.MODID, value = Dist.CLIENT, bus = Bus.MOD)
public class IPModels{
	public static final Map<String, Lazy<? extends IPModel>> MODELS = new HashMap<>();
	
	@SubscribeEvent
	public static void clientsetup(FMLClientSetupEvent event){
		addModel(ModelPumpjack::new);
	}
	
	public static void addModel(Supplier<? extends IPModel> constructor){
		Lazy<? extends IPModel> lazy = Lazy.of(constructor);
		String id = lazy.get().id();
		if(MODELS.containsKey(id)){
			throw new RuntimeException(String.format("Duplicate model, \"%s\" already used by %s", id, MODELS.get(id).getClass().toString()));
		}
		MODELS.put(id, lazy);
	}
	
	public static IPModel getModel(String id){
		if(MODELS.containsKey(id)){
			return MODELS.get(id).get();
		}
		
		throw new RuntimeException("Model " + id + " does not exist.");
	}
	
	@SubscribeEvent
	public static void pre(TextureStitchEvent.Pre event){
		if(event.getMap().getTextureLocation() == AtlasTexture.LOCATION_BLOCKS_TEXTURE){
			MODELS.values().forEach(lazy -> {
				IPModel model = lazy.get();
				event.addSprite(model.textureLocation());
			});
		}
	}
	
	@SubscribeEvent
	public static void post(TextureStitchEvent.Post event){
		if(event.getMap().getTextureLocation() == AtlasTexture.LOCATION_BLOCKS_TEXTURE){
			MODELS.values().forEach(lazy -> {
				IPModel model = lazy.get();
				model.spriteInit(event.getMap());
				model.init();
			});
		}
	}
}
