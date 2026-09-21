package com.gatoborrachon.realisticfinitefluids.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandFluidKillswitch extends CommandBase {
	
	public static int oceanKillswitchTicks = 0;

    @Override
    public String getName() {
        return "rffkillswitch";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/rffkillswitch [segundos]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // 0 = cualquier jugador puede correrlo, 2 = solo ops
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        int seconds = 1200; //200 ticks - 10 segundos // 1200 ticks - 60 segunods
        if (args.length > 0) {
            seconds = CommandBase.parseInt(args[0], 1, 3600)*20; //segundos * ticks por segundo
        }

        oceanKillswitchTicks = seconds;
        sender.sendMessage(new TextComponentString(
            "§eFluidos oceánicos desactivados temporalmente por " + seconds/20 + " segundos."));
    }
}
