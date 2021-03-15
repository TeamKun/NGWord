package com.github.kei7777.ngword;

import org.bukkit.plugin.java.JavaPlugin;

public class DependencyResolver {
    final String host = "https://www.spigotmc.org";
    final String query = "";

    JavaPlugin plugin;

    DependencyResolver(JavaPlugin plugin) {
        this.plugin = plugin;
    }


}
