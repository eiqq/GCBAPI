package org.EIQUI.GCBAPI.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;



public class PlayerKeyInput extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;
    private int key;
    private int prevkey;
    private boolean isPressed;
    private boolean cancelled = false;

    public PlayerKeyInput(Player p,int pre, int s,boolean b) {
        this.player = p;
        this.key = s;
        this.prevkey = pre;
        this.isPressed = b;
    }

    public Player getPlayer(){
        return this.player;
    }
    public int getKey(){
        return this.key;
    }
    public int getPreviousKey(){
        return this.prevkey;
    }
    public int getNewSlot(){
        return this.key;
    }
    public int getPreviousSlot(){
        return this.prevkey;
    }
    public boolean isPressed(){
        return this.isPressed;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
