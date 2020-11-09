package com.github.kei7777.ngword;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;

public class NGWord extends JavaPlugin {

    static Map<UUID, Hologram> holograms;
    static Map<UUID, String> playerwords;
    static List<String> words;

    static List<UUID> banned;

    @Override
    public void onEnable() {
        holograms = new HashMap<>();
        playerwords = new HashMap<>();
        words = new ArrayList<>();
        banned = new ArrayList<>();

        String filename = "words.txt";
        try (BufferedReader in = new BufferedReader(new FileReader(new File(this.getDataFolder() + File.separator + filename)))){
            String line;
            while((line = in.readLine()) != null) words.add(line);
        } catch (FileNotFoundException e){
            e.printStackTrace();
            this.setEnabled(false);
            return;
        } catch (IOException e){
            e.printStackTrace();
            this.setEnabled(false);
            return;
        }

        getServer().getPluginManager().registerEvents(new NGWordListener(this), this);
        getServer().getPluginCommand("ngword").setExecutor(new NGwordCommandExecutor(this));

        if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            getLogger().severe("*** HolographicDisplays is not installed or not enabled. ***");
            getLogger().severe("*** This plugin will be disabled. ***");
            this.setEnabled(false);
            return;
        }
    }
}
