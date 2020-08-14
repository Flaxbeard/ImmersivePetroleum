package flaxbeard.immersivepetroleum.common.util.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.OilWorldInfo;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.ReservoirType;
import flaxbeard.immersivepetroleum.common.IPSaveData;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ColumnPosArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class ReservoirCommand{
	private ReservoirCommand(){}
	
	// new TranslationTextComponent("chat.immersivepetroleum.command.reservoir.setCapacity.NFE", numberString)
	
	public static LiteralArgumentBuilder<CommandSource> create(){
		LiteralArgumentBuilder<CommandSource> lab=Commands.literal("reservoir")
				.requires(source->source.hasPermissionLevel(4));
		
		lab.then(Commands.literal("list")
				.executes(source->list(source.getSource().asPlayer())));
		
		lab.then(Commands.literal("get")
				.executes(source->get(source.getSource().asPlayer())));
		
		lab.then(setReservoir());
		
		lab.then(Commands.literal("setAmount")
				.then(Commands.argument("amount", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
						.executes(context->setAmount(context.getSource().asPlayer(), context.getArgument("amount", Integer.class)))));
		
		lab.then(Commands.literal("setCapacity")
				.then(Commands.argument("capacity", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
						.executes(context->setCapacity(context.getSource().asPlayer(), context.getArgument("capacity", Integer.class)))));
		
		return lab;
	}
	
	static int list(ServerPlayerEntity playerEntity){
		String s = "";
		int i = 0;
		for(ReservoirType res:PumpjackHandler.reservoirList.keySet()){
			s += ((i++) > 0 ? ", " : "") + res.name;
		}
		
		playerEntity.sendMessage(new StringTextComponent(s));
		return Command.SINGLE_SUCCESS;
	}
	
	static int get(ServerPlayerEntity playerEntity){
		OilWorldInfo info = getOilWorldInfo(playerEntity);
		
		String h = I18n.format("chat.immersivepetroleum.command.reservoir.get",
				TextFormatting.GOLD + (info.type != null ? info.type.name : "null") + TextFormatting.RESET,
				TextFormatting.GOLD + (info.overrideType != null ? info.overrideType.name : "null") + TextFormatting.RESET,
				TextFormatting.GOLD + (info.current + "/" + info.capacity + " mB") + TextFormatting.RESET);
		
		for(String g:h.split("<br>")){
			playerEntity.sendMessage(new StringTextComponent(g));
		}
		
		return Command.SINGLE_SUCCESS;
	}
	
	static LiteralArgumentBuilder<CommandSource> setReservoir(){
		RequiredArgumentBuilder<CommandSource, String> nameArg=Commands.argument("name", StringArgumentType.string());
		nameArg.suggests((context, builder)->{
			return ISuggestionProvider.suggest(PumpjackHandler.reservoirList.keySet().stream().map(type->type.name), builder);
		}).executes(command->{
			ServerPlayerEntity player=command.getSource().asPlayer();
			setReservoir(command, player.getPosition().getX()>>4, player.getPosition().getZ()>>4);
			return Command.SINGLE_SUCCESS;
		});
		nameArg.then(Commands.argument("location", ColumnPosArgument.columnPos())
				.executes(command->{
					ColumnPos pos=command.getArgument("location", ColumnPos.class);
					setReservoir(command, pos.x, pos.z);
					return Command.SINGLE_SUCCESS;
				}));
		
		LiteralArgumentBuilder<CommandSource> set = Commands.literal("setTest");
		set.then(nameArg);
		return set;
	}
	
	static void setReservoir(CommandContext<CommandSource> context, int xChunk, int zChunk){
		CommandSource sender=context.getSource();
		OilWorldInfo info=PumpjackHandler.getOilWorldInfo(sender.getWorld(), xChunk, zChunk);
		
		String name=context.getArgument("name", String.class);
		ReservoirType reservoir = null;
		for(ReservoirType res:PumpjackHandler.reservoirList.keySet())
			if(res.name.equalsIgnoreCase(name))
				reservoir = res;
		
		if(reservoir == null){
			sender.sendFeedback(new TranslationTextComponent("chat.immersivepetroleum.command.reservoir.set.invalidReservoir", name), true);
			return;
		}
		
		info.overrideType=reservoir;
		IPSaveData.setDirty();
		sender.sendFeedback(new TranslationTextComponent("chat.immersivepetroleum.command.reservoir.set.sucess", reservoir.name), true);
	}
	
	static int set(ServerPlayerEntity playerEntity, String name){
		OilWorldInfo info = getOilWorldInfo(playerEntity);
		
		ReservoirType reservoir = null;
		for(ReservoirType res:PumpjackHandler.reservoirList.keySet())
			if(res.name.equalsIgnoreCase(name))
				reservoir = res;
		
		if(reservoir == null){
			playerEntity.sendMessage(new TranslationTextComponent("chat.immersivepetroleum.command.reservoir.set.invalidReservoir", name));
			return Command.SINGLE_SUCCESS;
		}
		
		info.overrideType = reservoir;
		playerEntity.sendMessage(new TranslationTextComponent("chat.immersivepetroleum.command.reservoir.set.sucess", reservoir.name));
		IPSaveData.setDirty();
		
		return Command.SINGLE_SUCCESS;
	}
	
	static int setAmount(ServerPlayerEntity playerEntity, int amount){
		OilWorldInfo info = getOilWorldInfo(playerEntity);
		
		amount = Math.min(info.capacity, Math.max(0, amount)); // Clamping action; Prevents amount from going negative or over the capacity.
		
		// TODO Maybe add a message to inform the player that the value has been clamped?
		
		info.current = amount;
		playerEntity.sendMessage(new TranslationTextComponent("chat.immersivepetroleum.command.reservoir.setAmount.sucess", Integer.toString(amount)));
		IPSaveData.setDirty();
		
		return Command.SINGLE_SUCCESS;
	}
	
	static int setCapacity(ServerPlayerEntity playerEntity, int amount){
		OilWorldInfo info = getOilWorldInfo(playerEntity);
		
		amount = Math.max(0, amount);
		
		info.capacity = amount;
		playerEntity.sendMessage(new TranslationTextComponent("chat.immersivepetroleum.command.reservoir.setCapacity.sucess", Integer.toString(amount)));
		IPSaveData.setDirty();
		
		return Command.SINGLE_SUCCESS;
	}
	
	static OilWorldInfo getOilWorldInfo(ServerPlayerEntity playerEntity){
		ChunkPos coords=new ChunkPos(playerEntity.getPosition());
		return PumpjackHandler.getOilWorldInfo(playerEntity.getEntityWorld(), coords.x, coords.z);
	}
	
	static ChunkPos getChunkCoords(ServerPlayerEntity playerEntity){
		return new ChunkPos(playerEntity.getPosition());
	}
	
	@Deprecated // TODO Maybe add this somewhere again?
	static String getHelp(String subIdent){
		return "chat.immersivepetroleum.command.reservoir" + subIdent + ".help";
	}
}
