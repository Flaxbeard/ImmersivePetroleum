package flaxbeard.immersivepetroleum.api;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public class IPTags{
	
	public static class Blocks{
		public static final Tag<Block> asphalt=new BlockTags.Wrapper(forgeLoc("asphalt"));
	}
	
	public static class Items{
		public static final Tag<Item> bitumen=new ItemTags.Wrapper(forgeLoc("bitumen"));
	}
	
	public static class Fluids{
		public static final Tag<Fluid> crudeOil = new FluidTags.Wrapper(forgeLoc("crude_oil"));
		public static final Tag<Fluid> diesel = new FluidTags.Wrapper(forgeLoc("diesel"));
		public static final Tag<Fluid> gasoline = new FluidTags.Wrapper(forgeLoc("gasoline"));
		public static final Tag<Fluid> lubricant = new FluidTags.Wrapper(forgeLoc("lubricant"));
		public static final Tag<Fluid> napalm = new FluidTags.Wrapper(forgeLoc("napalm"));
	}
	
	private static ResourceLocation forgeLoc(String path){
		return new ResourceLocation("forge", path);
	}
}
