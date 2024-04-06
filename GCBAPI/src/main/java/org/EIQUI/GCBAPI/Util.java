package org.EIQUI.GCBAPI;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import static org.EIQUI.GCBAPI.main.that;

public class Util {

    // 라디안을 도로 변환하는 상수
    private static final double TO_DEGREES = 180.0 / Math.PI;
    // 도를 라디안으로 변환하는 상수
    private static final double TO_RADIANS = Math.PI / 180.0;

    public static void appendEntityToEntity(Entity app,Entity target, int killlater){
        appendEntityToEntity(app,target,1,1,killlater);
    }
    public static void appendEntityToEntity(Entity app,Entity target,float yawMultiplyer,float pitchMultiplyer, int killlater){
        appendEntityToEntity(app,target,yawMultiplyer,pitchMultiplyer,-90,90,killlater);
    }
    public static void appendEntityToEntity(Entity app,Entity target
            ,float yawMultiplyer,float pitchMultiplyer,float maxpitchUp,float maxpitchDown, int killlater){
        new BukkitRunnable() {
            Location l = target.getLocation();
            @Override
            public void run() {
                if(target.isDead() || !target.isValid()){
                    cancel();
                    if(killlater >= 0){
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                app.remove();
                            }
                        }.runTaskLater(that,killlater);
                    }
                    return;
                }
                l = target.getLocation();
                l.setYaw(l.getYaw()*yawMultiplyer);
                l.setPitch(l.getPitch()*pitchMultiplyer);
                if(l.getPitch() < maxpitchUp){
                    l.setPitch(maxpitchUp);
                }else if(l.getPitch() > maxpitchDown){
                    l.setPitch(maxpitchDown);
                }
                app.teleport(l);
            }
        }.runTaskTimer(that, 0L, 1);
    }
    public static Vector getMaxRotateVector(Vector current, Vector target, double maxAngleDegrees) {
        Vector currentDirection = current.clone().normalize();
        Vector targetDirection = target.clone().normalize();
        float angleBetween = currentDirection.angle(targetDirection);
        double maxAngleRadians = maxAngleDegrees * TO_RADIANS;
        if (angleBetween <= maxAngleRadians) {
            return target.clone();
        }
        Vector cross = currentDirection.clone().crossProduct(targetDirection);
        if(cross.dot(new Vector(0,1,0)) > 1){
            currentDirection.rotateAroundAxis(cross,-maxAngleRadians);
        }else{
            currentDirection.rotateAroundAxis(cross,maxAngleRadians);
        }
        return currentDirection.multiply(target.length());
    }

    public static float getYaw(Vector vector) {
        if (vector.getX() == 0.0 && vector.getZ() == 0.0) {
            return 0.0f;
        }
        return (float) (Math.atan2(vector.getZ(), vector.getX()) * TO_DEGREES);
    }

    public static float getPitch(Vector vector) {
        final double xy = Math.sqrt(vector.getX() * vector.getX() + vector.getZ() * vector.getZ());
        return (float) (Math.atan(vector.getY() / xy) * TO_DEGREES);
    }

    public static Vector getVector(double yaw, double pitch) {
        final double y = Math.sin(pitch * TO_RADIANS);
        final double div = Math.cos(pitch * TO_RADIANS);
        double x = Math.cos(yaw * TO_RADIANS);
        double z = Math.sin(yaw * TO_RADIANS);
        x *= div;
        z *= div;
        return new Vector(x, y, z);
    }

    public static void NaNToZero(Vector v) {
        Double x = v.getX();
        Double y = v.getY();
        Double z = v.getZ();
        if(x.isNaN()){v.setX(0.0d);}
        if(y.isNaN()){v.setY(0.0d);}
        if(z.isNaN()){v.setZ(0.0d);}
    }
    public static Vector setInfiniteTo(Vector v,double d) {
        Double x = v.getX();
        Double y = v.getY();
        Double z = v.getZ();
        if(x.isInfinite()){v.setX(d);}
        if(y.isInfinite()){v.setY(d);}
        if(z.isInfinite()){v.setZ(d);}
        return v;
    }
}
