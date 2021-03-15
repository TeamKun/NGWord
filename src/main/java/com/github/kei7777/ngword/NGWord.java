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
    String filename = "words.yml";
    String filename2 = "additionalWords.csv";
    ChatColor NGWordColor;
    static Map<UUID, List<String>> configuredNGWord = new HashMap<>();
    static List<List<String>> additionalWords = new ArrayList<>();
    static List<Player> bannedPlayers = new ArrayList<>();
    HiraganaConverter converter;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        saveResource("convertTable.csv", true);
        NGWordColor = ChatColor.valueOf(config.getString("NGWordColor"));
        try {
            getDataFolder().mkdir();
            File words = new File(getDataFolder(), filename);
            if (!words.exists()) saveResource(filename, false);
            File additional = new File(getDataFolder(), filename2);
            if (!additional.exists()) saveResource(filename2, false);
            //  loadList();
        } catch (Exception e) {
            e.printStackTrace();
            this.setEnabled(false);
            return;
        }


        converter = new HiraganaConverter(this);

        try (InputStreamReader in = new InputStreamReader(getResource(filename), "UTF-8")) {
            FileConfiguration wordsyml = YamlConfiguration.loadConfiguration(in);
            List<HashMap<String, String>> w = ((List<HashMap<String, String>>) wordsyml.getList("Words"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        getLogger().info(converter.convert("てすとっとしょ").toString());
        getServer().getPluginManager().registerEvents(new NGWordListener(this), this);
        getServer().getPluginCommand("ngword").setExecutor(new NGWordCommandExecutor(this));
    }

    /*
    public void loadList() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(this.getDataFolder(), filename)));
        String line;
        words.clear();
        while ((line = reader.readLine()) != null) words.add(line);
        reader.close();
    }*/

    public void saveList(List<String> words) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(new File(this.getDataFolder(), filename), true));
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
