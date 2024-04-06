package org.EIQUI.GCBAPI.Core.projectile;


import org.EIQUI.GCBAPI.Core.CC.Timestop;
import org.EIQUI.GCBAPI.Core.HitboxAPI;
import org.EIQUI.GCBAPI.Core.UnitCollision;
import org.EIQUI.GCBAPI.Core.projectile.Event.ProjectileEndEvent;
import org.EIQUI.GCBAPI.Core.projectile.Event.ProjectileHitBlockEvent;
import org.EIQUI.GCBAPI.Core.projectile.Event.ProjectileHitEntityEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

import static org.EIQUI.GCBAPI.main.that;

public class AABBProjectile extends Projectile{
    protected double hitvrp;
    protected double hitvrn;
    protected boolean hitentity;
    protected boolean hitblock;

    public AABBProjectile(String skill, Location l, Vector v, double d, double hr,double vrp,double vrn,boolean he,boolean hb) {
        super(null,skill, l, v, d, hr);
        this.hitvrp = vrp;
        this.hitvrn = vrn;
        this.hitentity = he;
        this.hitblock = hb;
    }
    public AABBProjectile(@Nullable Entity caster, String skill, Location l, Vector v, double d, double hr, double vrp, double vrn, boolean he, boolean hb) {
        super(caster, skill, l, v, d, hr);
        this.hitvrp = vrp;
        this.hitvrn = vrn;
        this.hitentity = he;
        this.hitblock = hb;
    }

    public static Entity spawn(String skill,Location l,Vector v,double d,double hr,double vrp,double vrn,boolean he,boolean hb){
        return spawn(null,skill,l,v,d,hr,vrp,vrn,he,hb);
    }
    public static Entity spawn(@Nullable Entity caster,String skill,Location l,Vector v,double d,double hr,double vrp,double vrn,boolean he,boolean hb){
        AABBProjectile projectile = new AABBProjectile(caster,skill,l,v,d,hr,vrp,vrn,he,hb);
        Entity e = projectile.projectileEntity;
        projectile.startTick();
        return e;
    }

    public static Entity spawnWithoutTick(@Nullable Entity caster,String skill,Location l,Vector v,double d,double hr,double vrp,double vrn,boolean he,boolean hb){
        AABBProjectile projectile = new AABBProjectile(caster,skill,l,v,d,hr,vrp,vrn,he,hb);
        projectile.checkTaskAndRemove();
        return projectile.projectileEntity;
    }

    @Override
    public void startTick(){
       this.task = new BukkitRunnable() {
            @Override
            public void run() {
                if(!tick()){
                    cancel();
                }
            }
        }.runTaskTimer(that, 0, 1);
    }

    @Override
    protected boolean tick(){
        if(!isValid()){
            this.deadLocation = this.projectileEntity.getLocation();
            if(!this.projectileEntity.isDead() && this.projectileEntity.isValid() ){
                zeroLengthEntityCollision();
                Bukkit.getPluginManager().callEvent(new ProjectileEndEvent(this));
            }
            markForRemoval();
            return false;
        }
        if(!Timestop.isTimestopped(this.projectileEntity)){
            entityCollision();
            if(!Timestop.isTimestopped(this.projectileEntity)){
                this.duration--;
            }
        }
        return true;
    }
    @Override
    protected void entityCollision(){
        int size = (int) Math.ceil(this.velocity.length()*DELTA);
        double min = Math.min(this.hitRadius,Math.min(this.hitvrp,hitvrn));
        if(min < 1){
            size = (int) Math.round(size/Math.max(0.25f,min));
        }
        if (!isValid() || Timestop.isTimestopped(this.projectileEntity)) {
            return;
        }
        for(int i = 0; i < size; i++){
            Location l = this.projectileEntity.getLocation();
            if(this.hitentity) {
                if (isValid() && !Timestop.isTimestopped(this.projectileEntity)) {
                    for (Entity e : HitboxAPI.getEntity_inCube(l.clone().add(hitRadius, hitvrp, hitRadius), l.clone().add(-hitRadius, hitvrn, -hitRadius), true)) {
                        if (checkTarget(this.projectileEntity, e)) {
                            ProjectileHitEntityEvent event = new ProjectileHitEntityEvent(this, new RayTraceResult(l.toVector(), e));
                            Bukkit.getPluginManager().callEvent(event);
                            if (!event.isCancelled()) {
                                hittedEntity.add(e);
                            }
                        }
                        if (!isValid() || Timestop.isTimestopped(this.projectileEntity)) {
                            return;
                        }
                    }
                }
            }
            if(this.hitblock) {
                if (isValid() && !Timestop.isTimestopped(this.projectileEntity)) {
                    for (Block b : UnitCollision.WithBlock(this.projectileEntity, this.velocity.clone().multiply(DELTA), hitRadius, hitvrp, hitvrn)) {
                        Vector nl = UnitCollision.WithBlock_Location(
                                this.projectileEntity, this.velocity.clone().multiply(DELTA), hitRadius, hitvrp, hitvrn).toVector();
                        ProjectileHitBlockEvent event = new ProjectileHitBlockEvent(this, new RayTraceResult(nl, b, BlockFace.NORTH));
                        Bukkit.getPluginManager().callEvent(event);
                        break;
                    }
                    if (!isValid() || Timestop.isTimestopped(this.projectileEntity)) {
                        return;
                    }
                }
            }
            if (!isValid() || Timestop.isTimestopped(this.projectileEntity)) {
                return;
            }
            Vector move = this.velocity.clone().multiply(DELTA*(1.0f/size));
            l.add(move);
            this.projectileEntity.teleport(l);
            size = (int) Math.ceil(this.velocity.length()*DELTA);
            min = Math.min(this.hitRadius,Math.min(this.hitvrp,hitvrn));
            if(min < 1){
                size = (int) Math.round(size/Math.max(0.25f,min));
            }
        }
    }
    @Override
    protected void zeroLengthEntityCollision() {
        if (!isValid() || Timestop.isTimestopped(this.projectileEntity)) {
            return;
        }
        Location l = this.projectileEntity.getLocation();
        if(this.hitentity) {
            if (isValid() && !Timestop.isTimestopped(this.projectileEntity)) {
                for (Entity e : HitboxAPI.getEntity_inCube(l.clone().add(hitRadius, hitvrp, hitRadius), l.clone().add(-hitRadius, hitvrn, -hitRadius), true)) {
                    if (checkTarget(this.projectileEntity, e)) {
                        ProjectileHitEntityEvent event = new ProjectileHitEntityEvent(this, new RayTraceResult(l.toVector(), e));
                        Bukkit.getPluginManager().callEvent(event);
                        if (!event.isCancelled()) {
                            hittedEntity.add(e);
                        }
                    }
                    if (!isValid() || Timestop.isTimestopped(this.projectileEntity)) {
                        return;
                    }
                }
            }
        }
        if(this.hitblock) {
            if (isValid() && !Timestop.isTimestopped(this.projectileEntity)) {
                for (Block b : UnitCollision.WithBlock(this.projectileEntity, this.velocity.clone().multiply(DELTA), hitRadius, hitvrp, hitvrn)) {
                    Vector nl = UnitCollision.WithBlock_Location(
                            this.projectileEntity, this.velocity.clone().multiply(DELTA), hitRadius, hitvrp, hitvrn).toVector();
                    ProjectileHitBlockEvent event = new ProjectileHitBlockEvent(this, new RayTraceResult(nl, b, BlockFace.NORTH));
                    Bukkit.getPluginManager().callEvent(event);
                    return;
                }
            }
        }
    }
}