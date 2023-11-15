package org.EIQUI.GCBAPI.Core.projectile.Event;

import org.EIQUI.GCBAPI.Core.projectile.Projectile;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.util.RayTraceResult;


public class ProjectileHitEntityEvent extends ProjectileEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private Entity victim;
    private Location hitlocation;
    private boolean cancelled = false;

    public ProjectileHitEntityEvent(Projectile p, RayTraceResult ray) {
        super(p);
        this.victim = ray.getHitEntity();
        this.hitlocation = ray.getHitPosition().toLocation(p.getProjectileEntity().getWorld());
        this.cancelled = false;
    }

    public Entity getVictim(){
        return this.victim;
    }

    @Override
    public Location getLocation(){
        return this.hitlocation;
    }


    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {

    }
}
