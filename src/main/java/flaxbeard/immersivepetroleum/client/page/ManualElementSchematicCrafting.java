package flaxbeard.immersivepetroleum.client.page;

import java.util.List;

import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.lib.manual.ManualElementCrafting;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.PositionedItemStack;
import flaxbeard.immersivepetroleum.common.IPContent.Items;
import net.minecraft.item.ItemStack;

public class ManualElementSchematicCrafting extends ManualElementCrafting{
	@SuppressWarnings("unchecked")
	public ManualElementSchematicCrafting(ManualInstance manual, String text, Object stack){
		super(manual, text, stack);
		
		List<PositionedItemStack[]>[] recipes = (List<PositionedItemStack[]>[]) new List[10]; // It changed to this.
		
		int xBase = (120 - (5) * 18) / 2;
		
		ItemStack schematic = new ItemStack(Items.itemProjector);
		ItemNBTHelper.putString(schematic, "multiblock", IEMultiblocks.ARC_FURNACE.getUniqueName().toString());
		
		// Init crafting slots with "air"
		PositionedItemStack[] pIngredients = new PositionedItemStack[10];
		for(int hh = 0;hh < 3;hh++){
			for(int ww = 0;ww < 3;ww++){
				pIngredients[hh * 3 + ww] = new PositionedItemStack(null, xBase + ww * 18, hh * 18);
			}
		}
		
		pIngredients[0] = new PositionedItemStack(new ItemStack(IEItems.Tools.manual, 1), xBase, 0);
		pIngredients[1] = new PositionedItemStack(new ItemStack(Items.itemProjector, 1), xBase + 18, 0);
		pIngredients[9] = new PositionedItemStack(schematic, xBase + 3 * 18 + 18, (int) (3 / 2f * 18) - 8);
		//recipes.put(stack, pIngredients);
		
		//ReflectionHelper.setPrivateValue(ManualElementCrafting.class, this, recipes, 1);
		
		//ReflectionHelper.setPrivateValue(ManualElementCrafting.class, this, new int[]{3 * 18}, 3);
		
	}
	
	@Override
	public void recalculateCraftingRecipes(){}
}
