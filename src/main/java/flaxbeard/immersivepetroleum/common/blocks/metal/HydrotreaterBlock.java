package flaxbeard.immersivepetroleum.common.blocks.metal;

import flaxbeard.immersivepetroleum.common.IPTileTypes;
import flaxbeard.immersivepetroleum.common.blocks.IPMetalMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.HydrotreaterTileEntity;

public class HydrotreaterBlock extends IPMetalMultiblock<HydrotreaterTileEntity>{
	public HydrotreaterBlock(){
		super("hydrotreater", () -> IPTileTypes.TREATER.get());
	}
}
