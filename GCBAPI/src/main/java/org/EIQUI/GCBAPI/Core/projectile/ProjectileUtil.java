package org.EIQUI.GCBAPI.Core.projectile;

import org.EIQUI.GCBAPI.Util;
import org.bukkit.util.Vector;


public final class ProjectileUtil {

    public static Vector getMaxRotateVector(Vector currentVelocity, Vector targetVelocity, double maxAngleDegrees) {
        return Util.getMaxRotateVector(currentVelocity,targetVelocity,maxAngleDegrees);
    }

}

