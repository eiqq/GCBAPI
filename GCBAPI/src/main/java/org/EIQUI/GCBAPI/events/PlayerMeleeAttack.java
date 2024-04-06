package org.EIQUI.GCBAPI.events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

import static org.EIQUI.GCBAPI.main.protocolManager;
import static org.EIQUI.GCBAPI.main.that;


public class PlayerMeleeAttack extends Event{
    private static final HandlerList HANDLERS = new HandlerList();
    private Player attacker;
    private Entity victim;
    private boolean cancelled;
    private static final Map<Player,Boolean> INTERACTEVENT_IS_COOLDOWN = new HashMap<>();
    public PlayerMeleeAttack(Player p,Entity v) {
        this.attacker = p;
        this.victim = v;
        this.cancelled = false;
    }

    public Player getAttacker(){
        return this.attacker;
    }
    public Entity getVictim(){
        return this.victim;
    }


    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static void registerProtocolLibPacketListener() {
        that.getServer().getPluginManager().registerEvents(new PlayerMeleeAttack.Handler(), that);
        protocolManager.addPacketListener(new PacketAdapter(that, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                if(packet.getEnumEntityUseActions().readSafely(0).getAction() ==  EnumWrappers.EntityUseAction.ATTACK) {
                    Player player = event.getPlayer();
                    int id = packet.getIntegers().read(0);
                    event.setCancelled(true);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Entity victim = protocolManager.getEntityFromID(player.getWorld(), id);
                            PlayerMeleeAttack mevent = new PlayerMeleeAttack(player, victim);
                            Bukkit.getPluginManager().callEvent(mevent);

                            if(!Boolean.TRUE.equals(INTERACTEVENT_IS_COOLDOWN.get(player))){
                                cooldown(player);
                                PlayerInteractEvent temp = new PlayerInteractEvent(player, Action.LEFT_CLICK_AIR,
                                        player.getInventory().getItemInMainHand(),null,null);
                                Bukkit.getPluginManager().callEvent(temp);
                            }
                        }
                    }.runTask(that);
                }
            }
        });
    }

    private static void cooldown(Player p){
        if(Boolean.TRUE.equals(INTERACTEVENT_IS_COOLDOWN.get(p))){
           return;
        }
        INTERACTEVENT_IS_COOLDOWN.put(p,true);
        new BukkitRunnable() {
            @Override
            public void run() {
                INTERACTEVENT_IS_COOLDOWN.put(p,false);
            }
        }.runTaskLater(that,1);
    }
    public static class Handler implements Listener {
        public Handler(){}
        @EventHandler(priority = EventPriority.MONITOR)
        public void onInteract(PlayerInteractEvent e){
            cooldown(e.getPlayer());
        }
    }

}
