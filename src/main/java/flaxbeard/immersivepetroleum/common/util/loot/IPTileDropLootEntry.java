package flaxbeard.immersivepetroleum.common.util.loot;

import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootPoolEntryType;
import net.minecraft.loot.StandaloneLootEntry;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class IPTileDropLootEntry extends StandaloneLootEntry{
	public static final ResourceLocation ID = new ResourceLocation(ImmersivePetroleum.MODID, "tile_drop");
	
	protected IPTileDropLootEntry(int weightIn, int qualityIn, ILootCondition[] conditionsIn, ILootFunction[] functionsIn){
		super(weightIn, qualityIn, conditionsIn, functionsIn);
	}
	
	@Override
	protected void func_216154_a(Consumer<ItemStack> stackConsumer, LootContext context){
		if(context.has(LootParameters.BLOCK_ENTITY)){
			TileEntity te = context.get(LootParameters.BLOCK_ENTITY);
			if(te instanceof ITileDrop){
				((ITileDrop) te).getTileDrops(context).forEach(stackConsumer);
			}
		}
	}
	
	@Override
	public LootPoolEntryType func_230420_a_(){
		return IPLootFunctions.tileDrop;
	}
	
	public static StandaloneLootEntry.Builder<?> builder(){
		return builder(IPTileDropLootEntry::new);
	}
	
	public static class Serializer extends StandaloneLootEntry.Serializer<IPTileDropLootEntry>{
		@Nonnull
		@Override
		protected IPTileDropLootEntry deserialize(@Nonnull JsonObject json, @Nonnull JsonDeserializationContext context, int weight, int quality, @Nonnull ILootCondition[] conditions, @Nonnull ILootFunction[] functions){
			return new IPTileDropLootEntry(weight, quality, conditions, functions);
		}
	}
}
