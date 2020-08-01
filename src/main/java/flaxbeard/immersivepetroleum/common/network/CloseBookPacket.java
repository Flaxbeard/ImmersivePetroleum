package flaxbeard.immersivepetroleum.common.network;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.network.IMessage;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class CloseBookPacket implements IMessage{
	
	public ResourceLocation name;
	
	public CloseBookPacket(ResourceLocation name){
		this.name = name;
	}
	
	public CloseBookPacket(PacketBuffer buf){
		boolean isStrNull = buf.readByte() == 0;
		if(!isStrNull){
			int len=((int)buf.readShort())&0xFFFF;
			byte[] array=new byte[len];
			buf.readBytes(array);
			this.name = new ResourceLocation(new String(array, StandardCharsets.UTF_8));
		}else{
			this.name = null;
		}
	}
	
	@Override
	public void toBytes(PacketBuffer buf){
		buf.writeByte(this.name == null ? 0 : 1);
		if(this.name != null){
			String str=this.name.toString();
			
			buf.writeShort(str.length());
			buf.writeByteArray(str.getBytes(StandardCharsets.UTF_8));
		}
	}
	
	@Override
	public void process(Supplier<Context> context){
		context.get().enqueueWork(()->{
			Context con=context.get();
			
			if(con.getDirection().getReceptionSide()==LogicalSide.SERVER && con.getSender()!=null){
				PlayerEntity p=con.getSender();
				ItemStack mainItem = p.getHeldItemMainhand();
				ItemStack offItem = p.getHeldItemOffhand();
				
				// "IEItems.Tools.manual" is just a guess
				boolean main = !mainItem.isEmpty() && mainItem.getItem() == IEItems.Tools.manual.asItem();//IEContent.itemTool && mainItem.getItemDamage() == 3;
				boolean off = !offItem.isEmpty() && offItem.getItem() == IEItems.Tools.manual.asItem();//IEContent.itemTool && offItem.getItemDamage() == 3;
				ItemStack target = main ? mainItem : offItem;
				
				if(main || off){
					if(this.name == null && ItemNBTHelper.hasKey(target, "lastMultiblock")){
						ItemNBTHelper.remove(target, "lastMultiblock");
					}else if(this.name != null){
						ItemNBTHelper.putString(target, "lastMultiblock", this.name.toString());
					}
				}
			}
		});
	}
	
	/*
	public static class Handler implements INetHandler<CloseBookPacket, IMessage>{
		
		@Override
		public IMessage onMessage(CloseBookPacket message, MessageContext ctx){
			ServerPlayerEntity player = ctx.getServerHandler().player;
			DimensionManager.getWorld(player.world.provider.getDimension()).addScheduledTask(new DoSync(player, message.name));
			
			return null;
		}
		
	}
	
	private static class DoSync implements Runnable{
		private PlayerEntity p;
		private ResourceLocation name;
		
		public DoSync(PlayerEntity p, ResourceLocation name){
			this.p = p;
			this.name = name;
		}
		
		@Override
		public void run(){
			if(p != null){
				ItemStack mainItem = p.getHeldItemMainhand();
				ItemStack offItem = p.getHeldItemOffhand();
				
				// "IEItems.Tools.manual" is just a guess
				boolean main = !mainItem.isEmpty() && mainItem.getItem() == IEItems.Tools.manual.asItem();//IEContent.itemTool && mainItem.getItemDamage() == 3;
				boolean off = !offItem.isEmpty() && offItem.getItem() == IEItems.Tools.manual.asItem();//IEContent.itemTool && offItem.getItemDamage() == 3;
				ItemStack target = main ? mainItem : offItem;
				
				if(main || off){
					if(name == null && ItemNBTHelper.hasKey(target, "lastMultiblock")){
						ItemNBTHelper.remove(target, "lastMultiblock");
					}else if(name != null){
						ItemNBTHelper.putString(target, "lastMultiblock", name.toString());
					}
				}
			}
		}
	}*/
}
