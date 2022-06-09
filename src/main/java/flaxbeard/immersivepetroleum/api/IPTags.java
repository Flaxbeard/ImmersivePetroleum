package flaxbeard.immersivepetroleum.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import com.google.common.base.Preconditions;

import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
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
		public static final ITag.INamedTag<Block> asphalt = createBlockTag(ResourceUtils.forge("asphalt"));
		public static final ITag.INamedTag<Block> petcoke = createBlockTag(ResourceUtils.forge("storage_blocks/petcoke"));
	}
	
	public static class Items{
		public static final ITag.INamedTag<Item> bitumen = createItemWrapper(ResourceUtils.forge("bitumen"));
		public static final ITag.INamedTag<Item> petcoke = createItemWrapper(ResourceUtils.forge("coal_petcoke"));
		public static final ITag.INamedTag<Item> petcokeDust = createItemWrapper(ResourceUtils.forge("dusts/coal_petcoke"));
		public static final ITag.INamedTag<Item> petcokeStorage = createItemWrapper(ResourceUtils.forge("storage_blocks/coal_petcoke"));
	}
	
	public static class Fluids{
		public static final ITag.INamedTag<Fluid> crudeOil = createFluidWrapper(ResourceUtils.forge("crude_oil"));
		public static final ITag.INamedTag<Fluid> diesel = createFluidWrapper(ResourceUtils.forge("diesel"));
		public static final ITag.INamedTag<Fluid> diesel_sulfur = createFluidWrapper(ResourceUtils.forge("diesel_sulfur"));
		public static final ITag.INamedTag<Fluid> gasoline = createFluidWrapper(ResourceUtils.forge("gasoline"));
		public static final ITag.INamedTag<Fluid> lubricant = createFluidWrapper(ResourceUtils.forge("lubricant"));
		public static final ITag.INamedTag<Fluid> napalm = createFluidWrapper(ResourceUtils.forge("napalm"));
	}
	
	public static class Utility{
		public static final ITag.INamedTag<Fluid> burnableInFlarestack = createFluidWrapper(ResourceUtils.ip("burnable_in_flarestack"));
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
}
