package flaxbeard.immersivepetroleum.common.util;

import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.OilWorldInfo;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.ReservoirType;
import flaxbeard.immersivepetroleum.common.IPSaveData;
import flaxbeard.immersivepetroleum.common.util.CommandHandler.IPSubCommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import java.util.ArrayList;
import java.util.Locale;

public class CommandReservoir extends IPSubCommand
{
	@Override
	public String getIdent()
	{
		return "reservoir";
	}

	@Override
	public void perform(CommandHandler handler, MinecraftServer server, ICommandSender sender, String[] args)
	{
		if (args.length > 1)
		{
			DimensionChunkCoords coords = new DimensionChunkCoords(sender.getEntityWorld().provider.getDimension(), (sender.getPosition().getX() >> 4), (sender.getPosition().getZ() >> 4));
			switch (args[1])
			{
				case "list":
					String s = "";
					int i = 0;
					for (ReservoirType res : PumpjackHandler.reservoirList.keySet())
					{
						s += ((i++) > 0 ? ", " : "") + res.name;
					}
					sender.sendMessage(new TextComponentString(s));
					break;
				case "get":
					OilWorldInfo info = PumpjackHandler.getOilWorldInfo(sender.getEntityWorld(), coords.x, coords.z);
					String h = I18n.translateToLocalFormatted("chat.immersivepetroleum.command." + getIdent() + ".get",
							TextFormatting.GOLD + (info.type != null ? info.type.name : "null") + TextFormatting.RESET,
							TextFormatting.GOLD + (info.overrideType != null ? info.overrideType.name : "null") + TextFormatting.RESET,
							TextFormatting.GOLD + (info.current + "/" + info.capacity + " mB") + TextFormatting.RESET);
					for (String g : h.split("<br>"))
					{
						sender.sendMessage(new TextComponentString(g));
					}
					break;
				case "set":
					info = PumpjackHandler.getOilWorldInfo(sender.getEntityWorld(), coords.x, coords.z);

					if (args.length < 3)
					{
						sender.sendMessage(new TextComponentTranslation("chat.immersivepetroleum.command." + getIdent() + ".set.clear", (info.overrideType != null ? info.overrideType.name : "null")));
						info.overrideType = null;
						return;
					}

					ReservoirType reservoir = null;

					for (ReservoirType res : PumpjackHandler.reservoirList.keySet())
					{
						if (res.name.equalsIgnoreCase(args[2]))
							reservoir = res;
					}

					if (reservoir == null)
					{
						sender.sendMessage(new TextComponentTranslation("chat.immersivepetroleum.command." + getIdent() + ".set.invalidReservoir", args[2]));
						return;
					}
					info.overrideType = reservoir;
					sender.sendMessage(new TextComponentTranslation("chat.immersivepetroleum.command." + getIdent() + ".set.sucess", reservoir.name));
					IPSaveData.setDirty(sender.getEntityWorld().provider.getDimension());
					break;
				case "setAmount":
					info = PumpjackHandler.getOilWorldInfo(sender.getEntityWorld(), coords.x, coords.z);

					if (args.length < 3)
					{
						h = I18n.translateToLocal(getHelp(".setAmount"));
						for (String str : h.split("<br>"))
						{
							sender.sendMessage(new TextComponentString(str));
						}
						return;
					}

					int amount = 0;
					try
					{
						amount = Integer.parseInt(args[2].trim());
					} catch (Exception e)
					{
						sender.sendMessage(new TextComponentTranslation("chat.immersivepetroleum.command." + getIdent() + ".setAmount.NFE", args[2].trim()));
						return;
					}
					amount = Math.min(info.capacity, amount);
					amount = Math.max(0, amount);
					info.current = amount;
					sender.sendMessage(new TextComponentTranslation("chat.immersivepetroleum.command." + getIdent() + ".setAmount.sucess", Integer.toString(amount)));
					IPSaveData.setDirty(sender.getEntityWorld().provider.getDimension());
					break;
				case "setCapacity":
					info = PumpjackHandler.getOilWorldInfo(sender.getEntityWorld(), coords.x, coords.z);

					if (args.length < 3)
					{
						h = I18n.translateToLocal(getHelp(".setCapacity"));
						for (String str : h.split("<br>"))
						{
							sender.sendMessage(new TextComponentString(str));
						}
						return;
					}

					amount = 0;
					try
					{
						amount = Integer.parseInt(args[2].trim());
					} catch (Exception e)
					{
						sender.sendMessage(new TextComponentTranslation("chat.immersivepetroleum.command." + getIdent() + ".setCapacity.NFE", args[2].trim()));
						return;
					}
					amount = Math.max(0, amount);
					info.capacity = amount;
					sender.sendMessage(new TextComponentTranslation("chat.immersivepetroleum.command." + getIdent() + ".setCapacity.sucess", Integer.toString(amount)));
					IPSaveData.setDirty(sender.getEntityWorld().provider.getDimension());
					break;
				default:
					sender.sendMessage(new TextComponentTranslation(getHelp("")));
					break;
			}
		}
		else
			sender.sendMessage(new TextComponentTranslation(getHelp("")));

	}

	@Override
	public ArrayList<String> getSubCommands(CommandHandler h, MinecraftServer server, ICommandSender sender, String[] args)
	{

		ArrayList<String> list = new ArrayList<String>();
		// subcommand argument autocomplete
		if (args.length > 1)
		{
			switch (args[0])
			{
				case "set":
					if (args.length > 2)
						break;
					for (MineralMix mineralMix : ExcavatorHandler.mineralList.keySet())
					{
						if (args[1].isEmpty() || mineralMix.name.toLowerCase(Locale.ENGLISH).startsWith(args[1].toLowerCase(Locale.ENGLISH)))
							list.add(mineralMix.name);
					}
					break;
			}
			return list;
		}

		for (String s : new String[]{"list", "get", "set", "setAmount", "setCapacity"})
		{
			if (args.length == 0)
				list.add(s);
			else if (s.toLowerCase(Locale.ENGLISH).startsWith(args[0].toLowerCase(Locale.ENGLISH)))
				list.add(s);
		}
		return list;
	}

	@Override
	public int getPermissionLevel()
	{
		return 4;
	}

}