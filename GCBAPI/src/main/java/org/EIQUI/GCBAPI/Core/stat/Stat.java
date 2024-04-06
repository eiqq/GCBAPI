package org.EIQUI.GCBAPI.Core.stat;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.logging.Level;

public class Stat {
    public static double getAttributeValue(LivingEntity e,Attribute attribute,String name){
        AttributeInstance inst = e.getAttribute(attribute);
        for (AttributeModifier modi : inst.getModifiers()) {
            if (modi.getName().equalsIgnoreCase(name)) {
                return modi.getAmount();
            }
        }
        return 0;
    }
    public static void setAttributeValue(LivingEntity e,Attribute attribute,String name,double value,AttributeModifier.Operation op){
        AttributeInstance inst = e.getAttribute(attribute);
        AttributeModifier newmodif = new AttributeModifier(name,value, op);
        for (AttributeModifier modi : inst.getModifiers()) {
            if (modi.getName().equalsIgnoreCase(name)) {
                inst.removeModifier(modi);
            }
        }
        inst.addModifier(newmodif);
    }

    public static void removeModifier(LivingEntity e,Attribute attribute,String name){
        for(AttributeModifier modi : e.getAttribute(attribute).getModifiers()){
            if(modi.getName().equalsIgnoreCase(name)){
                e.getAttribute(attribute).removeModifier(modi);
            }
        }
    }

    public static void removeAllModifier(LivingEntity e){
        for(Attribute att :Attribute.values()){
            try{
                for(AttributeModifier modi : e.getAttribute(att).getModifiers()){
                    e.getAttribute(att).removeModifier(modi);
                }
            }catch (Exception ex){
                Bukkit.getLogger().log(Level.INFO,"없는ATTribute");
            }
        }
    }

    public static double roundToPlace(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.ceil(value * scale) / scale;
    }

    public static final Armor ARMOR = new Armor();
    public static final AttackDamage ATTACK_DAMAGE = new AttackDamage();
    public static final AttackPower ATTACK_POWER = new AttackPower();
    public static final AttackRange ATTACK_RANGE = new AttackRange();
    public static final AttackSpeed ATTACK_SPEED = new AttackSpeed();
    public static final CooldownReduce COOLDOWN_REDUCE = new CooldownReduce();
    public static final Health HEALTH = new Health();
    public static final HealthRegen HEALTH_REGEN = new HealthRegen();
    public static final MovementSpeed MOVEMENT_SPEED = new MovementSpeed();

    public static void removeAll(LivingEntity e){
        ARMOR.clear(e);
        ATTACK_DAMAGE.clear(e);
        ATTACK_POWER.clear(e);
        ATTACK_RANGE.clear(e);
        ATTACK_SPEED.clear(e);
        HEALTH.clear(e);
        HEALTH_REGEN.clear(e);
        COOLDOWN_REDUCE.clear(e);
        MOVEMENT_SPEED.clear(e);
    }

    public static void clear(){
        ARMOR.clear();
        ATTACK_DAMAGE.clear();
        ATTACK_POWER.clear();
        ATTACK_RANGE.clear();
        ATTACK_SPEED.clear();
        HEALTH.clear();
        HEALTH_REGEN.clear();
        COOLDOWN_REDUCE.clear();
        MOVEMENT_SPEED.clear();
    }

    public static class StatHandler implements Listener {
        public StatHandler() {
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onDeath(EntityDeathEvent e) {
            if(!e.getEntityType().equals(EntityType.PLAYER)){
               removeAll(e.getEntity());
            }
        }
    }
}
