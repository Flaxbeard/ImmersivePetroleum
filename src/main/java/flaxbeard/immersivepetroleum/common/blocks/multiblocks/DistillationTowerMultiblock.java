package flaxbeard.immersivepetroleum.common.blocks.multiblocks;

import com.mojang.blaze3d.platform.GlStateManager;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPContent.Multiblock;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@SuppressWarnings("deprecation")
public class DistillationTowerMultiblock extends IETemplateMultiblock{
	public static final DistillationTowerMultiblock INSTANCE = new DistillationTowerMultiblock();
	
	private DistillationTowerMultiblock(){
		super(new ResourceLocation(ImmersivePetroleum.MODID, "multiblocks/distillationtower"),
				new BlockPos(0, 0, 0), new BlockPos(0, 1, 3), () -> IPContent.Multiblock.distillationtower.getDefaultState());
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
	public void renderFormedStructure(){
		if(renderStack==null)
			renderStack=new ItemStack(Multiblock.distillationtower);
		
		GlStateManager.pushMatrix();
		{
			// "Undo" the GUI Perspective Transform
			GlStateManager.scaled(16.0, 16.0, 16.0);
			GlStateManager.translated(0.030, 0.355, 0.300);
			GlStateManager.rotated(-225, 0, 1, 0);
			GlStateManager.rotated(-30, 1, 0, 0);
			
			GlStateManager.disableCull();
			ClientUtils.mc().getItemRenderer().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
			GlStateManager.enableCull();
		}
		GlStateManager.popMatrix();
	}
}
