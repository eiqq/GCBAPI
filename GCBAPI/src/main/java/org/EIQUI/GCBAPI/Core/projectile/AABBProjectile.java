package org.EIQUI.GCBAPI.Core.projectile;


import jline.internal.Nullable;
import org.EIQUI.GCBAPI.Core.CC.Timestop;
import org.EIQUI.GCBAPI.Core.projectile.Event.ProjectileEndEvent;
import org.EIQUI.GCBAPI.Core.projectile.Event.ProjectileHitBlockEvent;
import org.EIQUI.GCBAPI.Core.projectile.Event.ProjectileHitEntityEvent;
import org.EIQUI.GCBAPI.HitboxAPI;
import org.EIQUI.GCBAPI.UnitCollision;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

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
    public AABBProjectile(@Nullable Entity caster, String skill, Location l, Vector v, double d,  double hr,double vrp,double vrn,boolean he,boolean hb) {
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
        projectile.startTick();
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
        if(this.duration <= 0 || !this.projectileEntity.isValid() || this.projectileEntity.isDead()){
            this.deadLocation = this.projectileEntity.getLocation();
            if(!this.projectileEntity.isDead() && this.projectileEntity.isValid()){
                if(!Timestop.isTimestopped(this.projectileEntity)){
                    makecollision0();
                }
                if(!this.projectileEntity.isDead() && this.projectileEntity.isValid()){
                    Bukkit.getPluginManager().callEvent(new ProjectileEndEvent(this));
                }
            }
            this.projectileEntity.remove();
            projectiles.remove(this.projectileEntity,this);
            return false;
        }
        if(!Timestop.isTimestopped(this.projectileEntity)){
            makecollision();
            if(!Timestop.isTimestopped(this.projectileEntity)){
                this.duration--;
            }
        }
        return true;
    }
    @Override
    protected void makecollision(){
        int size = (int) Math.ceil(this.velocity.length()/20);
        double min = Math.min(this.hitRadius,Math.min(this.hitvrp,hitvrn));
        if(min < 1){
            size = (int) Math.round(size/Math.max(0.25f,min));
        }
        if (this.duration <= 0 || !this.projectileEntity.isValid() || this.velocity.length() < Vector.getEpsilon() || Timestop.isTimestopped(this.projectileEntity)) {
            return;
        }
        for(int i = 0; i < size; i++){
            Location l = this.projectileEntity.getLocation();
            if(this.hitentity) {
                if (!((!this.projectileEntity.isValid() || this.velocity.length() < Vector.getEpsilon() || Timestop.isTimestopped(this.projectileEntity)))) {
                    for (Entity e : HitboxAPI.getEntity_inCube(l.clone().add(hitRadius, hitvrp, hitRadius), l.clone().add(-hitRadius, hitvrn, -hitRadius), true)) {
                        if (checkTarget(this.projectileEntity, e)) {
                            ProjectileHitEntityEvent event = new ProjectileHitEntityEvent(this, new RayTraceResult(l.toVector(), e));
                            Bukkit.getPluginManager().callEvent(event);
                            if (!event.isCancelled()) {
                                hittedEntity.add(e);
                            }
                        }
                        if (this.duration <= 0 || !this.projectileEntity.isValid() || this.velocity.length() < Vector.getEpsilon()|| Timestop.isTimestopped(this.projectileEntity)) {
                            return;
                        }
                    }
                }
            }
            if(this.hitblock) {
                if (!((this.duration <= 0 || !this.projectileEntity.isValid() || this.velocity.length() < Vector.getEpsilon() || Timestop.isTimestopped(this.projectileEntity)))) {
                    for (Block b : UnitCollision.WithBlock(this.projectileEntity, this.velocity.clone().multiply(0.05), hitRadius, hitvrp, hitvrn)) {
                        Vector nl = UnitCollision.WithBlock_Location(
                                this.projectileEntity, this.velocity.clone().multiply(0.05), hitRadius, hitvrp, hitvrn).toVector();
                        ProjectileHitBlockEvent event = new ProjectileHitBlockEvent(this, new RayTraceResult(nl, b, BlockFace.NORTH));
                        Bukkit.getPluginManager().callEvent(event);
                        break;
                    }
                    if (!this.projectileEntity.isValid() || this.velocity.length() < Vector.getEpsilon() || Timestop.isTimestopped(this.projectileEntity)) {
                        return;
                    }
                }
            }
            if (this.duration <= 0 || !this.projectileEntity.isValid() || this.velocity.length() < Vector.getEpsilon() || Timestop.isTimestopped(this.projectileEntity)) {
                return;
            }
            Vector move = this.velocity.clone().multiply(0.05f*(1.0f/size));
            l.add(move);
            this.projectileEntity.teleport(l);
            size = (int) Math.ceil(this.velocity.length()/20);
            min = Math.min(this.hitRadius,Math.min(this.hitvrp,hitvrn));
            if(min < 1){
                size = (int) Math.round(size/Math.max(0.25f,min));
            }
        }
    }
    @Override
    protected void makecollision0() {
        if (!this.projectileEntity.isValid() || this.velocity.length() < Vector.getEpsilon() || Timestop.isTimestopped(this.projectileEntity)) {
            return;
        }
        Location l = this.projectileEntity.getLocation();
        if(this.hitentity) {
            if (!((!this.projectileEntity.isValid() || this.velocity.length() == 0 || Timestop.isTimestopped(this.projectileEntity)))) {
                for (Entity e : HitboxAPI.getEntity_inCube(l.clone().add(hitRadius, hitvrp, hitRadius), l.clone().add(-hitRadius, hitvrn, -hitRadius), true)) {
                    if (checkTarget(this.projectileEntity, e)) {
                        ProjectileHitEntityEvent event = new ProjectileHitEntityEvent(this, new RayTraceResult(l.toVector(), e));
                        Bukkit.getPluginManager().callEvent(event);
                        if (!event.isCancelled()) {
                            hittedEntity.add(e);
                        }
                    }
                    if (!this.projectileEntity.isValid() || this.velocity.length() < Vector.getEpsilon() || Timestop.isTimestopped(this.projectileEntity)) {
                        return;
                    }
                }
            }
        }
        if(this.hitblock) {
            if (!((!this.projectileEntity.isValid() || this.velocity.length() < Vector.getEpsilon() || Timestop.isTimestopped(this.projectileEntity)))) {
                for (Block b : UnitCollision.WithBlock(this.projectileEntity, this.velocity.clone().multiply(0.05), hitRadius, hitvrp, hitvrn)) {
                    Vector nl = UnitCollision.WithBlock_Location(
                            this.projectileEntity, this.velocity.clone().multiply(0.05), hitRadius, hitvrp, hitvrn).toVector();
                    ProjectileHitBlockEvent event = new ProjectileHitBlockEvent(this, new RayTraceResult(nl, b, BlockFace.NORTH));
                    Bukkit.getPluginManager().callEvent(event);
                    return;
                }
            }
        }
    }
}