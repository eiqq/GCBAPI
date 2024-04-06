package org.EIQUI.GCBAPI.Core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

import static org.EIQUI.GCBAPI.main.that;

public class GCBWorldBorder {
    private WorldBorder instance;
    private World world;
    private static final Map<World,GCBWorldBorder> BORDERS = new HashMap<>();

    public GCBWorldBorder(World w){
        w.getWorldBorder().setSize(300000);
        w.getWorldBorder().setCenter(0,0);
        instance = Bukkit.createWorldBorder();
        instance.setSize(300000);
        instance.setCenter(0,0);
        world = w;
        BORDERS.put(world,this);
    }
    public static void Initialize(){
        for(World w: Bukkit.getWorlds()){
            new GCBWorldBorder(w);
        }
        for(Player p :Bukkit.getOnlinePlayers()){
            updateBorderToPlayer(p);
        }
    }

    public static GCBWorldBorder getBorder(World w){
        return BORDERS.get(w);
    }

    public WorldBorder getInstance(){
        return instance;
    }

    public boolean isInside(Entity e){
        return isInside(e.getLocation());
    }
    public boolean isInside(Location l){
        double size = instance.getSize()/2.0d;
        Location center = instance.getCenter();
        return (l.getX() >= center.getX() - size) && (l.getX() <= center.getX() + size)
                && (l.getZ() >= center.getZ() - size) && (l.getZ() <= center.getZ() + size);
    }
    public static void updateBorderToPlayer(Player p){
        World playerw = p.getWorld();
        if(!BORDERS.containsKey(playerw)){
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                p.setWorldBorder(BORDERS.get(p.getWorld()).instance);
            }
        }.runTaskLater(that, 2L);
    }

    public static class GCBBorderHandler implements Listener {
        public GCBBorderHandler() {
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onJoin(PlayerJoinEvent e){
            updateBorderToPlayer(e.getPlayer());
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onTP(PlayerTeleportEvent e){
            if(!e.getTo().getWorld().equals(e.getPlayer().getWorld())){
                updateBorderToPlayer(e.getPlayer());
            }
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onRespawn(PlayerRespawnEvent e){
            updateBorderToPlayer(e.getPlayer());
        }

    }
}
