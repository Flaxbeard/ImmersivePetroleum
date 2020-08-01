package flaxbeard.immersivepetroleum.common.blocks.metal;

import java.util.Locale;

import flaxbeard.immersivepetroleum.common.blocks.BlockIPBase;
import net.minecraft.util.IStringSerializable;

public enum EnumIPMetalDevice implements IStringSerializable, BlockIPBase.IBlockEnum
{
	AUTOMATIC_LUBRICATOR,
	GAS_GENERATOR;

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
}