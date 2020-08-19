package flaxbeard.immersivepetroleum.common.network;

import java.util.function.Function;

import blusunrize.immersiveengineering.common.network.IMessage;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
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
			INSTANCE.send(PacketDistributor.PLAYER.with(()->(ServerPlayerEntity)player), message);
	}
	
	/** Client -> Server */
	public static <MSG> void sendToServer(MSG message){
		if(message!=null)
			INSTANCE.send(PacketDistributor.SERVER.noArg(), message);
	}
	
	/** Sends a packet to everyone in the specified dimension. <pre>Server -> Client</pre> */
	public static <MSG> void sendToDimension(DimensionType dim, MSG message){
		if(message!=null)
			INSTANCE.send(PacketDistributor.DIMENSION.with(()->dim), message);
	}
	
	public static void preInit()
	{
		registerMessage(MessageCloseBook.class, MessageCloseBook::new);
		registerMessage(MessageRotateSchematic.class, MessageRotateSchematic::new);
		//registerMessage(MessageReservoirListSync.class, MessageReservoirListSync::new);
		registerMessage(MessageConsumeBoatFuel.class, MessageConsumeBoatFuel::new);
	}
}
