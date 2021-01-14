package flaxbeard.immersivepetroleum.common.data;

import flaxbeard.immersivepetroleum.api.IPTags;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.FluidTagsProvider;

public class IPFluidTags extends FluidTagsProvider{
	public IPFluidTags(DataGenerator gen){
		super(gen);
	}
	
	@Override
	protected void registerTags(){
		getBuilder(IPTags.Fluids.diesel)
			.add(IPContent.Fluids.diesel);
		
		getBuilder(IPTags.Fluids.gasoline)
			.add(IPContent.Fluids.gasoline);
		
		getBuilder(IPTags.Fluids.lubricant)
			.add(IPContent.Fluids.lubricant);
		
		getBuilder(IPTags.Fluids.napalm)
			.add(IPContent.Fluids.napalm);
		
		getBuilder(IPTags.Fluids.crudeOil)
			.add(IPContent.Fluids.crudeOil);
	}
}
