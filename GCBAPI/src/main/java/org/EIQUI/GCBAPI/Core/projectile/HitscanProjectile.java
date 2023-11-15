package org.EIQUI.GCBAPI.Core.projectile;


import jline.internal.Nullable;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;


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
        projectile.startTick();
        return projectile.projectileEntity;
    }

    @Override
    public void startTick(){
        if(!tick()){
            return;
        }
        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                if(!tick()){
                    cancel();
                }
            }
        }.runTaskTimer(that, 1, 1);
    }
}