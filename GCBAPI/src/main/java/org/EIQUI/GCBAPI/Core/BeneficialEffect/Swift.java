package org.EIQUI.GCBAPI.Core.BeneficialEffect;

import org.EIQUI.GCBAPI.Core.CC.Timestop;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class Swift {
    private static final Map<LivingEntity, Set<Swift>> swifts = new ConcurrentHashMap<>();
    private static final Map<LivingEntity, Boolean> swifted = new ConcurrentHashMap<>();

    private Entity caster;
    private LivingEntity target;
    private final String name;
    private final UUID id;
    private int duration;
    private double power;
    private BukkitTask timerTask;
    private AttributeModifier swiftModifier;

    public Swift(@Nullable Entity caster, LivingEntity target, int duration, double power, String name, UUID id) {
        this.duration = duration;
        if(caster == null){
            this.caster = target;
        }else{
            this.caster = caster;
        }
        this.power = power;
        this.target = target;
        this.name = name;
        this.id = id;

        if(Boolean.TRUE.equals(swifted.get(target))){
            for(Swift t :swifts.get(target)){
                if(t.id.equals(id)){
                    t.removeSwift();
                }
            }
        }
        swifts.computeIfAbsent(target, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));

        swifted.put(this.target, true);
        swifts.get(target).add(this);
        swiftModifier = new AttributeModifier(id, name, this.power, AttributeModifier.Operation.ADD_SCALAR);
        this.target.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(swiftModifier);

        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(that, 0L, 1);
    }
    private void tick() {
        if (duration == 0 || !target.isValid() || target.isDead()) {
            removeSwift();
            return;
        }
        if(!Timestop.isTimestopped(target)){
            duration--;
        }
    }

    public static boolean isSwift(@Nullable Entity target) {
        if (target == null){
            return false;
        }
        return Boolean.TRUE.equals(swifted.get(target));
    }

    public void removeSwift() {
        if (swifts.containsKey(target)) {
            swifts.get(target).remove(this);
            timerTask.cancel();
            duration = 0;
            target.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(swiftModifier);
            if (swifts.get(target).isEmpty()) {
                swifted.put(target, false);
                swifts.get(target).clear();
            }
        }
    }

    public static void clear() {
        for (LivingEntity entity : swifts.keySet()) {
            removeAll(entity);
        }
        swifted.clear();
    }

    public static void removeAll(LivingEntity target) {
        if (swifts.containsKey(target)) {
            for (Swift t : swifts.get(target)) {
                t.removeSwift();
            }
            swifts.get(target).clear();
            swifted.put(target, false);
        }
    }

    public static void remove(Entity target, String identifier) {
        UUID id;
        try {
            id = UUID.fromString(identifier);
        } catch (IllegalArgumentException e) {
            id = null;
        }
        if (swifts.containsKey(target)) {
            for(Swift t: swifts.get(target)){
                if(id != null && t.id.equals(id) || t.name.equals(identifier)){
                    t.removeSwift();
                }
            }
        }
    }

    public static class SwiftHandler implements Listener {
        public SwiftHandler(){}
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
