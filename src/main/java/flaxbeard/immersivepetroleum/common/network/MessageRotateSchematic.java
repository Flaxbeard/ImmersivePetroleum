package flaxbeard.immersivepetroleum.common.network;

import java.util.function.Supplier;

import blusunrize.immersiveengineering.common.network.IMessage;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.immersivepetroleum.common.IPContent.Items;
import flaxbeard.immersivepetroleum.common.items.ProjectorItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageRotateSchematic implements IMessage{
	public int rotate;
	public boolean flip;
	
	public MessageRotateSchematic(int rotate, boolean flip){
		this.rotate = rotate;
		this.flip = flip;
	}
	public MessageRotateSchematic(PacketBuffer buf){
		this.rotate = buf.readByte();
		this.flip = buf.readBoolean();
	}
	
	@Override
	public void toBytes(PacketBuffer buf){
		buf.writeByte(this.rotate);
		buf.writeBoolean(this.flip);
	}
	
	@Override
	public void process(Supplier<Context> context){
		context.get().enqueueWork(()->{
			Context con=context.get();
			
			if(con.getDirection().getReceptionSide()==LogicalSide.SERVER && con.getSender()!=null){
				PlayerEntity player=con.getSender();
				ItemStack mainItem = player.getHeldItemMainhand();
				ItemStack secondItem = player.getHeldItemOffhand();
				
				boolean main = !mainItem.isEmpty() && mainItem.getItem() == Items.projector && ItemNBTHelper.hasKey(mainItem, "multiblock");
				boolean off = !secondItem.isEmpty() && secondItem.getItem() == Items.projector && ItemNBTHelper.hasKey(secondItem, "multiblock");
				
				
				if(main || off){
					ItemStack target = main ? mainItem : secondItem;
					
					ProjectorItem.setFlipped(target, this.flip);
					ProjectorItem.setRotate(target, this.rotate);
				}
			}
		});
	}
}
