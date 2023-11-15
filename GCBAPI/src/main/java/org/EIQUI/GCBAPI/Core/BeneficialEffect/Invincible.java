package org.EIQUI.GCBAPI.Core.BeneficialEffect;

import org.EIQUI.GCBAPI.Core.CC.Timestop;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class Invincible {
    private static final Map<Entity, Set<Invincible>> invincibles = new ConcurrentHashMap<>();
    private static final Map<Entity, Boolean> invincibleed = new ConcurrentHashMap<>();

    private Entity caster;
    private Entity target;
    private String name;
    private UUID id;
    private int duration;

    private BukkitTask timerTask;

    public Invincible(@Nullable Entity caster, Entity target, int duration, String name, UUID id) {
        this.duration = duration;
        if(caster == null){
            this.caster = target;
        }else{
            this.caster = caster;
        }
        this.target = target;
        this.name = name;
        this.id = id;

        if(Boolean.TRUE.equals(invincibleed.get(target))){
            for(Invincible t :invincibles.get(target)){
                if(t.id.equals(id)){
                    t.name = this.name;
                    t.duration = this.duration;
                    t.caster = this.caster;
                    return;
                }
            }
        }

        invincibles.computeIfAbsent(target, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));

        invincibleed.put(this.target, true);
        invincibles.get(target).add(this);
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(that, 0L, 1);
    }
    private void tick() {
        if (duration == 0 || !target.isValid() || target.isDead()) {
            removeInvincible();
            return;
        }
        if(!Timestop.isTimestopped(target)){
            duration--;
        }
    }

    public static boolean isInvincible(@Nullable Entity target) {
        if (target == null){
            return false;
        }
        return Boolean.TRUE.equals(invincibleed.get(target));
    }

    public void removeInvincible() {
        if (invincibles.containsKey(target)) {
            invincibles.get(target).remove(this);
            duration = 0;
            if (timerTask != null){
                timerTask.cancel();
            }
            if (invincibles.get(target).isEmpty()) {
                invincibleed.put(target,false);
                invincibles.get(target).clear();
            }
        }
    }

    public static void clear() {
        for (Entity entity : invincibles.keySet()) {
            removeAll(entity);
        }
        invincibleed.clear();
    }

    public static void removeAll(Entity target) {
        if (invincibles.containsKey(target)) {
            for (Invincible t : invincibles.get(target)) {
                t.removeInvincible();
            }
            invincibles.get(target).clear();
            invincibleed.put(target, false);
        }
    }

    public static void remove(Entity target, String identifier) {
        UUID id;
        try {
            id = UUID.fromString(identifier);
        } catch (IllegalArgumentException e) {
            id = null;
        }
        if (invincibles.containsKey(target)) {
            for(Invincible t: invincibles.get(target)){
                if(id != null && t.id.equals(id) || t.name.equals(identifier)){
                    t.removeInvincible();
                }
            }
        }
    }

    public static class InvincibleHandler implements Listener {
        public InvincibleHandler(){}
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
