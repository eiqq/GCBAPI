package org.EIQUI.GCBAPI.Core;

import org.EIQUI.GCBAPI.Basic;
import org.EIQUI.GCBAPI.Core.BeneficialEffect.Stealth;
import org.EIQUI.GCBAPI.Core.BeneficialEffect.Untargetable;
import org.EIQUI.GCBAPI.HitboxAPI;
import org.EIQUI.GCBAPI.Util;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class Targeter {

    private static boolean basicFiller(Entity targeter, Entity targeted){
        if(targeter.equals(targeted)){
            return false;
        }
        if(targeted instanceof Player){
            if (((Player)targeted).getGameMode().equals(GameMode.SPECTATOR)){
                return false;
            }
            if(!Stealth.canSeeStealth((Player) targeter,targeted)){
                return false;
            }
        }
        if(!Basic.basicTargetCheck(targeted,targeter)){
            return false;
        }
        if(Untargetable.isUntargetable(targeted)){
            return false;
        }
        if(targeted.getBoundingBox().getWidthX() <= Vector.getEpsilon()
                && targeted.getBoundingBox().getHeight() <= Vector.getEpsilon()){
            return false;
        }
        return true;
    }

    public static Entity getEntityTargetEntity(Entity e, @Nullable Vector eyevector,
                                               float radius, float range, boolean hitblock){
        return getEntityTargetEntity(e,eyevector,radius,range,hitblock,null);
    }
    public static Entity getEntityTargetEntity(Entity e, @Nullable Vector eyevector,
                                               float radius, float range, boolean hitblock,@Nullable Predicate predicate){
        range -= (radius*2);
        if(range < 0){
            range = 0;
        }
        Location eyelocation;

        if(e instanceof LivingEntity){
            eyelocation = ((LivingEntity)e).getEyeLocation();
        }else{
            eyelocation = e.getLocation();
        }
        if(eyevector == null){
            eyevector = Util.getVector(eyelocation.getYaw(),eyelocation.getPitch());
        }
        Predicate<Entity> pred;
        if(predicate != null){
            pred = ent -> (basicFiller(e,ent) && predicate.test(ent));
        }else{
            pred = ent -> (basicFiller(e,ent));
        }

        eyelocation = eyelocation.add(eyevector.normalize().multiply(radius));
        RayTraceResult ray;
        if(hitblock){
            RayTraceResult temprayblock =  eyelocation.getWorld()
                    .rayTraceBlocks(eyelocation,eyevector,range, FluidCollisionMode.NEVER, true);
            ray = eyelocation.getWorld()
                    .rayTrace(eyelocation,eyevector,range, FluidCollisionMode.NEVER,true,radius,pred);
            if(temprayblock != null && temprayblock.getHitBlock() != null){
                if(ray != null && ray.getHitEntity() != null){
                    if(temprayblock.getHitPosition().distanceSquared(eyelocation.toVector())
                            < ray.getHitPosition().distanceSquared(eyelocation.toVector())){
                        ray = null;
                    }
                }
            }
        }else{
            ray = eyelocation.getWorld()
                    .rayTraceEntities(eyelocation,eyevector,range,radius,pred);
        }
        if(ray == null || ray.getHitEntity() == null){
            return null;
        }
        return ray.getHitEntity();
    }


    public static Location getEntityTargetLocation(Entity e,@Nullable Vector eyevector,float radius,float range,
                                                   boolean hitentity,boolean hitblock){
        return getEntityTargetLocation(e,eyevector,radius,range,hitentity,hitblock,null);
    }
    public static Location getEntityTargetLocation(Entity e,@Nullable Vector eyevector,float radius,float range,
                                             boolean hitentity,boolean hitblock,@Nullable Predicate predicate){
        range -= (radius*2);
        if(range < 0){
            range = 0;
        }
        Location eyelocation;

        if(e instanceof LivingEntity){
            eyelocation = ((LivingEntity)e).getEyeLocation();
        }else{
            eyelocation = e.getLocation();
        }
        if(eyevector == null){
            eyevector = Util.getVector(eyelocation.getYaw(),eyelocation.getPitch());
        }
        Predicate<Entity> pred;
        if(predicate != null){
            pred = ent -> (basicFiller(e,ent) && predicate.test(ent));
        }else{
            pred = ent -> (basicFiller(e,ent));
        }

        eyelocation = eyelocation.add(eyevector.normalize().multiply(radius));
        RayTraceResult ray = null;
        if(hitblock && hitentity){
            ray = eyelocation.getWorld()
                    .rayTrace(eyelocation,eyevector,range, FluidCollisionMode.NEVER,true,radius,pred);

        }else if(hitentity){
            ray = eyelocation.getWorld()
                    .rayTraceEntities(eyelocation,eyevector,range,radius,pred);
        }else if(hitblock){
            ray = eyelocation.getWorld()
                    .rayTraceBlocks(eyelocation,eyevector,range, FluidCollisionMode.NEVER,true);
        }else{
            return eyelocation.add(eyevector.normalize().multiply(range));
        }
        if(ray == null || ray.getHitPosition() == null){
            return eyelocation.add(eyevector.normalize().multiply(range));
        }
        return ray.getHitPosition().toLocation(e.getWorld());
    }


    public static Entity getEntityTargetEntityIn2D(Entity e, @Nullable Vector eyevector,
                                                   float startradius,float endradius, float range, boolean hitblock){
        return getEntityTargetEntityIn2D(e,eyevector,startradius,endradius,range,hitblock,null);
    }
    public static Entity getEntityTargetEntityIn2D(Entity e, @Nullable Vector eyevector,
                                           float startradius,float endradius, float range, boolean hitblock,@Nullable Predicate predicate){
        if(startradius < 0){
            startradius = 0;
        }
        Location eyelocation;
        if(e instanceof LivingEntity){
            eyelocation = ((LivingEntity)e).getEyeLocation();
        }else{
            eyelocation = e.getLocation();
        }
        if(eyevector == null){
            eyevector = Util.getVector(eyelocation.getYaw(),eyelocation.getPitch());
        }
        Location finalEyelocation = eyelocation.clone();
        Vector finalEyevector = eyevector.clone();
        float finalStartradius = startradius;
        Predicate<Entity> hitboxpred = ent ->
                HitboxAPI.isHitboxCollide_ConeWithDot(finalEyelocation.clone(), finalEyevector.clone(),range, finalStartradius,endradius,ent);

        Predicate<Entity> pred;
        if(predicate != null){
            pred = ent -> (basicFiller(e,ent)) && hitboxpred.test(ent) && predicate.test(ent);
        }else{
            pred = ent -> (basicFiller(e,ent)) && hitboxpred.test(ent);
        }

        RayTraceResult ray;
        if(hitblock){
            RayTraceResult temprayblock =  eyelocation.getWorld()
                    .rayTraceBlocks(eyelocation,eyevector,range, FluidCollisionMode.NEVER, true);
            ray = eyelocation.getWorld()
                    .rayTraceEntities(eyelocation,eyevector,range,(Math.max(startradius,endradius)),pred);
            if(temprayblock != null && temprayblock.getHitBlock() != null){
                if(ray != null && ray.getHitEntity() != null){
                    if(temprayblock.getHitPosition().distanceSquared(eyelocation.toVector())
                            < ray.getHitPosition().distanceSquared(eyelocation.toVector())){
                        ray = null;
                    }
                }
            }
        }else{
            ray = eyelocation.getWorld()
                    .rayTraceEntities(eyelocation,eyevector,range,(Math.max(startradius,endradius)),pred);

        }
        if(ray == null || ray.getHitEntity() == null){
            return getEntityTargetEntity(e,eyevector,Math.min(startradius,endradius),range,hitblock,predicate);
        }
        return ray.getHitEntity();
    }


    public static Location getLocation(Location l,Vector v,float range){
        if(range <= 0){
            return l.clone();
        }
        RayTraceResult ray = l.getWorld().rayTraceBlocks(l,v,range,FluidCollisionMode.NEVER,true);
        if(ray == null || ray.getHitPosition() == null){
            return l.clone().add(v.clone().normalize().multiply(range));
        }
        return ray.getHitPosition().toLocation(l.getWorld());
    }

    public static Location getSurface(Location l,Vector v,float range){
        if(range < 0){
           range = 0;
        }
        RayTraceResult ray = l.getWorld().rayTraceBlocks(l,v,range,FluidCollisionMode.NEVER,true);
        if(ray == null || ray.getHitPosition() == null){
            return null;
        }
        return ray.getHitPosition().toLocation(l.getWorld());
    }

    public static Block getBlock(Location l, Vector v, float range){
        if(range < 0){
            range = 0;
        }
        RayTraceResult ray = l.getWorld().rayTraceBlocks(l,v,range,FluidCollisionMode.NEVER,true);
        if(ray == null || ray.getHitBlock() == null){
            return null;
        }
        return ray.getHitBlock();
    }
}





















