package org.EIQUI.GCBAPI.Core.BeneficialEffect;

import org.EIQUI.GCBAPI.Core.CC.Timestop;
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

public class Unstopable {
    private static final Map<Entity, Set<Unstopable>> unstopables = new ConcurrentHashMap<>();
    private static final Map<Entity, Boolean> unstopableed = new HashMap<>();

    private Entity caster;
    private Entity target;
    private final String name;
    private final UUID id;
    private int duration;
    private BukkitTask timerTask;

    public Unstopable(@Nullable Entity caster, Entity target, int duration, String name, UUID id) {
        this.duration = duration;
        if(caster == null){
            this.caster = target;
        }else{
            this.caster = caster;
        }
        this.target = target;
        this.name = name;
        this.id = id;

        if(Boolean.TRUE.equals(unstopableed.get(target))){
            for(Unstopable t :unstopables.get(target)){
                if(t.id.equals(id)){
                    t.removeUnstopable();
                }
            }
        }

        // Initialize the unstopables set if necessary.
        unstopables.computeIfAbsent(target, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));

        unstopableed.put(this.target, true);
        unstopables.get(target).add(this);
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(that, 0L, 1);
    }
    private void tick() {
        if (duration == 0 || !target.isValid() || target.isDead()) {
            removeUnstopable();
            return;
        }
        if(!Timestop.isTimestopped(target)){
            duration--;
        }
    }

    public static boolean isUnstopable(@Nullable Entity target) {
        if(target == null){
            return false;
        }
        return Boolean.TRUE.equals(unstopableed.get(target));
    }

    private void removeUnstopable() {
        if (unstopables.containsKey(target)) {
            unstopables.get(target).remove(this);
            timerTask.cancel();
            duration = 0;
            if (unstopables.get(target).isEmpty()) {
                unstopableed.put(target, false);
                unstopables.get(target).clear();
            }
        }
    }

    public static void clear() {
        for (Entity entity : unstopables.keySet()) {
            removeAll(entity);
        }
        unstopableed.clear();
    }

    public static void removeAll(Entity target) {
        if (unstopables.containsKey(target)) {
            for (Unstopable t : unstopables.get(target)) {
                t.removeUnstopable();
            }
            unstopables.get(target).clear();
            unstopableed.put(target, false);
        }
    }

    public static void remove(Entity target, String identifier) {
        UUID id;
        try {
            id = UUID.fromString(identifier);
        } catch (IllegalArgumentException e) {
            id = null;
        }
        if (unstopables.containsKey(target)) {
            for(Unstopable t: unstopables.get(target)){
                if(id != null && t.id.equals(id) || t.name.equals(identifier)){
                    t.removeUnstopable();
                }
            }
        }
    }

    public static class UnstopableHandler implements Listener {
        public UnstopableHandler(){}
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
