package flaxbeard.immersivepetroleum.common.network;

import java.util.function.Function;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class IPPacketHandler{
	public static final String NET_VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(ImmersivePetroleum.MODID, "main"))
			.networkProtocolVersion(() -> NET_VERSION)
			.serverAcceptedVersions(NET_VERSION::equals)
			.clientAcceptedVersions(NET_VERSION::equals)
			.simpleChannel();
	
	public static void preInit(){
		registerMessage(MessageDebugSync.class, MessageDebugSync::new);
		registerMessage(MessageConsumeBoatFuel.class, MessageConsumeBoatFuel::new);
		registerMessage(MessageProjectorSync.class, MessageProjectorSync::new);
	}
	
	private static int id = 0;
	public static <T extends INetMessage> void registerMessage(Class<T> type, Function<PacketBuffer, T> decoder){
		INSTANCE.registerMessage(id++, type, INetMessage::toBytes, decoder, (t, ctx) -> {
			t.process(ctx);
			ctx.get().setPacketHandled(true);
		});
	}
	
	/**
	 * Sends a server message directly to the player. Will not do anything if
	 * the provided instance is not a {@link ServerPlayerEntity} instance
	 * 
	 * @param serverPlayer The player to send to
	 * @param message The message to send
	 */
	public static <MSG> void sendToPlayer(PlayerEntity player, MSG message){
		if(message == null || !(player instanceof ServerPlayerEntity))
			return;
		
		INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), message);
	}
	
	/** Client -> Server */
	public static <MSG> void sendToServer(MSG message){
		if(message == null)
			return;
		
		INSTANCE.send(PacketDistributor.SERVER.noArg(), message);
	}
	
	/**
	 * Sends a packet to everyone in the specified dimension.
	 * 
	 * <pre>
	 * Server -> Client
	 * </pre>
	 */
	public static <MSG> void sendToDimension(RegistryKey<World> dim, MSG message){
		if(message == null)
			return;
		
		INSTANCE.send(PacketDistributor.DIMENSION.with(() -> dim), message);
	}
	
	public static <MSG> void sendAll(MSG message){
		if(message == null)
			return;
		
		INSTANCE.send(PacketDistributor.ALL.noArg(), message);
	}
}
