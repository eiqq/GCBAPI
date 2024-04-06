package org.EIQUI.GCBAPI.Core.BehavioralEffect;

import org.EIQUI.GCBAPI.Core.CC.Timestop;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class JumpBlock {
    private static final Map<Entity, JumpBlock> jumpblock = new ConcurrentHashMap<>();
    private static final Map<Entity, Boolean> jumpblocked = new HashMap<>();

    private LivingEntity target;
    private int duration;

    private BukkitTask timerTask;

    public JumpBlock(LivingEntity target, int duration) {
        this.duration = duration;
        this.target = target;

        if(isJumpBlock(target)){
            remove(target);
        }

        jumpblock.put(target,this);
        jumpblocked.put(target, true);
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
            target.removePotionEffect(PotionEffectType.JUMP);
            return;
        }
        if(target.getType().equals(EntityType.PLAYER)){
            target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP,5,128,false,false,false));
        }
        if(!Timestop.isTimestopped(target)){
            duration--;
        }
    }
    public static boolean isJumpBlock(@Nullable Entity target) {
        if (target == null){
            return false;
        }
        return Boolean.TRUE.equals(jumpblocked.get(target));
    }

    public static void clear() {
        for (Entity entity : jumpblock.keySet()) {
            remove(entity);
        }
        jumpblocked.clear();
    }

    public static void remove(Entity target) {
        if (isJumpBlock(target)) {
            jumpblocked.put(target, false);
            jumpblock.get(target).duration = 0;
            jumpblock.get(target).timerTask.cancel();
            jumpblock.remove(target);
        }
    }

    public static class JumpBlockHandler implements Listener {
        public JumpBlockHandler(){}
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
