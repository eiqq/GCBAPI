package org.EIQUI.GCBAPI.Core.BehavioralEffect;

import org.EIQUI.GCBAPI.Core.CC.Timestop;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class CustomBehavioralEffect {

    private static final Map<String,Map<Entity, CustomBehavioralEffect>> SAVELIST = new ConcurrentHashMap<>();
    private static final Map<String,Map<Entity, Boolean>> HAS_EFFECT_BOOL = new HashMap<>();

    private Entity target;
    private int duration;
    private BukkitTask timerTask;
    private String nameID;

    public CustomBehavioralEffect(LivingEntity target,String nameID, int duration) {
        this.duration = duration;
        this.target = target;
        this.nameID = nameID;
        if(hasEffect(target,nameID)){
            remove(target,nameID);
        }

        getOrCreate_SAVELIST(nameID).put(target,this);
        getOrCreate_BOOLLIST(nameID).put(target, true);
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(that, 0L, 1);
    }
    protected void tick() {
        if (duration == 0 || !target.isValid() || target.isDead()) {
            this.remove();
            return;
        }
        if(!Timestop.isTimestopped(target)){
            duration--;
        }
    }
    private void remove() {
        this.duration = 0;
        this.timerTask.cancel();
        getOrCreate_BOOLLIST(nameID).put(this.target, false);
        getOrCreate_SAVELIST(nameID).remove(target);
    }

    public static boolean hasEffect(@Nullable Entity target,@Nullable String nameID) {
        if (target == null || nameID == null){
            return false;
        }
        return Boolean.TRUE.equals(getOrCreate_BOOLLIST(nameID).get(target));
    }

    public static void remove(Entity target,String nameIDd) {
        Map<Entity, CustomBehavioralEffect> temp = getOrCreate_SAVELIST(nameIDd);
        for (Entity entity : temp.keySet()) {
            if(entity.equals(target)){
                temp.get(entity).remove();
            }
        }
    }

    public static void removeAll(Entity target) {
        for (String nameid : SAVELIST.keySet()) {
            for (Entity entity : SAVELIST.get(nameid).keySet()) {
                if (entity.equals(target)) {
                    SAVELIST.get(nameid).clear();
                }
            }
        }
    }

    public static void clear() {
        for (String id : SAVELIST.keySet()) {
            for (Entity entity : SAVELIST.get(id).keySet()) {
                SAVELIST.get(id).get(entity).remove();
            }
        }
        SAVELIST.clear();
        HAS_EFFECT_BOOL.clear();
    }

    public static Map<Entity, CustomBehavioralEffect> getOrCreate_SAVELIST(String nameIDd){
        SAVELIST.computeIfAbsent(nameIDd, k -> new ConcurrentHashMap<>());
        return SAVELIST.get(nameIDd);
    }
    public static Map<Entity, Boolean> getOrCreate_BOOLLIST(String nameIDd){
        HAS_EFFECT_BOOL.computeIfAbsent(nameIDd, k -> new HashMap<>());
        return HAS_EFFECT_BOOL.get(nameIDd);
    }
}
