package org.EIQUI.GCBAPI.Core.BehavioralEffect;

import org.EIQUI.GCBAPI.Core.CC.Timestop;
import org.EIQUI.GCBAPI.Core.glow.GlowAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

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
    private static final Map<Entity, Set<Player>> IS_GLOWTO = new ConcurrentHashMap<>();

    public GlowTo(Entity target, Player seer,@Nullable String color, int duration) {
        if(duration == 0 || color == null){
            remove(target,seer);
            return;
        }
        this.color = getValidColor(color);
        if (GLOWTO.containsKey(target)) {
            for(GlowTo g: GLOWTO.get(target)){
                if(g.seer.equals(seer) && g.duration > duration && this.color.equals(g.color)){
                    return;
                }
            }
        }
        this.duration = duration;
        this.target = target;
        this.seer = seer;

        GLOWTO.computeIfAbsent(this.target, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
        IS_GLOWTO.computeIfAbsent(this.target, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
        GLOWTO.get(this.target).add(this);
        IS_GLOWTO.get(this.target).add(this.seer);
        GlowAPI.setGlow(target,seer,GlowAPI.getColor(this.color));
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!tick()){
                    cancel();
                }
            }
        }.runTaskTimer(that, 0L, 1);
    }

    private void updateColor(){
        if (GLOWTO.containsKey(target) && !GLOWTO.get(target).isEmpty()) {
            GlowTo minimumDuration = this;
            for(GlowTo g: GLOWTO.get(target)){
                if(minimumDuration.duration <= 0){
                    minimumDuration = g;
                }else if(minimumDuration.duration > g.duration){
                    minimumDuration = g;
                }
            }
            if(minimumDuration.duration == 0){
                for(Player p : Bukkit.getOnlinePlayers()){
                    GlowAPI.unsetGlow(target,p);
                }
            }else{
                GlowAPI.setGlow(target,seer,GlowAPI.getColor(minimumDuration.color));
            }
            return;
        }
        for(Player p : Bukkit.getOnlinePlayers()){
            GlowAPI.unsetGlow(target,p);
        }
    }
    private boolean tick() {
        if (duration == 0 || target.isDead() || !target.isValid() || !seer.isValid()) {
            removeGlowTo();
            return false;
        }
        if(!Timestop.isTimestopped(target)) {
            duration--;
        }
        return true;
    }
    public void removeGlowTo() {
        duration = 0;
        if(GLOWTO.containsKey(target)){
            GLOWTO.get(target).remove(this);
        }
        if(IS_GLOWTO.containsKey(target)){
            IS_GLOWTO.get(target).remove(seer);
        }
        updateColor();
    }

    public static boolean isGlowTo(@Nullable Entity target, @Nullable Player seer) {
        if(target == null || seer == null || !target.isValid() || !seer.isValid()){
            return false;
        }
        return GlowAPI.isGlowTo(target,seer) || IS_GLOWTO.containsKey(target) && IS_GLOWTO.get(target).contains(seer);
    }

    public static void clear() {
        for (Entity entity : GLOWTO.keySet()) {
            removeAll(entity);
        }
        GLOWTO.clear();
    }

    public static void remove(Entity target,Player seer,String color) {
        if (GLOWTO.containsKey(target)) {
            String c = getValidColor(color);
            for(GlowTo g: GLOWTO.get(target)){
                if(g.seer.equals(seer) && g.color.equalsIgnoreCase(c)){
                    g.removeGlowTo();
                }
            }
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
        color = color.replaceAll("&","");
        color = color.toLowerCase();
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
            return "f";
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
