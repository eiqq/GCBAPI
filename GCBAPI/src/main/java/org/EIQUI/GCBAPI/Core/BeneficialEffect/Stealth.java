package org.EIQUI.GCBAPI.Core.BeneficialEffect;

import org.EIQUI.GCBAPI.Core.BehavioralEffect.GlowTo;
import org.EIQUI.GCBAPI.Core.CC.Timestop;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class Stealth {
    private static final Map<Entity, Set<Stealth>> stealths = new ConcurrentHashMap<>();
    private static final Map<Entity, Boolean> stealthed = new HashMap<>();

    private Entity caster;
    private Entity target;
    private final String name;
    private final UUID id;
    private int duration;

    private BukkitTask timerTask;

    public Stealth(@Nullable Entity caster, Entity target, int duration, String name, UUID id) {
        this.duration = duration;
        if(caster == null){
            this.caster = target;
        }else{
            this.caster = caster;
        }
        this.target = target;
        this.name = name;
        this.id = id;

        if(Boolean.TRUE.equals(stealthed.get(target))){
            for(Stealth t :stealths.get(target)){
                if(t.id.equals(id)){
                    t.duration = duration;
                    t.caster = caster;
                    return;
                }
            }
        }

        stealths.computeIfAbsent(target, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));

        stealthed.put(this.target, true);
        stealths.get(target).add(this);
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(that, 0L, 1);
    }
    private void tick() {
        if (duration == 0 || !target.isValid() || target.isDead()) {
            removeStealth();
            return;
        }
        if(!Timestop.isTimestopped(target)){
            duration--;
        }
    }
    public static boolean isStealth(@Nullable Entity target) {
        if (target == null){
            return false;
        }
        return Boolean.TRUE.equals(stealthed.get(target));
    }

    public void removeStealth() {
        if (stealths.containsKey(target)) {
            stealths.get(target).remove(this);
            timerTask.cancel();
            duration = 0;
            if (stealths.get(target).isEmpty()) {
                stealthed.put(target, false);
                stealths.get(target).clear();
            }
        }
    }

    public static boolean hasStealth(Entity e, String identifier) {
        if(!isStealth(e)){
            return false;
        }
        UUID id;
        try {
            id = UUID.fromString(identifier);
        } catch (IllegalArgumentException em) {
            id = null;
        }
        for (Stealth b : stealths.get(e)) {
            if (id != null && b.id.equals(id) || b.name.equals(identifier)) {
                return true;
            }
        }
        return false;
    }

    public static void clear() {
        for (Entity entity : stealths.keySet()) {
            removeAll(entity);
        }
        stealthed.clear();
    }

    public static void removeAll(Entity target) {
        if (stealths.containsKey(target)) {
            for (Stealth t : stealths.get(target)) {
                t.removeStealth();
            }
            stealths.get(target).clear();
            stealthed.put(target, false);
        }
    }

    public static void remove(Entity target, String identifier) {
        UUID id;
        try {
            id = UUID.fromString(identifier);
        } catch (IllegalArgumentException e) {
            id = null;
        }
        if (stealths.containsKey(target)) {
            for(Stealth t: stealths.get(target)){
                if(id != null && t.id.equals(id) || t.name.equals(identifier)){
                    t.removeStealth();
                }
            }
        }
    }


    public static boolean canSeeStealth(@Nullable Player p, @Nullable Entity e){
        if(p == null || e == null || !e.isValid()){
            return false;
        }
        if (p.getGameMode().equals(GameMode.SPECTATOR)) {
            return true;
        }
        if(!isStealth(e)){
            return true;
        }
        if(GlowTo.isGlowTo(e,p) || e.isGlowing()
                || (e instanceof LivingEntity && ((LivingEntity)e).hasPotionEffect(PotionEffectType.GLOWING)) ){
            return true;
        }
        return false;
    }
    public static class StealthHandler implements Listener {
        public StealthHandler(){}
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
