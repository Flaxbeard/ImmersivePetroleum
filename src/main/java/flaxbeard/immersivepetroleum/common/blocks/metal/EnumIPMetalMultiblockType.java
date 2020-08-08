package flaxbeard.immersivepetroleum.common.blocks.metal;

import java.util.Locale;

import net.minecraft.util.IStringSerializable;

public enum EnumIPMetalMultiblockType implements IStringSerializable{
	DISTILLATION_TOWER(false),
	DISTILLATION_TOWER_PARENT(false),
	PUMPJACK(false),
	PUMPJACK_PARENT(false);
	
	private boolean needsCustomState;
	
	EnumIPMetalMultiblockType(boolean needsCustomState){
		this.needsCustomState = needsCustomState;
	}
	
	@Override
	public String getName(){
		return this.toString().toLowerCase(Locale.ENGLISH);
	}
	
	public boolean needsCustomState(){
		return this.needsCustomState;
	}
	
	public String getCustomState(){
		String[] split = getName().split("_");
		String s = split[0].toLowerCase(Locale.ENGLISH);
		for(int i = 1;i < split.length;i++){
			s += split[i].substring(0, 1).toUpperCase(Locale.ENGLISH) + split[i].substring(1).toLowerCase(Locale.ENGLISH);
		}
		return s;
	}
}
