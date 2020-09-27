package flaxbeard.immersivepetroleum.common.data;

import flaxbeard.immersivepetroleum.api.IPTags;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;

public class IPItemTags extends ItemTagsProvider{
	
	@SuppressWarnings("deprecation")
	public IPItemTags(DataGenerator generatorIn, BlockTagsProvider exhelper){
		super(generatorIn, exhelper);
	}
	
	@Override
	protected void registerTags(){
		getOrCreateBuilder(IPTags.Items.bitumen)
			.add(IPContent.Items.bitumen);
	}
}
