package flaxbeard.immersivepetroleum.common.data;

import java.util.HashMap;
import java.util.Map;

import blusunrize.immersiveengineering.common.data.models.LoadedModelBuilder;
import blusunrize.immersiveengineering.common.data.models.LoadedModelProvider;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

public class IPLoadedModels extends LoadedModelProvider{
	final Map<ResourceLocation, LoadedModelBuilder> models = new HashMap<>();
	public IPLoadedModels(DataGenerator gen, ExistingFileHelper exHelper){
		super(gen, ImmersivePetroleum.MODID, "block", exHelper);
	}
	
	@Override
	protected void registerModels(){
		super.generatedModels.putAll(models);
	}
	
	public void backupModels(){
		models.putAll(super.generatedModels);
	}
	
	@Override
	public String getName(){
		return "Loaded Models";
	}
}
