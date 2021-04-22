package flaxbeard.immersivepetroleum.api.event;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
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
		public PlaceBlock(IMultiblock multiblock, World templateWorld, BlockPos templatePos, World world, BlockPos worldPos, BlockState state, Rotation rotation){
			super(multiblock, templateWorld, templatePos, world, worldPos, state, rotation);
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
		public PlaceBlockPost(IMultiblock multiblock, World templateWorld, BlockPos templatePos, World world, BlockPos worldPos, BlockState state, Rotation rotation){
			super(multiblock, templateWorld, templatePos, world, worldPos, state, rotation);
		}
	}
	
	@Cancelable
	public static class RenderBlock extends ProjectorEvent{
		public RenderBlock(IMultiblock multiblock, World templateWorld, BlockPos templatePos, World world, BlockPos worldPos, BlockState state, Rotation rotation){
			super(multiblock, templateWorld, templatePos, world, worldPos, state, rotation);
		}
		
		public void setState(BlockState state){
			this.state = state;
		}
		
		public void setState(Block block){
			this.state = block.getDefaultState();
		}
	}
	
	protected IMultiblock multiblock;
	protected World realWorld;
	protected World templateWorld;
	protected Rotation rotation;
	protected BlockPos worldPos;
	protected BlockPos templatePos;
	protected BlockState state;
	
	public ProjectorEvent(IMultiblock multiblock, World templateWorld, BlockPos templatePos, World world, BlockPos worldPos, BlockState state, Rotation rotation){
		super();
		this.multiblock = multiblock;
		this.realWorld = world;
		this.templateWorld = templateWorld;
		this.worldPos = worldPos;
		this.templatePos = templatePos;
		this.state = state;
		this.rotation = rotation;
	}
	
	public IMultiblock getMultiblock(){
		return multiblock;
	}
	
	public World getWorld(){
		return this.realWorld;
	}
	
	public World getTemplateWorld(){
		return this.templateWorld;
	}
	
	public Rotation getRotation(){
		return this.rotation;
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
		return this.templateWorld.getBlockState(this.templatePos);
	}
}
