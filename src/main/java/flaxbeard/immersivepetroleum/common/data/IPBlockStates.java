package flaxbeard.immersivepetroleum.common.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
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
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
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
		simpleBlock(IPContent.blockAsphalt); // FIXME Item not showing up properly.
		
		createMultiblock(IPContent.Multiblock.distillationtower, new ResourceLocation(ImmersivePetroleum.MODID, "models/distillation_tower"));
		
		gasGeneratorState();
		
		for(IPFluid f:IPFluid.LIST){
			ResourceLocation still=f.getAttributes().getStillTexture();
			ModelFile model = getBuilder("block/fluid/"+f.getRegistryName().getPath()).texture("particle", still);
			
			getVariantBuilder(f.block).partialState().setModels(new ConfiguredModel(model));
		}
		
		loadedModels.backupModels();
	}

	private void gasGeneratorState(){
		ExistingModelFile gasGenModel=getExistingFile(modLoc("block/generator"));
		MultiPartBlockStateBuilder gasGen=getMultipartBuilder(IPContent.blockGasGenerator);
		
		Direction.Plane.HORIZONTAL.forEach(dir->{
			int rotation=90*dir.getHorizontalIndex();
			gasGen.part()
				.modelFile(gasGenModel).rotationY(rotation).addModel()
				.condition(GasGeneratorBlock.FACING, dir).end();
		});
		
		getBuilder(ImmersivePetroleum.MODID+":item/gas_generator").parent(gasGenModel).gui3d(true); // FIXME Item not showing up properly.
	}
	
	/** Used basicly for every multiblock-block */
	private final ConfiguredModel EMPTY_MODEL = new ConfiguredModel(
			new ExistingModelFile(new ResourceLocation(ImmersiveEngineering.MODID, "block/ie_empty"), existingFileHelper)
	);
	
	// Mostly from blusunrize.immersiveengineering.common.data.BlockStates
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
