package flaxbeard.immersivepetroleum.common.blocks.multiblocks;

import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.blocks.metal.PumpjackTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PumpjackMultiblock extends IETemplateMultiblock{
	public static final PumpjackMultiblock INSTANCE = new PumpjackMultiblock();
	
	private PumpjackMultiblock(){
		super(new ResourceLocation(ImmersivePetroleum.MODID, "multiblocks/pumpjack"), new BlockPos(0, 0, 0), new BlockPos(0, 0, 0), () -> null);
	}
	
	@Override
	public float getManualScale(){
		return 12;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRenderFormedStructure(){
		return true;
	}
	
	Object te;
	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderFormedStructure(){
		if(te == null){
			te = new PumpjackTileEntity.TileEntityPumpjackParent();
		}
		
		ImmersivePetroleum.proxy.renderTile((TileEntity) te);
	}
}
