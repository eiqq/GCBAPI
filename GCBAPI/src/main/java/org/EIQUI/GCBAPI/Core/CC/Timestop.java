package org.EIQUI.GCBAPI.Core.CC;

import org.EIQUI.GCBAPI.UnitCollision;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class Timestop {
    private static final Map<Entity, Set<Timestop>> timestops = new ConcurrentHashMap<>();
    private static final Map<Entity, Boolean> timestoped = new ConcurrentHashMap<>();
    private static final Map<Entity, Location> timestoplocation = new ConcurrentHashMap<>();


    private Entity caster;
    private Entity target;
    private String name;
    private UUID id;
    private int duration;
    private BukkitTask timerTask;
    private BukkitTask effectTask;

    public Timestop(@Nullable Entity caster, Entity target, int duration, String name, UUID id) {
        this.duration = duration;
        if(caster == null){
            this.caster = target;
        }else{
            this.caster = caster;
        }
        this.target = target;
        this.name = name;
        this.id = id;

        if(timestops.containsKey(target)){
            for(Timestop t :timestops.get(target)){
                if(t.id.equals(id)){
                    t.duration = this.duration;
                    t.caster = this.caster;
                    t.id = this.id;
                    return;
                }
            }
        }

        timestops.computeIfAbsent(target, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));

        timestoplocation.put(this.target, UnitCollision.WithBlock_Location(this.target,this.target.getVelocity(),
                this.target.getBoundingBox().getWidthX()/2,this.target.getBoundingBox().getHeight(),0));
        timestopEffect();
        timestoped.put(target, true);
        timestops.get(target).add(this);
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(that, 0L, 1);
    }
    private void tick() {
        if (duration == 0 || !target.isValid() || target.isDead()) {
            removeTimestop();
            return;
        }
        duration--;
    }
    private void timestopEffect() {
        target.teleport(timestoplocation.get(target));
        if (isTimestopped(target)) {
            return;
        }
        effectTask = new BukkitRunnable() {
            @Override
            public void run() {
                target.teleport(timestoplocation.get(target));
            }
        }.runTaskTimer(that, 0L, 1);
    }

    public static boolean isTimestopped(@Nullable Entity target) {
        if (target == null){
            return false;
        }
        return Boolean.TRUE.equals(timestoped.get(target));
    }

    private void removeTimestop() {
        if (timestops.containsKey(target)) {
            timestops.get(target).remove(this);
            timerTask.cancel();
            if (effectTask != null){
                effectTask.cancel();
            }
            duration = 0;
            if (timestops.get(target).isEmpty()) {
                timestoped.put(target, false);
                timestoplocation.remove(target);
            }
        }
    }

    public static void clear() {
        for (Entity entity : timestops.keySet()) {
            removeAll(entity);
        }
        timestoplocation.clear();
        timestoped.clear();
    }

    public static void removeAll(Entity target) {
        if (Boolean.TRUE.equals(timestoped.get(target))) {
            for (Timestop t : timestops.get(target)) {
                t.removeTimestop();
            }
            timestops.get(target).clear();
            timestoped.put(target, false);
            timestoplocation.remove(target);
        }
    }

    public static void remove(Entity target, String identifier) {
        if (timestoped.get(target).equals(Boolean.TRUE)) {
            UUID id;
            try {
                id = UUID.fromString(identifier);
            } catch (IllegalArgumentException e) {
                id = null;
            }
            for(Timestop t: timestops.get(target)){
                if(id != null && t.id.equals(id) || t.name.equals(identifier)){
                    t.removeTimestop();
                }
            }
        }
    }
    public static class TimestopHandler implements Listener {
        public TimestopHandler(){}
        @EventHandler(priority = EventPriority.MONITOR)
        public void onTeleport(EntityTeleportEvent e){
            if(isTimestopped(e.getEntity())){
                timestoplocation.put(e.getEntity(),e.getTo());
            }
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerTeleport(PlayerTeleportEvent e){
            if(isTimestopped(e.getPlayer())){
                timestoplocation.put(e.getPlayer(),e.getTo());
            }
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onMove(PlayerMoveEvent e){
            if(isTimestopped(e.getPlayer())){
                e.setCancelled(true);
            }
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onDeath(EntityDeathEvent e){
            removeAll(e.getEntity());
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onQuit(PlayerQuitEvent e){
            removeAll(e.getPlayer());
        }

    }
}
