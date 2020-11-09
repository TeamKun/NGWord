package com.github.kei7777.ngword;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class NGWordListener implements Listener {
    NGWord plugin;
    public NGWordListener(NGWord ngWord) {
        this.plugin = ngWord;
    }

    @EventHandler
    public void onJoin(AsyncPlayerPreLoginEvent e){
        if(NGWord.banned.contains(e.getUniqueId())){
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ChatColor.RED + "\nあなたはNGワードを発言したため入ることができません！\nあなたのNGワード: " + NGWord.playerwords.get(e.getUniqueId()) + "\n\n");
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        if(NGWord.playerwords.containsKey(e.getPlayer().getUniqueId())){
            String raw = e.getMessage().replaceAll("§", "");
            if(raw.contains(NGWord.playerwords.get(e.getPlayer().getUniqueId()))){
                new BukkitRunnable(){@Override public void run() {
                    e.getPlayer().getLocation().getWorld().strikeLightningEffect(e.getPlayer().getLocation());
                    e.getPlayer().kickPlayer(ChatColor.RED + "\nあなたはNGワードを発言したため入ることができません！\nあなたのNGワード: " + NGWord.playerwords.get(e.getPlayer().getUniqueId()) + "\n\n");
                }
                }.runTask(this.plugin);
                NGWord.holograms.get(e.getPlayer().getUniqueId()).delete();
                Bukkit.broadcastMessage(ChatColor.RED + e.getPlayer().getName() + "がNGワードを発言しました！！ (" + NGWord.playerwords.get(e.getPlayer().getUniqueId()) + ")");
                NGWord.banned.add(e.getPlayer().getUniqueId());
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e){
        Location from = e.getFrom();
        Location to = e.getTo();
        Player p = e.getPlayer();
        if(from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
            return;
        }

        if(NGWord.playerwords.containsKey(p.getUniqueId())){
            if(!NGWord.holograms.containsKey(p.getUniqueId())){
                Hologram holo = HologramsAPI.createHologram(plugin, p.getEyeLocation().clone().add(0,1.2,0));
                holo.appendTextLine("ワード: " + NGWord.playerwords.get(p.getUniqueId()));
                holo.getVisibilityManager().hideTo(p);
                NGWord.holograms.put(p.getUniqueId(), holo);
            }
            Hologram holo = NGWord.holograms.get(p.getUniqueId());
            holo.getVisibilityManager().hideTo(p);
            holo.teleport(p.getEyeLocation().clone().add(0,1.2,0));
        }
    }
}
