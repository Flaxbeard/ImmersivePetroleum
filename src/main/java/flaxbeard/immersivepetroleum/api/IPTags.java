package flaxbeard.immersivepetroleum.api;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

public class IPTags{
	private static final Map<ITag.INamedTag<Block>, ITag.INamedTag<Item>> toItemTag = new HashMap<>();
	
	public static class Blocks{
		public static final ITag.INamedTag<Block> asphalt=createBlockWrapper(forgeLoc("asphalt"));
	}
	
	public static class Items{
		public static final ITag.INamedTag<Item> bitumen=createItemWrapper(forgeLoc("bitumen"));
	}
	
	public static class Fluids{
		public static final ITag.INamedTag<Fluid> crudeOil = createFluidWrapper(forgeLoc("crude_oil"));
		public static final ITag.INamedTag<Fluid> diesel = createFluidWrapper(forgeLoc("diesel"));
		public static final ITag.INamedTag<Fluid> gasoline = createFluidWrapper(forgeLoc("gasoline"));
		public static final ITag.INamedTag<Fluid> lubricant = createFluidWrapper(forgeLoc("lubricant"));
		public static final ITag.INamedTag<Fluid> napalm = createFluidWrapper(forgeLoc("napalm"));
	}
	
	public static ITag.INamedTag<Item> getItemTag(ITag.INamedTag<Block> blockTag){
		Preconditions.checkArgument(toItemTag.containsKey(blockTag));
		return toItemTag.get(blockTag);
	}
	
	private static ITag.INamedTag<Block> createBlockWrapper(ResourceLocation name){
		ITag.INamedTag<Block> block = BlockTags.makeWrapperTag(name.toString());
		toItemTag.put(block, createItemWrapper(name));
		return block;
	}
	
	private static ITag.INamedTag<Item> createItemWrapper(ResourceLocation name){
		return ItemTags.makeWrapperTag(name.toString());
	}
	
	private static ITag.INamedTag<Fluid> createFluidWrapper(ResourceLocation name){
		return FluidTags.makeWrapperTag(name.toString());
	}
	
	private static ResourceLocation forgeLoc(String path){
		return new ResourceLocation("forge", path);
	}
}
