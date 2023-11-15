package org.EIQUI.GCBAPI.Core.CC;

import org.EIQUI.GCBAPI.PacketAPI;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class Stun {
    private static final Map<Entity, Set<Stun>> stuns = new ConcurrentHashMap<>();
    private static final Map<Entity, Boolean> stuned = new ConcurrentHashMap<>();
    private static final Map<Entity, Location> stunlocation = new ConcurrentHashMap<>();

    private Entity caster;
    private LivingEntity target;
    private String name;
    private UUID id;
    private int duration;

    private BukkitTask timerTask;

    public Stun(@Nullable Entity caster, LivingEntity target, int duration, String name, UUID id) {
        this.duration = duration;
        if(caster == null){
            this.caster = target;
        }else{
            this.caster = caster;
        }
        this.target = target;
        this.name = name;
        this.id = id;

        if(stuns.containsKey(target)){
            for(Stun t :stuns.get(target)){
                if(t.id.equals(id)){
                    t.removeStun();
                }
            }
        }

        stuns.computeIfAbsent(target, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
        stunlocation.put(this.target, target.getLocation());
        stuns.get(target).add(this);
        if (target.getType().isAlive()) {
            stunEffect();
        }
        stuned.put(target, true);
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(that, 0L, 1);
    }
    private void tick() {
        if (duration == 0 || !target.isValid() || target.isDead()) {
            removeStun();
            return;
        }
        if(!Timestop.isTimestopped(target)){
            duration--;
        }
    }
    private void stunEffect() {
        if (Boolean.TRUE.equals(stuned.get(target))) {
            return;
        }
        boolean b = false;
        if(target.getType().equals(EntityType.PLAYER)){
            b = true;
        }
        boolean finalB = b;
        PotionEffect potion = new PotionEffect(PotionEffectType.JUMP,5,128,false,false,false);
        PotionEffect potion2 = new PotionEffect(PotionEffectType.SLOW,5,10,false,false,false);
        BukkitTask efftask = new BukkitRunnable() {
            @Override
            public void run() {
                if (Boolean.FALSE.equals(stuned.get(target))) {
                    this.cancel();
                    target.removePotionEffect(PotionEffectType.JUMP);
                    target.removePotionEffect(PotionEffectType.SLOW);
                    return;
                }
                if(finalB){
                    if(!Timestop.isTimestopped(target)){
                        Location l = stunlocation.get(target);
                        PacketAPI.sendLookAt((Player) target,l.getYaw(),l.getPitch());
                    }
                    target.addPotionEffect(potion);
                }else{
                    Location l = stunlocation.get(target);
                    target.setRotation(l.getYaw(),l.getPitch());
                }
                target.addPotionEffect(potion2);
            }
        }.runTaskTimer(that, 0L, 1);
    }

    public static boolean isStuned(@Nullable Entity target) {
        if (target == null){
            return false;
        }
        return Boolean.TRUE.equals(stuned.get(target));
    }

    private void removeStun() {
        if (stuns.containsKey(target)) {
            stuns.get(target).remove(this);
            timerTask.cancel();
            duration = 0;
            if (stuns.get(target).isEmpty()) {
                stuned.put(target, false);
                stunlocation.remove(target);
            }
        }
    }

    public static void clear() {
        for (Entity entity : stuns.keySet()) {
            removeAll(entity);
        }
        stunlocation.clear();
        stuned.clear();
    }

    public static void removeAll(Entity target) {
        if (Boolean.TRUE.equals(stuned.get(target))) {
            for (Stun t : stuns.get(target)) {
                t.removeStun();
            }
            stuns.get(target).clear();
            stuned.put(target, false);
            stunlocation.remove(target);
        }
    }

    public static void remove(Entity target, String identifier) {
        UUID id;
        try {
            id = UUID.fromString(identifier);
        } catch (IllegalArgumentException e) {
            id = null;
        }
        if (stuns.containsKey(target)) {
            for(Stun t: stuns.get(target)){
                if(id != null && t.id.equals(id) || t.name.equals(identifier)){
                    t.removeStun();
                }
            }
        }
    }
    public static class StunHandler implements Listener {
        public StunHandler(){}
        @EventHandler(priority = EventPriority.MONITOR)
        public void onTeleport(EntityTeleportEvent e){
            if(isStuned(e.getEntity())){
                stunlocation.put(e.getEntity(),e.getTo());
            }
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerTeleport(PlayerTeleportEvent e){
            if(isStuned(e.getPlayer())){
                stunlocation.put(e.getPlayer(),e.getTo());
            }
        }
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
