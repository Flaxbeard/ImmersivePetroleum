package flaxbeard.immersivepetroleum.common.fluid;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class FluidDiesel extends Fluid
{
	private static final String HAS_SULFUR = "hasSulfur";
	private final ResourceLocation sulfurFlowing;
	private final ResourceLocation sulfurStill;

	public FluidDiesel(String fluidName, ResourceLocation still, ResourceLocation flowing, ResourceLocation sulfurStill, ResourceLocation sulfurFlowing)
	{
		super(fluidName, still, flowing);
		this.sulfurStill = sulfurStill;
		this.sulfurFlowing = sulfurFlowing;
	}

	public String getLocalizedName(FluidStack stack)
	{
		if (hasSulfur(stack)) {
			String s = this.getUnlocalizedName() + ".sulfur";
			return I18n.translateToLocal(s);
		}
		return super.getLocalizedName(stack);
	}

	public static FluidStack addSulfur(FluidStack fs)
	{
		if (fs.getFluid() instanceof FluidDiesel)
		{
			if (fs.tag == null) {
				fs.tag = new NBTTagCompound();
			}
			fs.tag.setBoolean(HAS_SULFUR, true);
		}
		return fs;
	}

	public static boolean hasSulfur(FluidStack stack)
	{
		return stack.tag != null && stack.tag.hasKey(HAS_SULFUR) && stack.tag.getBoolean(HAS_SULFUR);
	}

	@Override
	public ResourceLocation getStill(FluidStack stack)
	{
		return hasSulfur(stack) ? sulfurStill : super.getStill(stack);
	}

	@Override
	public ResourceLocation getFlowing(FluidStack stack)
	{
		return hasSulfur(stack) ? sulfurFlowing : super.getFlowing(stack);
	}
}
