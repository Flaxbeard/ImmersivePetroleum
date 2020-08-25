package flaxbeard.immersivepetroleum.common.data;

import java.util.Arrays;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.models.connection.ConnectionLoader;
import blusunrize.immersiveengineering.common.data.models.LoadedModelBuilder;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.AutoLubricatorBlock;
import flaxbeard.immersivepetroleum.common.blocks.GasGeneratorBlock;
import flaxbeard.immersivepetroleum.common.util.fluids.IPFluid;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelFile.ExistingModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate;

public class IPBlockStates extends BlockStateProvider{
	/** ResourceLocation("forge","obj") */
	private static final ResourceLocation FORGE_LOADER=new ResourceLocation("forge","obj");
	
	final IPLoadedModels loadedModels;
	final ExistingFileHelper exFileHelper;
	public IPBlockStates(DataGenerator gen, ExistingFileHelper exFileHelper, IPLoadedModels loadedModels){
		super(gen, ImmersivePetroleum.MODID, exFileHelper);
		this.loadedModels=loadedModels;
		this.exFileHelper=exFileHelper;
	}
	
	@Override
	protected void registerStatesAndModels(){
		// Dummy Oil Ore
		ModelFile dummyOilOreModel=cubeAll(IPContent.Blocks.dummyBlockOilOre);
		getVariantBuilder(IPContent.Blocks.dummyBlockOilOre).partialState()
			.setModels(new ConfiguredModel(dummyOilOreModel));
		itemModelWithParent(IPContent.Blocks.dummyBlockOilOre, dummyOilOreModel);
		
		// Dummy Pipe
		ModelFile dummyPipeModel=getExistingFile(modLoc("block/dummy_pipe"));
		getVariantBuilder(IPContent.Blocks.dummyBlockPipe).partialState()
			.setModels(new ConfiguredModel(dummyPipeModel));
		itemModelWithParent(IPContent.Blocks.dummyBlockPipe, dummyPipeModel);
		
		// Dummy Conveyor
		ModelFile dummyConveyorModel=getExistingFile(modLoc("block/dummy_conveyor"));
		getVariantBuilder(IPContent.Blocks.dummyBlockConveyor).partialState()
			.setModels(new ConfiguredModel(dummyConveyorModel));
		getItemBuilder(IPContent.Blocks.dummyBlockConveyor)
			.parent(dummyConveyorModel)
			.texture("particle", new ResourceLocation(ImmersiveEngineering.MODID, "block/conveyor/conveyor"));
		
		// Multiblocks
		//createMultiblock(IPContent.Multiblock.distillationtower, modLoc("models/distillation_tower"));
		createEmptyMultiblock(IPContent.Multiblock.pumpjack, modLoc("models/pumpjack"));
		
		distillationtower();
		
		// "Normal" Blocks
		simpleBlockWithItem(IPContent.Blocks.blockAsphalt);
		gasGenerator();
		
		autolubricator();
		
		// Fluids
		for(IPFluid f:IPFluid.LIST){
			ResourceLocation still=f.getAttributes().getStillTexture();
			ModelFile model = getBuilder("block/fluid/"+f.getRegistryName().getPath()).texture("particle", still);
			
			getVariantBuilder(f.block).partialState().setModels(new ConfiguredModel(model));
		}
		
		loadedModels.backupModels();
	}
	
	private void distillationtower(){
		ResourceLocation idleTexture=modLoc("multiblock/distillation_tower");
		ResourceLocation modelNormal=modLoc("models/multiblock/obj/distillationtower.obj");
		ResourceLocation modelMirrored=modLoc("models/multiblock/obj/distillationtower_mirrored.obj");
		
		LoadedModelBuilder normal=towerModel(modelNormal, idleTexture, "_idle");
		LoadedModelBuilder mirrored=towerModel(modelMirrored, idleTexture, "_mirrored_idle");
		
		createMultiblock(IPContent.Multiblock.distillationtower, normal, mirrored, idleTexture);
	}
	
