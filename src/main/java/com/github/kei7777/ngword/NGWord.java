package com.github.kei7777.ngword;

import com.nametagedit.plugin.NametagEdit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;

public class NGWord extends JavaPlugin {
    String wordsFilename = "words.yml";
    String addWordsFilename = "additionalWords.csv";
    ChatColor NGWordColor;
    static Map<UUID, List<String>> configuredNGWord = new HashMap<>();
    static List<List<String>> additionalWords = new ArrayList<>();
    static List<Player> bannedPlayers = new ArrayList<>();
    HiraganaConverter converter;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource(wordsFilename, false);
        saveResource(addWordsFilename, false);
        FileConfiguration config = getConfig();
        NGWordColor = ChatColor.valueOf(config.getString("NGWordColor"));
        try {
            getDataFolder().mkdir();
            //  loadList();
        } catch (Exception e) {
            e.printStackTrace();
            this.setEnabled(false);
            return;
        }

        converter = new HiraganaConverter(this);

        //words.ymlを読み込み設定を行う
        try (InputStreamReader in = new InputStreamReader(new FileInputStream(new File(getDataFolder(), wordsFilename)), "UTF-8")) {
            FileConfiguration wordsyml = YamlConfiguration.loadConfiguration(in);
            List<HashMap<String, ?>> w = ((List<HashMap<String, ?>>) wordsyml.getList("Words"));
            for (HashMap<String, ?> map : w) {
                try {
                    UUID uuid = UUID.fromString(map.get("UUID").toString());
                    String mcid = map.get("Name").toString();
                    List<String> ngwords = ((List<String>) map.get("NGWord"));
                    List<String> prons = ((List<String>) map.get("Pron"));
                    for (String ng : ngwords) {
                        configuredNGWord.put(uuid, new ArrayList<>());
                        configuredNGWord.get(uuid).add(ng);
                        configuredNGWord.get(uuid).add(converter.toKatakana(ng));
                    }
                    for (String pron : prons) {
                        configuredNGWord.get(uuid).add(pron);
                        configuredNGWord.get(uuid).add(converter.toKatakana(pron));
                        configuredNGWord.get(uuid).addAll(converter.toRomaji(pron));
                    }
                    setNG(uuid, ngwords.get(0));
                } catch (NullPointerException e) {
                    //空のフィールドがあった場合は飛ばす
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        getServer().getPluginManager().registerEvents(new NGWordListener(this), this);
        getServer().getPluginCommand("ngword").setExecutor(new NGWordCommandExecutor(this));
    }

    /*
    public void loadWordsFile() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(this.getDataFolder(), filename)));
        String line;
        words.clear();
        while ((line = reader.readLine()) != null) words.add(line);
        reader.close();
    }*/

    public void saveList(List<String> words) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(new File(this.getDataFolder(), wordsFilename), true));
        for (String w : words) {
            writer.println(w);
        }
        writer.flush();
        writer.close();
    }

    public void setNG(Player p, String word) {
        NametagEdit.getApi().setSuffix(p, " " + NGWordColor + word);
    }

    public void setNG(UUID uuid, String word) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) setNG(p, word);
    }


}
