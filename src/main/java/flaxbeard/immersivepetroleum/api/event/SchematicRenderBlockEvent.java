package flaxbeard.immersivepetroleum.api.event;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class SchematicRenderBlockEvent extends Event{
	private World world;
	private Rotation rotation;
	private IMultiblock multiblock;
	private BlockPos worldPos;
	private BlockPos templatePos;
	private BlockState state;
	private CompoundNBT nbt;
	
	public SchematicRenderBlockEvent(IMultiblock multiblock, World world, BlockPos worldPos, BlockPos templatePos, BlockState state, CompoundNBT nbt, Rotation rotation){
		super();
		this.world = world;
		this.multiblock = multiblock;
		this.worldPos=worldPos;
		this.state=state;
		this.nbt=nbt;
		this.templatePos=templatePos;
		this.rotation=rotation;
	}
	
	public World getWorld(){
		return this.world;
	}
	
	public IMultiblock getMultiblock(){
		return this.multiblock;
	}
	
	public Rotation getRotate(){
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
	
	public CompoundNBT getNBT(){
		return this.nbt;
	}
	
	@Deprecated
	public ItemStack getItemStack(){
		return ItemStack.EMPTY;
	}
	
	@Deprecated
	public int getIndex(){
		return 0;
	}
	
	@Deprecated
	public void setItemStack(ItemStack itemStack){}
	
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
