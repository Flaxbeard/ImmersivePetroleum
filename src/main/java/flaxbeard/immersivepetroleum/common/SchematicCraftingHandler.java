package flaxbeard.immersivepetroleum.common;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
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
		return ItemStack.EMPTY;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv)
	{
		return new SchematicResult(inv).remaining;
	}
	
	private class SchematicResult
	{
		private final boolean canCraft;
		private final NonNullList<ItemStack> remaining;
		private final ItemStack output;
		
		private ItemStack manual;
		int manualStack = 0;

		public SchematicResult(InventoryCrafting inv)
		{
			this.manual = ItemStack.EMPTY;
			this.canCraft = process(inv);
			if (canCraft)
			{
				remaining = NonNullList.withSize(9, ItemStack.EMPTY);
				remaining.set(manualStack, manual.copy());
				String last = "";
				if (ItemNBTHelper.hasKey(manual, "lastMultiblock"))
				{
					last = ItemNBTHelper.getString(manual, "lastMultiblock");
				}
				else
				{
					last = ItemNBTHelper.getString(manual, "multiblock");
				}
				ItemStack op = new ItemStack(IPContent.itemSchematic, 1, 0);
				ItemNBTHelper.setString(op, "multiblock", last);
				output = op;
			}
			else
			{
				remaining = NonNullList.withSize(9, ItemStack.EMPTY);
				output = ItemStack.EMPTY;;
			}
		}
		
		private boolean process(InventoryCrafting inv)
		{
			boolean hasPaper = false;
			for (int i = 0; i < inv.getSizeInventory(); i++)
			{
				ItemStack stack = inv.getStackInSlot(i);
				if (!stack.isEmpty())
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
						if (!manual.isEmpty() && ItemNBTHelper.hasKey(stack, "lastMultiblock"))
						{
							manual = stack;
							manualStack = i;
						}
						else
						{
							return false;
						}
					}
					else if (stack.getItem() == IPContent.itemSchematic)
					{
						if (!manual.isEmpty() && ItemNBTHelper.hasKey(stack, "multiblock"))
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
			return !manual.isEmpty() && hasPaper;
		}
	}

}
