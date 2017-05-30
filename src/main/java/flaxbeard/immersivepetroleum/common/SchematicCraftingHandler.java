package flaxbeard.immersivepetroleum.common;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;

public class SchematicCraftingHandler implements IRecipe
{
	static
	{
		RecipeSorter.register(ImmersivePetroleum.MODID + ":schematicCrafting", SchematicCraftingHandler.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
	}

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn)
	{
		return new SchematicResult(inv).canCraft;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		return new SchematicResult(inv).output;
	}

	@Override
	public int getRecipeSize()
	{
		return 0;
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return null;
	}

	@Override
	public ItemStack[] getRemainingItems(InventoryCrafting inv)
	{
		return new SchematicResult(inv).remaining;
	}
	
	private class SchematicResult
	{
		private final boolean canCraft;
		private final ItemStack[] remaining;
		private final ItemStack output;
		
		private ItemStack manual;
		int manualStack = 0;

		public SchematicResult(InventoryCrafting inv)
		{
			this.manual = null;
			this.canCraft = process(inv);
			if (canCraft)
			{
				remaining = new ItemStack[9];
				remaining[manualStack] = ItemStack.copyItemStack(manual);
				
				String last = ItemNBTHelper.getString(manual, "lastMultiblock");
				ItemStack op = new ItemStack(IPContent.itemSchematic, 1, 0);
				ItemNBTHelper.setString(op, "multiblock", last);
				output = op;
			}
			else
			{
				remaining = new ItemStack[9];
				output = null;
			}
		}
		
		private boolean process(InventoryCrafting inv)
		{
			boolean hasPaper = false;
			for (int i = 0; i < inv.getSizeInventory(); i++)
			{
				ItemStack stack = inv.getStackInSlot(i);
				if (stack != null)
				{
					int[] ids = OreDictionary.getOreIDs(stack);
					boolean isPaper = false;
					for (int id : ids)
					{
						if (id == OreDictionary.getOreID("paper"))
						{
							isPaper = true;
							break;
						}
					}
					if (stack.getItem() == IEContent.itemTool && stack.getItemDamage() == 3)
					{
						if (manual == null && ItemNBTHelper.hasKey(stack, "lastMultiblock"))
						{
							manual = stack;
							manualStack = i;
						}
						else
						{
							return false;
						}
					}
					else if (isPaper)
					{
						if (!hasPaper)
						{
							hasPaper = true;
						}
						else
						{
							return false;
						}
					}
					else
					{
						return false;
					}
					
				}
			}
			return manual != null && hasPaper;
		}
	}

}
