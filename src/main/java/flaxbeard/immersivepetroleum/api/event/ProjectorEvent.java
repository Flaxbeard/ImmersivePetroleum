package flaxbeard.immersivepetroleum.api.event;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import flaxbeard.immersivepetroleum.common.util.projector.MultiblockProjection.IMultiblockBlockReader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Based on the old events from Flaxbeard
 * 
 * @author TwistedGate
 */
@Cancelable
public class ProjectorEvent extends Event{
	
	@Cancelable
	public static class PlaceBlock extends ProjectorEvent{
		public PlaceBlock(IMultiblockBlockReader blockAccess, BlockPos templatePos, World world, BlockPos worldPos, BlockState state, Rotation rotation){
			super(blockAccess, templatePos, world, worldPos, state, rotation);
		}
		
		public void setBlockState(BlockState state){
			this.state = state;
		}
		
		public void setState(Block block){
			this.state = block.getDefaultState();
		}
	}
	
	@Cancelable
	public static class PlaceBlockPost extends ProjectorEvent{
		public PlaceBlockPost(IMultiblockBlockReader blockAccess, BlockPos templatePos, World world, BlockPos worldPos, BlockState state, Rotation rotation){
			super(blockAccess, templatePos, world, worldPos, state, rotation);
		}
	}
	
	@Cancelable
	public static class RenderBlock extends ProjectorEvent{
		public RenderBlock(IMultiblockBlockReader blockAccess, BlockPos templatePos, World world, BlockPos worldPos, BlockState state, Rotation rotation){
			super(blockAccess, templatePos, world, worldPos, state, rotation);
		}
		
		public void setState(BlockState state){
			this.state = state;
		}
		
		public void setState(Block block){
			this.state = block.getDefaultState();
		}
	}
	
	protected World world;
	protected Rotation rotation;
	protected IMultiblockBlockReader blockAccess;
	protected BlockPos worldPos;
	protected BlockPos templatePos;
	protected BlockState state;
	
	public ProjectorEvent(IMultiblockBlockReader blockAccess, BlockPos templatePos, World world, BlockPos worldPos, BlockState state, Rotation rotation){
		super();
		this.world = world;
		this.blockAccess = blockAccess;
		this.worldPos = worldPos;
		this.templatePos = templatePos;
		this.state = state;
		this.rotation = rotation;
	}
	
	public World getWorld(){
		return this.world;
	}
	
	public Rotation getRotation(){
		return this.rotation;
	}
	
	public IMultiblockBlockReader getMultiblockBlockAccess(){
		return this.blockAccess;
	}
	
	public IMultiblock getMultiblock(){
		return this.blockAccess.getMultiblock();
	}
	
	public BlockPos getWorldPos(){
		return this.worldPos;
	}
	
	public BlockPos getTemplatePos(){
		return this.templatePos;
	}
	
	public BlockState getState(){
		return this.state;
	}
	
	/** Always returns the BlockState found in the Template */
	public BlockState getTemplateState(){
		return this.blockAccess.getBlockState(this.templatePos);
	}
}
