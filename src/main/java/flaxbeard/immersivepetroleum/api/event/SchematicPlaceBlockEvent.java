package flaxbeard.immersivepetroleum.api.event;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * @deprecated Use {@link flaxbeard.immersivepetroleum.api.event.ProjectorEvent.PlaceBlock}
 */
@Deprecated
@Cancelable
public class SchematicPlaceBlockEvent extends Event{
	@Deprecated
	public SchematicPlaceBlockEvent(IMultiblock multiblock, World world, BlockPos worldPos, BlockPos templatePos, BlockState state, CompoundNBT nbt, Rotation rotation){
	}
	
	@Deprecated
	public void setBlockState(BlockState state){
	}
	
	@Deprecated
	public World getWorld(){
		return null;
	}
	
	@Deprecated
	public IMultiblock getMultiblock(){
		return null;
	}
	
	@Deprecated
	public Rotation getRotate(){
		return null;
	}
	
	@Deprecated
	public BlockPos getWorldPos(){
		return null;
	}
	
	@Deprecated
	public BlockPos getTemplatePos(){
		return null;
	}
	
	@Deprecated
	public BlockState getState(){
		return null;
	}
	
	@Deprecated
	public CompoundNBT getNBT(){
		return null;
	}
	
	@Deprecated
	public int getIndex(){
		return 0;
	}
	
	@Deprecated
	public int getL(){
		return 0;
	}
	
	@Deprecated
	public int getH(){
		return 0;
	}
	
	@Deprecated
	public int getW(){
		return 0;
	}
}
