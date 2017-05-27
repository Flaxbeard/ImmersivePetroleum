package flaxbeard.immersivepetroleum.common.blocks.metal;

import java.util.Locale;

import net.minecraft.util.IStringSerializable;
import flaxbeard.immersivepetroleum.common.blocks.BlockIPBase;

public enum BlockTypes_MetalDecorationSlab implements IStringSerializable, BlockIPBase.IBlockEnum
{
	STEEL_SCAFFOLDING_0(true),
	STEEL_SCAFFOLDING_1(true),
	STEEL_SCAFFOLDING_2(true),
	ALUMINUM_SCAFFOLDING_0(true),
	ALUMINUM_SCAFFOLDING_1(true),
	ALUMINUM_SCAFFOLDING_2(true);

	private boolean isScaffold;

	BlockTypes_MetalDecorationSlab(boolean isScaffold)
	{
		this.isScaffold = isScaffold;
	}

	@Override
	public String getName()
	{
		return this.toString().toLowerCase(Locale.ENGLISH);
	}
	@Override
	public int getMeta()
	{
		return ordinal();
	}
	@Override
	public boolean listForCreative()
	{
		return true;
	}

	public boolean isScaffold()
	{
		return isScaffold;
	}
}