package org.EIQUI.GCBAPI.Core.BehavioralEffect;

import org.EIQUI.GCBAPI.Core.CC.Timestop;
import org.EIQUI.GCBAPI.PacketAPI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class GlowTo {

    private Entity target;
    private int duration;
    private String color;
    private Player seer;
    private static final Map<Entity, Set<GlowTo>> GLOWTO = new ConcurrentHashMap<>();
    private BukkitTask timerTask;

    public GlowTo(Entity target, Player seer,String color, int duration) {
        this.duration = duration;
        this.target = target;
        this.color = getValidColor(color);
        this.seer = seer;

        remove(target,seer);

        GLOWTO.computeIfAbsent(target, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
        GLOWTO.get(target).add(this);
        PacketAPI.sendGlow(seer,target,color);
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(that, 0L, 1);
    }
    private void tick() {
        if (duration == 0 || !target.isValid() || !seer.isValid()) {
            removeGlowTo();
            return;
        }
        if(!Timestop.isTimestopped(target)){
            duration--;
            if(duration%5 == 0){
                PacketAPI.sendGlow(seer,target,color);
            }
        }else{
            PacketAPI.sendGlow(seer,target,color);
        }
    }
    public void removeGlowTo() {
        duration = 0;
        timerTask.cancel();
        GLOWTO.get(target).remove(this);
        PacketAPI.sendTeamRemove(seer,target,color);
        if(GLOWTO.get(target).isEmpty()){
            PacketAPI.sendGlowRemove(seer,target);
            GLOWTO.get(target).clear();
        }
    }

    public static boolean isGlowTo(@Nullable Entity target, @Nullable Player seer) {
        if (target == null || seer == null){
            return false;
        }
        if (GLOWTO.containsKey(target)) {
            for(GlowTo g: GLOWTO.get(target)){
                if(g.seer.equals(seer)){
                    return true;
                }
            }
        }
        return false;
    }

    public static void clear() {
        for (Entity entity : GLOWTO.keySet()) {
            removeAll(entity);
        }
    }

    public static void remove(Entity target,Player seer) {
        if (GLOWTO.containsKey(target)) {
            for(GlowTo g: GLOWTO.get(target)){
                if(g.seer.equals(seer)){
                    g.removeGlowTo();
                }
            }
        }
    }

    public static void removeSeer(Player seer) {
        for(Set<GlowTo> set: GLOWTO.values()) {
            for (GlowTo g : set) {
                if (g.seer.equals(seer)) {
                    g.removeGlowTo();
                }
            }
        }
    }

    public static void removeAll(Entity target) {
        if (GLOWTO.containsKey(target)) {
            for(GlowTo g: GLOWTO.get(target)){
                g.removeGlowTo();
            }
        }
    }
    private static String getValidColor(String color){
        if( !color.equalsIgnoreCase("0") &&
                !color.equalsIgnoreCase("1")&&
                !color.equalsIgnoreCase("2")&&
                !color.equalsIgnoreCase("3")&&
                !color.equalsIgnoreCase("4")&&
                !color.equalsIgnoreCase("5")&&
                !color.equalsIgnoreCase("6")&&
                !color.equalsIgnoreCase("7")&&
                !color.equalsIgnoreCase("8")&&
                !color.equalsIgnoreCase("9")&&
                !color.equalsIgnoreCase("a")&&
                !color.equalsIgnoreCase("b")&&
                !color.equalsIgnoreCase("c")&&
                !color.equalsIgnoreCase("d")&&
                !color.equalsIgnoreCase("e")&&
                !color.equalsIgnoreCase("f")){
            return "&f";
        }
        return color;
    }

    public static class GLOWTOHandler implements Listener {
        public GLOWTOHandler(){}
        @EventHandler(priority = EventPriority.MONITOR)
        public void onDeath(EntityDeathEvent e){
            removeAll(e.getEntity());
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onQuit(PlayerQuitEvent e){
            removeAll(e.getPlayer());
            removeSeer(e.getPlayer());
        }
    }
}