	private LoadedModelBuilder towerModel(ResourceLocation model, ResourceLocation texture, String add){
		LoadedModelBuilder re=this.loadedModels.withExistingParent(getMultiblockPath(IPContent.Multiblock.distillationtower)+add, mcLoc("block"))
				.texture("texture", texture)
				.texture("particle", texture)
				.additional("flip-v", true)
				.additional("model", model)
				.additional("detectCullableFaces", false)
				.loader(FORGE_LOADER);
		return re;
	}
	
	private void autolubricator(){
		BlockModelBuilder lube_empty=withExistingParent("lube_empty", new ResourceLocation(ImmersiveEngineering.MODID, "block/ie_empty"))
				.texture("particle", new ResourceLocation("immersivepetroleum", "models/lubricator"));
		
		LoadedModelBuilder lubeModel=this.loadedModels.withExistingParent(getPath(IPContent.Blocks.blockAutolubricator),
				mcLoc("block"))
				.loader(FORGE_LOADER)
				.additional("model", modLoc("models/block/obj/autolubricator.obj"))
				.additional("flip-v", true)
				.texture("texture", new ResourceLocation("immersivepetroleum", "models/lubricator"))
				.texture("particle", new ResourceLocation("immersivepetroleum", "models/lubricator"));
		
		VariantBlockStateBuilder lubeBuilder = getVariantBuilder(IPContent.Blocks.blockAutolubricator);
		for(Direction dir:AutoLubricatorBlock.FACING.getAllowedValues()){
			int rot = (90 * dir.getHorizontalIndex()) + 90 % 360;
			
			lubeBuilder.partialState()
				.with(AutoLubricatorBlock.SLAVE, false)
				.with(AutoLubricatorBlock.FACING, dir)
				.setModels(new ConfiguredModel(lubeModel, 0, rot, false));
			
			lubeBuilder.partialState()
				.with(AutoLubricatorBlock.SLAVE, true)
				.with(AutoLubricatorBlock.FACING, dir)
				.setModels(new ConfiguredModel(lube_empty));
		}
	}
	
	private void gasGenerator(){
		JsonObject basemodel=new JsonObject();
		basemodel.addProperty("loader", "forge:obj");
		basemodel.addProperty("model", modLoc("models/block/obj/generator.obj").toString());
		basemodel.addProperty("flip-v", true);
		
		LoadedModelBuilder model=loadedModels.getBuilder(getPath(IPContent.Blocks.blockGasGenerator))
			.texture("texture", modLoc("block/obj/generator"))
			.texture("particle", modLoc("block/gen_bottom"))
			.loader(ConnectionLoader.LOADER_NAME)
			.additional("base_model", basemodel)
			.additional("layers", Arrays.asList("TRANSLUCENT", "SOLID"))
			;
		
		
		VariantBlockStateBuilder builder=getVariantBuilder(IPContent.Blocks.blockGasGenerator);
		Direction.Plane.HORIZONTAL.forEach(dir->{
			int rotation=90*dir.getHorizontalIndex();
			
			builder.partialState()
				.with(GasGeneratorBlock.FACING, dir)
				.addModels(new ConfiguredModel(model, 0, rotation, false));
		});
		
		//getItemBuilder(IPContent.Blocks.blockGasGenerator).parent(getExistingFile(modLoc("block/generator")));
	}
	
	/** Used basicly for every multiblock-block */
	private final ConfiguredModel EMPTY_MODEL = new ConfiguredModel(
			new ExistingModelFile(new ResourceLocation(ImmersiveEngineering.MODID, "block/ie_empty"), existingFileHelper)
	);
	
	/** From {@link blusunrize.immersiveengineering.common.data.BlockStates} 
	 * @param idleTexture */
	private void createMultiblock(Block b, ModelFile masterModel, ModelFile mirroredModel, ResourceLocation particleTexture){
		createMultiblock(b, masterModel, mirroredModel, IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL, IEProperties.MIRRORED, 180, particleTexture);
	}
	
