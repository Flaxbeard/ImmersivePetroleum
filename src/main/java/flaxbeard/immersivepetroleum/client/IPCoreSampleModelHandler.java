package flaxbeard.immersivepetroleum.client;

import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import flaxbeard.immersivepetroleum.client.model.ModelCoresampleExtended;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class IPCoreSampleModelHandler{
	public static IPCoreSampleModelHandler instance = new IPCoreSampleModelHandler();
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onModelBakeEvent(ModelBakeEvent event){
		
		ModelResourceLocation mLoc = new ModelResourceLocation(IEBlocks.MetalDevices.sampleDrill.getRegistryName(), "inventory");
		//event.getModelRegistry().put(mLoc, new ModelCoresampleExtended());
		
	}
}
