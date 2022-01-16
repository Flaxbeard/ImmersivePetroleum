package flaxbeard.immersivepetroleum.common.gui;

import flaxbeard.immersivepetroleum.common.blocks.tileentities.HydrotreaterTileEntity;
import flaxbeard.immersivepetroleum.common.multiblocks.HydroTreaterMultiblock;
import net.minecraft.entity.player.PlayerInventory;

public class HydrotreaterContainer extends MultiblockAwareGuiContainer<HydrotreaterTileEntity>{
	public HydrotreaterContainer(int id, PlayerInventory playerInventory, final HydrotreaterTileEntity tile){
		super(tile, id, HydroTreaterMultiblock.INSTANCE);
	}
}
