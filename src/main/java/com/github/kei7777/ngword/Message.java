package com.github.kei7777.ngword;

import org.bukkit.ChatColor;

public class Message {
    public static String SuccessMsg(String msg) {
        return ChatColor.GREEN + msg;
    }

    public static String FailureMsg(String msg) {
        return ChatColor.RED + msg;
    }
}
