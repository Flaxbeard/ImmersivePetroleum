package flaxbeard.immersivepetroleum.common.data;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.IPTags;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class IPBlockTags extends BlockTagsProvider{
	public IPBlockTags(DataGenerator dataGen, ExistingFileHelper exFileHelper){
		super(dataGen, ImmersivePetroleum.MODID, exFileHelper);
	}
	
	@Override
	protected void registerTags(){
		getOrCreateBuilder(IPTags.Blocks.asphalt)
			.add(IPContent.Blocks.asphalt);
	}
}
