package org.EIQUI.GCBAPI.Core.BehavioralEffect;

import org.EIQUI.GCBAPI.Core.CC.Timestop;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class CannotBaseAttack {
    private static final Map<Entity, CannotBaseAttack> cannotbaseattack = new ConcurrentHashMap<>();
    private static final Map<Entity, Boolean> cannotbaseattacked = new ConcurrentHashMap<>();

    private LivingEntity target;
    private int duration;

    private BukkitTask timerTask;

    public CannotBaseAttack(LivingEntity target, int duration) {
        this.duration = duration;
        this.target = target;

        if(isCannotBaseAttack(target)){
            remove(target);
        }

        cannotbaseattack.put(target,this);
        cannotbaseattacked.put(target, true);
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
    public static boolean isCannotBaseAttack(@Nullable Entity target) {
        if (target == null){
            return false;
        }
        return Boolean.TRUE.equals(cannotbaseattacked.get(target));
    }

    public static void clear() {
        for (Entity entity : cannotbaseattack.keySet()) {
            remove(entity);
        }
        cannotbaseattacked.clear();
    }

    public static void remove(Entity target) {
        if (isCannotBaseAttack(target)) {
            cannotbaseattacked.put(target, false);
            cannotbaseattack.get(target).duration = 0;
            cannotbaseattack.get(target).timerTask.cancel();
            cannotbaseattack.remove(target);
        }
    }

    public static class CannotBaseAttackHandler implements Listener {
        public CannotBaseAttackHandler(){}
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
