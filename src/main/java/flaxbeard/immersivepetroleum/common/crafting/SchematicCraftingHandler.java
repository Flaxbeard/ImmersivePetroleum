package flaxbeard.immersivepetroleum.common.crafting;

import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.immersivepetroleum.common.IPContent.Items;
import flaxbeard.immersivepetroleum.common.items.ProjectorItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;

public class SchematicCraftingHandler implements ICraftingRecipe{
	public static RegistryObject<SpecialRecipeSerializer<SchematicCraftingHandler>> SERIALIZER;
	
	private final ResourceLocation id;
	public SchematicCraftingHandler(ResourceLocation id){
		this.id = id;
	}
	
	@Override
	public IRecipeSerializer<?> getSerializer(){
		return SERIALIZER.get();
	}
	
	@Override
	public boolean matches(CraftingInventory inv, World worldIn){
		return new SchematicResult(inv).canCraft;
	}
	
	@Override
	public ItemStack getCraftingResult(CraftingInventory inv){
		return new SchematicResult(inv).output;
	}
	
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv){
		return new SchematicResult(inv).remaining;
	}
	
	@Override
	public boolean canFit(int width, int height){
		return width >= 2 && height >= 2;
	}
	
	@Override
	public ResourceLocation getId(){
		return this.id;
	}
	
	@Override
	public ItemStack getRecipeOutput(){
		return ItemStack.EMPTY;
	}
	
	private class SchematicResult{
		private final boolean canCraft;
		private final NonNullList<ItemStack> remaining;
		private final ItemStack output;
		
		private ItemStack manual;
		int manualStack = 0;
		
		public SchematicResult(CraftingInventory inv){
			this.manual = ItemStack.EMPTY;
			this.canCraft = isValid(inv);
			if(canCraft){
				remaining = NonNullList.withSize(9, ItemStack.EMPTY);
				remaining.set(manualStack, manual.copy());
				String last = ItemNBTHelper.getString(manual, "lastMultiblock");
				ItemStack op = new ItemStack(Items.itemProjector, 1);
				ItemNBTHelper.putString(op, "multiblock", last);
				ProjectorItem.setFlipped(op, true);
				output = op;
			}else{
				remaining = NonNullList.withSize(9, ItemStack.EMPTY);
				output = ItemStack.EMPTY;
			}
		}
		
		private boolean isValid(CraftingInventory inv){
			boolean hasPaper = false;
			for(int i = 0;i < inv.getSizeInventory();i++){
				ItemStack stack = inv.getStackInSlot(i);
				if(!stack.isEmpty()){
					if(stack.getItem() == IEItems.Tools.manual){
						if(manual.isEmpty() && ItemNBTHelper.hasKey(stack, "lastMultiblock")){
							manual = stack;
							manualStack = i;
						}else{
							return false;
						}
					}else if(stack.getItem() == Items.itemProjector){
						if(!hasPaper){
							hasPaper = true;
						}else{
							return false;
						}
					}else{
						return false;
					}
				}
			}
			return !manual.isEmpty() && hasPaper;
		}
	}
}
