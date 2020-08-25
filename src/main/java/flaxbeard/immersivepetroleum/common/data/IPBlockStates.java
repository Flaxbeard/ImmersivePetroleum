package flaxbeard.immersivepetroleum.common.data;

import java.util.Arrays;

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
import net.minecraft.util.ResourceLocation;
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
		getBuilder(ImmersivePetroleum.MODID+":item/"+IPContent.Blocks.dummyBlockConveyor.getRegistryName().getPath())
			.parent(dummyConveyorModel)
			.texture("particle", new ResourceLocation("immersiveengineering", "block/conveyor/conveyor"));
		
		// Multiblocks
		createMultiblock(IPContent.Multiblock.distillationtower, new ResourceLocation(ImmersivePetroleum.MODID, "models/distillation_tower"));
		createMultiblock(IPContent.Multiblock.pumpjack, new ResourceLocation(ImmersivePetroleum.MODID, "models/pumpjack"));
		
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
	
	}
	
	private void autolubricator(){
		LoadedModelBuilder lubeModel=this.loadedModels.withExistingParent(getPath(IPContent.Blocks.blockAutolubricator),
				mcLoc("block"))
				.loader(FORGE_LOADER)
				.additional("model", modLoc("models/block/obj/autolubricator.obj"))
				.additional("flip-v", true)
				.texture("texture", new ResourceLocation("immersivepetroleum", "models/lubricator"))
				.texture("particle", new ResourceLocation("immersivepetroleum", "models/lubricator"));
		
		// Block(s)
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
				.setModels(EMPTY_MODEL);
		}
	}
	
	private void gasGenerator(){
		JsonObject basemodel=new JsonObject();
		basemodel.addProperty("loader", "forge:obj");
		basemodel.addProperty("model", modLoc("models/block/obj/generator.obj").toString());
		basemodel.addProperty("flip-v", true);
		
		LoadedModelBuilder model=loadedModels.getBuilder(IPContent.Blocks.blockGasGenerator.getRegistryName().toString())
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
		
		getBuilder(ImmersivePetroleum.MODID+":item/gas_generator").parent(getExistingFile(modLoc("block/generator")));
	}
	
	/** Used basicly for every multiblock-block */
	private final ConfiguredModel EMPTY_MODEL = new ConfiguredModel(
			new ExistingModelFile(new ResourceLocation(ImmersiveEngineering.MODID, "block/ie_empty"), existingFileHelper)
	);
	
	/** From {@link blusunrize.immersiveengineering.common.data.BlockStates} and altered to only result in empty models */
	private void createMultiblock(Block block, ResourceLocation particletexture){
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
