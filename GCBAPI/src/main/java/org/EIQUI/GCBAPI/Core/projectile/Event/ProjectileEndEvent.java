package org.EIQUI.GCBAPI.Core.projectile.Event;

import org.EIQUI.GCBAPI.Core.projectile.Projectile;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class ProjectileEndEvent extends ProjectileEvent{
    private Location loc;
    public ProjectileEndEvent(Projectile p) {
        super(p);
        this.loc = p.getDeadLocation();
    }

    @Override
    public Location getLocation(){
        return this.loc;
    }

}
