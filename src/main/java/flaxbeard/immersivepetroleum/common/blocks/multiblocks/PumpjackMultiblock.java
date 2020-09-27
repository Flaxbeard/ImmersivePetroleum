package flaxbeard.immersivepetroleum.common.blocks.multiblocks;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.metal.PumpjackTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

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
	@OnlyIn(Dist.CLIENT)
	List<BakedQuad> list;
	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderFormedStructure(MatrixStack transform, IRenderTypeBuffer buffer){
		if(this.te == null){
			this.te = new PumpjackTileEntity();
			this.te.setOverrideState(IPContent.Multiblock.pumpjack.getDefaultState().with(IEProperties.FACING_HORIZONTAL, Direction.WEST));
		}
		
		if(this.list==null){
			BlockState state=IPContent.Multiblock.pumpjack.getDefaultState().with(IEProperties.FACING_HORIZONTAL, Direction.NORTH);
			IBakedModel model=ClientUtils.mc().getBlockRendererDispatcher().getModelForState(state);
			this.list=model.getQuads(state, null, Utils.RAND, EmptyModelData.INSTANCE);
		}
		
		if(this.list!=null && this.list.size()>0){
			World world=ClientUtils.mc().world;
			if(world!=null){
				Tessellator tes=Tessellator.getInstance();
				BufferBuilder buf=tes.getBuffer();
				
				buf.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
				//buffer.getBuffer(RenderType.getSolid());
				//ClientUtils.renderModelTESRFast(this.list, buf, world, te.getPos(), -1);
				ClientUtils.renderModelTESRFast(this.list, buffer.getBuffer(RenderType.getSolid()), transform, 15);
				
				transform.push();
				transform.translate(0.0, -1.0, -1);
				ImmersivePetroleum.proxy.renderTile(this.te, buffer.getBuffer(RenderType.getSolid()), transform, buffer);
				transform.pop();
				
				transform.push();
				transform.translate(1.0, 1.0, 1.0);
				ClientUtils.bindAtlas();
				tes.draw();
				transform.pop();
			}
		}
	}
}
