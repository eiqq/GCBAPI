package org.EIQUI.GCBAPI.Core.BeneficialEffect;

import org.EIQUI.GCBAPI.Core.CC.Timestop;
import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class Untargetable {
    private static final Map<Entity, Set<Untargetable>> untargetables = new ConcurrentHashMap<>();
    private static final Map<Entity, Boolean> untargetableed = new ConcurrentHashMap<>();

    private Entity caster;
    private Entity target;
    private String name;
    private UUID id;
    private int duration;

    private BukkitTask timerTask;

    public Untargetable(@Nullable Entity caster, Entity target, int duration, String name, UUID id) {
        this.duration = duration;
        if(caster == null){
            this.caster = target;
        }else{
            this.caster = caster;
        }
        this.target = target;
        this.name = name;
        this.id = id;

        if(Boolean.TRUE.equals(untargetableed.get(target))){
            for(Untargetable t :untargetables.get(target)){
                if(t.id.equals(id)){
                    t.duration = this.duration;
                    t.name = this.name;
                    t.caster = this.caster;
                    return;
                }
            }
        }

        // Initialize the untargetables set if necessary.
        untargetables.computeIfAbsent(target, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));

        untargetableed.put(this.target, true);
        untargetables.get(target).add(this);
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(that, 0L, 1); // assuming 1 tick = 1 second for simplicity
    }
    private void tick() {
        if (duration == 0 || !target.isValid() || target.isDead()) {
            removeUntargetable();
            return;
        }
        if(!Timestop.isTimestopped(target)){
            duration--;
        }
    }

    public static boolean isUntargetable(@Nullable Entity target) {
        if (target == null){
            return false;
        }
        return Boolean.TRUE.equals(untargetableed.get(target));
    }

    private void removeUntargetable() {
        if (untargetables.containsKey(target)) {
            untargetables.get(target).remove(this);
            timerTask.cancel();
            duration = 0;
            if (untargetables.get(target).isEmpty()) {
                untargetableed.put(target, false);
                untargetables.get(target).clear();
            }
        }
    }

    public static void clear() {
        for (Entity entity : untargetables.keySet()) {
            removeAll(entity);
        }
        untargetableed.clear();
    }

    public static void removeAll(Entity target) {
        if (untargetables.containsKey(target)) {
            for (Untargetable t : untargetables.get(target)) {
                t.removeUntargetable();
            }
            untargetables.get(target).clear();
            untargetableed.put(target, false);
        }
    }

    public static void remove(Entity target, String identifier) {
        UUID id;
        try {
            id = UUID.fromString(identifier);
        } catch (IllegalArgumentException e) {
            id = null;
        }
        if (untargetables.containsKey(target)) {
            for(Untargetable t: untargetables.get(target)){
                if(id != null && t.id.equals(id) || t.name.equals(identifier)){
                    t.removeUntargetable();
                }
            }
        }
    }

    public static boolean checkIsUntargetableEntity(@Nullable Entity target){
        if(target == null || target.isDead() || !target.isValid()){
            return true;
        }
        EntityType type = target.getType();
        if(!type.isAlive()){
            if(type != EntityType.INTERACTION){
                return true;
            }
        }else if (type == EntityType.PLAYER){
            if (((HumanEntity)target).getGameMode() == GameMode.SPECTATOR){
                return true;
            }
        }else if(type == EntityType.ARMOR_STAND) {
            if (((ArmorStand)target).isMarker()) {
                return true;
            }
        }
        return (target.getBoundingBox().getWidthX() < Vector.getEpsilon()
                && target.getBoundingBox().getWidthZ() < Vector.getEpsilon()
                && target.getBoundingBox().getHeight() < Vector.getEpsilon());
    }

    public static class UntargetableHandler implements Listener {
        public UntargetableHandler(){}
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
