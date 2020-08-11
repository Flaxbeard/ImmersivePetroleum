package flaxbeard.immersivepetroleum.common.blocks;

import java.util.function.Supplier;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.metal.MetalMultiblockBlock;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockReader;

public class IPMetalMultiblock extends MetalMultiblockBlock{
	private Supplier<TileEntityType<?>> parent;
	public IPMetalMultiblock(String name, Supplier<TileEntityType<?>> te, Supplier<TileEntityType<?>> parent, IProperty<?>... additionalProperties){
		super(name, te, additionalProperties);
		this.parent=parent;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world){
		if(!state.get(IEProperties.MULTIBLOCKSLAVE)){
			return this.parent.get().create();
		}else{
			return super.createTileEntity(state, world);
		}
	}
	
	@Override
	public ResourceLocation createRegistryName(){
		return new ResourceLocation(ImmersivePetroleum.MODID, name);
	}
}
