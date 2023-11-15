package org.EIQUI.GCBAPI.Core.projectile.Event;

import org.EIQUI.GCBAPI.Core.projectile.Projectile;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class ProjectileEvent extends Event{
    private static final HandlerList HANDLERS = new HandlerList();
    private Entity caster;
    private Entity projectile;
    private String skill;
    private Location loc;

    private Projectile pobj;
    public ProjectileEvent(Projectile p) {
        this.caster = p.getCaster();
        this.projectile = p.getProjectileEntity();
        this.skill = p.getSkill();
        this.loc = p.getProjectileEntity().getLocation();
        this.pobj = p;
    }

    public Entity getCaster(){
        return this.caster;
    }
    public Entity getProjectile(){
        return this.projectile;
    }
    public String getSkill(){
        return this.skill;
    }

    public Projectile getProjectileObject(){
        return pobj;
    }
    public Location getLocation(){
        return this.loc;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
