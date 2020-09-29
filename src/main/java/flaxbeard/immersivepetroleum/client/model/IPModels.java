package flaxbeard.immersivepetroleum.client.model;

import java.util.ArrayList;
import java.util.List;
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
@Mod.EventBusSubscriber(modid = ImmersivePetroleum.MODID, value = Dist.CLIENT, bus=Bus.MOD)
public class IPModels{
	public static final List<Lazy<? extends IPModel>> MODELS=new ArrayList<>();
	
	@SubscribeEvent
	public static void clientsetup(FMLClientSetupEvent event){
		addModel(ModelPumpjack::new);
	}
	
	public static void addModel(Supplier<? extends IPModel> constructor){
		MODELS.add(Lazy.of(constructor));
	}
	
	@SubscribeEvent
	public static void pre(TextureStitchEvent.Pre event){
		if(event.getMap().getTextureLocation()==AtlasTexture.LOCATION_BLOCKS_TEXTURE){
			MODELS.forEach(lazy->{
				IPModel model=lazy.get();
				event.addSprite(model.textureLocation());
			});
		}
	}
	
	@SubscribeEvent
	public static void post(TextureStitchEvent.Post event){
		if(event.getMap().getTextureLocation()==AtlasTexture.LOCATION_BLOCKS_TEXTURE){
			MODELS.forEach(lazy->{
				IPModel model=lazy.get();
				model.spriteInit(event.getMap());
				model.init();
				model.postInit();
			});
		}
	}
	
	/*
	@SubscribeEvent
	public static void pre(TextureStitchEvent.Pre event){
		if(event.getMap().getTextureLocation()==AtlasTexture.LOCATION_BLOCKS_TEXTURE){
			event.addSprite(arm_texture);
		}
	}
	
	@SubscribeEvent
	public static void post(TextureStitchEvent.Post event){
		if(event.getMap().getTextureLocation()==AtlasTexture.LOCATION_BLOCKS_TEXTURE){
			TextureAtlasSprite sprite=event.getMap().getSprite(arm_texture);
			event.getMap();
			MultiblockPumpjackRenderer.model=new ModelPumpjack(sprite);
		}
	}
	*/
}