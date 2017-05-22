package flaxbeard.immersivepetroleum.common;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import blusunrize.immersiveengineering.common.IEContent;
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
		if (IPConfig.sample_displayBorder && mc.thePlayer != null)
		{
			ItemStack mainItem = mc.thePlayer.getHeldItemMainhand();
			ItemStack secondItem = mc.thePlayer.getHeldItemOffhand();
			
			boolean main = mainItem != null && mainItem.getItem() instanceof ItemCoresample && ItemNBTHelper.hasKey(mainItem, "coords");
			boolean off = secondItem != null && secondItem.getItem() instanceof ItemCoresample && ItemNBTHelper.hasKey(secondItem, "coords");
			ItemStack target = main ? mainItem : secondItem;
			
			if (main || off)
			{
			
				int[] coords = ItemNBTHelper.getIntArray(target, "coords");
				World world = DimensionManager.getWorld(coords[0]);
				
				if (world.provider.getDimension() == mc.thePlayer.worldObj.provider.getDimension())
				{
					EntityPlayer player = mc.thePlayer;
					
					WorldBorder wb = player.worldObj.getWorldBorder();
		
					double x = wb.getCenterX();
					double z = wb.getCenterZ();
					int range = wb.getSize();
					int warn = wb.getWarningDistance();
					
					wb.setCenter(player.posX, player.posZ);
					wb.setSize(8);
					wb.setWarningDistance(500);
					
					GlStateManager.translate((coords[1] << 4) + 8, 0, (coords[2] << 4) + 8);
					mc.renderGlobal.renderWorldBorder(player, event.getPartialTicks());
					
					wb.setCenter(x, z);
					wb.setSize(range);
					wb.setWarningDistance(warn);
				}
			}
		}
		GlStateManager.popMatrix();
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
				if (drill.sample != null)
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
