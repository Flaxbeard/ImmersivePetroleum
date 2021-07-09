package flaxbeard.immersivepetroleum.common.data;

import java.util.Arrays;

import blusunrize.immersiveengineering.common.util.loot.DropInventoryLootEntry;
import blusunrize.immersiveengineering.common.util.loot.MBOriginalBlockLootEntry;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.data.loot.LootGenerator;
import flaxbeard.immersivepetroleum.common.util.loot.IPTileDropLootEntry;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IDataProvider;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

public class IPBlockLoot extends LootGenerator implements IDataProvider{
	public IPBlockLoot(DataGenerator gen){
		super(gen);
	}
	
	@Override
	public String getName(){
		return "LootTablesBlock";
	}
	
	@Override
	protected void registerTables(){
		registerSelfDropping(IPContent.Blocks.asphalt);
		registerSelfDropping(IPContent.Blocks.asphalt_slab);
		registerSelfDropping(IPContent.Blocks.asphalt_stair);
		registerSelfDropping(IPContent.Blocks.flarestack);
		
		register(IPContent.Blocks.gas_generator, tileDrop());
		register(IPContent.Blocks.auto_lubricator, tileDrop());
		
		registerMultiblock(IPContent.Multiblock.distillationtower);
		registerMultiblock(IPContent.Multiblock.pumpjack);
		registerMultiblock(IPContent.Multiblock.hydrotreater);
	}
	
	private void registerMultiblock(Block b){
		register(b, dropInv(), dropOriginalBlock());
	}
	
	private LootPool.Builder dropOriginalBlock(){
		return createPoolBuilder().addEntry(MBOriginalBlockLootEntry.builder());
	}
	
	private LootPool.Builder dropInv(){
		return createPoolBuilder().addEntry(DropInventoryLootEntry.builder());
	}
	
	private LootPool.Builder tileDrop(){
		return createPoolBuilder().addEntry(IPTileDropLootEntry.builder());
	}
	
	private void registerSelfDropping(Block b, LootPool.Builder... pool){
		LootPool.Builder[] withSelf = Arrays.copyOf(pool, pool.length + 1);
		withSelf[withSelf.length - 1] = singleItem(b);
		register(b, withSelf);
	}
	
	private LootPool.Builder singleItem(IItemProvider in){
		return createPoolBuilder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(in));
	}
	
	private void register(Block b, LootPool.Builder... pools){
		LootTable.Builder builder = LootTable.builder();
		for(LootPool.Builder pool:pools)
			builder.addLootPool(pool);
		register(b, builder);
	}
	
	private void register(Block b, LootTable.Builder table){
		register(b.getRegistryName(), table);
	}
	
	private void register(ResourceLocation name, LootTable.Builder table){
		if(tables.put(toTableLoc(name), table.setParameterSet(LootParameterSets.BLOCK).build()) != null)
			throw new IllegalStateException("Duplicate loot table " + name);
	}
	
	private LootPool.Builder createPoolBuilder(){
		return LootPool.builder().acceptCondition(SurvivesExplosion.builder());
	}
	
	private ResourceLocation toTableLoc(ResourceLocation in){
		return new ResourceLocation(in.getNamespace(), "blocks/" + in.getPath());
	}
}
