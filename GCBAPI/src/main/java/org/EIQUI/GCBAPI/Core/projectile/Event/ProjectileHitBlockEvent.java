package org.EIQUI.GCBAPI.Core.projectile.Event;

import org.EIQUI.GCBAPI.Core.projectile.Projectile;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.util.RayTraceResult;


public class ProjectileHitBlockEvent extends ProjectileEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private Block block;
    private BlockFace face;
    private Location hitlocation;
    private boolean cancelled = false;

    public ProjectileHitBlockEvent(Projectile p, RayTraceResult ray) {
        super(p);
        this.block = ray.getHitBlock();
        this.face = ray.getHitBlockFace();
        this.hitlocation = ray.getHitPosition().toLocation(p.getProjectileEntity().getWorld());
        this.cancelled = false;
    }

    public Block getBlock(){
        return this.block;
    }

    public BlockFace getBlockFace(){
        return this.face;
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
