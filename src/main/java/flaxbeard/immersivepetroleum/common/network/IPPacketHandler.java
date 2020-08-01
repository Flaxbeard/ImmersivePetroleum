package flaxbeard.immersivepetroleum.common.network;

import java.util.function.Function;

import blusunrize.immersiveengineering.common.network.IMessage;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class IPPacketHandler
{
	public static final String NET_VERSION="1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(ImmersivePetroleum.MODID, "main"))
			.networkProtocolVersion(()->NET_VERSION)
			.serverAcceptedVersions(NET_VERSION::equals)
			.clientAcceptedVersions(NET_VERSION::equals)
			.simpleChannel();
	
	private static int id=0;
	public static <T extends IMessage> void registerMessage(Class<T> type, Function<PacketBuffer, T> decoder){
		INSTANCE.registerMessage(id++, type, IMessage::toBytes, decoder, (t, ctx)->{
			t.process(ctx);
			ctx.get().setPacketHandled(true);
		});
	}
	
	/**
	 * Sends a server message directly to the player.
	 * Will not do anything if the provided instance is not a {@link ServerPlayerEntity} instance
	 * 
	 * @param serverPlayer The player to send to
	 * @param message The message to send
	 */
	public static <MSG> void sendToPlayer(PlayerEntity player, MSG message){
		if(player instanceof ServerPlayerEntity)
			IPPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(()->(ServerPlayerEntity)player), message);
	}
	
	public static void preInit()
	{
		registerMessage(CloseBookPacket.class, CloseBookPacket::new);
		registerMessage(RotateSchematicPacket.class, RotateSchematicPacket::new);
		//registerMessage(MessageReservoirListSync.class, MessageReservoirListSync::new);
		registerMessage(ConsumeBoatFuelPacket.class, ConsumeBoatFuelPacket::new);
	}
}
