package flaxbeard.immersivepetroleum.common.blocks.multiblocks;

import com.mojang.blaze3d.platform.GlStateManager;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
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
		super(new ResourceLocation(ImmersivePetroleum.MODID, "multiblocks/distillationtower"), new BlockPos(0, 0, 0), new BlockPos(0, 1, 0), () -> null);
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
	
	@OnlyIn(Dist.CLIENT)
	private static ItemStack renderStack;
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderFormedStructure(){
		if(renderStack==null)
			renderStack=new ItemStack(Multiblock.distillationtower);
		
		GlStateManager.disableCull();
		ClientUtils.mc().getItemRenderer().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.enableCull();
	}
}
