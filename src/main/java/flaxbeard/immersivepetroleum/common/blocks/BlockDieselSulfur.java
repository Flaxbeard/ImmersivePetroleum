package flaxbeard.immersivepetroleum.common.blocks;

import flaxbeard.immersivepetroleum.common.fluid.FluidDiesel;
import net.minecraft.block.material.Material;
import net.minecraftforge.fluids.Fluid;

public class BlockDieselSulfur extends BlockIPFluid {
	public BlockDieselSulfur(String name, Fluid fluid, Material material) {
		super(name, fluid, material);
		this.stack = FluidDiesel.addSulfur(this.stack);
	}

	@Override
	public String getRenderName()
	{
		return "diesel_sulfur";
	}
}
