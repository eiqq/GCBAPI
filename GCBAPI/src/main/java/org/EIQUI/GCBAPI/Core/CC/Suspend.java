package org.EIQUI.GCBAPI.Core.CC;

import org.EIQUI.GCBAPI.Core.UnitVector;
import org.EIQUI.GCBAPI.UnitCollision;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class Suspend {
    private static final Map<Entity, Set<Suspend>> suspends = new ConcurrentHashMap<>();
    private static final Map<Entity, Boolean> suspended = new ConcurrentHashMap<>();
    private static final Map<Entity, Location> suspendlocation = new ConcurrentHashMap<>();


    private Entity caster;
    private Entity target;
    private String name;
    private UUID id;
    private int duration;
    private BukkitTask timerTask;

    public Suspend(@Nullable Entity caster, Entity target, int duration, String name, UUID id) {
        this.duration = duration;
        if(caster == null){
            this.caster = target;
        }else{
            this.caster = caster;
        }
        this.target = target;
        this.name = name;
        this.id = id;

        if(suspends.containsKey(target)){
            for(Suspend t :suspends.get(target)){
                if(t.id.equals(id)){
                    t.removeSuspend();
                }
            }
        }

        // Initialize the suspends set if necessary.
        suspends.computeIfAbsent(target, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));

        // Store the initial location.
        suspendlocation.put(this.target, UnitCollision.WithBlock_Location(this.target,this.target.getVelocity(),
                this.target.getBoundingBox().getWidthX()/2,this.target.getBoundingBox().getHeight(),0));
        if (target.getType().isAlive()) {
            suspendEffect();
        }
        suspended.put(target, true);
        suspends.get(target).add(this);
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(that, 0L, 1);
    }
    private void tick() {
        if (duration == 0 || !target.isValid() || target.isDead()) {
            removeSuspend();
            return;
        }
        if(!Timestop.isTimestopped(target)){
            duration--;
        }
    }
    private void suspendEffect() {
        if (Boolean.TRUE.equals(suspended.get(target))) {
            return;
        }
        BukkitTask efftask = new BukkitRunnable() {
            @Override
            public void run() {
                if (Boolean.FALSE.equals(suspended.get(target))) {
                    this.cancel();
                    return;
                }
                if(UnitVector.hasVector(target)){
                    suspendlocation.put(target,target.getLocation());
                }else{
                    target.teleport(suspendlocation.get(target));
                }
            }
        }.runTaskTimer(that, 0L, 1);
    }

    public static boolean isSuspended(@Nullable Entity target) {
        if (target == null){
            return false;
        }
        return Boolean.TRUE.equals(suspended.get(target));
    }

    private void removeSuspend() {
        if (suspends.containsKey(target)) {
            suspends.get(target).remove(this);
            timerTask.cancel();
            duration = 0;
            if (suspends.get(target).isEmpty()) {
                suspended.put(target, false);
                suspendlocation.remove(target);
            }
        }
    }

    public static void clear() {
        for (Entity entity : suspends.keySet()) {
            removeAll(entity);
        }
        suspendlocation.clear();
        suspended.clear();
    }

    public static void removeAll(Entity target) {
        if (Boolean.TRUE.equals(suspended.get(target))) {
            for (Suspend t : suspends.get(target)) {
                t.removeSuspend();
            }
            suspends.get(target).clear();
            suspended.put(target, false);
            suspendlocation.remove(target);
        }
    }

    public static void remove(Entity target, String identifier) {
        UUID id;
        try {
            id = UUID.fromString(identifier);
        } catch (IllegalArgumentException e) {
            id = null;
        }
        if (suspends.containsKey(target)) {
            for(Suspend t: suspends.get(target)){
                if(id != null && t.id.equals(id) || t.name.equals(identifier)){
                    t.removeSuspend();
                }
            }
        }
    }
    public static class SuspendHandler implements Listener {
        public SuspendHandler(){}
        @EventHandler(priority = EventPriority.MONITOR)
        public void onTeleport(EntityTeleportEvent e){
            if(isSuspended(e.getEntity())){
                suspendlocation.put(e.getEntity(),e.getTo());
            }
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerTeleport(PlayerTeleportEvent e){
            if(isSuspended(e.getPlayer())){
                suspendlocation.put(e.getPlayer(),e.getTo());
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
