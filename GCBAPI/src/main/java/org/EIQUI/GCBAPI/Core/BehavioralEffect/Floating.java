package org.EIQUI.GCBAPI.Core.BehavioralEffect;

import org.EIQUI.GCBAPI.Core.CC.Timestop;
import org.EIQUI.GCBAPI.Core.UnitVector;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class Floating {
    private static final Map<Entity, Floating> floating = new ConcurrentHashMap<>();
    private static final Map<Entity, Boolean> floatinged = new HashMap<>();

    private Entity target;
    private int duration;

    private BukkitTask timerTask;

    public Floating(Entity target, int duration) {
        this.duration = duration;
        this.target = target;

        if(isFloating(target)){
            remove(target);
        }

        floating.put(target,this);
        floatinged.put(target, true);

        if(target.hasGravity()){
            if(target.getType().equals(EntityType.PLAYER)){
                if (((Player)target).getGameMode() != GameMode.SPECTATOR){
                    target.setGravity(false);
                }
            }else{
                target.setGravity(false);
            }
        }

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
            if(target.getType().equals(EntityType.PLAYER)){
                if (((Player)target).getGameMode() != GameMode.SPECTATOR){
                    target.setGravity(true);
                }
            }else{
                target.setGravity(true);
            }
            return;
        }
        if(!Timestop.isTimestopped(target)){
            if(!UnitVector.hasVector(target)){
                target.setVelocity(new Vector(0,0,0));
            }
            duration--;
        }
    }
    public static boolean isFloating(@Nullable Entity target) {
        if (target == null){
            return false;
        }
        return Boolean.TRUE.equals(floatinged.get(target));
    }

    public static void clear() {
        for (Entity entity : floating.keySet()) {
            remove(entity);
        }
        floatinged.clear();
    }

    public static void remove(Entity target) {
        if (isFloating(target)) {
            floatinged.put(target, false);
            floating.get(target).duration = 0;
            floating.get(target).timerTask.cancel();
            floating.remove(target);
        }
    }

    public static class FloatingHandler implements Listener {
        public FloatingHandler(){}
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
