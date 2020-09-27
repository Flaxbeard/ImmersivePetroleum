package flaxbeard.immersivepetroleum.common.util.enums;

import java.util.Locale;

import net.minecraft.util.IStringSerializable;

public enum EnumIPMetalDevice implements IStringSerializable{
	GAS_GENERATOR,
	AUTOMATIC_LUBRICATOR;
	
	@Override
	public String getString(){
		return this.toString().toLowerCase(Locale.ENGLISH);
	}
	
	@Deprecated
	public String getName(){
		return getString();
	}
}
