package flaxbeard.immersivepetroleum.common.blocks.metal;

import flaxbeard.immersivepetroleum.common.blocks.BlockIPBase;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockTypes_Dummy implements IStringSerializable, BlockIPBase.IBlockEnum
{
	PIPE,
	CONVEYOR,
	OIL_DEPOSIT;

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
		return false;
	}
}