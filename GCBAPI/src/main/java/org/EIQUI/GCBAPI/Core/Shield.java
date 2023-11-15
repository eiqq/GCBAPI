package org.EIQUI.GCBAPI.Core;

import org.EIQUI.GCBAPI.Core.CC.Timestop;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class Shield {
    private Entity caster;
    private final int originalDuration;
    private volatile int duration;
    private float originalamount;
    private float amount;
    private final Entity target;
    private String name;
    private UUID id;
    private int priority;

    private BukkitTask timerTask;
    private static final Map<Entity, LinkedHashSet<Shield>> SHIELDS = new ConcurrentHashMap<>();
    private static final Map<Entity, Boolean> SHIELDED = new ConcurrentHashMap<>();

    private static final Comparator<Shield> COMPARATOR = Comparator.comparingInt(p -> p.priority);

    public Shield(Entity target,float amount , int duration, String name){
        this(target,target,amount,duration,name,UUID.randomUUID());
    }
    public Shield(@Nullable Entity caster, Entity target,float amount , int duration, String name){
        this(caster,target,amount,duration,name,UUID.randomUUID());
    }
    public Shield(@Nullable Entity caster, Entity target,float amount , int duration, String name, UUID id) {
        if(caster == null){
            this.caster = target;
        }else{
            this.caster = caster;
        }
        this.target = target;
        this.amount = amount;
        if(this.amount < 0){
            this.amount = 0;
        }
        this.originalamount = this.amount;
        this.duration = duration;
        this.originalDuration = duration;
        this.name = name;
        this.id = id;
        if(duration < 0){
            this.priority = 1000000;
        }else{
            this.priority = duration;
        }

        if(SHIELDS.containsKey(target)){
            for(Shield t :SHIELDS.get(target)){
                if(t.id.equals(id)){
                    t.removeShield();
                }
            }
        }
        SHIELDED.put(this.target,true);
        if (!SHIELDS.containsKey(this.target)){
            SHIELDS.put(this.target,new LinkedHashSet<>());
        }
        SHIELDS.get(this.target).add(this);
        sort(this.target);
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(that, 0L, 1L);
    }

    private static void sort(Entity e){
        if (!SHIELDS.containsKey(e)){
            return;
        }
        List<Shield> tempList = new ArrayList<>(SHIELDS.get(e));
        Collections.sort(tempList, COMPARATOR);
        tempList.forEach(shield -> {
            if(shield.amount <= 0 || shield.duration == 0){
                shield.removeShield();
            }
        });
        SHIELDS.put(e,new LinkedHashSet<>(tempList));
    }
    private void tick() {
        if (duration == 0 || amount <= Vector.getEpsilon() || target.isDead() || !target.isValid()) {
            removeShield();
            sort(target);
            return;
        }
        if (duration > 0 && !Timestop.isTimestopped(target)) {
            duration--;
        }
    }

    public boolean isExists(){
        if(!isShielded(target)){
            return false;
        }if(duration == 0 || amount <= Vector.getEpsilon()){
            return false;
        }
        return SHIELDS.get(target).contains(this);
    }

    private void removeShield() {
        if (SHIELDS.containsKey(target)) {
            SHIELDS.get(target).remove(this);
            duration = 0;
            amount = 0;
            if(timerTask != null){
                timerTask.cancel();
            }
            if (SHIELDS.get(target).isEmpty()) {
                SHIELDED.put(target, false);
                SHIELDS.get(target).clear();
            }
        }else{
            SHIELDED.put(target, false);
        }
    }

    public static Entity getCaster(Entity e, String identifier) {
        if(!isShielded(e)){
            return null;
        }
        UUID id;
        try {
            id = UUID.fromString(identifier);
        } catch (IllegalArgumentException em) {
            id = null;
        }
        for (Shield b : SHIELDS.get(e)) {
            if (id != null && b.id.equals(id) || b.name.equals(identifier)) {
                return b.caster;
            }
        }
        return null;
    }

    public static boolean isShielded(@Nullable Entity e){
        if(e == null){
            return false;
        }
        return Boolean.TRUE.equals(SHIELDED.get(e));
    }

    public static boolean hasShield(@Nullable Entity e, String identifier) {
        if(e == null){
            return false;
        }
        if(!isShielded(e)){
            return false;
        }
        UUID id;
        try {
            id = UUID.fromString(identifier);
        } catch (IllegalArgumentException em) {
            id = null;
        }
        for (Shield b : SHIELDS.get(e)) {
            if (id != null && b.id.equals(id) || b.name.equals(identifier)) {
                return true;
            }
        }
        return false;
    }

    public static float getAmount(Entity e, String identifier){
        if(!isShielded(e)){
            return 0;
        }
        UUID id;
        try {
            id = UUID.fromString(identifier);
        } catch (IllegalArgumentException em) {
            id = null;
        }
        for (Shield b : SHIELDS.get(e)) {
            if (id != null && b.id.equals(id) || b.name.equals(identifier)) {
                return b.amount;
            }
        }
        return 0;
    }

    public static float getTotalAmount(Entity e){
        if(!isShielded(e)){
            return 0;
        }
        float ret = 0;
        for(Shield s: SHIELDS.get(e)){
            if( s.amount > 0 && s.duration != 0){
                ret += s.amount;
            }
        }
        return ret;
    }

    public static float getReducedByDamage(Entity e,float damage){
        if(!isShielded(e) || damage <= 0){
            return damage;
        }
        for(Shield s: SHIELDS.get(e)){
            if(s.amount - damage < 0){
                damage -= s.amount;
                s.removeShield();
            }else{
                s.amount -= damage;
                return 0;
            }
        }
        return damage;
    }

    public static int getDuration(Entity e, String identifier){
        if(!isShielded(e)){
            return 0;
        }
        UUID id;
        try {
            id = UUID.fromString(identifier);
        } catch (IllegalArgumentException em) {
            id = null;
        }
        for (Shield b : SHIELDS.get(e)) {
            if (id != null && b.id.equals(id) || b.name.equals(identifier)) {
                return b.duration;
            }
        }
        return 0;
    }

    public static void remove(Entity e, String identifier){
        if(!isShielded(e)){
            return;
        }
        UUID id;
        try {
            id = UUID.fromString(identifier);
        } catch (IllegalArgumentException em) {
            id = null;
        }
        for (Shield b : SHIELDS.get(e)) {
            if (id != null && b.id.equals(id) || b.name.equals(identifier)) {
                b.removeShield();
            }
        }
    }
    public static void removeAll(Entity e){
        if(!isShielded(e)){
            return;
        }
        for (Shield b : SHIELDS.get(e)) {
            b.removeShield();
        }
        SHIELDS.get(e).clear();
        SHIELDED.put(e,false);
    }

    public static void setAmount(Entity e,float amount, String identifier){
        if(!isShielded(e)){
            return;
        }
        if(amount < 0){
            amount = 0;
        }
        UUID id;
        try {
            id = UUID.fromString(identifier);
        } catch (IllegalArgumentException em) {
            id = null;
        }
        for (Shield b : SHIELDS.get(e)) {
            if (id != null && b.id.equals(id) || b.name.equals(identifier)) {
                b.amount = amount;
            }
        }
    }

    public static void addAmount(Entity e,float amount, String identifier){
        setAmount(e,getAmount(e,identifier)+amount,identifier);
    }

    public static void setDuration(Entity e,int d, String identifier){
        if(!isShielded(e)){
            return;
        }
        UUID id;
        try {
            id = UUID.fromString(identifier);
        } catch (IllegalArgumentException em) {
            id = null;
        }
        for (Shield b : SHIELDS.get(e)) {
            if (id != null && b.id.equals(id) || b.name.equals(identifier)) {
                b.duration = d;
                if (d < 0){
                    b.timerTask.cancel();
                }
            }
        }
    }

    public static void resetDuration(Entity e, String identifier){
        if(!isShielded(e)){
            return;
        }
        UUID id;
        try {
            id = UUID.fromString(identifier);
        } catch (IllegalArgumentException em) {
            id = null;
        }
        for (Shield b : SHIELDS.get(e)) {
            if (id != null && b.id.equals(id) || b.name.equals(identifier)) {
                b.duration = b.originalDuration;
            }
        }
    }

    public static void clear(){
        for (Set<Shield> shields : SHIELDS.values()) {
            for (Shield b : shields) {
                if (b.timerTask != null) {
                    b.timerTask.cancel();
                }
                b.removeShield();
            }
        }
        SHIELDS.clear();
        SHIELDED.clear();
    }

    public static class ShieldHandler implements Listener {
        public ShieldHandler() {
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onDeath(EntityDeathEvent e) {
            removeAll(e.getEntity());
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onQuit(PlayerQuitEvent e){
            removeAll(e.getPlayer());
        }
    }
}
