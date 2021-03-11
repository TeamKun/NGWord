package com.github.kei7777.ngword;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Message {
    public static void sendSuccessMsg(CommandSender p, String msg) {
        p.sendMessage(ChatColor.GREEN + msg);
    }

    public static void sendFailureMsg(CommandSender p, String msg) {
        p.sendMessage(ChatColor.RED + msg);
    }
}
