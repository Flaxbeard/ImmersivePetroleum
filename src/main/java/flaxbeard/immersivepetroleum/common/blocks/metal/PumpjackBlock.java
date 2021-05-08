package flaxbeard.immersivepetroleum.common.blocks.metal;

import flaxbeard.immersivepetroleum.common.IPTileTypes;
import flaxbeard.immersivepetroleum.common.blocks.IPMetalMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.PumpjackTileEntity;

public class PumpjackBlock extends IPMetalMultiblock<PumpjackTileEntity>{
	public PumpjackBlock(){
		super("pumpjack", () -> IPTileTypes.PUMP.get());
	}
}
