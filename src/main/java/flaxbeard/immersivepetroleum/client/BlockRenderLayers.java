package flaxbeard.immersivepetroleum.client;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = ImmersivePetroleum.MODID, value = Dist.CLIENT, bus = Bus.MOD)
public class BlockRenderLayers{
	
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event){
		RenderTypeLookup.setRenderLayer(IPContent.Blocks.auto_lubricator, t->t==RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(IPContent.Blocks.gas_generator, BlockRenderLayers::gasLayer);
		RenderTypeLookup.setRenderLayer(IPContent.Blocks.dummyConveyor, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(IPContent.Blocks.dummyOilOre, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(IPContent.Blocks.dummyPipe, RenderType.getCutout());
		
		RenderTypeLookup.setRenderLayer(IPContent.Multiblock.distillationtower, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(IPContent.Multiblock.pumpjack, RenderType.getCutout());
	}
	
	public static boolean lubeLayer(RenderType t){
		return t==RenderType.getTranslucent();
	}
	
	public static boolean gasLayer(RenderType t){
		return t==RenderType.getSolid() || t==RenderType.getCutout();
	}
}
