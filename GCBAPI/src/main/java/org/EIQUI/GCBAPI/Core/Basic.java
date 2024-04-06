package org.EIQUI.GCBAPI.Core;

import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;

import javax.annotation.Nullable;


public final class Basic {
    public static boolean basicTargetCheck(@Nullable Entity target, @Nullable Entity e){
        if (target == null || e == null || target.equals(e)){
            return false;
        }
        if(target.isDead() || !target.isValid()){
            return false;
        }
        EntityType type = target.getType();
        if (!type.isAlive()){
            if(type == EntityType.INTERACTION){
                return (target.getName().toLowerCase().contains("skill") || target.getName().equalsIgnoreCase("all"));
            }
            return false;
        }
        else if(type == EntityType.ARMOR_STAND){
            if(((ArmorStand)target).isMarker()){
                return false;
            }
        }
        return true;
    }


    public static boolean checkBaseAttack(Entity target, Entity e){
        if (target.equals(e)){
            return false;
        }
        EntityType type = target.getType();
        if (!type.isAlive()){
            if(target.getType() == EntityType.INTERACTION){
                return (target.getName().toLowerCase().contains("baseattack") || target.getName().equalsIgnoreCase("all"));
            }
            return false;
        }else if (type == EntityType.PLAYER){
            if (((HumanEntity)target).getGameMode() == GameMode.SPECTATOR){
                return false;
            }
        }
        return true;
    }
}
