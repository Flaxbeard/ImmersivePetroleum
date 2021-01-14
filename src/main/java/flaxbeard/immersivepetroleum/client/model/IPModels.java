package flaxbeard.immersivepetroleum.client.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.client.model.ModelLubricantPipes.Crusher;
import flaxbeard.immersivepetroleum.client.model.ModelLubricantPipes.Excavator;
import flaxbeard.immersivepetroleum.client.model.ModelLubricantPipes.Pumpjack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

/** A central place for all of ImmersivePetroleums Models that arent OBJ's */
@Mod.EventBusSubscriber(modid = ImmersivePetroleum.MODID, value = Dist.CLIENT, bus = Bus.MOD)
public class IPModels{
	@SubscribeEvent
	public static void init(FMLConstructModEvent event){
		add(ModelPumpjack.ID, new ModelPumpjack());
		
		add(Crusher.ID, new Crusher());
		
		add(Excavator.ID_NORMAL, new Excavator(false));
		add(Excavator.ID_MIRRORED, new Excavator(true));
		
		add(Pumpjack.ID_NORMAL, new Pumpjack(false));
		add(Pumpjack.ID_MIRRORED, new Pumpjack(true));
	}
	
	private static final Map<String, IPModel> MODELS = new HashMap<>();
	
	/**
	 * @param id The String-ID of the Model.
	 * @param constructor The model constructor
	 */
	public static void add(String id, IPModel model){
		if(MODELS.containsKey(id)){
			ImmersivePetroleum.log.error("Duplicate ID, \"{}\" already used by {}. Skipping.", id, MODELS.get(id).getClass());
		}else{
			model.init();
			MODELS.put(id, model);
		}
	}
	
	/**
	 * @param id The String-ID of the Model.
	 * @return The Model assigned to <code>id</code> or <code>null</code>
	 */
	public static Supplier<IPModel> getSupplier(String id){
		return () -> MODELS.get(id);
	}
	
	/**
	 * @return An unmodifiable collection of all added Models
	 */
	public static Collection<IPModel> getModels(){
		return Collections.unmodifiableCollection(MODELS.values());
	}
}
