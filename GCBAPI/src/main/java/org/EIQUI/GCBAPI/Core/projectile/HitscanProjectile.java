package org.EIQUI.GCBAPI.Core.projectile;


import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

import static org.EIQUI.GCBAPI.main.that;

public class HitscanProjectile extends Projectile{
    public HitscanProjectile(String skill, Location l, Vector v, double d, double hr) {
        super(null,skill, l, v, d, hr);
    }
    public HitscanProjectile(@Nullable Entity caster, String skill, Location l, Vector v, double d, double hr) {
        super(caster, skill, l, v, d, hr);
    }

    public static Entity spawn(String skill,Location l,Vector v,double d,double hr){
        return spawn(null,skill,l,v,d,hr);
    }
    public static Entity spawn(@Nullable Entity caster,String skill,Location l,Vector v,double d,double hr){
        HitscanProjectile projectile = new HitscanProjectile(caster,skill,l,v,d,hr);
        Entity e = projectile.projectileEntity;
        projectile.startTick();
        return e;
    }

    public static Entity spawnWithoutTick(@Nullable Entity caster,String skill,Location l,Vector v,double d,double hr){
        HitscanProjectile projectile = new HitscanProjectile(caster,skill,l,v,d,hr);
        projectile.checkTaskAndRemove();
        return projectile.projectileEntity;
    }

    @Override
    public void startTick(){
        if(!tick()){
            return;
        }
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if(!tick()){
                    cancel();
                }
            }
        }.runTaskTimer(that, 1, 1);
    }
    @Override
    protected void markForRemoval(){
        if(task != null){
            task.cancel();
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                SAVED_PROJECTILES.remove(projectileEntity);
                projectileEntity.remove();
                duration = 0;
            }
        }.runTaskLater(that,1);

    }
}