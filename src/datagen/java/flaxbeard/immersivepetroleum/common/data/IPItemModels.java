package flaxbeard.immersivepetroleum.common.data;

import javax.annotation.Nullable;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.fluids.IPFluid;
import net.minecraft.data.DataGenerator;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder.Perspective;
import net.minecraftforge.client.model.generators.loaders.DynamicBucketModelBuilder;
import net.minecraftforge.client.model.generators.loaders.OBJLoaderBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class IPItemModels extends ItemModelProvider{
	public IPItemModels(DataGenerator gen, ExistingFileHelper exHelper){
		super(gen, ImmersivePetroleum.MODID, exHelper);
	}
	
	@Override
	public String getName(){
		return "Item Models";
	}
	
	@Override
	protected void registerModels(){
		String debugItem = name(IPContent.debugItem);
		
		getBuilder(debugItem)
			.parent(getExistingFile(mcLoc("item/generated")))
			.texture("layer0", modLoc("item/schematic"));
		
		genericItem(IPContent.Items.bitumen);
		genericItem(IPContent.Items.petcoke);
		genericItem(IPContent.Items.petcokedust);
		genericItem(IPContent.Items.oil_can);
		genericItem(IPContent.Items.speedboat);
		
		genericItem(IPContent.BoatUpgrades.ice_breaker);
		genericItem(IPContent.BoatUpgrades.reinforced_hull);
		genericItem(IPContent.BoatUpgrades.paddles);
		genericItem(IPContent.BoatUpgrades.rudders);
		genericItem(IPContent.BoatUpgrades.tank);
		
		pumpjackItem();
		distillationtowerItem();
		cokerunitItem();
		hydrotreaterItem();
		generatorItem();
		autolubeItem();
		flarestackItem();
		
		getBuilder(ImmersivePetroleum.MODID+":item/"+IPContent.Items.projector.getRegistryName().getPath())
			.parent(getExistingFile(modLoc("item/mb_projector")));
		
		for(IPFluid f:IPFluid.FLUIDS)
			createBucket(f);
	}
	
	private void hydrotreaterItem(){
		ItemModelBuilder model = obj(IPContent.Multiblock.hydrotreater, "multiblock/obj/hydrotreater.obj")
				.texture("texture", modLoc("multiblock/hydrotreater"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, Perspective.FIRSTPERSON_LEFT, new Vector3f(0, 0, 0), new Vector3f(0, 225, 0), 0.0625F);
		doTransform(trans, Perspective.FIRSTPERSON_RIGHT, new Vector3f(0, 0, 0), new Vector3f(0, 45, 0), 0.0625F);
		doTransform(trans, Perspective.THIRDPERSON_LEFT, new Vector3f(0, 2.5F, 0), new Vector3f(75, 225, 0), 0.0625F);
		doTransform(trans, Perspective.THIRDPERSON_RIGHT, new Vector3f(0, 2.5F, 0), new Vector3f(75, 45, 0), 0.0625F);
		doTransform(trans, Perspective.HEAD, new Vector3f(0, 8, 0), null, 0.25F);
		doTransform(trans, Perspective.GUI, new Vector3f(-1, -1, 0), new Vector3f(30, 225, 0), 0.15625F);
		doTransform(trans, Perspective.GROUND, new Vector3f(0, 0, 0), null, 0.125F);
		doTransform(trans, Perspective.FIXED, new Vector3f(0, -1, 0), null, 0.125F);
	}
	
	private void cokerunitItem(){
		ItemModelBuilder model = obj(IPContent.Multiblock.cokerunit, "multiblock/obj/cokerunit.obj")
				.texture("texture", modLoc("multiblock/cokerunit"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, Perspective.FIRSTPERSON_LEFT, new Vector3f(0, 0, 0), new Vector3f(0, 225, 0), 0.03125F);
		doTransform(trans, Perspective.FIRSTPERSON_RIGHT, new Vector3f(0, 0, 0), new Vector3f(0, 45, 0), 0.03125F);
		doTransform(trans, Perspective.THIRDPERSON_LEFT, new Vector3f(0, 2.5F, 0), new Vector3f(75, 225, 0), 0.03125F);
		doTransform(trans, Perspective.THIRDPERSON_RIGHT, new Vector3f(0, 2.5F, 0), new Vector3f(75, 45, 0), 0.03125F);
		doTransform(trans, Perspective.HEAD, new Vector3f(0, 12, 0), null, 0.125F);
		doTransform(trans, Perspective.GUI, new Vector3f(0, -4, 0), new Vector3f(30, 225, 0), 0.0625F);
		doTransform(trans, Perspective.GROUND, new Vector3f(0, -8, 0), null, 0.03125F);
		doTransform(trans, Perspective.FIXED, new Vector3f(0, -8, 0), null, 0.0625F);
	}
	
	private void flarestackItem(){
		ItemModelBuilder model = obj(IPContent.Blocks.flarestack, "block/obj/flarestack.obj")
				.texture("texture", modLoc("block/obj/flarestack"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, Perspective.FIRSTPERSON_LEFT, new Vector3f(0, 2, 0), new Vector3f(0, 45, 0), 0.25F);
		doTransform(trans, Perspective.FIRSTPERSON_RIGHT, new Vector3f(0, 2, 0), new Vector3f(0, 45, 0), 0.25F);
		doTransform(trans, Perspective.THIRDPERSON_LEFT, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 0.25F);
		doTransform(trans, Perspective.THIRDPERSON_RIGHT, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 0.25F);
		doTransform(trans, Perspective.HEAD, new Vector3f(0, 12, 0), null, 0.75F);
		doTransform(trans, Perspective.GUI, new Vector3f(0, -3, 0), new Vector3f(30, 225, 0), 0.4F);
		doTransform(trans, Perspective.GROUND, new Vector3f(0, 3, 0), null, 0.25F);
		doTransform(trans, Perspective.FIXED, new Vector3f(0, -4, 0), null, 0.5F);
	}
	
	private void generatorItem(){
		ItemModelBuilder model = obj(IPContent.Blocks.gas_generator, "block/obj/generator.obj")
				.texture("texture", modLoc("block/obj/generator"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, Perspective.FIRSTPERSON_LEFT, new Vector3f(0, 2.0f, 0), new Vector3f(0, 225, 0), 0.4F);
		doTransform(trans, Perspective.FIRSTPERSON_RIGHT, new Vector3f(0, 2.0f, 0), new Vector3f(0, 45, 0), 0.4F);
		doTransform(trans, Perspective.THIRDPERSON_LEFT, new Vector3f(0, 2.5f, 0), new Vector3f(75, 225, 0), 0.375F);
		doTransform(trans, Perspective.THIRDPERSON_RIGHT, new Vector3f(0, 2.5F, 0), new Vector3f(75, 45, 0), 0.375F);
		doTransform(trans, Perspective.HEAD, new Vector3f(0, 13, 0), null, 0.8F);
		doTransform(trans, Perspective.GUI, new Vector3f(0, 0, 0), new Vector3f(30, 225, 0), 0.625F);
		doTransform(trans, Perspective.GROUND, new Vector3f(0, 3, 0), null, 0.25F);
		doTransform(trans, Perspective.FIXED, new Vector3f(0, 0, 0), null, 0.5F);
	}
	
	private void autolubeItem(){
		ItemModelBuilder model = obj(IPContent.Blocks.auto_lubricator, "block/obj/autolubricator.obj")
			.texture("texture", modLoc("models/lubricator"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, Perspective.FIRSTPERSON_LEFT, new Vector3f(0, 2, 0), new Vector3f(0, 45, 0), 0.25F);
		doTransform(trans, Perspective.FIRSTPERSON_RIGHT, new Vector3f(0, 2, 0), new Vector3f(0, 45, 0), 0.25F);
		doTransform(trans, Perspective.THIRDPERSON_LEFT, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 0.25F);
		doTransform(trans, Perspective.THIRDPERSON_RIGHT, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 0.25F);
		doTransform(trans, Perspective.HEAD, new Vector3f(0, 12, 0), null, 0.75F);
		doTransform(trans, Perspective.GUI, new Vector3f(0, -3, 0), new Vector3f(30, 225, 0), 0.4F);
		doTransform(trans, Perspective.GROUND, new Vector3f(0, 3, 0), null, 0.25F);
		doTransform(trans, Perspective.FIXED, new Vector3f(0, -4, 0), null, 0.5F);
	}
	
	private void pumpjackItem(){
		ItemModelBuilder model = obj(IPContent.Multiblock.pumpjack, "item/obj/pumpjack_itemmockup.obj")
			.texture("texture_base", modLoc("multiblock/pumpjack_base"))
			.texture("texture_armature", modLoc("models/pumpjack_armature"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, Perspective.FIRSTPERSON_LEFT, new Vector3f(-1.75F, 2.5F, 1.25F), new Vector3f(0, 225, 0), 0.03125F);
		doTransform(trans, Perspective.FIRSTPERSON_RIGHT, new Vector3f(-1.75F, 2.5F, 1.75F), new Vector3f(0, 225, 0), 0.03125F);
		doTransform(trans, Perspective.THIRDPERSON_LEFT, new Vector3f(-0.75F, 0, -1.25F), new Vector3f(0, 90, 0), 0.03125F);
		doTransform(trans, Perspective.THIRDPERSON_RIGHT, new Vector3f(1.0F, 0, -1.75F), new Vector3f(0, 270, 0), 0.03125F);
		doTransform(trans, Perspective.HEAD, new Vector3f(0, 8, -8), null, 0.2F);
		doTransform(trans, Perspective.GUI, new Vector3f(6, -6, 0), new Vector3f(30, 225, 0), 0.1875F);
		doTransform(trans, Perspective.GROUND, new Vector3f(-1.5F, 3, -1.5F), null, 0.0625F);
		doTransform(trans, Perspective.FIXED, new Vector3f(-1, -8, -2), null, 0.0625F);
	}
	
	private void distillationtowerItem(){
		ItemModelBuilder model = obj(IPContent.Multiblock.distillationtower, "multiblock/obj/distillationtower.obj")
			.texture("texture", modLoc("multiblock/distillation_tower"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, Perspective.FIRSTPERSON_LEFT, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 0.03125F);
		doTransform(trans, Perspective.FIRSTPERSON_RIGHT, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 0.03125F);
		doTransform(trans, Perspective.THIRDPERSON_LEFT, new Vector3f(-0.75F, -5, -1.25F), new Vector3f(0, 90, 0), 0.03125F);
		doTransform(trans, Perspective.THIRDPERSON_RIGHT, new Vector3f(1.0F, -5, -1.75F), new Vector3f(0, 270, 0), 0.03125F);
		doTransform(trans, Perspective.HEAD, new Vector3f(1.5F, 8, 1.5F), null, 0.2F);
		doTransform(trans, Perspective.GUI, new Vector3f(-1, -6, 0), new Vector3f(30, 225, 0), 0.0625F);
		doTransform(trans, Perspective.GROUND, new Vector3f(1, 0, 1), null, 0.0625F);
		doTransform(trans, Perspective.FIXED, new Vector3f(0, -8, 0), null, 0.0625F);
	}
	
	private final Vector3f ZERO = new Vector3f(0, 0, 0);
	private void doTransform(ModelBuilder<?>.TransformsBuilder transform, Perspective type, Vector3f translation, @Nullable Vector3f rotationAngle, float scale){
		if(rotationAngle == null){
			rotationAngle = ZERO;
		}
		
		transform.transform(type)
				.translation(translation.getX(), translation.getY(), translation.getZ())
				.rotation(rotationAngle.getX(), rotationAngle.getY(), rotationAngle.getZ())
				.scale(scale)
				.end();
	}
	
	private ItemModelBuilder obj(IItemProvider item, String model){
		return getBuilder(item.asItem().getRegistryName().toString())
				.customLoader(OBJLoaderBuilder::begin)
				.modelLocation(modLoc("models/" + model)).flipV(true).end();
	}
	
	private void genericItem(Item item){
		if(item == null){
			StackTraceElement where = new NullPointerException().getStackTrace()[1];
			IPDataGenerator.log.warn("Skipping null item. ( {} -> {} )", where.getFileName(), where.getLineNumber());
			return;
		}
		String name = name(item);
		
		getBuilder(name)
			.parent(getExistingFile(mcLoc("item/generated")))
			.texture("layer0", modLoc("item/"+name));
	}
	
	private void createBucket(Fluid f){
		withExistingParent(f.getFilledBucket().asItem().getRegistryName().getPath(), forgeLoc("item/bucket"))
			.customLoader(DynamicBucketModelBuilder::begin)
			.fluid(f);
	}
	
	private String name(IItemProvider item){
		return item.asItem().getRegistryName().getPath();
	}
	
	protected ResourceLocation ieLoc(String str){
		return new ResourceLocation(ImmersiveEngineering.MODID, str);
	}
	
	protected ResourceLocation forgeLoc(String str){
		return new ResourceLocation("forge", str);
	}
}
