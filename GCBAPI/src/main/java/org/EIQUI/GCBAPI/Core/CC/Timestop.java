package org.EIQUI.GCBAPI.Core.CC;

import org.EIQUI.GCBAPI.Core.UnitCollision;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class Timestop {
    private static final Map<Entity, Set<Timestop>> timestops = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> timestoped = new HashMap<>();
    private static final Map<UUID, Location> timestoplocation = new HashMap<>();
    private Entity caster;
    private Entity target;
    private UUID targetUUID;
    private String name;
    private UUID id;
    private int duration;
    private BukkitTask timerTask;

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
        this.targetUUID = target.getUniqueId();
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
        timestoplocation.put(targetUUID, UnitCollision.WithBlock_Location(this.target,this.target.getVelocity(),
                this.target.getBoundingBox().getWidthX()/2,this.target.getBoundingBox().getHeight(),0));
        if(timestops.get(target).isEmpty()){
            timestopEffect();
        }
        timestoped.put(targetUUID, true);
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
        if (!target.getType().isAlive() || !target.isValid()) {
            return;
        }
        if(!target.isInsideVehicle()) {
            target.teleport(timestoplocation.get(targetUUID));
        }
        LivingEntity livingtarget = ((LivingEntity)target);
        livingtarget.setCollidable(false);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!Boolean.TRUE.equals(timestoped.get(targetUUID))) {
                    cancel();
                    livingtarget.setCollidable(true);
                    livingtarget.setFallDistance(0);
                    return;
                }
                if(!target.isInsideVehicle()){
                    target.teleport(timestoplocation.get(targetUUID));
                }
            }
        }.runTaskTimer(that, 0L, 1);
    }

    public static boolean isTimestopped(@Nullable Entity target) {
        if (target == null){
            return false;
        }
        return Boolean.TRUE.equals(timestoped.get(target.getUniqueId()));
    }

    private void removeTimestop() {
        if (timestops.containsKey(target)) {
            timestops.get(target).remove(this);
            timerTask.cancel();
            duration = 0;
            if (timestops.get(target).isEmpty()) {
                timestoped.put(targetUUID, false);
                timestoplocation.remove(targetUUID);
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
            timestoped.put(target.getUniqueId(), false);
            for (Timestop t : timestops.get(target)) {
                t.removeTimestop();
            }
            timestops.get(target).clear();
            timestoplocation.remove(target.getUniqueId());
        }
    }

    public static void remove(Entity target, String identifier) {
        if (timestoped.get(target.getUniqueId()).equals(Boolean.TRUE)) {
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
            if(e.getEntityType().isAlive() && !e.getEntityType().equals(EntityType.PLAYER) && isTimestopped(e.getEntity())){
                timestoplocation.put(e.getEntity().getUniqueId(),e.getTo());
            }
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerTeleport(PlayerTeleportEvent e){
            if(isTimestopped(e.getPlayer())){
                timestoplocation.put(e.getPlayer().getUniqueId(),e.getTo());
            }
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onDeath(EntityDeathEvent e){
            new BukkitRunnable() {
                @Override
                public void run() {
                    removeAll(e.getEntity());
                }
            }.runTaskLater(that, 2l);
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onQuit(PlayerQuitEvent e){
            removeAll(e.getPlayer());
        }
        @EventHandler(priority = EventPriority.LOWEST)
        public void onVector(PlayerVelocityEvent e){
            if(isTimestopped(e.getPlayer())){
                e.setCancelled(true);
            }
        }
    }
}