	/** From {@link blusunrize.immersiveengineering.common.data.BlockStates} */
	private void createMultiblock(Block b, ModelFile masterModel, @Nullable ModelFile mirroredModel, IProperty<Boolean> isSlave, EnumProperty<Direction> facing, @Nullable IProperty<Boolean> mirroredState, int rotationOffset, ResourceLocation particleTex){
		Preconditions.checkArgument((mirroredModel == null) == (mirroredState == null));
		VariantBlockStateBuilder builder = getVariantBuilder(b);
		builder.partialState()
			.with(isSlave, true)
			.setModels(new ConfiguredModel(
					withExistingParent(getMultiblockPath(b)+"_empty", EMPTY_MODEL.model.getLocation())
					.texture("particle", particleTex)));
		
		boolean[] possibleMirrorStates;
		if(mirroredState != null)
			possibleMirrorStates = new boolean[]{false, true};
		else
			possibleMirrorStates = new boolean[1];
		for(boolean mirrored:possibleMirrorStates)
			for(Direction dir:facing.getAllowedValues()){
				final int angleY;
				final int angleX;
				if(facing.getAllowedValues().contains(Direction.UP)){
					angleX = -90 * dir.getYOffset();
					if(dir.getAxis() != Axis.Y)
						angleY = getAngle(dir, rotationOffset);
					else
						angleY = 0;
				}else{
					angleY = getAngle(dir, rotationOffset);
					angleX = 0;
				}
				
				ModelFile model = mirrored ? mirroredModel : masterModel;
				PartialBlockstate partialState = builder.partialState()
						.with(isSlave, false)
						.with(facing, dir);
				
				if(mirroredState != null)
					partialState = partialState.with(mirroredState, mirrored);
				
				partialState.setModels(new ConfiguredModel(model, angleX, angleY, true));
			}
	}
	
	/** From {@link blusunrize.immersiveengineering.common.data.BlockStates} */
	private int getAngle(Direction dir, int offset){
		return (int) ((dir.getHorizontalAngle() + offset) % 360);
	}
	
	/**
	 * Gives every state an empty model
	 * <pre>From {@link blusunrize.immersiveengineering.common.data.BlockStates}
	 * 
	 * Altered to only result in empty models</pre>
	 * 
	 * @param block
	 * @param particletexture
	 */
	private void createEmptyMultiblock(Block block, ResourceLocation particletexture){
		IProperty<Boolean> isSlave=IEProperties.MULTIBLOCKSLAVE;
		EnumProperty<Direction> facing=IEProperties.FACING_HORIZONTAL;
		IProperty<Boolean> mirroredState=IEProperties.MIRRORED;
		
		VariantBlockStateBuilder builder=getVariantBuilder(block);
		builder.partialState()
				.with(isSlave, true)
				.setModels(new ConfiguredModel(
						withExistingParent(getMultiblockPath(block)+"_empty", EMPTY_MODEL.model.getLocation())
						.texture("particle", particletexture)));
		
		boolean[] possibleMirrorStates;
		if(mirroredState!=null)
			possibleMirrorStates = new boolean[]{false, true};
		else
			possibleMirrorStates = new boolean[1];
		for(boolean mirrored : possibleMirrorStates)
			for(Direction dir : facing.getAllowedValues())
			{
				PartialBlockstate partialState = builder.partialState()
						.with(isSlave, false)
						.with(facing, dir);
				if(mirroredState!=null)
					partialState = partialState.with(mirroredState, mirrored);
				partialState.setModels(EMPTY_MODEL);
			}
	}
	
	private String getMultiblockPath(Block b){
		return "multiblock/"+getPath(b);
	}
	
	private String getPath(Block b){
		return b.getRegistryName().getPath();
	}
	
	private void itemModelWithParent(Block block, ModelFile parent){
		getItemBuilder(block).parent(parent)
			.texture("particle", modLoc("block/"+getPath(block)));
	}
	
	private void simpleBlockWithItem(Block block){
		ModelFile file=cubeAll(block);
		
		getVariantBuilder(block).partialState()
			.setModels(new ConfiguredModel(file));
		itemModelWithParent(block, file);
	}
	
	private BlockModelBuilder getItemBuilder(Block block){
		return getBuilder(modLoc("item/"+getPath(block)).toString());
	}
}
