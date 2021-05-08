package flaxbeard.immersivepetroleum.common.fluids;

import net.minecraft.world.IWorldReader;

public class CrudeOilFluid extends IPFluid{
	public CrudeOilFluid(){
		super("oil", 1000, 2250);
	}
	
	@Override
	public int getTickRate(IWorldReader p_205569_1_){
		return 10;
	}
}
