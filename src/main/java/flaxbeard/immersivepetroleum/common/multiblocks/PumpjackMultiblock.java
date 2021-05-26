package flaxbeard.immersivepetroleum.common.multiblocks;

import java.util.List;
import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.PumpjackTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

public class PumpjackMultiblock extends IETemplateMultiblock{
	private static final Random RAND = new Random();
	public static final PumpjackMultiblock INSTANCE = new PumpjackMultiblock();
	
	private PumpjackMultiblock(){
		super(new ResourceLocation(ImmersivePetroleum.MODID, "multiblocks/pumpjack"),
				new BlockPos(1, 0, 0), new BlockPos(1, 1, 4), new BlockPos(3, 4, 6), () -> IPContent.Multiblock.pumpjack.getDefaultState());
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
			this.te.setOverrideState(IPContent.Multiblock.pumpjack.getDefaultState().with(IEProperties.FACING_HORIZONTAL, Direction.NORTH));
		}
		
		if(this.list == null){
			BlockState state = IPContent.Multiblock.pumpjack.getDefaultState().with(IEProperties.FACING_HORIZONTAL, Direction.NORTH);
			IBakedModel model = ClientUtils.mc().getBlockRendererDispatcher().getModelForState(state);
			this.list = model.getQuads(state, null, RAND, EmptyModelData.INSTANCE);
		}
		
		if(this.list != null && this.list.size() > 0){
			World world = ClientUtils.mc().world;
			if(world != null){
				transform.push();
				transform.translate(1, 0, 0);
				RenderUtils.renderModelTESRFast(this.list, buffer.getBuffer(RenderType.getSolid()), transform, 0xF000F0, OverlayTexture.NO_OVERLAY);
				
				transform.push();
				transform.rotate(rot);
				transform.translate(-2, -1, -1);
				ImmersivePetroleum.proxy.renderTile(this.te, buffer.getBuffer(RenderType.getSolid()), transform, buffer);
				transform.pop();
				
				transform.pop();
			}
		}
	}
	
	final Quaternion rot = new Quaternion(new Vector3f(0F, 1F, 0F), 90, true);
}
