package flaxbeard.immersivepetroleum.common.network;

import java.util.function.Supplier;

import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.network.IMessage;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageCloseBook implements IMessage{
	
	public ResourceLocation name;
	
	public MessageCloseBook(ResourceLocation name){
		this.name = name;
	}
	
	public MessageCloseBook(PacketBuffer buf){
		boolean isStrNull = buf.readByte() == 0;
		if(!isStrNull){
			String str=buf.readString(32767); // Because *apparently* PacketBuffer.readString() is client only?!
			this.name = new ResourceLocation(str);
		}else{
			this.name = null;
		}
	}
	
	@Override
	public void toBytes(PacketBuffer buf){
		buf.writeByte(this.name == null ? 0 : 1);
		if(this.name != null){
			String str=this.name.toString();
			buf.writeString(str);
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
				boolean main = !mainItem.isEmpty() && mainItem.getItem() == IEItems.Tools.manual.asItem();
				boolean off = !offItem.isEmpty() && offItem.getItem() == IEItems.Tools.manual.asItem();
				
				if(main || off){
					ItemStack target = main ? mainItem : offItem;
					
					if(this.name == null && ItemNBTHelper.hasKey(target, "lastMultiblock")){
						ItemNBTHelper.remove(target, "lastMultiblock");
						ImmersivePetroleum.log.debug("Removed Multiblock-NBT from {}", target.getDisplayName().getUnformattedComponentText());
					}else if(this.name != null){
						ItemNBTHelper.putString(target, "lastMultiblock", this.name.toString());
						ImmersivePetroleum.log.debug("Added Multiblock-NBT to {} -> {}", target.getDisplayName().getUnformattedComponentText(), this.name.toString());
					}
				}
			}
		});
	}
}
