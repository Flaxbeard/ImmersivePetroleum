package flaxbeard.immersivepetroleum.common.util.loot;

import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootEntry;
import net.minecraft.loot.LootPoolEntryType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class IPLootFunctions{
	public static LootPoolEntryType tileDrop;
	
	public static void modConstruction(){
		tileDrop = registerEntry(IPTileDropLootEntry.ID, new IPTileDropLootEntry.Serializer());
	}
	
	private static LootPoolEntryType registerEntry(ResourceLocation id, ILootSerializer<? extends LootEntry> serializer){
		return Registry.register(Registry.LOOT_POOL_ENTRY_TYPE, id, new LootPoolEntryType(serializer));
	}
}
