package flaxbeard.immersivepetroleum.common.fluid;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class FluidDiesel extends Fluid
{
	private static final String HAS_SULFUR = "hasSulfur";

	public FluidDiesel(String fluidName, ResourceLocation still, ResourceLocation flowing)
	{
		super(fluidName, still, flowing);
	}

	public String getLocalizedName(FluidStack stack)
	{
		if (stack.tag != null && stack.tag.hasKey(HAS_SULFUR) && stack.tag.getBoolean(HAS_SULFUR)) {
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
}
