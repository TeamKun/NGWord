package com.github.kei7777.ngword;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.Normalizer;
import java.util.stream.Collectors;

public class NGWordListener implements Listener {
    NGWord plugin;

    public NGWordListener(NGWord ngWord) {
        this.plugin = ngWord;
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        if (NGWord.bannedPlayers.stream().map(x -> x.getUniqueId()).collect(Collectors.toList()).contains(e.getUniqueId())) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ChatColor.RED + "\nあなたはNGワードを発言したため入ることができません！\nあなたのNGワード: " + NGWord.configuredNGWord.get(e.getUniqueId()).get(0) + "\n\n");
            return;
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        if (NGWord.configuredNGWord.containsKey(e.getPlayer().getUniqueId())) {
            plugin.setNG(e.getPlayer(), NGWord.configuredNGWord.get(e.getPlayer().getUniqueId()).get(0));
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (NGWord.configuredNGWord.containsKey(e.getPlayer().getUniqueId())) {
            String raw = Normalizer.normalize(e.getMessage().replaceAll("§.", ""), Normalizer.Form.NFKC).replaceAll(" ", "");
            for (String ng : NGWord.configuredNGWord.get(e.getPlayer().getUniqueId())) {
                if (raw.contains(ng)) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            NGWord.bannedPlayers.add(e.getPlayer());
                            e.getPlayer().getWorld().strikeLightningEffect(e.getPlayer().getLocation());
                            e.getPlayer().kickPlayer(ChatColor.RED + "\nあなたはNGワードを発言したため入ることができません！\nあなたのNGワード: " + NGWord.configuredNGWord.get(e.getPlayer().getUniqueId()).get(0) + "\n\n");
                        }
                    }.runTask(this.plugin);
                    Bukkit.broadcastMessage(ChatColor.RED + e.getPlayer().getName() + "がNGワードを発言しました！！ (" + NGWord.configuredNGWord.get(e.getPlayer().getUniqueId()) + ")");
                }
            }

        }
    }
}
