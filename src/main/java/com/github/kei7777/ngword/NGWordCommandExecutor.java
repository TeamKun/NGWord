package com.github.kei7777.ngword;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NGWordCommandExecutor implements CommandExecutor, TabCompleter {
    List<String> subCmdList = Arrays.asList("word", "random", "set", "reset", "pardon");

    public NGWordCommandExecutor(NGWord ngWord) {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;

        String subcmd = args[0].toLowerCase();
        switch (subcmd) {
            case "word":
                sender.sendMessage(Message.SuccessMsg("登録単語一覧"));
                for (String word : NGWord.words) {
                    sender.sendMessage(ChatColor.DARK_GREEN + " - " + word);
                }
                return true;
            case "random":
                for (Player p : Bukkit.getOnlinePlayers()) {
                    Collections.shuffle(NGWord.words);
                    NGWord.configuredNGWord.put(p.getUniqueId(), NGWord.words.get(0));
                    Bukkit.getPluginManager().callEvent(new NGWordUpdateEvent(p, true));
                }
                sender.sendMessage(Message.SuccessMsg("正常にランダムなNGワードを配りました."));
                return true;
            case "set": {
                if (args.length < 3) {
                    sender.sendMessage(Message.FailureMsg("/ngword set <player> <word>"));
                    return true;
                }
                String name = args[1];
                Player p = Bukkit.getPlayer(name);
                if (p == null) {
                    sender.sendMessage(Message.FailureMsg(name + "は存在しません."));
                    return true;
                }
                String word = Stream.of(args).skip(2).collect(Collectors.joining(" "));
                NGWord.configuredNGWord.put(p.getUniqueId(), word);
                Bukkit.getPluginManager().callEvent(new NGWordUpdateEvent(p, true));
                sender.sendMessage(Message.SuccessMsg("正常にセットしました."));
                return true;
            }
            case "reset":
                NGWord.holograms.values().forEach(Hologram::delete);
                NGWord.holograms.clear();
                NGWord.configuredNGWord.clear();
                NGWord.bannedPlayers.clear();
                sender.sendMessage(Message.SuccessMsg("正常にリセットしました."));
                return true;
            case "pardon":
                if (args.length < 2) {
                    sender.sendMessage(Message.FailureMsg("/ngword pardon <player>"));
                    return true;
                }

                if (args[1].equalsIgnoreCase("@a")) {
                    NGWord.bannedPlayers.clear();
                    sender.sendMessage(Message.SuccessMsg("全てのプレイヤーのBANを解除しました."));
                    return true;
                }

                String name = args[1];
                NGWord.bannedPlayers.removeIf(x -> x.getName().equalsIgnoreCase(name));
                sender.sendMessage(Message.SuccessMsg(name + "のBANを解除しました."));
                return true;
        }
        return false;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1)
            return subCmdList.stream()
                    .filter(e -> e.startsWith(args[0]))
                    .collect(Collectors.toList());

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "pardon":
                    return Stream.concat(NGWord.bannedPlayers.stream()
                            .map(Player::getName), Stream.of("@a"))
                            .filter(e -> e.startsWith(args[1]))
                            .collect(Collectors.toList());
                case "set":
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(e -> e.startsWith(args[1]))
                            .collect(Collectors.toList());
                case "reset":
                    return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }
}