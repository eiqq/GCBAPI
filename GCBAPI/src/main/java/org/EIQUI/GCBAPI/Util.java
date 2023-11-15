package org.EIQUI.GCBAPI;

import org.bukkit.util.Vector;

public class Util {

    private static float getYaw(Vector vector){
        if (Double.valueOf(vector.getX()).equals(0.0) && Double.valueOf(vector.getZ()).equals(0.0)) {
            return 0.0f;
        }
        return (float)(Math.atan2(vector.getZ(), vector.getX()) * 57.29577951308232);
    }
    private static float getPitch(Vector vector){
        final double xy = Math.sqrt(vector.getX() * vector.getX() + vector.getZ() * vector.getZ());
        return (float)(Math.atan(vector.getY() / xy) * 57.29577951308232);
    }

    public static Vector getVector(double yaw, double pitch){
        final double y = Math.sin(pitch * 0.017453292519943295);
        final double div = Math.cos(pitch * 0.017453292519943295);
        double x = Math.cos(yaw * 0.017453292519943295);
        double z = Math.sin(yaw * 0.017453292519943295);
        x *= div;
        z *= div;
        return new Vector(x, y, z);
    }
}
