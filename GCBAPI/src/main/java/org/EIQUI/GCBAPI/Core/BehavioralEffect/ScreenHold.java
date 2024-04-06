package org.EIQUI.GCBAPI.Core.BehavioralEffect;

import org.EIQUI.GCBAPI.Core.CC.Timestop;
import org.EIQUI.GCBAPI.Core.PacketAPI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class ScreenHold {
    private static final Map<Entity, ScreenHold> screenhold = new ConcurrentHashMap<>();
    private static final Map<Entity, Boolean> screenholded = new HashMap<>();

    private Entity target;
    private int duration;
    private float yaw;
    private float pitch;

    private BukkitTask timerTask;

    public ScreenHold(Entity target,float yaw,float pitch, int duration) {
        this.duration = duration;
        this.target = target;
        this.yaw = yaw;
        this.pitch = pitch;

        if(isScreenHold(target)){
            remove(target);
        }

        screenhold.put(target,this);
        screenholded.put(target, true);

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
        }
        if(!Timestop.isTimestopped(target)){
            if (target.getType().equals(EntityType.PLAYER)){
                PacketAPI.sendLookAt((Player) target,yaw,pitch);
            }else{
                target.setRotation(yaw,pitch);
            }
            duration--;
        }
    }
    public static boolean isScreenHold(@Nullable Entity target) {
        if (target == null){
            return false;
        }
        return Boolean.TRUE.equals(screenholded.get(target));
    }

    public static void clear() {
        for (Entity entity : screenhold.keySet()) {
            remove(entity);
        }
        screenholded.clear();
    }

    public static void remove(Entity target) {
        if (isScreenHold(target)) {
            screenholded.put(target, false);
            screenhold.get(target).duration = 0;
            screenhold.get(target).timerTask.cancel();
            screenhold.remove(target);
        }
    }

    public static class ScreenHoldHandler implements Listener {
        public ScreenHoldHandler(){}
        @EventHandler(priority = EventPriority.MONITOR)
        public void onTeleport(EntityTeleportEvent e){
            if(isScreenHold(e.getEntity())){
                screenhold.get(e.getEntity()).yaw = e.getTo().getYaw();
                screenhold.get(e.getEntity()).pitch = e.getTo().getPitch();
            }
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerTeleport(PlayerTeleportEvent e){
            if(isScreenHold(e.getPlayer())){
                screenhold.get(e.getPlayer()).yaw = e.getTo().getYaw();
                screenhold.get(e.getPlayer()).pitch = e.getTo().getPitch();
            }
        }
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
