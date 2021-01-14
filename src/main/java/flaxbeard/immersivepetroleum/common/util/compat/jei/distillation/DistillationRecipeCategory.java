package flaxbeard.immersivepetroleum.common.util.compat.jei.distillation;

import java.util.ArrayList;
import java.util.List;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.util.compat.jei.IPRecipeCategory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class DistillationRecipeCategory extends IPRecipeCategory<DistillationRecipe>{
	public static final ResourceLocation ID = new ResourceLocation(ImmersivePetroleum.MODID, "distillation");
	
	private final IDrawableStatic tankOverlay;
	public DistillationRecipeCategory(IGuiHelper guiHelper){
		super(DistillationRecipe.class, guiHelper, ID, "block.immersivepetroleum.distillationtower");
		ResourceLocation background = new ResourceLocation(ImmersivePetroleum.MODID, "textures/gui/distillation.png");
		setBackground(guiHelper.createDrawable(background, 51, 0, 81, 77));
		setIcon(new ItemStack(IPContent.Multiblock.distillationtower));
		this.tankOverlay = guiHelper.createDrawable(background, 177, 31, 20, 51);
	}
	
	@Override
	public void setIngredients(DistillationRecipe recipe, IIngredients ingredients){
		List<FluidStack> out = new ArrayList<>();
		for(FluidStack fluid:recipe.getFluidOutputs()){
			if(fluid != null)
				out.add(fluid);
		}
		
		ingredients.setInputs(VanillaTypes.FLUID, recipe.getInputFluid().getMatchingFluidStacks());
		ingredients.setOutputs(VanillaTypes.FLUID, out);
	}
	
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, DistillationRecipe recipe, IIngredients ingredients){
		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		
		if(recipe.getInputFluid() != null){
			guiFluidStacks.init(0, true, 9, 19, 20, 51, 100, false, this.tankOverlay);
			guiFluidStacks.set(0, recipe.getInputFluid().getMatchingFluidStacks());
		}
		
		guiFluidStacks.init(1, false, 61, 21, 16, 47, 100, false, null);
		guiFluidStacks.set(1, recipe.getFluidOutputs());
	}
}
