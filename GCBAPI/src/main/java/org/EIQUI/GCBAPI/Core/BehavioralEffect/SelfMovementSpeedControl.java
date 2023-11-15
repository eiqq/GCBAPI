package org.EIQUI.GCBAPI.Core.BehavioralEffect;

import org.EIQUI.GCBAPI.Core.CC.Timestop;
import org.EIQUI.GCBAPI.Core.UnitVector;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class SelfMovementSpeedControl {
    private static final Map<LivingEntity, SelfMovementSpeedControl> selfmovementspeedcontrol = new ConcurrentHashMap<>();
    private static final Map<LivingEntity, Boolean> selfmovementspeedcontroled = new ConcurrentHashMap<>();

    private LivingEntity target;
    private int duration;
    private double power;

    private AttributeModifier modifier;
    private BukkitTask timerTask;

    public SelfMovementSpeedControl(LivingEntity target, int duration, double power) {
        this.duration = duration;
        this.target = target;
        this.power = power;

        if(isSelfMovementSpeedControl(target)){
            remove(target);
        }

        selfmovementspeedcontrol.put(target,this);
        selfmovementspeedcontroled.put(target, true);
        modifier = new AttributeModifier("SELFMSCONTROL", this.power, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
        target.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(modifier);
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(that, 0L, 1);
    }
    private void tick() {
        if (duration == 0 || !target.isValid() || target.isDead()) {
            remove(target);
            return;
        }
        if(!Timestop.isTimestopped(target)){
            duration--;
        }
    }
    public static boolean isSelfMovementSpeedControl(@Nullable Entity target) {
        if (target == null){
            return false;
        }
        return Boolean.TRUE.equals(selfmovementspeedcontroled.get(target));
    }

    public static void clear() {
        for (LivingEntity entity : selfmovementspeedcontrol.keySet()) {
            remove(entity);
        }
        selfmovementspeedcontroled.clear();
    }

    public static void remove(LivingEntity target) {
        if (isSelfMovementSpeedControl(target)) {
            selfmovementspeedcontroled.put(target, false);
            selfmovementspeedcontrol.get(target).duration = 0;
            selfmovementspeedcontrol.get(target).timerTask.cancel();
            if (selfmovementspeedcontrol.get(target).modifier != null){
                target.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                        .removeModifier(selfmovementspeedcontrol.get(target).modifier);
            }
            selfmovementspeedcontrol.remove(target);
        }
    }

    public static class SelfMovementSpeedControlHandler implements Listener {
        public SelfMovementSpeedControlHandler(){}
        @EventHandler(priority = EventPriority.MONITOR)
        public void onDeath(EntityDeathEvent e){
            remove(e.getEntity());
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onQuit(PlayerQuitEvent e){
            remove(e.getPlayer());
        }
    }
}
