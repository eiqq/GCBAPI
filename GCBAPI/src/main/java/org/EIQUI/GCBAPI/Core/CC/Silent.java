package org.EIQUI.GCBAPI.Core.CC;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class Silent {
    private static final Map<Entity, Set<Silent>> silents = new ConcurrentHashMap<>();
    private static final Map<Entity, Boolean> silented = new ConcurrentHashMap<>();

    private Entity caster;
    private Entity target;
    private String name;
    private UUID id;
    private int duration;

    private BukkitTask timerTask;

    public Silent(@Nullable Entity caster, Entity target, int duration, String name, UUID id) {
        this.duration = duration;
        if(caster == null){
            this.caster = target;
        }else{
            this.caster = caster;
        }
        this.target = target;
        this.name = name;
        this.id = id;

        if(silents.containsKey(target)){
            for(Silent t :silents.get(target)){
                if(t.id.equals(id)){
                    t.duration = this.duration;
                    t.name = this.name;
                    t.caster = this.caster;
                    return;
                }
            }
        }


        silents.computeIfAbsent(target, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));


        silents.get(target).add(this);
        silented.put(target, true);
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(that, 0L, 1);
    }
    private void tick() {
        if (duration == 0 || !target.isValid() || target.isDead()) {
            removeSilent();
            return;
        }
        if(!Timestop.isTimestopped(target)){
            duration--;
        }
    }

    public static boolean isSilented(@Nullable Entity target) {
        if (target == null){
            return false;
        }
        return Boolean.TRUE.equals(silented.get(target));
    }

    public void removeSilent() {
        if (silents.containsKey(target)) {
            silents.get(target).remove(this);
            duration = 0;
            timerTask.cancel();
            if (silents.get(target).isEmpty()) {
                silented.put(target, false);
                silents.get(target).clear();
            }
        }
    }

    public static void clear() {
        for (Entity entity : silents.keySet()) {
            removeAll(entity);
        }
        silented.clear();
    }

    public static void removeAll(Entity target) {
        if (Boolean.TRUE.equals(silented.get(target))) {
            for (Silent t : silents.get(target)) {
                t.removeSilent();
            }
            silents.get(target).clear();
            silented.put(target, false);
        }
    }

    public static void remove(Entity target, String identifier) {
        UUID id;
        try {
            id = UUID.fromString(identifier);
        } catch (IllegalArgumentException e) {
            id = null;
        }
        if (silents.containsKey(target)) {
            for(Silent t: silents.get(target)){
                if(id != null && t.id.equals(id) || t.name.equals(identifier)){
                    t.removeSilent();
                }
            }
        }
    }

    public static class SilentHandler implements Listener {
        public SilentHandler(){}
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
