package flaxbeard.immersivepetroleum.common.network;

import java.util.function.Supplier;

import blusunrize.immersiveengineering.common.network.IMessage;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.items.ItemProjector;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class RotateSchematicPacket implements IMessage{
	public int rotate;
	public boolean flip;
	
	public RotateSchematicPacket(int rotate, boolean flip){
		this.rotate = rotate;
		this.flip = flip;
	}
	public RotateSchematicPacket(PacketBuffer buf){
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
				PlayerEntity p=con.getSender();
				ItemStack mainItem = p.getHeldItemMainhand();
				ItemStack secondItem = p.getHeldItemOffhand();
				
				boolean main = !mainItem.isEmpty() && mainItem.getItem() == IPContent.itemProjector && ItemNBTHelper.hasKey(mainItem, "multiblock");
				boolean off = !secondItem.isEmpty() && secondItem.getItem() == IPContent.itemProjector && ItemNBTHelper.hasKey(secondItem, "multiblock");
				
				ItemStack target = main ? mainItem : secondItem;
				
				if(main || off){
					ItemProjector.setFlipped(target, this.flip);
					ItemProjector.setRotate(target, this.rotate);
				}
			}
		});
	}
}
