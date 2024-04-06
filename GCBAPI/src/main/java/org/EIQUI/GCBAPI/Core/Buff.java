package org.EIQUI.GCBAPI.Core;

import org.EIQUI.GCBAPI.Core.CC.Timestop;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class Buff {

    private Entity caster; // Nullable
    private final int originalDuration;
    private volatile int duration;
    private final Entity target;
    private final String name;
    private int stack = 1;
    private static final Map<Entity, Set<Buff>> BUFFS = new ConcurrentHashMap<>();
    private static final Map<Entity, Set<String>> HAS_BUFF = new ConcurrentHashMap<>();
    private BukkitTask task;

    public Buff(){
        caster = null;
        originalDuration = 0;
        duration = 0;
        target = null;
        name = null;
        stack = 0;
    }

    public Buff(@Nullable Entity caster, Entity target, int duration, String name) {
        if(caster == null){
            this.caster = target;
        }else{
            this.caster = caster;
        }
        this.target = target;
        this.duration = duration;
        this.originalDuration = duration;
        this.name = name;
        if(HAS_BUFF.containsKey(target) && HAS_BUFF.get(target).contains(name)){
            for(Buff buf :BUFFS.get(target)){
                if(buf.name.equals(this.name)){
                    buf.remove();
                }
            }
        }
        BUFFS.computeIfAbsent(target, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
        HAS_BUFF.computeIfAbsent(target, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
        BUFFS.get(target).add(this);
        HAS_BUFF.get(target).add(name);
        startTick();
    }
    private void startTick(){
        if(!target.isValid()){
            remove();
            return;
        }
        // 타이머 설정
        if (duration >= 0 && (task == null || task.isCancelled())) {
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if(!tick()){
                        cancel();
                    }
                }
            }.runTaskTimer(that, 0L, 1L);
        }else if(duration < 0 && task != null){
            task.cancel();
        }
    }
    private boolean tick() {
        if (duration == 0 || !target.isValid() || stack <= 0) {
            remove();
            return false;
        }
        if (!Timestop.isTimestopped(target)) {
            duration--;
        }
        return true;
    }

    public void remove() {
        duration = 0;
        stack = 0;
        if (BUFFS.containsKey(target) && BUFFS.get(target) != null){
            BUFFS.get(target).remove(this);
        }
        if (HAS_BUFF.containsKey(target) && HAS_BUFF.get(target) != null){
            HAS_BUFF.get(target).remove(name);
        }
        if(task != null){
            task.cancel();
        }
    }

    public void setStack(int s){
        this.stack += s;
    }
    public int getStack(){
        return this.stack;
    }
    public Entity getTarget(){
        return target;
    }
    public Entity getCaster(){
        return caster;
    }
    public void setCaster(@Nullable Entity e){
        caster = e;
    }
    public int getDuration(){
        return duration;
    }
    public void setDuration(int i ){
        duration = i;
        startTick();
    }
    public int getOriginalDuration(){
        return originalDuration;
    }
    public String getName(){
        return name;
    }
    //-----------------------------------------------
    public static Buff getBuff(Entity e, String name){
        if(HAS_BUFF.containsKey(e) && HAS_BUFF.get(e).contains(name)) {
            for (Buff b : BUFFS.get(e)) {
                if (b.name.equals(name)) {
                    return b;
                }
            }
        }
        return new Buff();
    }

    public static Entity getCaster(Entity e, String name) {
        if(HAS_BUFF.containsKey(e) && HAS_BUFF.get(e).contains(name)) {
            for (Buff b : BUFFS.get(e)) {
                if (b.name.equals(name)) {
                    return b.caster;
                }
            }
        }
        return null;
    }

    public static boolean hasBuff(@Nullable Entity e, String name) {
        if(e == null){
            return false;
        }
        if(HAS_BUFF.containsKey(e)){
            return Boolean.TRUE.equals(HAS_BUFF.get(e).contains(name));
        }
        return false;
    }

    public static boolean hasAny(Entity e){
        if(!BUFFS.containsKey(e)){
            return false;
        }
        return HAS_BUFF.get(e).size() > 0;
    }

    public static int getDuration(Entity e, String name){
        if(!HAS_BUFF.containsKey(e) || !HAS_BUFF.get(e).contains(name)) {
            return 0;
        }
        for(Buff b: BUFFS.get(e)){
            if (b.name.equals(name)){
                return b.duration;
            }
        }
        return 0;
    }

    public static void remove(Entity e, String name){
        if(HAS_BUFF.containsKey(e) && HAS_BUFF.get(e).contains(name)) {
            for (Buff b : BUFFS.get(e)) {
                if (b.name.equals(name)) {
                    b.remove();
                    return;
                }
            }
        }
    }
    public static void removeAll(Entity e){
        if(BUFFS.containsKey(e)) {
            for (Buff b : BUFFS.get(e)) {
                b.remove();
            }
            BUFFS.get(e).clear();

        }
        if(HAS_BUFF.containsKey(e)) {
            HAS_BUFF.get(e).clear();
        }
    }
    public static void clear(){
        if(BUFFS.size() == 0){
            return;
        }
        for (Set<Buff> buffs : BUFFS.values()) {
            for (Buff b : buffs) {
                b.remove();
            }
        }
        BUFFS.clear();
        HAS_BUFF.clear();
    }

    public static void setDuration(Entity e, String name,int d){
        if(HAS_BUFF.containsKey(e) && HAS_BUFF.get(e).contains(name)) {
            for (Buff b : BUFFS.get(e)) {
                if (b.name.equals(name)) {
                    b.duration = d;
                    b.startTick();
                    return;
                }
            }
        }
    }

    public static void resetDuration(Entity e, String name){
        if(HAS_BUFF.containsKey(e) && HAS_BUFF.get(e).contains(name)) {
            for (Buff b : BUFFS.get(e)) {
                if (b.name.equals(name)) {
                    b.duration = b.originalDuration;
                    b.startTick();
                    return;
                }
            }
        }
    }

    public static void setStack(Entity e, String name,int stack){
        if(HAS_BUFF.containsKey(e) && HAS_BUFF.get(e).contains(name)) {
            for (Buff b : BUFFS.get(e)) {
                if (b.name.equals(name)) {
                    b.stack = stack;
                    return;
                }
            }
        }
    }
    public static int getStack(Entity e, String name){
        if(HAS_BUFF.containsKey(e) && HAS_BUFF.get(e).contains(name)) {
            for (Buff b : BUFFS.get(e)) {
                if (b.name.equals(name)) {
                    return b.stack;
                }
            }
        }
        return 0;
    }

    public static class BuffHandler implements Listener {
        public BuffHandler() {
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onDeath(EntityDeathEvent e) {
            if(!e.getEntityType().equals(EntityType.PLAYER)){
                removeAll(e.getEntity());
            }
        }
    }
}
