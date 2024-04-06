package org.EIQUI.GCBAPI.Core.skill;

import org.EIQUI.GCBAPI.Core.CC.Timestop;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class Skill {
    private static final Map<UUID, Map<Integer, String>> SKILL_MAP = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, Integer>> COOLDOWN_MAP = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<String>> IS_COOLDOWN = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<String>> IS_CASTING = new ConcurrentHashMap<>();

    public static boolean isCasting(@Nullable Entity e,@Nullable String skillname){
        if(e == null || skillname == null){
            return false;
        }
        IS_CASTING.computeIfAbsent(e.getUniqueId(), k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
        return IS_CASTING.get(e.getUniqueId()).contains(skillname.toLowerCase());
    }
    public static void setCasting(@Nullable Entity e, @Nullable String skillname, boolean b){
        if(e == null || skillname == null){
            return;
        }
        IS_CASTING.computeIfAbsent(e.getUniqueId(), k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
        if(Boolean.TRUE.equals(b)){
            IS_CASTING.get(e.getUniqueId()).add(skillname.toLowerCase());
        }else{
            IS_CASTING.get(e.getUniqueId()).remove(skillname.toLowerCase());
        }
    }

    public static void setSkill(Entity e, int slot,@Nullable String skillname) {
        if(skillname == null){
            return;
        }
        getOrCreateSkillMap(e.getUniqueId()).put(slot,skillname.toLowerCase());
    }

    public static String getSkill(Entity e, int slot) {
        return getOrCreateSkillMap(e.getUniqueId()).getOrDefault(slot, null);
    }

    public static int whereSkill(@Nullable Entity e,@Nullable String skillname) {
        if(skillname == null && !hasSkill(e,skillname)){
            return -1;
        }
        skillname = skillname.toLowerCase();
        Map<Integer, String> skills = getOrCreateSkillMap(e.getUniqueId());
        for (Map.Entry<Integer, String> entry : skills.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(skillname)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public static boolean hasSkill(@Nullable Entity e,@Nullable String skillname) {
        if(skillname == null || e == null){
            return false;
        }
        return getOrCreateSkillMap(e.getUniqueId()).containsValue(skillname.toLowerCase());
    }

    public static void setCooldown(Entity e,@Nullable String skillname, double cdInSec) {
        if(skillname == null){
            return;
        }
        if(cdInSec < 0){
            cdInSec = 0;

        }
        UUID id = e.getUniqueId();
        skillname = skillname.toLowerCase();
        int cdInTick = (int) Math.round(cdInSec * 20.0d);
        COOLDOWN_MAP.computeIfAbsent(id, k -> new HashMap<>());
        COOLDOWN_MAP.get(id).put(skillname, cdInTick);
        startTickCooldown(e, skillname);
    }

    public static double getCooldown(Entity e,@Nullable String skillname) {
        if(skillname == null){
            return 0;
        }
        UUID entityId = e.getUniqueId();
        skillname = skillname.toLowerCase();
        return getOrCreateCooldownMap(entityId).getOrDefault(skillname, 0)*0.05d;
    }

    public static double getCooldownAt(Entity e, int slot) {
        return getCooldown(e,getSkill(e,slot));
    }

    public static boolean isCooldown(Entity e,@Nullable String skillname) {
        if(skillname == null){
            return false;
        }
        return getOrCreateIsCooldownSet(e.getUniqueId()).contains(skillname.toLowerCase());
    }

    private static void startTickCooldown(Entity e, final String skillname) {
        if (isCooldown(e, skillname)) {
            return;
        }
        UUID entityId = e.getUniqueId();
        if (getOrCreateCooldownMap(entityId).getOrDefault(skillname, 0) <= 0) {
            getOrCreateIsCooldownSet(entityId).remove(skillname);
            return;
        }
        getOrCreateIsCooldownSet(entityId).add(skillname);
        Map<String, Integer> cooldownMap = getOrCreateCooldownMap(entityId);
        new BukkitRunnable() {
            @Override
            public void run() {
                int currentCd = cooldownMap.getOrDefault(skillname, 0);
                if (currentCd <= 0 || !isCooldown(e,skillname)) {
                    getOrCreateIsCooldownSet(entityId).remove(skillname);
                    cooldownMap.put(skillname,0);
                    cancel();
                    return;
                }
                if (!Timestop.isTimestopped(e)) {
                    cooldownMap.put(skillname, currentCd - 1);
                }
            }
        }.runTaskTimer(that, 1, 1);
    }


    public static void clear(Entity e){
        UUID id = e.getUniqueId();
        if(COOLDOWN_MAP.containsKey(id)){
            COOLDOWN_MAP.get(id).clear();
            COOLDOWN_MAP.remove(id);
        }
        if(SKILL_MAP.containsKey(id)){
            SKILL_MAP.get(id).clear();
            SKILL_MAP.remove(id);
        }
        if(IS_COOLDOWN.containsKey(id)){
            IS_COOLDOWN.get(id).clear();
            IS_COOLDOWN.remove(id);
        }
        if( IS_CASTING.containsKey(id)){
            IS_CASTING.get(id).clear();
            IS_CASTING.remove(id);
        }
    }
    public static void clear(){
        for(Map<String, Integer> a : COOLDOWN_MAP.values()){
            a.clear();
        }
        for(Map<Integer, String> a :  SKILL_MAP.values()){
            a.clear();
        }
        for(Set<String> a : IS_COOLDOWN.values()){
            a.clear();
        }
        SKILL_MAP.clear();
        COOLDOWN_MAP.clear();
        IS_COOLDOWN.clear();
        IS_CASTING.clear();
    }

    private static Map<Integer, String> getOrCreateSkillMap(UUID entityId) {
        return SKILL_MAP.computeIfAbsent(entityId, k -> new ConcurrentHashMap<>());
    }

    private static Map<String, Integer> getOrCreateCooldownMap(UUID entityId) {
        return COOLDOWN_MAP.computeIfAbsent(entityId, k -> new ConcurrentHashMap<>());
    }
    private static Set<String> getOrCreateIsCooldownSet(UUID entityId) {
        return IS_COOLDOWN.computeIfAbsent(entityId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
    }

    public static class SkillHandler implements Listener {
        public SkillHandler(){}
        @EventHandler(priority = EventPriority.MONITOR)
        public void onDeath(EntityDeathEvent e){
            if(!e.getEntityType().equals(EntityType.PLAYER)){
                clear(e.getEntity());
            }
        }
    }
}
