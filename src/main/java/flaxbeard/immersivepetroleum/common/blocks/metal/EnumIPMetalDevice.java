package flaxbeard.immersivepetroleum.common.blocks.metal;

import java.util.Locale;

import net.minecraft.util.IStringSerializable;

public enum EnumIPMetalDevice implements IStringSerializable{
	AUTOMATIC_LUBRICATOR, GAS_GENERATOR;
	
	@Override
	public String getName(){
		return this.toString().toLowerCase(Locale.ENGLISH);
	}
}
