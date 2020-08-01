package flaxbeard.immersivepetroleum.common.blocks.multiblocks;

import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.blocks.metal.DistillationTowerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DistillationTowerMultiblock extends IETemplateMultiblock{
	public static final DistillationTowerMultiblock INSTANCE = new DistillationTowerMultiblock();
	
	private DistillationTowerMultiblock(){
		super(new ResourceLocation(ImmersivePetroleum.MODID, "multiblocks/distillationtower"), new BlockPos(0, 0, 0), new BlockPos(0, 0, 0), () -> null);
	}
	
	@Override
	public float getManualScale(){
		return 9;
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
			te = new DistillationTowerTileEntity.TileEntityDistillationTowerParent();
		}
		
		ImmersivePetroleum.proxy.renderTile((TileEntity) te);
	}
}
