package flaxbeard.immersivepetroleum.common.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.data.models.LoadedModelBuilder;
import blusunrize.immersiveengineering.common.data.models.LoadedModelProvider;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.util.fluids.IPFluid;
import net.minecraft.data.DataGenerator;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;

public class IPItemModels extends LoadedModelProvider{
	IPBlockStates blockStates;
	public IPItemModels(DataGenerator gen, ExistingFileHelper exHelper, IPBlockStates blockstates){
		super(gen, ImmersivePetroleum.MODID, "item", exHelper);
		this.blockStates=blockstates;
	}
	
	@Override
	public String getName(){
		return "Item Models";
	}
	
	@Override
	protected void registerModels(){
		genericItem(IPContent.itemBitumen);
		genericItem(IPContent.itemOilCan);
		genericItem(IPContent.itemSpeedboat);
		genericItem(IPContent.itemUpgradeBreaker);
		genericItem(IPContent.itemUpgradeHull);
		genericItem(IPContent.itemUpgradePaddles);
		genericItem(IPContent.itemUpgradeRudders);
		genericItem(IPContent.itemUpgradeTank);
		
		getBuilder(IPContent.itemProjector)
			.parent(getExistingFile(modLoc("item/schematic")));
		
		getBuilder(IPContent.blockGasGenerator)
			.parent(getExistingFile(modLoc("block/generator")));
		
		cubeAll("asphalt", modLoc("block/asphalt"));
		
		for(IPFluid f:IPFluid.LIST)
			createBucket(f);
	}
	
	private void genericItem(Item item){
		if(item==null){
			StackTraceElement where=new NullPointerException().getStackTrace()[1];
			IPDataGenerator.log.warn("Skipping null item. ( {} -> {} )", where.getFileName(), where.getLineNumber());
			return;
		}
		String name=name(item);
		
		getBuilder(name)
			.parent(getExistingFile(mcLoc("item/generated")))
			.texture("layer0", modLoc("item/"+name));
	}
	
	private void createBucket(Fluid f){
		getBuilder(f.getFilledBucket())
			.loader(forgeLoc("bucket"))
			.additional("fluid", f.getRegistryName())
			.parent(new UncheckedModelFile(forgeLoc("item/bucket")));
	}
	
	private LoadedModelBuilder getBuilder(IItemProvider item){
		return getBuilder(name(item));
	}
	
	private String name(IItemProvider item){
		return item.asItem().getRegistryName().getPath();
	}
	
	private String name(Item item){
		return item.getRegistryName().getPath();
	}
	
	protected ResourceLocation ieLoc(String str){
		return new ResourceLocation(ImmersiveEngineering.MODID, str);
	}
	
	protected ResourceLocation forgeLoc(String str){
		return new ResourceLocation("forge", str);
	}
}
