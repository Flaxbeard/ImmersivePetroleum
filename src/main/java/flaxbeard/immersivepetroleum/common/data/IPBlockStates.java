package flaxbeard.immersivepetroleum.common.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
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
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelFile.ExistingModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate;

public class IPBlockStates extends BlockStateProvider{
	final IPLoadedModels loadedModels;
	public IPBlockStates(DataGenerator gen, ExistingFileHelper exFileHelper, IPLoadedModels loadedModels){
		super(gen, ImmersivePetroleum.MODID, exFileHelper);
		this.loadedModels=loadedModels;
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
		gasGeneratorState();
		
		// AutoLubricator
		VariantBlockStateBuilder lubeBuilder = getVariantBuilder(IPContent.Blocks.blockAutolubricator);
		for(Direction dir:AutoLubricatorBlock.FACING.getAllowedValues()){
			lubeBuilder.partialState()
				.with(AutoLubricatorBlock.SLAVE, false)
				.with(AutoLubricatorBlock.FACING, dir)
				.setModels(EMPTY_MODEL);
			
			lubeBuilder.partialState()
				.with(AutoLubricatorBlock.SLAVE, true)
				.with(AutoLubricatorBlock.FACING, dir)
				.setModels(EMPTY_MODEL);
		}
		getBuilder(ImmersivePetroleum.MODID+":/item/"+IPContent.Blocks.blockAutolubricator.getRegistryName().getPath())
			.parent(getExistingFile(modLoc("block/lubricator_full")));
		
		// Fluids
		for(IPFluid f:IPFluid.LIST){
			ResourceLocation still=f.getAttributes().getStillTexture();
			ModelFile model = getBuilder("block/fluid/"+f.getRegistryName().getPath()).texture("particle", still);
			
			getVariantBuilder(f.block).partialState().setModels(new ConfiguredModel(model));
		}
		
		loadedModels.backupModels();
	}
	
	private void itemModelWithParent(Block block, ModelFile parent){
		getBuilder(ImmersivePetroleum.MODID+":item/"+block.getRegistryName().getPath())
			.parent(parent)
			.texture("particle", modLoc("block/"+block.getRegistryName().getPath()));
	}
	
	private void simpleBlockWithItem(Block block){
		ModelFile file=cubeAll(block);
		
		getVariantBuilder(block).partialState()
			.setModels(new ConfiguredModel(file));
		itemModelWithParent(block, file);
	}
	
	private void gasGeneratorState(){
		BlockModelBuilder model=withExistingParent(IPContent.Blocks.blockGasGenerator.getRegistryName().toString(), modLoc("block/generator"))
				.texture("particle", modLoc("block/gen_top"));
		
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
						withExistingParent(block.getRegistryName()+"_empty", EMPTY_MODEL.model.getLocation())
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
}
