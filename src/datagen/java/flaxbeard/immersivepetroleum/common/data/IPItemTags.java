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
		IPTags.forAllBlocktags(this::copy);
		
		getOrCreateBuilder(IPTags.Items.bitumen).addItemEntry(IPContent.Items.bitumen);
		getOrCreateBuilder(IPTags.Items.petcoke).addItemEntry(IPContent.Items.petcoke);
		getOrCreateBuilder(IPTags.Items.petcokeDust).addItemEntry(IPContent.Items.petcokedust);
		getOrCreateBuilder(IPTags.Items.petcokeStorage).addItemEntry(IPContent.Blocks.petcoke.asItem());
	}
}
