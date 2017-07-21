package flaxbeard.immersivepetroleum.common.util;

import java.util.ArrayList;
import java.util.Locale;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import blusunrize.immersiveengineering.api.Lib;
import flaxbeard.immersivepetroleum.common.util.CommandHandler.IPSubCommand;

public class CommandHelp extends IPSubCommand
{
	@Override
	public String getIdent()
	{
		return "help";
	}

	@Override
	public void perform(CommandHandler handler, MinecraftServer server, ICommandSender sender, String[] args)
	{
		if(args.length>1)
		{
			String sub = "";
			for(int i=2;i<args.length;i++)
				sub += "."+args[i];
			for(IPSubCommand com : handler.commands)
			{
				if(com.getIdent().equalsIgnoreCase(args[1]))
				{
					String h = I18n.translateToLocal(com.getHelp(sub));
					for(String s : h.split("<br>"))
						sender.addChatMessage(new TextComponentString(s));
				}
			}
		}
		else
		{
			String h = I18n.translateToLocal(getHelp(""));
			for(String s : h.split("<br>"))
				sender.addChatMessage(new TextComponentString(s));
			String sub = "";
			int i=0;
			for(IPSubCommand com : handler.commands)
				sub += ((i++)>0?", ":"")+com.getIdent();
			sender.addChatMessage(new TextComponentTranslation(Lib.CHAT_COMMAND+"available",sub));
		}
	}

	@Override
	public ArrayList<String> getSubCommands(CommandHandler h, MinecraftServer server, ICommandSender sender, String[] args)
	{
		ArrayList<String> list = new ArrayList<>();
		for(IPSubCommand sub : h.commands)
			if(sub!=this && sender.canCommandSenderUseCommand(sub.getPermissionLevel(),h.getCommandName()))
			{
				if(args.length==1)
				{
					if(args[0].isEmpty() || sub.getIdent().startsWith(args[0].toLowerCase(Locale.ENGLISH)))
						list.add(sub.getIdent());
				}
				else if(sub.getIdent().equalsIgnoreCase(args[0]))
				{
					String[] redArgs = new String[args.length-1];
					System.arraycopy(args, 1, redArgs, 0, redArgs.length);
					ArrayList<String> subCommands = sub.getSubCommands(h, server, sender, redArgs);
					if(subCommands!=null)
						list.addAll(subCommands);
				}
			}
		return list;
	}

	@Override
	public int getPermissionLevel()
	{
		return 0;
	}
}