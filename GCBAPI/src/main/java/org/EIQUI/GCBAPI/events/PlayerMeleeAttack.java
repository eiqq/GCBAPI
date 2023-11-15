package org.EIQUI.GCBAPI.events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;


import static org.EIQUI.GCBAPI.main.protocolManager;
import static org.EIQUI.GCBAPI.main.that;


public class PlayerMeleeAttack extends Event{
    private static final HandlerList HANDLERS = new HandlerList();
    private Player attacker;
    private Entity victim;
    private boolean cancelled;
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
                            PlayerInteractEvent temp = new PlayerInteractEvent(player, Action.LEFT_CLICK_AIR,
                                    player.getInventory().getItemInMainHand(),null,null);
                            Entity victim = protocolManager.getEntityFromID(player.getWorld(), id);
                            PlayerMeleeAttack mevent = new PlayerMeleeAttack(player, victim);
                            Bukkit.getPluginManager().callEvent(mevent);
                            //Bukkit.getPluginManager().callEvent(temp);
                        }
                    }.runTask(that);
                }
            }
        });
    }

}
