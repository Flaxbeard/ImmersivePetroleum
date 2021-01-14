package flaxbeard.immersivepetroleum.client;

import blusunrize.immersiveengineering.common.blocks.IEBlocks.StoneDecoration;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class IPCoreSampleModelHandler{
	public static IPCoreSampleModelHandler instance = new IPCoreSampleModelHandler();
	
	@SuppressWarnings("unused")
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onModelBakeEvent(ModelBakeEvent event){
		
		ModelResourceLocation mLoc = new ModelResourceLocation(StoneDecoration.coresample.getRegistryName(), "inventory");
		// event.getModelRegistry().put(mLoc, new ModelCoresampleExtended());
	}
}
