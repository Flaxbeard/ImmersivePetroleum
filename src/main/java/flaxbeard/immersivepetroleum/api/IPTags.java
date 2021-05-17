package flaxbeard.immersivepetroleum.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import com.google.common.base.Preconditions;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

public class IPTags{
	private static final Map<ITag.INamedTag<Block>, ITag.INamedTag<Item>> toItemTag = new HashMap<>();
	
	public static class Blocks{
		public static final ITag.INamedTag<Block> asphalt = createBlockTag(forgeLoc("asphalt"));
		public static final ITag.INamedTag<Block> petcoke = createBlockTag(forgeLoc("storage_blocks/petcoke"));
	}
	
	public static class Items{
		public static final ITag.INamedTag<Item> bitumen = createItemWrapper(forgeLoc("bitumen"));
		public static final ITag.INamedTag<Item> petcoke = createItemWrapper(forgeLoc("coal_petcoke"));
		public static final ITag.INamedTag<Item> petcokeDust = createItemWrapper(forgeLoc("dusts/coal_petcoke"));
		public static final ITag.INamedTag<Item> petcokeStorage = createItemWrapper(forgeLoc("storage_blocks/coal_petcoke"));
	}
	
	public static class Fluids{
		public static final ITag.INamedTag<Fluid> crudeOil = createFluidWrapper(forgeLoc("crude_oil"));
		public static final ITag.INamedTag<Fluid> diesel = createFluidWrapper(forgeLoc("diesel"));
		public static final ITag.INamedTag<Fluid> diesel_sulfur = createFluidWrapper(forgeLoc("diesel_sulfur"));
		public static final ITag.INamedTag<Fluid> gasoline = createFluidWrapper(forgeLoc("gasoline"));
		public static final ITag.INamedTag<Fluid> lubricant = createFluidWrapper(forgeLoc("lubricant"));
		public static final ITag.INamedTag<Fluid> napalm = createFluidWrapper(forgeLoc("napalm"));
	}
	
	public static class Utility{
		public static final ITag.INamedTag<Fluid> burnableInFlarestack = createFluidWrapper(modLoc("burnable_in_flarestack"));
	}
	
	public static ITag.INamedTag<Item> getItemTag(ITag.INamedTag<Block> blockTag){
		Preconditions.checkArgument(toItemTag.containsKey(blockTag));
		return toItemTag.get(blockTag);
	}
	
	private static ITag.INamedTag<Block> createBlockTag(ResourceLocation name){
		ITag.INamedTag<Block> blockTag = createBlockWrapper(name);
		toItemTag.put(blockTag, createItemWrapper(name));
		return blockTag;
	}
	
	public static void forAllBlocktags(BiConsumer<INamedTag<Block>, INamedTag<Item>> out){
		for(Entry<INamedTag<Block>, INamedTag<Item>> entry:toItemTag.entrySet())
			out.accept(entry.getKey(), entry.getValue());
	}
	
	private static ITag.INamedTag<Block> createBlockWrapper(ResourceLocation name){
		return BlockTags.makeWrapperTag(name.toString());
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
	
	private static ResourceLocation modLoc(String path){
		return new ResourceLocation(ImmersivePetroleum.MODID, path);
	}
}
