package flaxbeard.immersivepetroleum.common.blocks.stone;

import flaxbeard.immersivepetroleum.common.blocks.BlockIPBase;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockTypes_IPStoneDecoration implements IStringSerializable, BlockIPBase.IBlockEnum
{
	ASPHALT;

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