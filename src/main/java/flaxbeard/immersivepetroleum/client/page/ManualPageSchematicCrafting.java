package flaxbeard.immersivepetroleum.client.page;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockArcFurnace;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualPages;
import com.google.common.collect.ArrayListMultimap;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class ManualPageSchematicCrafting extends ManualPages.Crafting
{
	public ManualPageSchematicCrafting(ManualInstance manual, String text, Object stack)
	{
		super(manual, text, stack);

		ArrayListMultimap<Object, PositionedItemStack[]> recipes = ArrayListMultimap.create();


		int xBase = (120 - (5) * 18) / 2;

		ItemStack schematic = new ItemStack(IPContent.itemProjector);
		ItemNBTHelper.setString(schematic, "multiblock", MultiblockArcFurnace.instance.getUniqueName());

		PositionedItemStack[] pIngredients = new PositionedItemStack[10];
		for (int hh = 0; hh < 3; hh++)
		{
			for (int ww = 0; ww < 3; ww++)
			{
				pIngredients[hh * 3 + ww] = new PositionedItemStack(null, xBase + ww * 18, hh * 18);
			}
		}
		pIngredients[0] = new PositionedItemStack(new ItemStack(IEContent.itemTool, 1, 3), xBase, 0);
		pIngredients[1] = new PositionedItemStack(new ItemStack(IPContent.itemProjector, 1, 0), xBase + 18, 0);
		pIngredients[9] = new PositionedItemStack(schematic, xBase + 3 * 18 + 18, (int) (3 / 2f * 18) - 8);
		recipes.put(stack, pIngredients);

		ReflectionHelper.setPrivateValue(ManualPages.Crafting.class, this, recipes, 1);

		ReflectionHelper.setPrivateValue(ManualPages.Crafting.class, this, new int[]{3 * 18}, 3);


	}

	@Override
	public void recalculateCraftingRecipes()
	{
	}

}
