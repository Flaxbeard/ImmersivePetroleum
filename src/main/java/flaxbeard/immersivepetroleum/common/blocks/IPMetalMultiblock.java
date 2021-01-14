package flaxbeard.immersivepetroleum.common.blocks;

import java.util.function.Supplier;

import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.MetalMultiblockBlock;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;

public class IPMetalMultiblock<T extends MultiblockPartTileEntity<T>> extends MetalMultiblockBlock<T>{
	public IPMetalMultiblock(String name, Supplier<TileEntityType<T>> te, Property<?>... additionalProperties){
		super(name, te, additionalProperties);
	}
	
	@Override
	public ResourceLocation createRegistryName(){
		return new ResourceLocation(ImmersivePetroleum.MODID, name);
	}
}
