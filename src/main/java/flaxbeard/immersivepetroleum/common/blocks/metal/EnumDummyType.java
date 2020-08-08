package flaxbeard.immersivepetroleum.common.blocks.metal;

import java.util.Locale;

import net.minecraft.util.IStringSerializable;

public enum EnumDummyType implements IStringSerializable{
	PIPE,
	CONVEYOR,
	OIL_DEPOSIT;
	
	@Override
	public String getName(){
		return this.toString().toLowerCase(Locale.ENGLISH);
	}
}
