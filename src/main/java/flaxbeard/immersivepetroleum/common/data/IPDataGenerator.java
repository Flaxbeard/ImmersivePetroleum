package flaxbeard.immersivepetroleum.common.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@EventBusSubscriber(modid=ImmersivePetroleum.MODID, bus=Bus.MOD)
public class IPDataGenerator{
	public static final Logger log=LogManager.getLogger(ImmersivePetroleum.MODID+"/DataGenerator");
	
	@SubscribeEvent
	public static void generate(GatherDataEvent event){
		if(event.includeServer()){
			DataGenerator generator=event.getGenerator();
			
			IPBlockTags blockTags=new IPBlockTags(generator);
			
			generator.addProvider(new IPBlockTags(generator));
			generator.addProvider(new IPItemTags(generator, blockTags));
			generator.addProvider(new IPFluidTags(generator));
			generator.addProvider(new IPRecipes(generator));
			
			IPLoadedModels loadedModels=new IPLoadedModels(generator, event.getExistingFileHelper());
			IPBlockStates blockstates=new IPBlockStates(generator, event.getExistingFileHelper(), loadedModels);
			
			generator.addProvider(blockstates);
			generator.addProvider(loadedModels);
			generator.addProvider(new IPItemModels(generator, event.getExistingFileHelper(), blockstates));
		}
	}
}
