package org.EIQUI.GCBAPI.Core.BehavioralEffect;

import org.EIQUI.GCBAPI.Core.CC.Timestop;
import org.EIQUI.GCBAPI.Core.UnitVector;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
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

public class Hold {
    private static final Map<Entity, Hold> hold = new ConcurrentHashMap<>();
    private static final Map<Entity, Boolean> holded = new HashMap<>();

    private Entity target;
    private int duration;
    private Location loc;

    private BukkitTask timerTask;

    public Hold(Entity target,Location l, int duration) {
        this.duration = duration;
        this.target = target;
        this.loc = l;

        if(isHold(target)){
            remove(target);
        }

        hold.put(target,this);
        holded.put(target, true);

        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if(!tick()){
                    cancel();
                }
            }
        }.runTaskTimer(that, 0L, 1);
    }
    private boolean tick() {
        if (duration == 0 || !target.isValid() || target.isDead()) {
            remove(target);
            return false;
        }
        if(!Timestop.isTimestopped(target)){
            if(UnitVector.hasVector(target)){
                loc = target.getLocation();
            }else if(!target.isInsideVehicle()){
                target.teleport(loc);
            }
            duration--;
        }
        return true;
    }
    public static boolean isHold(@Nullable Entity target) {
        if (target == null){
            return false;
        }
        return Boolean.TRUE.equals(holded.get(target));
    }

    public static void clear() {
        for (Entity entity : hold.keySet()) {
            remove(entity);
        }
        holded.clear();
    }

    public static void remove(Entity target) {
        if (isHold(target)) {
            holded.put(target, false);
            hold.get(target).duration = 0;
            hold.get(target).timerTask.cancel();
            hold.remove(target);
        }
    }

    public static class HoldHandler implements Listener {
        public HoldHandler(){}
        @EventHandler(priority = EventPriority.MONITOR)
        public void onTeleport(EntityTeleportEvent e){
            if(isHold(e.getEntity())){
                hold.get(e.getEntity()).loc = e.getTo();
            }
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerTeleport(PlayerTeleportEvent e){
            if(isHold(e.getPlayer())){
                hold.get(e.getPlayer()).loc = e.getTo();
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
