package org.EIQUI.GCBAPI.Core.CC;

import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class CC {
    private static final  Map<String,Map<Entity, Set<CC>>> SAVED_CC = new ConcurrentHashMap<>();
    private static final Map<String,Map<Entity, Boolean>> HAS_CC = new HashMap<>();

    protected Entity caster;
    protected Entity target;
    protected String name;
    protected UUID id;
    protected int duration;
    protected BukkitTask timerTask;
    protected String type;

    public CC(@Nullable Entity caster, Entity target, int duration, String name, UUID id,String type) {
        this.duration = duration;
        if(caster == null){
            this.caster = target;
        }else{
            this.caster = caster;
        }
        this.target = target;
        this.name = name;
        this.id = id;
        this.type = type;
    }

    protected void startTick() {
        SAVED_CC.computeIfAbsent(type, k -> new ConcurrentHashMap<>());
        SAVED_CC.get(type).computeIfAbsent(target, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
        SAVED_CC.get(type).get(target).add(this);
        HAS_CC.computeIfAbsent(type, k -> new ConcurrentHashMap<>());
        HAS_CC.get(type).put(target, true);
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(that, 0L, 1);
    }

    protected void tick() {
        if (duration == 0 || !target.isValid() || target.isDead()) {
            remove();
            return;
        }
        if(!Timestop.isTimestopped(target)){
            duration--;
        }
    }

    public static boolean isCCed(@Nullable Entity target,String type) {
        if (target == null){
            return false;
        }
        if (!HAS_CC.containsKey(type)){
            return false;
        }
        return Boolean.TRUE.equals(HAS_CC.get(type).get(target));
    }

    public void remove() {
        if (SAVED_CC.containsKey(target)) {
            SAVED_CC.get(target).remove(this);
            duration = 0;
            timerTask.cancel();
            if (SAVED_CC.get(target).isEmpty()) {
                SAVED_CC.get(target).clear();
            }
        }
    }

}
