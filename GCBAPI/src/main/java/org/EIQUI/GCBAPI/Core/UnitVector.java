package org.EIQUI.GCBAPI.Core;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UnitVector {

    private static final Map<Entity,UnitVector> UNITVECTORS = new ConcurrentHashMap<>();

    private Entity caster;
    private Entity target;
    private Vector direction;
    private float speed;
    private String skill;
    private UUID id;
    private boolean isCC;

    public UnitVector(Entity target, Vector dir, float speed, String skill, UUID id){
        this(null,target,dir,speed,skill,id,false);
    }
    public UnitVector(Entity target, Vector dir, float speed, String skill){
        this(null,target,dir,speed,skill,UUID.randomUUID(),false);
    }
    public UnitVector(Entity target, Vector dir, float speed, String skill, UUID id,boolean isCC){
        this(null,target,dir,speed,skill,id,isCC);
    }
    public UnitVector(@Nullable Entity caster, Entity target, Vector dir, float speed, String skill, UUID id,boolean isCC){
        this.caster = caster;
        this.target = target;
        this.direction = dir.clone().normalize();
        this.speed = speed;
        this.skill = skill;
        this.id = id;
        this.isCC = isCC;
        UNITVECTORS.put(target,this);
    }
    public static boolean hasVector(@Nullable Entity e){
        if (e == null){
            return false;
        }
        return UNITVECTORS.containsKey(e);
    }

    public static Vector getDirection(Entity e){
        if(hasVector(e)){
            return UNITVECTORS.get(e).direction;
        }
        return new Vector(0,0,0);
    }
    public static float getSpeed(Entity e){
        if(hasVector(e)){
            return UNITVECTORS.get(e).speed;
        }
        return 0;
    }
    public static Vector getVector(Entity e){
        if(hasVector(e)){
            UnitVector u = UNITVECTORS.get(e);
            return u.direction.clone().normalize().multiply(u.speed);
        }
        return new Vector(0,0,0);
    }
    public static UUID getID(Entity e){
        if(hasVector(e)){
            return UNITVECTORS.get(e).id;
        }
        return null;
    }
    public static String getSkill(Entity e){
        if(hasVector(e)){
            return UNITVECTORS.get(e).skill;
        }
        return "";
    }

    public static void setSpeed(Entity e,float speed){
        if(hasVector(e)){
            UNITVECTORS.get(e).speed = speed;
        }
    }
    public static void setDirection(Entity e,Vector v){
        if(hasVector(e)){
            UNITVECTORS.get(e).direction = v.clone().normalize();
        }
    }
    public static void addSpeed(Entity e,float s){
        if(hasVector(e)){
            setSpeed(e,getSpeed(e)+s);
        }
    }
    public static void delete(Entity e){
        UNITVECTORS.remove(e);
    }

    public static boolean isCC(Entity e){
        if(hasVector(e)){
            return UNITVECTORS.get(e).isCC;
        }
        return false;
    }

    public static void clear(){
        UNITVECTORS.clear();
    }
}

