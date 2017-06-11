package flaxbeard.immersivepetroleum.common.blocks.wood;

import java.util.Locale;

import net.minecraft.util.IStringSerializable;
import flaxbeard.immersivepetroleum.common.blocks.BlockIPBase;

public enum BlockTypes_WoodDecorationSlab implements IStringSerializable, BlockIPBase.IBlockEnum
{
	SCAFFOLDING(true);
	
	private boolean isScaffold;

	BlockTypes_WoodDecorationSlab(boolean isScaffold)
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