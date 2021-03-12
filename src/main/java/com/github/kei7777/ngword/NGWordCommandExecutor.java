package com.github.kei7777.ngword;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NGWordCommandExecutor implements CommandExecutor, TabCompleter {
    List<String> subCmdList = Arrays.asList("list", "pardon", "random", "reload", "reset", "set");
    NGWord plugin;

    public NGWordCommandExecutor(NGWord ngWord) {
        this.plugin = ngWord;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;

        String subCmd = args[0].toLowerCase();
        switch (subCmd) {
            case "list":
                sender.sendMessage(Message.SuccessMsg("登録単語一覧"));
                for (String word : NGWord.words) {
                    sender.sendMessage(ChatColor.DARK_GREEN + " - " + word);
                }
                return true;
            case "pardon":
                if (args.length < 2) {
                    sender.sendMessage(Message.FailureMsg("/ngword pardon <player>"));
                    return true;
                }

                if (args[1].equalsIgnoreCase("@a")) {
                    NGWord.bannedPlayers.clear();
                    sender.sendMessage(Message.SuccessMsg("全てのプレイヤーのBANを解除しました."));
                } else {
                    String name = args[1];
                    NGWord.bannedPlayers.removeIf(x -> x.getName().equalsIgnoreCase(name));
                    sender.sendMessage(Message.SuccessMsg(name + "のBANを解除しました."));
                }
                return true;
            case "random":
                for (Player p : Bukkit.getOnlinePlayers()) {
                    Collections.shuffle(NGWord.words);
                    NGWord.configuredNGWord.put(p.getUniqueId(), NGWord.words.get(0));
                    plugin.setNG(p, NGWord.words.get(0));
                }
                sender.sendMessage(Message.SuccessMsg("全てのプレイヤーにNGワードをランダムで配りました."));
                return true;
            case "reload":
                try {
                    plugin.loadList();
                } catch (IOException e) {
                    sender.sendMessage(Message.FailureMsg("単語リスト読み込み時にエラーが発生しました."));
                }
                sender.sendMessage(Message.SuccessMsg("単語リストを更新しました."));
                return true;
            case "reset":
                NGWord.configuredNGWord.clear();
                sender.sendMessage(Message.SuccessMsg("NGワードをリセットしました."));
                NGWord.bannedPlayers.clear();
                sender.sendMessage(Message.SuccessMsg("全てのプレイヤーのBANを解除しました."));
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
                plugin.setNG(p, word);
                sender.sendMessage(Message.SuccessMsg(p.getName() + "にNGワードをセットしました."));
                return true;
            }
        }
        return false;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1)
            return subCmdList.stream()
                    .filter(e -> e.startsWith(args[0]))
                    .collect(Collectors.toList());

        String subCmd = args[0].toLowerCase();
        if (args.length == 2) {
            switch (subCmd) {
                case "reset":
                    return Collections.emptyList();
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
            }
        }

        if (args.length == 3 && subCmd.equals("set"))
            return NGWord.words.stream()
                    .filter(e -> e.startsWith(args[2]))
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }
}