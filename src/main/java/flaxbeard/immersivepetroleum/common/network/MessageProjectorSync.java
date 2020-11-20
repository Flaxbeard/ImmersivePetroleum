package flaxbeard.immersivepetroleum.common.network;

import java.util.function.Supplier;

import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.util.projector.Settings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageProjectorSync implements INetMessage{
	
	public static void sendToServer(Settings settings, Hand hand){
		IPPacketHandler.sendToServer(new MessageProjectorSync(settings, hand, true));
	}
	
	public static void sendToClient(PlayerEntity player, Settings settings, Hand hand){
		IPPacketHandler.sendToPlayer(player, new MessageProjectorSync(settings, hand, false));
	}
	
	boolean forServer;
	CompoundNBT nbt;
	Hand hand;
	
	public MessageProjectorSync(Settings settings, Hand hand, boolean toServer){
		this(settings.toNbt(), hand, toServer);
	}
	
	public MessageProjectorSync(CompoundNBT nbt, Hand hand, boolean toServer){
		this.nbt = nbt;
		this.forServer = toServer;
		this.hand = hand;
	}
	
	public MessageProjectorSync(PacketBuffer buf){
		this.nbt = buf.readCompoundTag();
		this.forServer = buf.readBoolean();
		this.hand = Hand.values()[buf.readByte()];
	}
	
	@Override
	public void toBytes(PacketBuffer buf){
		buf.writeCompoundTag(this.nbt);
		buf.writeBoolean(this.forServer);
		buf.writeByte(this.hand.ordinal());
	}
	
	@Override
	public void process(Supplier<Context> context){
		context.get().enqueueWork(() -> {
			Context con = context.get();
			
			if(con.getDirection().getReceptionSide() == getSide() && con.getSender() != null){
				PlayerEntity player = con.getSender();
				ItemStack held = player.getHeldItem(this.hand);
				
				if(!held.isEmpty() && held.getItem() == IPContent.Items.projector){
					Settings settings = new Settings(this.nbt);
					settings.applyTo(held);
				}
			}
		});
	}
	
	LogicalSide getSide(){
		return this.forServer ? LogicalSide.SERVER : LogicalSide.CLIENT;
	}
}
