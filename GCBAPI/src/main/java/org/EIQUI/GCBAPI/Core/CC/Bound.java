package org.EIQUI.GCBAPI.Core.CC;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class Bound {
    private static final Map<LivingEntity, Set<Bound>> bounds = new ConcurrentHashMap<>();
    private static final Map<LivingEntity, Boolean> bounded = new ConcurrentHashMap<>();

    private Entity caster;
    private LivingEntity target;
    private final String name;
    private final UUID id;
    private int duration;

    private BukkitTask timerTask;

    private static final AttributeModifier BOUND_MODIFIER =
            new AttributeModifier(UUID.randomUUID(), "BOUND", -2, AttributeModifier.Operation.MULTIPLY_SCALAR_1);

    public Bound(@Nullable Entity caster, LivingEntity target, int duration, String name, UUID id) {
        this.duration = duration;
        if(caster == null){
            this.caster = target;
        }else{
            this.caster = caster;
        }
        this.target = target;
        this.name = name;
        this.id = id;

        if(bounds.containsKey(target)){
            for(Bound t :bounds.get(target)){
                if(t.id.equals(id)){
                    t.removeBound();
                }
            }
        }
        // Initialize the bounds set if necessary.
        bounds.computeIfAbsent(target, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
        bounds.get(target).add(this);
        BoundEffect();
        bounded.put(target, true);
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(that, 0L, 1);
    }
    private void tick() {
        if (duration == 0 || !target.isValid() || target.isDead()) {
            removeBound();
            return;
        }
        if(!Timestop.isTimestopped(target)){
            duration--;
        }
    }
    private void BoundEffect() {
        if (Boolean.TRUE.equals(bounded.get(target))) {
            return;
        }
        target.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(BOUND_MODIFIER);
        BukkitTask efftask = new BukkitRunnable() {
            @Override
            public void run() {
                if (Boolean.FALSE.equals(bounded.get(target))) {
                    this.cancel();
                    target.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(BOUND_MODIFIER);
                }
            }
        }.runTaskTimer(that, 0L, 1);
    }

    public static boolean isBounded(@Nullable Entity target) {
        if (target == null){
            return false;
        }
        return Boolean.TRUE.equals(bounded.get(target));
    }

    public void removeBound() {
        if (bounds.containsKey(target)) {
            bounds.get(target).remove(this);
            timerTask.cancel();
            duration = 0;
            if (bounds.get(target).isEmpty()) {
                bounded.put(target, false);
                bounds.get(target).clear();
                target.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(BOUND_MODIFIER);
            }
        }
    }

    public static void clear() {
        for (LivingEntity entity : bounds.keySet()) {
            removeAll(entity);
        }
        bounded.clear();
    }

    public static void removeAll(LivingEntity target) {
        if (Boolean.TRUE.equals(bounded.get(target))) {
            for (Bound b : bounds.get(target)) {
                b.removeBound();
            }
            bounds.get(target).clear();
            bounded.put(target, false);
            target.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(
                            new AttributeModifier("BOUND", -1, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
        }
    }

    public static void remove(Entity target, String identifier) {
        UUID id;
        try {
            id = UUID.fromString(identifier);
        } catch (IllegalArgumentException e) {
            id = null;
        }
        if (bounds.containsKey(target)) {
            for(Bound b: bounds.get(target)){
                if(id != null && b.id.equals(id) || b.name.equals(identifier)){
                    b.removeBound();
                }
            }
        }
    }
    public static class BoundHandler implements Listener {
        public BoundHandler(){}
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
