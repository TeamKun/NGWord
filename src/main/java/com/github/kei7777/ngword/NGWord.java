package com.github.kei7777.ngword;

import com.nametagedit.plugin.NametagEdit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class NGWord extends JavaPlugin {
    String ngwordsFilename = "ngwords.yml";
    static String addWordsFilename = "additionalWords.yml";
    ChatColor NGWordColor;
    static Map<UUID, List<String>> configuredNGWord = new HashMap<>();
    static Map<String, List<String>> ngwords = new HashMap<>();
    static Map<String, List<String>> additionalNGWords = new HashMap<>();
    static List<Player> bannedPlayers = new ArrayList<>();
    HiraganaConverter converter;

    @Override
    public void onEnable() {
        getDataFolder().mkdir();
        saveDefaultConfig();
        saveResource(ngwordsFilename, false);
        saveResource(addWordsFilename, false);
        FileConfiguration config = getConfig();
        NGWordColor = ChatColor.valueOf(config.getString("NGWordColor"));
        converter = new HiraganaConverter();
        
        getServer().getPluginManager().registerEvents(new NGWordListener(this), this);
        getServer().getPluginCommand("ngword").setExecutor(new NGWordCommandExecutor(this));
    }

    //words.ymlを読み込みMapリストを作成する
    public Map<UUID, List<String>> loadNGWordsFile() throws IOException, NullPointerException {
        Map<UUID, List<String>> corr = new HashMap<>();
        try (InputStreamReader in = new InputStreamReader(new FileInputStream(new File(getDataFolder(), ngwordsFilename)), "UTF-8")) {
            FileConfiguration wordsyml = YamlConfiguration.loadConfiguration(in);
            List<HashMap<String, ?>> w = ((List<HashMap<String, ?>>) wordsyml.getList("Words"));
            for (HashMap<String, ?> map : w) {
                try {
                    UUID uuid = UUID.fromString(map.get("UUID").toString());
                    List<String> ngwords = ((List<String>) map.get("NGWord"));
                    List<String> prons = ((List<String>) map.get("Pron"));
                    for (String ng : ngwords) {
                        ng = ng.toLowerCase();
                        corr.put(uuid, new ArrayList<>());
                        corr.get(uuid).add(ng);
                        corr.get(uuid).add(converter.toHiragana(ng));
                    }
                    for (String pron : prons) {
                        pron = pron.toLowerCase();
                        corr.get(uuid).add(pron);
                        corr.get(uuid).addAll(converter.toRomaji(pron));
                    }

                } catch (NullPointerException e) {
                    //空のフィールドがあった場合は飛ばす
                }
            }
        }
        return corr;
    }

    //additionalWords.ymlを読み込みリストを作成する.
    public List<List<String>> loadAddWordsFile() throws IOException, NullPointerException {
        List<List<String>> lists = new ArrayList<>();
        try (InputStreamReader in = new InputStreamReader(new FileInputStream(new File(getDataFolder(), addWordsFilename)), "UTF-8")) {
            FileConfiguration addwordsyml = YamlConfiguration.loadConfiguration(in);
            List<HashMap<String, List<String>>> aw = ((List<HashMap<String, List<String>>>) addwordsyml.getList("Words"));
            for (HashMap<String, List<String>> map : aw) {
                try {
                    List<String> list = new ArrayList<>();
                    for (String ng : map.get("NGWord")) {
                        list.add(ng);
                    }
                    for (String pron : map.get("Pron")) {
                        list.add(pron);
                        list.addAll(converter.toRomaji(pron));
                    }
                    lists.add(list);
                } catch (NullPointerException e) {
                    //空のフィールドがあった場合は飛ばす
                }
            }
        }
        return lists;
    }

    public void setNG(Player p, String word) {
        p.sendMessage(Message.SuccessMsg("あなたにNGワードが設定されました."));
        NametagEdit.getApi().setSuffix(p, " " + NGWordColor + word);
    }

    public void setNG(UUID uuid, String word) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) setNG(p, word);
    }
}
