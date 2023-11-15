package org.EIQUI.GCBAPI.fabric;

import org.EIQUI.GCBAPI.events.PlayerKeyInput;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import static org.EIQUI.GCBAPI.main.that;

public class FabricC2S implements PluginMessageListener, Listener{
    private static HashMap<UUID, Boolean> hasMod = new HashMap<>();
    private static HashMap<UUID, String> version = new HashMap<>();
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if (channel.equals("gcb:gcb")) {
            hasMod.put(player.getUniqueId(),true);
            String rec = new String(message);
            String[] packet = rec.split(":");
            String header = String.valueOf(packet[0]);
            if(header.contains("KeyInput")){
                int hand = player.getInventory().getHeldItemSlot();
                int number = Integer.valueOf(packet[1]);
                PlayerKeyInput event = new PlayerKeyInput(player,hand,number,Boolean.valueOf(packet[2]));
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()){
                    if (number < 10){
                        player.getInventory().setHeldItemSlot(hand);
                    }
                }
            }else{
                version.put(player.getUniqueId(),rec);
            }
        }

    }

    public static void setMod(Player p,boolean b){
        hasMod.put(p.getUniqueId(),b);
    }
    public static boolean hasMod(Player p){
        return hasMod.containsKey(p.getUniqueId());
    }
    public static String getVersion(Player p){
        if(version.containsKey(p.getUniqueId())){
            return version.get(p.getUniqueId());
        }
        return "noMod";
    }
    public static void reset(Player e){
        version.remove(e.getPlayer().getUniqueId());
        hasMod.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void join(PlayerJoinEvent e) {
        reset(e.getPlayer());
        that.getLogger().log(Level.INFO, e.getPlayer().getName() + ": 전송");
        Bukkit.getScheduler().runTaskLater(that,() -> {
            Fabric.sendPacketToFabric(e.getPlayer(), "gcb:gcb", "d");
        }, 20);
    }
    @EventHandler
    public void quit(PlayerQuitEvent e) {
        that.getLogger().log(Level.INFO, e.getPlayer().getName() + ": 데이터삭제");
        reset(e.getPlayer());
    }
}

