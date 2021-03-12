package com.github.kei7777.ngword;

import com.nametagedit.plugin.NametagEdit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
        BufferedReader in = new BufferedReader(new FileReader(new File(this.getDataFolder(), filename)));
        String line;
        words.clear();
        while ((line = in.readLine()) != null) words.add(line);
    }

    public void setNG(Player p, String word) {
        NametagEdit.getApi().setSuffix(p, " " + ChatColor.RED + word);
    }
}
