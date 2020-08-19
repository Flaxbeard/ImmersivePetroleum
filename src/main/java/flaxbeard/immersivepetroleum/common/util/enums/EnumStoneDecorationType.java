package flaxbeard.immersivepetroleum.common.util.enums;

import java.util.Locale;

import net.minecraft.util.IStringSerializable;

public enum EnumStoneDecorationType implements IStringSerializable{
	ASPHALT;
	
	@Override
	public String getName(){
		return this.toString().toLowerCase(Locale.ENGLISH);
	}
}
