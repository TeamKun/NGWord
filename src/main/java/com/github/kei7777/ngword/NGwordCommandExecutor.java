package com.github.kei7777.ngword;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class NGwordCommandExecutor implements CommandExecutor, TabCompleter {
    public NGwordCommandExecutor(NGWord ngWord) {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "権限がありません。");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("/ngword word 単語一覧。\n/ngword set 全員にNGワードを選択。\n/ngword reset 全員の選択されているNGワードをリセット");
            return true;
        }

        if ("word".equalsIgnoreCase(args[0])) {
            sender.sendMessage(ChatColor.GREEN + "登録単語一覧");
            for(String s : NGWord.words){
                sender.sendMessage(" - " + s);
            }
        } else if ("set".equalsIgnoreCase(args[0])) {
            for(Player p : Bukkit.getOnlinePlayers()){
                Collections.shuffle(NGWord.words);
                NGWord.playerwords.put(p.getUniqueId(), NGWord.words.get(0));
            }
            sender.sendMessage("正常にセットしました。");
        } else if ("reset".equalsIgnoreCase(args[0])) {
            for(Map.Entry<UUID, Hologram> entry : NGWord.holograms.entrySet()){
                entry.getValue().delete();
            }
            NGWord.holograms.clear();
            NGWord.playerwords.clear();
            NGWord.banned.clear();
            sender.sendMessage("正常にリセットしました。");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Arrays.asList("word", "set", "reset");
    }
}
