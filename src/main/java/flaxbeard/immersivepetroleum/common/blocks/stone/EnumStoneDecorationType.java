package flaxbeard.immersivepetroleum.common.blocks.stone;

import java.util.Locale;

import flaxbeard.immersivepetroleum.common.blocks.BlockIPBase;
import net.minecraft.util.IStringSerializable;

public enum EnumStoneDecorationType implements IStringSerializable, BlockIPBase.IBlockEnum{
	ASPHALT;
	
	@Override
	public String getName(){
		return this.toString().toLowerCase(Locale.ENGLISH);
	}
	
	@Override
	public int getMeta(){
		return ordinal();
	}
	
	@Override
	public boolean listForCreative(){
		return true;
	}
}
