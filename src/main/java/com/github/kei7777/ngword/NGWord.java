package com.github.kei7777.ngword;

import com.nametagedit.plugin.NametagEdit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;

public class NGWord extends JavaPlugin {
    String filename = "words.txt";
    static Map<UUID, String> configuredNGWord = new HashMap<>();
    static List<String> words = new ArrayList<>();
    static List<Player> bannedPlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        try {
            getDataFolder().mkdir();
            new File(getDataFolder(), filename).createNewFile();
            loadList();
        } catch (Exception e) {
            e.printStackTrace();
            this.setEnabled(false);
            return;
        }
        getServer().getPluginManager().registerEvents(new NGWordListener(this), this);
        getServer().getPluginCommand("ngword").setExecutor(new NGWordCommandExecutor(this));
    }

    public void loadList() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(this.getDataFolder(), filename)));
        String line;
        words.clear();
        while ((line = reader.readLine()) != null) words.add(line);
        reader.close();
    }

    public void saveList(List<String> words) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(new File(this.getDataFolder(), filename), true));
        for (String w : words) {
            writer.println(w);
        }
        writer.flush();
        writer.close();
    }

    public void setNG(Player p, String word) {
        NametagEdit.getApi().setSuffix(p, " " + ChatColor.RED + word);
    }
}
