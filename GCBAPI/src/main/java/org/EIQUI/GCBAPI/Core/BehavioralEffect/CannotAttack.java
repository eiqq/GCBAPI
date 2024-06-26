package org.EIQUI.GCBAPI.Core.BehavioralEffect;

import org.EIQUI.GCBAPI.Core.CC.Timestop;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class CannotAttack {
    private static final Map<Entity, CannotAttack> cannotattack = new ConcurrentHashMap<>();
    private static final Map<Entity, Boolean> cannotattacked = new HashMap<>();

    private LivingEntity target;
    private int duration;

    private BukkitTask timerTask;

    public CannotAttack(LivingEntity target, int duration) {
        this.duration = duration;
        this.target = target;

        if(isCannotAttack(target)){
            remove(target);
        }

        cannotattack.put(target,this);
        cannotattacked.put(target, true);
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
    public static boolean isCannotAttack(@Nullable Entity target) {
        if (target == null){
            return false;
        }
        return Boolean.TRUE.equals(cannotattacked.get(target));
    }

    public static void clear() {
        for (Entity entity : cannotattack.keySet()) {
            remove(entity);
        }
        cannotattacked.clear();
    }

    public static void remove(Entity target) {
        if (isCannotAttack(target)) {
            cannotattacked.put(target, false);
            cannotattack.get(target).duration = 0;
            cannotattack.get(target).timerTask.cancel();
            cannotattack.remove(target);
        }
    }

    public static class CannotAttackHandler implements Listener {
        public CannotAttackHandler(){}
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
