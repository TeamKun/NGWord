package com.github.kei7777.ngword;

import com.nametagedit.plugin.NametagEdit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.NullConversationPrefix;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NGWordCommandExecutor implements CommandExecutor, TabCompleter {
    NGWord plugin;
    List<String> subCmdList = Arrays.asList("add", "list", "pardon", "random", "load", "reset", "remove", "set");
    Map<String, SubCommand> subCmds = new HashMap<>();
    ConversationFactory factory;

    public NGWordCommandExecutor(NGWord ngWord) {
        this.plugin = ngWord;
        setupSubCmds();
        factory = new ConversationFactory(plugin)
                .withModality(true)
                .withPrefix(new NullConversationPrefix())
                .withTimeout(120)
                .thatExcludesNonPlayersWithMessage("test console");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;
        String subCmd = args[0].toLowerCase();
        if (!subCmds.containsKey(subCmd)) {
            sender.sendMessage(Message.FailureMsg("/ngword " + subCmd + "は存在しません."));
            return true;
        }
        return subCmds.get(subCmd).execute(sender, command, args);
    }

    private void setupSubCmds() {
        subCmds.put("add", (sender, command, args) -> {
            factory.withFirstPrompt(new AdditionalWordPrompt(plugin))
                    .buildConversation(((Conversable) sender)).begin();
            return true;
        });
        subCmds.put("list", (sender, command, args) -> {
            sender.sendMessage(Message.SuccessMsg("追加登録単語一覧"));
            for (String key : NGWord.additionalNGWords.keySet()) {
                sender.sendMessage(ChatColor.DARK_GREEN + " - " + key);
            }
            return true;
        });
        subCmds.put("pardon", (sender, command, args) -> {
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
        });
        subCmds.put("random", (sender, command, args) -> {
            List<List<String>> ngwords = new ArrayList<>();
            for (String key : NGWord.ngwords.keySet()) {
                ngwords.add(NGWord.ngwords.get(key));
            }
            for (String key : NGWord.additionalNGWords.keySet()) {
                ngwords.add(NGWord.additionalNGWords.get(key));
            }

            for (Player p : Bukkit.getOnlinePlayers()) {
                Collections.shuffle(ngwords);
                NGWord.configuredNGWord.put(p.getUniqueId(), ngwords.get(0));
                plugin.setNG(p, ngwords.get(0).get(0));
            }
            sender.sendMessage(Message.SuccessMsg("全てのプレイヤーにNGワードをランダムで設定しました."));
            return true;
        });
        subCmds.put("load", (sender, command, args) -> {
            try {
                Map<UUID, List<String>> map = plugin.loadNGWordsFile();
                NGWord.ngwords.clear();
                for (UUID uuid : map.keySet()) {
                    plugin.setNG(uuid, map.get(uuid).get(0));
                    NGWord.ngwords.put(map.get(uuid).get(0), map.get(uuid));
                }
                NGWord.configuredNGWord = map;
                Map<String, List<String>> addMap = new HashMap<>();
                List<List<String>> lists = plugin.loadAddWordsFile();
                for (List<String> list : lists) {
                    addMap.put(list.get(0), list);
                }
                NGWord.additionalNGWords = addMap;
            } catch (Exception e) {
                sender.sendMessage(Message.FailureMsg("ロードに失敗しました."));
                return true;
            }
            Bukkit.getLogger().info(NGWord.configuredNGWord.toString());
            Bukkit.getLogger().info(NGWord.ngwords.get("やほ-").toString());
            sender.sendMessage(Message.SuccessMsg("ロードが完了しました."));
            return true;
        });
        subCmds.put("reset", (sender, command, args) -> {
            NGWord.configuredNGWord.clear();
            Bukkit.getOnlinePlayers().forEach(e -> NametagEdit.getApi().clearNametag(e));
            sender.sendMessage(Message.SuccessMsg("全てのプレイヤーのNGワードを削除しました."));
            NGWord.bannedPlayers.clear();
            sender.sendMessage(Message.SuccessMsg("全てのプレイヤーのBANを解除しました."));
            return true;
        });
        subCmds.put("remove", ((sender, command, args) -> {
            if (args.length < 2) {
                sender.sendMessage(Message.FailureMsg("/ngword remove <word>"));
                return true;
            }

            String word = args[1];
            if (!NGWord.additionalNGWords.containsKey(word)) {
                sender.sendMessage(Message.FailureMsg(word + "は登録されていません."));
                return true;
            }
            try (InputStreamReader in = new InputStreamReader(new FileInputStream(new File(plugin.getDataFolder(), NGWord.addWordsFilename)), "UTF-8")) {
                FileConfiguration addwordsyml = YamlConfiguration.loadConfiguration(in);
                List<HashMap<String, List<String>>> aw = ((List<HashMap<String, List<String>>>) addwordsyml.getList("Words"));
                HashMap<String, List<String>> rm = null;
                for (HashMap<String, List<String>> map : aw) {
                    for (String ng : map.get("NGWord")) {
                        if (ng.equals(word)) {
                            rm = map;
                        }
                    }
                }
                aw.remove(rm);
                addwordsyml.set("Words", aw);
                addwordsyml.save(new File(plugin.getDataFolder(), NGWord.addWordsFilename));

            } catch (Exception e) {
                sender.sendMessage(Message.FailureMsg("削除中にエラーが発生しました."));
                return true;
            }
            NGWord.additionalNGWords.remove(word);
            sender.sendMessage(Message.SuccessMsg("「" + word + "」が削除されました."));
            return true;
        }));
        subCmds.put("set", (sender, command, args) -> {
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
            if (!NGWord.additionalNGWords.containsKey(args[2])) {
                sender.sendMessage(Message.FailureMsg(args[2] + "は登録されていません."));
                return true;
            }
            List<String> words = NGWord.additionalNGWords.get(args[2]);
            NGWord.configuredNGWord.put(p.getUniqueId(), words);
            plugin.setNG(p, words.get(0));
            sender.sendMessage(Message.SuccessMsg(p.getName() + "にNGワードをセットしました."));
            return true;
        });
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
                case "remove":
                    return NGWord.additionalNGWords.keySet().stream()
                            .filter(e -> e.startsWith(args[1]))
                            .collect(Collectors.toList());
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
            return NGWord.additionalNGWords.keySet().stream()
                    .filter(e -> e.startsWith(args[2]))
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }

}