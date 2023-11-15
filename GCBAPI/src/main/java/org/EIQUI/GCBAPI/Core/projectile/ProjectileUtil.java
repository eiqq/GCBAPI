package org.EIQUI.GCBAPI.Core.projectile;

import org.bukkit.util.Vector;


public final class ProjectileUtil {

    public static Vector getMaxRotateVector(Vector currentVelocity, Vector targetVelocity, double maxAngleDegrees) {
        // 벡터를 정규화합니다.
        Vector currentDirection = currentVelocity.clone().normalize();
        Vector targetDirection = targetVelocity.clone().normalize();
        // 두 벡터 사이의 각도를 계산합니다.
        float angleBetween = currentDirection.angle(targetDirection);
        // 최대 회전 각도를 라디안으로 변환합니다.
        float maxAngleRadians = (float) Math.toRadians(maxAngleDegrees);

        if (angleBetween <= maxAngleRadians) {
            return targetVelocity.clone();
        }
        // 각도가 허용치를 초과하는 경우, 벡터를 조정합니다.
        // 회전 축을 계산합니다. (두 벡터의 외적)
        Vector cross = currentDirection.clone().crossProduct(targetDirection).normalize();
        if(cross.dot(new Vector(0,1,0)) > 1){
            currentDirection.rotateAroundAxis(cross,-maxAngleRadians);
        }else{
            currentDirection.rotateAroundAxis(cross,maxAngleRadians);
        }
        return currentDirection.multiply(targetVelocity.length());
    }

/*
    private static Set<Entity> RaytraceEntities(@Nullable Collection<Entity> save, Entity c, Entity projectile, String skill, Vector v, double distance, double hr){
        Set<Entity> checker = new HashSet();
        if(save != null){
            checker.addAll(save);
        }
        Predicate<Entity> pred = ent -> !checker.contains(ent) && checktarget(projectile,ent);
        Location l = projectile.getLocation();
        World w = l.getWorld();
        RayTraceResult temp;
        do{
            if(v.length() == 0){
                return checker;
            }
            temp = w.rayTraceEntities(l,v,distance,hr,pred);
            if(temp == null){
                return checker;
            }
            ProjectileHitEntityEvent event = new ProjectileHitEntityEvent(c,projectile,skill,temp);
            Bukkit.getPluginManager().callEvent(event);
            checker.add(event.victim);
            if(Timestop.isTimestopped(projectile)){
                return checker;
            }
            if(projectile.isDead()){
                return checker;
            }
            l = projectile.getLocation();
            w = l.getWorld();
            c = event.caster;
            skill = event.skill;
            v = event.provecafterevent;
            pred = ent -> !checker.contains(ent) && checktarget(projectile,ent);
        }while (true);
    }

    private static Set<Entity> RaytraceEntities(Entity c,Entity projectile,String skill, Vector v, double distance, double hr){
        return RaytraceEntities(new HashSet<>(),c,projectile,skill,v,distance,hr);
    }

    public static Set<Entity> RaytraceEntitiesStopAtBlock(Entity c,Entity projectile,String skill, Vector v, double distance, double hr){
        Set<Entity> checker = new HashSet();
        Predicate<Entity> pred = ent -> !checker.contains(ent) && checktarget(projectile,ent);
        Location l = projectile.getLocation();
        World w = l.getWorld();
        RayTraceResult temp;
        do{
            if(v.length() == 0){
                return checker;
            }
            temp = w.rayTrace(l,v,distance, FluidCollisionMode.NEVER,true,hr,pred);
            if(temp == null){
                return checker;
            }
            if(temp.getHitBlock() != null){
                return checker;
            }
            ProjectileHitEntityEvent event = new ProjectileHitEntityEvent(c,projectile,skill,temp);
            Bukkit.getPluginManager().callEvent(event);
            checker.add(event.victim);
            if(Timestop.isTimestopped(projectile)){
                return checker;
            }
            if(projectile.isDead()){
                return checker;
            }
            l = projectile.getLocation();
            w = l.getWorld();
            c = event.caster;
            skill = event.skill;
            v = event.provecafterevent;
            pred = ent -> !checker.contains(ent) && checktarget(projectile,ent);
        }while (true);
    }
 */
}

