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
		RenderTypeLookup.setRenderLayer(IPContent.Blocks.auto_lubricator, BlockRenderLayers::lubeLayer);
		RenderTypeLookup.setRenderLayer(IPContent.Blocks.gas_generator, BlockRenderLayers::solidCutout);
		RenderTypeLookup.setRenderLayer(IPContent.Blocks.flarestack, BlockRenderLayers::stackLayer);
		
		RenderTypeLookup.setRenderLayer(IPContent.Blocks.dummyConveyor, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(IPContent.Blocks.dummyOilOre, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(IPContent.Blocks.dummyPipe, RenderType.getCutout());
		
		RenderTypeLookup.setRenderLayer(IPContent.Multiblock.distillationtower, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(IPContent.Multiblock.pumpjack, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(IPContent.Multiblock.cokerunit, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(IPContent.Multiblock.hydrotreater, RenderType.getCutout());
		
		RenderTypeLookup.setRenderLayer(IPContent.Fluids.crudeOil, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(IPContent.Fluids.diesel, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(IPContent.Fluids.diesel_sulfur, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(IPContent.Fluids.gasoline, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(IPContent.Fluids.lubricant, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(IPContent.Fluids.napalm, RenderType.getTranslucent());
		
		RenderTypeLookup.setRenderLayer(IPContent.Fluids.crudeOil.getFlowingFluid(), RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(IPContent.Fluids.diesel.getFlowingFluid(), RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(IPContent.Fluids.diesel_sulfur.getFlowingFluid(), RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(IPContent.Fluids.gasoline.getFlowingFluid(), RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(IPContent.Fluids.lubricant.getFlowingFluid(), RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(IPContent.Fluids.napalm.getFlowingFluid(), RenderType.getTranslucent());
	}
	
	public static boolean lubeLayer(RenderType t){
		return t == RenderType.getTranslucent();
	}
	
	public static boolean stackLayer(RenderType t){
		return t == RenderType.getCutout();
	}
	
	public static boolean solidCutout(RenderType t){
		return t == RenderType.getSolid() || t == RenderType.getCutout();
	}
}
