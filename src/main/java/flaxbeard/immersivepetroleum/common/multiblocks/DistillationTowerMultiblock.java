package flaxbeard.immersivepetroleum.common.multiblocks;

import com.mojang.blaze3d.matrix.MatrixStack;

import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPContent.Multiblock;
import flaxbeard.immersivepetroleum.common.util.MCUtil;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DistillationTowerMultiblock extends IETemplateMultiblock{
	public static final DistillationTowerMultiblock INSTANCE = new DistillationTowerMultiblock();
	
	private DistillationTowerMultiblock(){
		super(ResourceUtils.ip("multiblocks/distillationtower"),
				new BlockPos(2, 0, 2), new BlockPos(0, 1, 3), new BlockPos(4, 16, 4), () -> IPContent.Multiblock.distillationtower.getDefaultState());
	}
	
	@Override
	public float getManualScale(){
		return 6;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRenderFormedStructure(){
		return true;
	}
	
	@OnlyIn(Dist.CLIENT)
	private static ItemStack renderStack;
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderFormedStructure(MatrixStack transform, IRenderTypeBuffer buffer){
		if(renderStack == null)
			renderStack = new ItemStack(Multiblock.distillationtower);
		
		// "Undo" the GUI Perspective Transform
		transform.translate(2.5, 0.5, 2.5);
		
		MCUtil.getItemRenderer().renderItem(
				renderStack,
				ItemCameraTransforms.TransformType.NONE,
				0xf000f0,
				OverlayTexture.NO_OVERLAY,
				transform, buffer);
	}
}
