package org.EIQUI.GCBAPI.Core.CC;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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

public class Slow {
    private static final Map<LivingEntity, Set<Slow>> slows = new ConcurrentHashMap<>();
    private static final Map<LivingEntity, Boolean> slowed = new ConcurrentHashMap<>();

    private Entity caster;
    private LivingEntity target;
    private String name;
    private UUID id;
    private int duration;
    private double power;

    private BukkitTask timerTask;
    private AttributeModifier slowModifier;

    public Slow(@Nullable Entity caster, LivingEntity target, int duration, double power, String name, UUID id) {
        this.duration = duration;
        if(caster == null){
            this.caster = target;
        }else{
            this.caster = caster;
        }
        this.target = target;
        this.name = name;
        this.power = -Math.abs(power);
        this.id = id;
        if (this.power < -1){
            this.power = -1;
        }

        if(slows.containsKey(target)){
            for(Slow t :slows.get(target)){
                if(t.id.equals(id)){
                    t.removeSlow();
                }
            }
        }

        slows.computeIfAbsent(target, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
        slows.get(target).add(this);
        slowed.put(target, true);
        slowModifier = new AttributeModifier(id, name, this.power, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
        this.target.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(slowModifier);
        this.timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(that, 0L, 1);
    }
    private void tick() {
        if (duration == 0 || !target.isValid() || target.isDead()) {
            removeSlow();
            return;
        }
        if(!Timestop.isTimestopped(target)){
            duration--;
        }
    }

    public static boolean isSlowed(@Nullable Entity target) {
        if (target == null){
            return false;
        }
        return Boolean.TRUE.equals(slowed.get(target));
    }

    public void setPower(double p){
        target.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(slowModifier);
        slowModifier = new AttributeModifier(id, name, p, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
        target.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(slowModifier);
        power = p;
    }
    public double getPower(){
        return power;
    }

    public static void setPower(LivingEntity ent,String identifier, double p){
        if (!slows.containsKey(ent)){
            return;
        }
        UUID id;
        try {
            id = UUID.fromString(identifier);
        } catch (IllegalArgumentException e) {
            id = null;
        }
        for(Slow s : slows.get(ent)){
            if(id != null && s.id.equals(id) || s.name.equals(identifier)){
                s.setPower(p);
            }
        }
    }

    public static double getPower(LivingEntity ent,String identifier){
        if (!slows.containsKey(ent)){
            return 0;
        }
        UUID id;
        try {
            id = UUID.fromString(identifier);
        } catch (IllegalArgumentException e) {
            id = null;
        }
        for(Slow s : slows.get(ent)){
            if(id != null && s.id.equals(id) || s.name.equals(identifier)){
                return s.getPower();
            }
        }
        return 0;
    }
    public void removeSlow() {
        if (slows.containsKey(target)) {
            target.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(this.slowModifier);
            slows.get(target).remove(this);
            duration = 0;
            if(timerTask != null){
                timerTask.cancel();
            }
            if (slows.get(target).isEmpty()) {
                slowed.put(target, false);
                slows.get(target).clear();
            }
        }
    }

    public static void clear() {
        for (LivingEntity entity : slows.keySet()) {
            removeAll(entity);
        }
        slowed.clear();
    }

    public static void removeAll(LivingEntity target) {
        if (Boolean.TRUE.equals(slowed.get(target))) {
            for (Slow t : slows.get(target)) {
                t.removeSlow();
            }
            slows.get(target).clear();
            slowed.put(target, false);
        }
    }

    public static void remove(LivingEntity target, String identifier) {
        UUID id;
        try {
            id = UUID.fromString(identifier);
        } catch (IllegalArgumentException e) {
            id = null;
        }

        if (slows.containsKey(target)) {
            Set<Slow> stops = slows.get(target);
            UUID finalId = id;
            stops.removeIf(t -> (finalId != null && t.id.equals(finalId)) || t.name.equals(identifier));
            if (stops.isEmpty()) {
                slowed.put(target, false);
            }
        }
    }
    public static class SlowHandler implements Listener {
        public SlowHandler(){}
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
