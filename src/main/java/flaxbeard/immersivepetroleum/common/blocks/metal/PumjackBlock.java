package flaxbeard.immersivepetroleum.common.blocks.metal;

import flaxbeard.immersivepetroleum.common.blocks.IPMetalMultiblock;

public class PumjackBlock extends IPMetalMultiblock{
	public PumjackBlock(){
		super("pumpjack",
				()->PumpjackTileEntity.TYPE,
				()->PumpjackTileEntity.PumpjackParentTileEntity.TYPE);
	}
}
