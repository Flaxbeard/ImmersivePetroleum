package flaxbeard.immersivepetroleum.common.blocks;

import flaxbeard.immersivepetroleum.common.IPTileTypes;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.HydrotreaterTileEntity;

public class HydrotreaterBlock extends IPMetalMultiblock<HydrotreaterTileEntity>{
	public HydrotreaterBlock(){
		super("hydrotreater", () -> IPTileTypes.TREATER.get());
	}
}
