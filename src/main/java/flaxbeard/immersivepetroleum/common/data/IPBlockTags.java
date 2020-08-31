package flaxbeard.immersivepetroleum.common.data;

import flaxbeard.immersivepetroleum.api.IPTags;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;

public class IPBlockTags extends BlockTagsProvider{
	public IPBlockTags(DataGenerator generatorIn){
		super(generatorIn);
	}
	
	@Override
	protected void registerTags(){
		getBuilder(IPTags.Blocks.asphalt)
			.add(IPContent.Blocks.asphalt);
	}
}
