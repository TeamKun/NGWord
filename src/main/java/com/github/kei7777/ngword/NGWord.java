package com.github.kei7777.ngword;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class NGWord extends JavaPlugin {

    static Map<UUID, Hologram> holograms;
    static Map<UUID, String> configuredNGWord;
    static List<String> words;

    static List<Player> bannedPlayers;

    @Override
    public void onEnable() {
        holograms = new HashMap<>();
        configuredNGWord = new HashMap<>();
        words = new ArrayList<>();
        bannedPlayers = new ArrayList<>();
        String filename = "words.txt";

        try {
            getDataFolder().mkdir();
            new File(getDataFolder(), filename).createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
            this.setEnabled(false);
            return;
        }

        try (BufferedReader in = new BufferedReader(new FileReader(new File(this.getDataFolder(), filename)))) {
            String line;
            while ((line = in.readLine()) != null) words.add(line);
        } catch (IOException e) {
            e.printStackTrace();
            this.setEnabled(false);
            return;
        }

        getServer().getPluginManager().registerEvents(new NGWordListener(this), this);
        getServer().getPluginCommand("ngword").setExecutor(new NGWordCommandExecutor(this));
    }

}
