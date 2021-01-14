package flaxbeard.immersivepetroleum.common.data;

import flaxbeard.immersivepetroleum.api.IPTags;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;

public class IPItemTags extends ItemTagsProvider{
	public IPItemTags(DataGenerator generatorIn){
		super(generatorIn);
	}
	
	@Override
	protected void registerTags(){
		getBuilder(IPTags.Items.bitumen)
			.add(IPContent.Items.bitumen);
	}
}
