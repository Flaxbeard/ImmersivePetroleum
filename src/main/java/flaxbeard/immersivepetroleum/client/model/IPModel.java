package flaxbeard.immersivepetroleum.client.model;

import java.util.function.Function;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.util.ResourceLocation;

public abstract class IPModel extends Model{
	public IPModel(Function<ResourceLocation, RenderType> renderTypeIn){
		super(renderTypeIn);
	}
	
	/**
	 * This is where the model parts should be created, to keep things seperate.
	 * (And for easier refreshing)
	 */
	public abstract void init();
}
