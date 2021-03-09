package flaxbeard.immersivepetroleum.common.data;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.IPTags;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class IPItemTags extends ItemTagsProvider{
	public IPItemTags(DataGenerator dataGen, BlockTagsProvider blockTags, ExistingFileHelper exFileHelper){
		super(dataGen, blockTags, ImmersivePetroleum.MODID, exFileHelper);
	}
	
	@Override
	protected void registerTags(){
		getOrCreateBuilder(IPTags.Items.bitumen)
			.add(IPContent.Items.bitumen);
	}
}
