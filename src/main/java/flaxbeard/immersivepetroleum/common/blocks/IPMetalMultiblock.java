package flaxbeard.immersivepetroleum.common.blocks;

import java.util.Iterator;
import java.util.function.Supplier;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.MetalMultiblockBlock;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLLoader;

public class IPMetalMultiblock<T extends MultiblockPartTileEntity<T>> extends MetalMultiblockBlock<T>{
	public IPMetalMultiblock(String name, Supplier<TileEntityType<T>> te){
		super(name, te);
		
		// Nessesary hacks
		if(!FMLLoader.isProduction()){
			IEContent.registeredIEBlocks.remove(this);
			Iterator<Item> it = IEContent.registeredIEItems.iterator();
			while(it.hasNext()){
				Item item = it.next();
				if(item instanceof BlockItemIE && ((BlockItemIE) item).getBlock() == this){
					it.remove();
					break;
				}
			}
		}
		
		IPContent.registeredIPBlocks.add(this);
		
		BlockItem bItem = new BlockItemIE(this, new Item.Properties().group(ImmersivePetroleum.creativeTab));
		IPContent.registeredIPItems.add(bItem.setRegistryName(getRegistryName()));
	}
	
	@Override
	public ResourceLocation createRegistryName(){
		return ResourceUtils.ip(this.name);
	}
}
