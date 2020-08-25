package flaxbeard.immersivepetroleum.common.blocks;

import java.util.function.Supplier;

import blusunrize.immersiveengineering.common.blocks.metal.MetalMultiblockBlock;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;

public class IPMetalMultiblock extends MetalMultiblockBlock{
	public IPMetalMultiblock(String name, Supplier<TileEntityType<?>> type, IProperty<?>... additionalProperties){
		super(name, type, additionalProperties);
	}
	
	@Override
	public ResourceLocation createRegistryName(){
		return new ResourceLocation(ImmersivePetroleum.MODID, name);
	}
	
	@Override
	public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer){
		return layer==BlockRenderLayer.TRANSLUCENT || layer==BlockRenderLayer.SOLID;
	}
}
