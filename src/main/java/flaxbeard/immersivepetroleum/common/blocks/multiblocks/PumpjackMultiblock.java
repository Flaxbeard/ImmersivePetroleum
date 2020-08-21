package flaxbeard.immersivepetroleum.common.blocks.multiblocks;

import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.metal.PumpjackTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PumpjackMultiblock extends IETemplateMultiblock{
	public static final PumpjackMultiblock INSTANCE = new PumpjackMultiblock();
	
	private PumpjackMultiblock(){
		super(new ResourceLocation(ImmersivePetroleum.MODID, "multiblocks/pumpjack"),
				new BlockPos(1, 0, 0), new BlockPos(1, 1, 4), () -> IPContent.Multiblock.pumpjack.getDefaultState());
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
	
	@OnlyIn(Dist.CLIENT)
	private PumpjackTileEntity te;
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderFormedStructure(){
		if(this.te == null){
			this.te = new PumpjackTileEntity();
		}
		
		ImmersivePetroleum.proxy.renderTile(this.te);
	}
}
