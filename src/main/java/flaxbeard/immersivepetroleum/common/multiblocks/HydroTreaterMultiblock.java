package flaxbeard.immersivepetroleum.common.multiblocks;

import com.mojang.blaze3d.matrix.MatrixStack;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPContent.Multiblock;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HydroTreaterMultiblock extends IETemplateMultiblock{
	public static final HydroTreaterMultiblock INSTANCE = new HydroTreaterMultiblock();
	
	public HydroTreaterMultiblock(){
		super(new ResourceLocation(ImmersivePetroleum.MODID, "multiblocks/hydrotreater"),
				new BlockPos(1, 0, 2), new BlockPos(1, 1, 3), new BlockPos(3, 3, 4),
				() -> IPContent.Multiblock.hydrotreater.getDefaultState());
	}
	
	@Override
	public float getManualScale(){
		return 12.0F;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRenderFormedStructure(){
		return true;
	}
	
	@OnlyIn(Dist.CLIENT)
	private static ItemStack renderStack;
	
	@Override
	public void renderFormedStructure(MatrixStack transform, IRenderTypeBuffer buffer){
		if(renderStack == null)
			renderStack = new ItemStack(Multiblock.hydrotreater);
		
		// "Undo" the GUI Perspective Transform
		transform.translate(1.5, 0.5, 2.5);
		
		ClientUtils.mc().getItemRenderer().renderItem(
				renderStack,
				ItemCameraTransforms.TransformType.NONE,
				0xf000f0,
				OverlayTexture.NO_OVERLAY,
				transform, buffer);
	}
}
