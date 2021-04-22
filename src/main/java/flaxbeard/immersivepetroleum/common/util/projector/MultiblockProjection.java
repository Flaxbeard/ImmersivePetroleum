package flaxbeard.immersivepetroleum.common.util.projector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.utils.TemplateWorldCreator;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;

/**
 * Class for handling projection placement<br>
 * <br>
 * Flipping only supports {@link Mirror#NONE} and {@link Mirror#FRONT_BACK}
 * 
 * @author TwistedGate
 */
public class MultiblockProjection{
	final IMultiblock multiblock;
	final World realWorld;
	final World templateWorld;
	final PlacementSettings settings = new PlacementSettings();
	final Int2ObjectMap<List<Template.BlockInfo>> layers = new Int2ObjectArrayMap<>();
	final Mutable offset = new Mutable();
	final int blockcount;
	boolean isDirty = true;
	public MultiblockProjection(@Nonnull World world, @Nonnull IMultiblock multiblock){
		Objects.requireNonNull(world, "World cannot be null!");
		Objects.requireNonNull(multiblock, "Multiblock cannot be null!");
		
		this.multiblock = multiblock;
		this.realWorld = world;
		
		List<Template.BlockInfo> blocks = multiblock.getStructure(world);
		this.templateWorld = TemplateWorldCreator.CREATOR.getValue().makeWorld(blocks, pos -> true);
		
		this.blockcount = blocks.size();
		for(Template.BlockInfo info:blocks){
			List<Template.BlockInfo> list = this.layers.get(info.pos.getY());
			if(list == null){
				list = new ArrayList<>();
				this.layers.put(info.pos.getY(), list);
			}
			
			list.add(info);
		}
	}
	
	public MultiblockProjection setRotation(Rotation rotation){
		if(this.settings.getRotation() != rotation){
			this.settings.setRotation(rotation);
			this.isDirty = true;
		}
		
		return this;
	}
	
	/**
	 * Sets the mirrored state.
	 * 
	 * <pre>
	 * true = {@link Mirror#FRONT_BACK}
	 * 
	 * false = {@link Mirror#NONE}
	 * </pre>
	 */
	public MultiblockProjection setFlip(boolean mirror){
		Mirror m = mirror ? Mirror.FRONT_BACK : Mirror.NONE;
		if(this.settings.getMirror() != m){
			this.settings.setMirror(m);
			this.isDirty = true;
		}
		
		return this;
	}
	
	public void reset(){
		this.settings.setRotation(Rotation.NONE);
		this.settings.setMirror(Mirror.NONE);
		this.offset.setPos(0, 0, 0);
	}
	
	/** Total amount of blocks present in the multiblock */
	public int getBlockCount(){
		return this.blockcount;
	}
	
	/** Amount of layers in this projection */
	public int getLayerCount(){
		return this.layers.size();
	}
	
	public int getLayerSize(int layer){
		if(layer < 0 || layer >= this.layers.size()){
			return 0;
		}
		
		return this.layers.get(layer).size();
	}
	
	public World getTemplateWorld(){
		return this.templateWorld;
	}
	
	public IMultiblock getMultiblock(){
		return this.multiblock;
	}
	
	@Override
	public boolean equals(Object obj){
		if(this == obj)
			return true;
		if(obj instanceof MultiblockProjection){
			MultiblockProjection other = (MultiblockProjection) obj;
			return this.multiblock.getUniqueName().equals(other.multiblock.getUniqueName()) &&
					this.settings.getMirror() == other.settings.getMirror() &&
					this.settings.getRotation() == other.settings.getRotation();
		}
		
		return false;
	}
	
	/**
	 * Single-Layer based projection processing
	 * 
	 * @param layer The layer to work on
	 * @param predicate What to do per block
	 * @return true if it was interrupted
	 */
	public boolean process(int layer, Predicate<Info> predicate){
		updateData();
		
		List<Template.BlockInfo> blocks = this.layers.get(layer);
		for(Template.BlockInfo info:blocks){
			BlockPos transformedPos = Template.transformedBlockPos(this.settings, info.pos).subtract(this.offset);
			
			if(predicate.test(new Info(this.multiblock, this.templateWorld, this.settings, info.pos, transformedPos))){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Multi-Layer based projection processing. (Do all at once)
	 * 
	 * @param predicate What to do per block
	 * @return true if it was stopped pre-maturely, false if it went through
	 *         everything
	 */
	public boolean processAll(BiPredicate<Integer, Info> predicate){
		updateData();
		
		for(int layer = 0;layer < getLayerCount();layer++){
			List<Template.BlockInfo> blocks = this.layers.get(layer);
			for(Template.BlockInfo info:blocks){
				BlockPos transformedPos = Template.transformedBlockPos(this.settings, info.pos).subtract(this.offset);
				
				if(predicate.test(layer, new Info(this.multiblock, this.templateWorld, this.settings, info.pos, transformedPos))){
					return true;
				}
			}
		}
		return false;
	}
	
	private void updateData(){
		if(!this.isDirty)
			return;
		this.isDirty = false;
		
		boolean mirrored = this.settings.getMirror() == Mirror.FRONT_BACK;
		Rotation rotation = this.settings.getRotation();
		Vector3i size = this.multiblock.getSize(this.realWorld);
		
		// Align corners first
		if(!mirrored){
			switch(rotation){
				case CLOCKWISE_90:		 this.offset.setPos(1 - size.getZ(), 0, 0);break;
				case CLOCKWISE_180:		 this.offset.setPos(1 - size.getX(), 0, 1 - size.getZ());break;
				case COUNTERCLOCKWISE_90:this.offset.setPos(0, 0, 1 - size.getX());break;
				default:				 this.offset.setPos(0, 0, 0);break;
			}
		}else{
			switch(rotation){
				case NONE:			this.offset.setPos(1 - size.getX(), 0, 0);break;
				case CLOCKWISE_90:	this.offset.setPos(1 - size.getZ(), 0, 1 - size.getX());break;
				case CLOCKWISE_180:	this.offset.setPos(0, 0, 1 - size.getZ());break;
				default:			this.offset.setPos(0, 0, 0);break;
			}
		}
		
		// Center the whole thing
		int x = ((rotation.ordinal() % 2 == 0) ? size.getX() : size.getZ()) / 2;
		int z = ((rotation.ordinal() % 2 == 0) ? size.getZ() : size.getX()) / 2;
		this.offset.setAndOffset(this.offset, x, 0, z);
	}
	
	// STATIC CLASSES
	
	public static final class Info{
		/** Template Position */
		public final BlockPos templatePos;
		
		/** Transformed Template Position */
		public final BlockPos tPos;
		
		/** Currently applied template transformation */
		public final PlacementSettings settings;
		
		/** The multiblock being processed */
		public final IMultiblock multiblock;
		
		public final World templateWorld;
		
		public Info(IMultiblock multiblock, World templateWorld, PlacementSettings settings, BlockPos templatePos, BlockPos transformedPos){
			this.multiblock = multiblock;
			this.templateWorld = templateWorld;
			this.settings = settings;
			this.templatePos = templatePos;
			this.tPos = transformedPos;
		}
	}
}
