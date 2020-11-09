package com.github.kei7777.ngword;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class NGWordUpdateEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    public final boolean refresh;

    public NGWordUpdateEvent(Player who) {
        this(who, false);
    }

    public NGWordUpdateEvent(Player who, boolean refresh) {
        super(who);
        this.refresh = true;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean cancel) {

    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}