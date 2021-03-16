package com.github.kei7777.ngword;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface SubCommand {
    public boolean execute(CommandSender sender, Command command, String[] args);
}