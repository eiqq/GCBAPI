package org.EIQUI.GCBAPI.Core;

import org.EIQUI.GCBAPI.Core.BeneficialEffect.Stealth;
import org.EIQUI.GCBAPI.Core.BeneficialEffect.Untargetable;
import org.EIQUI.GCBAPI.Util;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
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
        if(targeter.equals(targeted) || targeted.isDead() || !targeted.isValid()){
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

    private static Location getEyeLocation(Entity e) {
        if (e instanceof LivingEntity) {
            return ((LivingEntity) e).getEyeLocation();
        } else {
            return e.getLocation();
        }
    }
    private static Vector getEyeVector(@Nullable Vector eyevector, Location eyeLocation) {
        return eyevector != null ? eyevector : Util.getVector(eyeLocation.getYaw(), eyeLocation.getPitch());
    }

    private static RayTraceResult performRayTrace(Location source, Vector direction, double range,double radius, boolean hitblock, Predicate<Entity> pred) {
        RayTraceResult ray;
        if (hitblock) {
            ray = source.getWorld().rayTrace(source, direction, range, FluidCollisionMode.NEVER, true, radius, pred);
        } else {
            ray = source.getWorld().rayTraceEntities(source, direction, range,radius, pred);
        }
        return ray;
    }

    public static Entity getEntityTargetEntity(Entity e, @Nullable Vector eyevector,
                                               float radius, float range, boolean hitblock){
        return getEntityTargetEntity(e,eyevector,radius,range,hitblock,null);
    }
    public static Entity getEntityTargetEntity(Entity e, @Nullable Vector eyevector,
                                               float radius, float range, boolean hitblock,@Nullable Predicate predicate){
        range = Math.max(range - (radius * 2), 0);
        Location eyelocation = getEyeLocation(e);
        eyevector = getEyeVector(eyevector, eyelocation);

        Predicate<Entity> pred = ent -> basicFiller(e, ent) && (predicate == null || predicate.test(ent));
        eyelocation.add(eyevector.normalize().multiply(radius));

        RayTraceResult ray = performRayTrace(eyelocation, eyevector, range,radius, hitblock, pred);
        return ray != null && ray.getHitEntity() != null ? ray.getHitEntity() : null;
    }


    public static Location getEntityTargetLocation(Entity e,@Nullable Vector eyevector,float radius,float range,
                                                   boolean hitentity,boolean hitblock){
        return getEntityTargetLocation(e,eyevector,radius,range,hitentity,hitblock,null);
    }
    public static Location getEntityTargetLocation(Entity e,@Nullable Vector eyevector,float radius,float range,
                                             boolean hitentity,boolean hitblock,@Nullable Predicate predicate){
        range = Math.max(range - (radius * 2), 0);
        Location eyelocation = getEyeLocation(e);
        eyevector = getEyeVector(eyevector, eyelocation);

        Predicate<Entity> pred = ent -> (hitentity) && (basicFiller(e, ent) &&
                        (predicate == null || predicate.test(ent))) ;
        eyelocation.add(eyevector.normalize().multiply(radius));
        RayTraceResult ray = performRayTrace(eyelocation, eyevector, range,radius, hitblock, pred);
        if (ray != null) {
            ray.getHitPosition();
            return ray.getHitPosition().toLocation(e.getWorld());
        }
        return eyelocation.add(eyevector.normalize().multiply(range));
    }


    public static Entity getEntityTargetEntityIn2D(Entity e, @Nullable Vector eyevector,
                                                   float startradius,float endradius, float range, boolean hitblock){
        return getEntityTargetEntityIn2D(e,eyevector,startradius,endradius,range,hitblock,null);
    }
    public static Entity getEntityTargetEntityIn2D(Entity e, @Nullable Vector eyevector,
                                                   double startRadius, double endRadius, double range, boolean hitblock,
                                                   @Nullable Predicate<Entity> predicate) {
        startRadius = Math.max(startRadius, 0);
        // 엔티티의 시야 위치를 결정
        Location eyeLocation = e instanceof LivingEntity ? ((LivingEntity)e).getEyeLocation() : e.getLocation();
        // 시야 벡터가 제공되지 않은 경우 계산
        eyevector = eyevector != null ? eyevector : Util.getVector(eyeLocation.getYaw(),eyeLocation.getPitch());

        World world = eyeLocation.getWorld();
        if (world != null) {
            if (hitblock) {
                RayTraceResult blockResult = world.rayTraceBlocks(eyeLocation,eyevector,range,FluidCollisionMode.NEVER,true);
                if (blockResult != null && blockResult.getHitBlock() != null) {
                    range = Math.min(blockResult.getHitPosition().distance(eyeLocation.toVector()),range);
                }
            }
            double finalStartRadius = startRadius;
            double finalRange = range;
            Vector finalEyevector = eyevector;
            Predicate<Entity> entityPredicate = entity ->
                    basicFiller(e, entity) &&
                    HitboxAPI.isCollide_ConeWithDot(eyeLocation, finalEyevector, finalRange, finalStartRadius, endRadius, entity) &&
                            (predicate == null || predicate.test(entity));

            RayTraceResult entityResult = performRayTrace(eyeLocation, eyevector, range, Math.max(startRadius, endRadius),hitblock, entityPredicate);
            if (entityResult != null && entityResult.getHitEntity() != null) {
                return entityResult.getHitEntity();
            }
        }
        return null;
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