package com.github.kei7777.ngword;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NGWordCommandExecutor implements CommandExecutor, TabCompleter {
    public NGWordCommandExecutor(NGWord ngWord) {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;

        switch (args[0].toLowerCase()) {
            case "word":
                Message.sendSuccessMsg(sender, "登録単語一覧");
                for (String word : NGWord.words) {
                    sender.sendMessage(" - " + word);
                }
                return true;
            case "random":
                for (Player p : Bukkit.getOnlinePlayers()) {
                    Collections.shuffle(NGWord.words);
                    NGWord.playerwords.put(p.getUniqueId(), NGWord.words.get(0));
                    Bukkit.getPluginManager().callEvent(new NGWordUpdateEvent(p, true));
                }
                sender.sendMessage("正常にランダムなNGワードを配りました。");
                return true;
            case "set":
                if (args.length < 3) {
                    sender.sendMessage("/ngword set <player> <word>");
                    return false;
                }
                Player p = Bukkit.getPlayer(args[1]);
                if (p == null) {
                    sender.sendMessage("プレイヤー見つからない");
                    return true;
                }
                String word = Stream.of(args).skip(2).collect(Collectors.joining(" "));
                NGWord.playerwords.put(p.getUniqueId(), word);
                Bukkit.getPluginManager().callEvent(new NGWordUpdateEvent(p, true));
                Message.sendSuccessMsg(sender, "正常にセットしました。");
                return true;
            case "reset":
                NGWord.holograms.values().forEach(Hologram::delete);
                NGWord.holograms.clear();
                NGWord.playerwords.clear();
                NGWord.bannedPlayers.clear();
                Message.sendSuccessMsg(sender, "正常にリセットしました。");
                return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1)
            return Stream.of("word", "random", "set", "reset")
                    .filter(e -> e.startsWith(args[0]))
                    .collect(Collectors.toList());

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
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