package org.EIQUI.GCBAPI.Core.skill;

import org.EIQUI.GCBAPI.Core.stat.Stat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class SkillStacker {

    private static final Map<Entity, Map<String, SkillStacker>> STACKERS = new ConcurrentHashMap<>();
    private final Entity target;
    private final String skill;
    private volatile int stackcooldown;
    private volatile int cooldown;
    private final int maxstack;
    private volatile int stack;
    private final boolean cooldownRducible;
    private volatile boolean setToRemove = false;
    private BukkitTask timerTask;

    public SkillStacker(Entity e, String skill, double cd, int maxstack, boolean cdr){
        if(getOrCreateStackMap(e).containsKey(skill)){
            remove(e,skill);
        }
        this.target = e;
        this.skill = skill;
        this.stackcooldown = (int)Math.ceil(cd*20);
        this.cooldown = stackcooldown;
        this.maxstack = maxstack;
        this.stack = 0;
        if(this.target instanceof LivingEntity){
            this.cooldownRducible = cdr;
        }else{
            this.cooldownRducible = false;
        }
        this.timerTask = tick();
        getOrCreateStackMap(e).put(this.skill,this);
    }

    public static int getStack(Entity e,String skill){
        Map<String,SkillStacker> tempmap = getOrCreateStackMap(e);
        if(tempmap.containsKey(skill)){
            return tempmap.get(skill).stack;
        }
        return 0;
    }
    public static int getMaxStack(Entity e,String skill){
        Map<String,SkillStacker> tempmap = getOrCreateStackMap(e);
        if(tempmap.containsKey(skill)){
            return tempmap.get(skill).maxstack;
        }
        return 0;
    }

    public static double getCooldown(Entity e,String skill){
        Map<String,SkillStacker> tempmap = getOrCreateStackMap(e);
        if(tempmap.containsKey(skill)){
            return tempmap.get(skill).cooldown*0.05;
        }
        return 0;
    }

    public static boolean hasStacker(Entity e,String skill){
        return getOrCreateStackMap(e).containsKey(skill);
    }

    public static void setStack(Entity e,String skill,int i){
        Map<String,SkillStacker> tempmap = getOrCreateStackMap(e);
        if(tempmap.containsKey(skill)){
            if(i < 0){i = 0;}
            tempmap.get(skill).stack = i;
        }
    }

    public static void setCooldown(Entity e,String skill,double cd){
        Map<String,SkillStacker> tempmap = getOrCreateStackMap(e);
        if(tempmap.containsKey(skill)){
            if(cd < 0){cd = 0;}
            tempmap.get(skill).cooldown = (int)Math.ceil(cd*20);
        }
    }

    public static void remove(Entity e,String skill){
        Map<String,SkillStacker> tempmap = getOrCreateStackMap(e);
        if(tempmap.containsKey(skill)){
            tempmap.get(skill).setToRemove = true;
            tempmap.get(skill).timerTask = null;
            tempmap.remove(skill);
        }
    }

    private BukkitTask tick(){
        BukkitTask b = new BukkitRunnable() {
            @Override
            public void run() {
                if(setToRemove){
                    cancel();
                    return;
                }
                if(stack < maxstack){
                   if(cooldown <= 0){
                       stack++;
                       if(cooldownRducible){
                           double ncd = Stat.COOLDOWN_REDUCE.calculateCooldownReduce((LivingEntity)target,stackcooldown);
                           cooldown = (int)Math.round(ncd);
                       }else{
                           cooldown = stackcooldown;
                       }
                   }else{
                       cooldown--;
                   }
                }
            }
        }.runTaskTimer(that, 1, 1);
        return b;
    }

    private static void clear(){
        for(Entity e : STACKERS.keySet()){
            for(SkillStacker stk : STACKERS.get(e).values()){
                stk.setToRemove = true;
            }
        }
        STACKERS.clear();
    }
    private static void clear(Entity e){
        if(STACKERS.containsKey(e)){
            for(SkillStacker stk : STACKERS.get(e).values()){
                stk.setToRemove = true;
            }
        }
        STACKERS.remove(e);
    }

    private static Map<String, SkillStacker> getOrCreateStackMap(Entity e) {
        return STACKERS.computeIfAbsent(e, k -> new ConcurrentHashMap<>());
    }

    public static class SkillStackerHandler implements Listener {
        public SkillStackerHandler(){}
        @EventHandler(priority = EventPriority.MONITOR)
        public void onDeath(EntityDeathEvent e){
            if(!e.getEntityType().equals(EntityType.PLAYER)){
                clear(e.getEntity());
            }
        }
    }
}




