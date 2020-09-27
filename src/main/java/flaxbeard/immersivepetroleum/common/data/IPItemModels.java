package flaxbeard.immersivepetroleum.common.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.data.models.LoadedModelBuilder;
import blusunrize.immersiveengineering.common.data.models.LoadedModelProvider;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.util.fluids.IPFluid;
import net.minecraft.data.DataGenerator;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.model.generators.ModelBuilder.Perspective;
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

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
		String debugItem=name(IPContent.debugItem);
		
		getBuilder(debugItem)
			.parent(getExistingFile(mcLoc("item/generated")))
			.texture("layer0", modLoc("item/schematic"));
		
		genericItem(IPContent.Items.bitumen);
		genericItem(IPContent.Items.oil_can);
		genericItem(IPContent.Items.speedboat);
		
		genericItem(IPContent.BoatUpgrades.ice_breaker);
		genericItem(IPContent.BoatUpgrades.reinforced_hull);
		genericItem(IPContent.BoatUpgrades.paddles);
		genericItem(IPContent.BoatUpgrades.rudders);
		genericItem(IPContent.BoatUpgrades.tank);
		
		pumpjackItem();
		distillationtowerItem();
		generatorItem();
		autolubeItem();
		
		getBuilder(IPContent.Items.projector)
			.parent(getExistingFile(modLoc("item/mb_projector")));
		
		for(IPFluid f:IPFluid.LIST)
			createBucket(f);
	}
	
	private void generatorItem(){
		LoadedModelBuilder model=obj(IPContent.Blocks.gas_generator, "block/obj/generator.obj")
				.texture("texture", modLoc("block/obj/generator"));
		
		model.transformationMap()
		.setTransformations(Perspective.FIRSTPERSON_LEFT,
				createMatrix(new Vector3d(0, 2.0, 0), new Vector3d(0, 225, 0), 0.4))
		
		.setTransformations(Perspective.FIRSTPERSON_RIGHT,
				createMatrix(new Vector3d(0, 2.0, 0), new Vector3d(0, 45, 0), 0.4))
		
		.setTransformations(Perspective.THIRDPERSON_LEFT,
				createMatrix(new Vector3d(0, 2.5, 0), new Vector3d(75, 225, 0), 0.375))
		
		.setTransformations(Perspective.THIRDPERSON_RIGHT,
				createMatrix(new Vector3d(0, 2.5, 0), new Vector3d(75, 45, 0), 0.375))
		
		.setTransformations(Perspective.HEAD,
				createMatrix(new Vector3d(0, 13, 0), null, 0.8))
		
		.setTransformations(Perspective.GUI,
				createMatrix(new Vector3d(0, 0, 0), new Vector3d(30, 225, 0), 0.625))
		
		.setTransformations(Perspective.GROUND,
				createMatrix(new Vector3d(0, 3, 0), null, 0.25))
		
		.setTransformations(Perspective.FIXED,
				createMatrix(new Vector3d(0, 0, 0), null, 0.5))
		;
	}
	
	private void autolubeItem(){
		LoadedModelBuilder model=obj(IPContent.Blocks.auto_lubricator, "block/obj/autolubricator.obj")
			.texture("texture", modLoc("models/lubricator"));
		
		model.transformationMap()
			.setTransformations(Perspective.FIRSTPERSON_LEFT,
					createMatrix(new Vector3d(0, 2, 0), new Vector3d(0, 45, 0), 0.25))
			
			.setTransformations(Perspective.FIRSTPERSON_RIGHT,
					createMatrix(new Vector3d(0, 2, 0), new Vector3d(0, 45, 0), 0.25))
			
			.setTransformations(Perspective.THIRDPERSON_LEFT,
					createMatrix(new Vector3d(0, 0, 0), new Vector3d(0, 0, 0), 0.25))
			
			.setTransformations(Perspective.THIRDPERSON_RIGHT,
					createMatrix(new Vector3d(0, 0, 0), new Vector3d(0, 0, 0), 0.25))
			
			.setTransformations(Perspective.HEAD,
					createMatrix(new Vector3d(0, 12, 0), null, 0.75))
			
			.setTransformations(Perspective.GUI,
					createMatrix(new Vector3d(0, -3, 0), new Vector3d(30, 225, 0), 0.4))
			
			.setTransformations(Perspective.GROUND,
					createMatrix(new Vector3d(0, 3, 0), null, 0.25))
			
			.setTransformations(Perspective.FIXED,
					createMatrix(new Vector3d(0, -4, 0), null, 0.5))
			;
	}
	
	private void pumpjackItem(){
		LoadedModelBuilder model=obj(IPContent.Multiblock.pumpjack, "item/obj/pumpjack_itemmockup.obj")
			.texture("texture_base", modLoc("multiblock/pumpjack_base"))
			.texture("texture_armature", modLoc("models/pumpjack_armature"))
			;
		
		model.transformationMap()
			.setTransformations(Perspective.FIRSTPERSON_LEFT,
					createMatrix(new Vector3d(-1.75, 2.5, 1.25), new Vector3d(0, 225, 0), 0.03125))
			
			.setTransformations(Perspective.FIRSTPERSON_RIGHT,
					createMatrix(new Vector3d(-1.75, 2.5, 1.75), new Vector3d(0, 225, 0), 0.03125))
			
			.setTransformations(Perspective.THIRDPERSON_LEFT,
					createMatrix(new Vector3d(-0.75, 0, -1.25), new Vector3d(0, 90, 0), 0.03125))
			
			.setTransformations(Perspective.THIRDPERSON_RIGHT,
					createMatrix(new Vector3d(1.0, 0, -1.75), new Vector3d(0, 270, 0), 0.03125))
			
			.setTransformations(Perspective.HEAD,
					createMatrix(new Vector3d(0, 8, -8), null, 0.2))
			
			.setTransformations(Perspective.GUI,
					createMatrix(new Vector3d(6, -6, 0), new Vector3d(30, 225, 0), 0.1875))
			
			.setTransformations(Perspective.GROUND,
					createMatrix(new Vector3d(-1.5, 3, -1.5), null, 0.0625))
			
			.setTransformations(Perspective.FIXED,
					createMatrix(new Vector3d(-1, -8, -2), null, 0.0625))
			;
	}
	
	private void distillationtowerItem(){
		LoadedModelBuilder model=obj(IPContent.Multiblock.distillationtower, "multiblock/obj/distillationtower.obj")
			.texture("texture", modLoc("multiblock/distillation_tower"));
		
		model.transformationMap()
			.setTransformations(Perspective.FIRSTPERSON_LEFT,
					createMatrix(new Vector3d(-1.75, 2.5, 1.25), new Vector3d(0, 225, 0), 0.03125))
			
			.setTransformations(Perspective.FIRSTPERSON_RIGHT,
					createMatrix(new Vector3d(-1.75, 2.5, 1.75), new Vector3d(0, 225, 0), 0.03125))
			
			.setTransformations(Perspective.THIRDPERSON_LEFT,
					createMatrix(new Vector3d(-0.75, -5, -1.25), new Vector3d(0, 90, 0), 0.03125))
			
			.setTransformations(Perspective.THIRDPERSON_RIGHT,
					createMatrix(new Vector3d(1.0, -5, -1.75), new Vector3d(0, 270, 0), 0.03125))
			
			.setTransformations(Perspective.HEAD,
					createMatrix(new Vector3d(-4.75, 8, -4.75), null, 0.2))
			
			.setTransformations(Perspective.GUI,
					createMatrix(new Vector3d(3, -6, 0), new Vector3d(30, 225, 0), 0.0625))
			
			.setTransformations(Perspective.GROUND,
					createMatrix(new Vector3d(-1.5, 3, -1.5), null, 0.0625))
			
			.setTransformations(Perspective.FIXED,
					createMatrix(new Vector3d(-1, -8, -2), null, 0.0625))
			;
	}
	
	/**
	 * Short-hand for easy matrix creation.
	 * 
	 * @param translation Location of the model origin. (x/16, y/16, z/16)
	 * @param rotationAngle Rotation in Degrees. (XYZ Euler)
	 * @param scale Size of the model.
	 * @return
	 */
	private Matrix4 createMatrix(Vector3d translation, Vector3d rotationAngle, double scale){
		Matrix4 mat=new Matrix4().setIdentity();
		mat.translate(translation.x/16D, translation.y/16D, translation.z/16D);
		
		if(rotationAngle!=null){
			if(rotationAngle.x!=0.0)
				mat.rotate(Math.toRadians(rotationAngle.x), 1, 0, 0);
			
			if(rotationAngle.y!=0.0)
				mat.rotate(Math.toRadians(rotationAngle.y), 0, 1, 0);
			
			if(rotationAngle.z!=0.0)
				mat.rotate(Math.toRadians(rotationAngle.z), 0, 0, 1);
		}
		
		mat.scale(scale, scale, scale);
		return mat;
	}
	
	private LoadedModelBuilder obj(IItemProvider item, String model){
		return getBuilder(item)
			.loader(forgeLoc("obj"))
			.additional("model", modLoc("models/"+model))
			.additional("flip-v", true);
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
	
	protected ResourceLocation ieLoc(String str){
		return new ResourceLocation(ImmersiveEngineering.MODID, str);
	}
	
	protected ResourceLocation forgeLoc(String str){
		return new ResourceLocation("forge", str);
	}
}
