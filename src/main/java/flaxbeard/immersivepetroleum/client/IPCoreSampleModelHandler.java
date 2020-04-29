package flaxbeard.immersivepetroleum.client;

import blusunrize.immersiveengineering.common.IEContent;
import flaxbeard.immersivepetroleum.client.model.ModelCoresampleExtended;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class IPCoreSampleModelHandler
{
	public static IPCoreSampleModelHandler instance = new IPCoreSampleModelHandler();

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onModelBakeEvent(ModelBakeEvent event)
	{

		ModelResourceLocation mLoc = new ModelResourceLocation(new ResourceLocation("immersiveengineering", IEContent.itemCoresample.itemName), "inventory");
		event.getModelRegistry().putObject(mLoc, new ModelCoresampleExtended());

	}
}
