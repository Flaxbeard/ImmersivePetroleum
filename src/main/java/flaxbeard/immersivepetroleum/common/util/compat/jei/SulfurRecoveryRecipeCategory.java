package flaxbeard.immersivepetroleum.common.util.compat.jei;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Locale;

import com.mojang.blaze3d.matrix.MatrixStack;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.SulfurRecoveryRecipe;
import flaxbeard.immersivepetroleum.common.IPContent;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class SulfurRecoveryRecipeCategory extends IPRecipeCategory<SulfurRecoveryRecipe>{
	public static final ResourceLocation ID = new ResourceLocation(ImmersivePetroleum.MODID, "hydrotreater");
	
	private final IDrawableStatic tankOverlay;
	public SulfurRecoveryRecipeCategory(IGuiHelper guiHelper){
		super(SulfurRecoveryRecipe.class, guiHelper, ID, "block.immersivepetroleum.hydrotreater");
		ResourceLocation background = new ResourceLocation(ImmersivePetroleum.MODID, "textures/gui/jei/hydrotreater.png");
		setBackground(guiHelper.createDrawable(background, 0, 0, 113, 75));
		setIcon(new ItemStack(IPContent.Multiblock.hydrotreater));
		
		this.tankOverlay = guiHelper.createDrawable(background, 113, 0, 20, 51);
	}
	
	@Override
	public void setIngredients(SulfurRecoveryRecipe recipe, IIngredients ingredients){
		ingredients.setInputs(VanillaTypes.FLUID, recipe.inputFluid.getMatchingFluidStacks());
		if(recipe.inputFluidSecondary != null){
			ingredients.setInputs(VanillaTypes.FLUID, recipe.inputFluidSecondary.getMatchingFluidStacks());
		}
		
		ingredients.setOutputs(VanillaTypes.FLUID, Arrays.asList(recipe.output));
		ingredients.setOutputs(VanillaTypes.ITEM, Arrays.asList(recipe.outputItem));
	}
	
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, SulfurRecoveryRecipe recipe, IIngredients ingredients){
		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		
		int id = 0;
		
		guiFluidStacks.init(id, true, 25, 3, 20, 51, 1, false, this.tankOverlay);
		guiFluidStacks.set(id++, recipe.inputFluid.getMatchingFluidStacks());
		
		guiFluidStacks.init(id, true, 3, 3, 20, 51, 1, false, this.tankOverlay);
		guiFluidStacks.set(id++, recipe.inputFluidSecondary != null ? recipe.inputFluidSecondary.getMatchingFluidStacks() : Arrays.asList(FluidStack.EMPTY));
		
		guiFluidStacks.init(id, false, 71, 3, 20, 51, 1, false, this.tankOverlay);
		guiFluidStacks.set(id++, recipe.output);
		
		guiItemStacks.init(id, false, 93, 20);
		guiItemStacks.set(id++, Arrays.asList(recipe.outputItem));
	}
	
	@Override
	public void draw(SulfurRecoveryRecipe recipe, MatrixStack matrix, double mouseX, double mouseY){
		super.draw(recipe, matrix, mouseX, mouseY);
		DecimalFormat formatter = new DecimalFormat("#.##");
		
		IDrawable background = getBackground();
		int bWidth = background.getWidth();
		int bHeight = background.getHeight();
		FontRenderer font = Minecraft.getInstance().fontRenderer;
		
		int time = recipe.getTotalProcessTime();
		int energy = recipe.getTotalProcessEnergy();
		int chance = (int)(100 * recipe.chance);
		
		String text0 = I18n.format("desc.immersiveengineering.info.ift", formatter.format(energy));
		font.drawString(matrix, text0, bWidth / 2 - font.getStringWidth(text0) / 2, bHeight - (font.FONT_HEIGHT * 2), 0);

		String text1 = I18n.format("desc.immersiveengineering.info.seconds", formatter.format(time / 20D));
		font.drawString(matrix, text1, bWidth / 2 - font.getStringWidth(text1) / 2, bHeight - font.FONT_HEIGHT, 0);
		
		String text2 = String.format(Locale.US, "%d%%", chance);
		font.drawString(matrix, text2, bWidth+3 - font.getStringWidth(text2), bHeight / 2 + 4, 0);
	}
}
