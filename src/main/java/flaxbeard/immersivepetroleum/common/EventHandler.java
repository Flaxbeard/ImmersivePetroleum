package flaxbeard.immersivepetroleum.common;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySampleDrill;
import blusunrize.immersiveengineering.common.items.ItemCoresample;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.common.Config.IPConfig;

public class EventHandler
{
	@SubscribeEvent
	public void onSave(WorldEvent.Save event)
	{
		IPSaveData.setDirty(0);
	}
	
	@SubscribeEvent
	public void onUnload(WorldEvent.Unload event)
	{
		IPSaveData.setDirty(0);
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderLast(RenderWorldLastEvent event)
	{
		GlStateManager.pushMatrix();
		Minecraft mc = Minecraft.getMinecraft();
		if (IPConfig.sample_displayBorder && mc.player != null)
		{
			ItemStack mainItem = mc.player.getHeldItemMainhand();
			ItemStack secondItem = mc.player.getHeldItemOffhand();
			
			boolean main = !mainItem.isEmpty() && mainItem.getItem() instanceof ItemCoresample && ItemNBTHelper.hasKey(mainItem, "coords");
			boolean off = !secondItem.isEmpty() && secondItem.getItem() instanceof ItemCoresample && ItemNBTHelper.hasKey(secondItem, "coords");
			
			boolean chunkBorders = false;
			for(EnumHand hand : EnumHand.values())
				if(OreDictionary.itemMatches(new ItemStack(IEContent.blockMetalDevice1,1, BlockTypes_MetalDevice1.SAMPLE_DRILL.getMeta()), ClientUtils.mc().player.getHeldItem(hand),true))
				{
					chunkBorders = true;
					break;
				}
			if(!chunkBorders && ClientUtils.mc().objectMouseOver!=null && ClientUtils.mc().objectMouseOver.typeOfHit==Type.BLOCK && ClientUtils.mc().world.getTileEntity(ClientUtils.mc().objectMouseOver.getBlockPos()) instanceof TileEntitySampleDrill)
				chunkBorders = true;

			ItemStack target = main ? mainItem : secondItem;
			
			if (!chunkBorders && (main || off))
			{
			
				int[] coords = ItemNBTHelper.getIntArray(target, "coords");
				World world = DimensionManager.getWorld(coords[0]);
				
				if (world.provider.getDimension() == mc.player.world.provider.getDimension())
				{
					EntityPlayer player = mc.player;
					
					renderChunkBorder(coords[1] << 4, coords[2] << 4);
				}
			}
		}
		GlStateManager.popMatrix();
		
	}
	
	@SideOnly(Side.CLIENT)
	public void renderChunkBorder(int chunkX, int chunkZ)
	{
		EntityPlayer player = ClientUtils.mc().player;

		double px = TileEntityRendererDispatcher.staticPlayerX;
		double py = TileEntityRendererDispatcher.staticPlayerY;
		double pz = TileEntityRendererDispatcher.staticPlayerZ;
		int y = Math.min((int)player.posY-2,player.getEntityWorld().getChunkFromBlockCoords(new BlockPos(chunkX, 0, chunkZ)).getLowestHeight());
		float h = (float)Math.max(32, player.posY-y+4);
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer vertexbuffer = tessellator.getBuffer();

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		float r = Lib.COLOUR_F_ImmersiveOrange[0];
		float g = Lib.COLOUR_F_ImmersiveOrange[1];
		float b = Lib.COLOUR_F_ImmersiveOrange[2];
		vertexbuffer.setTranslation(chunkX-px, y+2-py, chunkZ-pz);
		GlStateManager.glLineWidth(5f);
		vertexbuffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		vertexbuffer.pos( 0,0, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos( 0,h, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,0, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,h, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,0,16).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,h,16).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos( 0,0,16).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos( 0,h,16).color(r,g,b,.375f).endVertex();

		vertexbuffer.pos( 0,2, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,2, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos( 0,2, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos( 0,2,16).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos( 0,2,16).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,2,16).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,2, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,2,16).color(r,g,b,.375f).endVertex();
		tessellator.draw();
		vertexbuffer.setTranslation(0, 0, 0);
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void handlePickupItem(RightClickBlock event)
	{
		BlockPos pos = event.getPos();
		IBlockState state = event.getWorld().getBlockState(pos);
		if (state.getBlock() == IEContent.blockMetalDevice1)
		{
			TileEntity te = event.getWorld().getTileEntity(pos);
			if (te instanceof TileEntitySampleDrill)
			{
				TileEntitySampleDrill drill = (TileEntitySampleDrill) te;
				
				if (drill.dummy != 0)
				{
					te = event.getWorld().getTileEntity(pos.add(0, - drill.dummy, 0));
					if (te instanceof TileEntitySampleDrill)
					{
						drill = (TileEntitySampleDrill) te;
					}
				}
				if (!drill.sample.isEmpty())
				{
					if(ItemNBTHelper.hasKey(drill.sample, "coords"))
					{
						int[] coords = ItemNBTHelper.getIntArray(drill.sample, "coords");
						World world = DimensionManager.getWorld(coords[0]);
						
						int amnt = PumpjackHandler.getOilAmount(world, coords[1], coords[2]);
						ItemNBTHelper.setInt(drill.sample, "oil", amnt);
					}
				}
			}
		}

	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	@SideOnly(Side.CLIENT)
	public void handleItemTooltip(ItemTooltipEvent event)
	{
		ItemStack stack = event.getItemStack();
		if (stack.getItem() instanceof ItemCoresample)
		{
			if(ItemNBTHelper.hasKey(stack, "oil"))
			{
				int amnt = ItemNBTHelper.getInt(stack, "oil");
				List<String> tooltip = event.getToolTip();
				if (amnt > 0)
				{
					int est = (amnt / 1000) * 1000;
					String test = new DecimalFormat("#,###.##").format(est);
					tooltip.add(2, I18n.format("chat.immersivepetroleum.info.coresample.oil", test));
				}
				else
				{
					tooltip.add(2, I18n.format("chat.immersivepetroleum.info.coresample.noOil"));
				}
			}
		}
	}

}
