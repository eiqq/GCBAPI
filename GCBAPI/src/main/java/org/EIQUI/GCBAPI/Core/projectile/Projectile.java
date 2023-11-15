package org.EIQUI.GCBAPI.Core.projectile;

import jline.internal.Nullable;
import org.EIQUI.GCBAPI.Core.CC.Timestop;
import org.EIQUI.GCBAPI.Core.projectile.Event.ProjectileEndEvent;
import org.EIQUI.GCBAPI.Core.projectile.Event.ProjectileHitBlockEvent;
import org.EIQUI.GCBAPI.Core.projectile.Event.ProjectileHitEntityEvent;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Predicate;

import static org.EIQUI.GCBAPI.main.that;

public class Projectile{

    protected Entity projectileEntity;
    protected Vector velocity;
    protected String skill;
    protected Location spawnedLoc;
    protected double duration;
    protected double maxduration;
    protected double hitRadius;
    protected Entity owner;
    protected Location deadLocation;
    protected Set<Entity> hittedEntity = new HashSet<>();
    protected BukkitTask task;
    protected Entity target;
    public static Map<Entity,Projectile> projectiles = new HashMap<>();

    public Projectile(String skill,Location l,Vector v,double d,double hr){
        this(null,skill,l,v,d,hr);
    }
    public Projectile(@Nullable Entity caster,String skill,Location l,Vector v,double d,double hr){
        this.projectileEntity = l.getWorld().spawnEntity(l, EntityType.MARKER);
        this.velocity = v.clone();
        this.skill = skill;
        this.spawnedLoc = l.clone();
        this.duration = d;
        this.maxduration = d;
        this.hitRadius = hr;
        this.owner = caster;

        if (this.velocity.length() <= 0){
            this.velocity = new Vector(0,Vector.getEpsilon(),0);
        }
        projectiles.put(this.projectileEntity,this);
    }

    public static Entity spawn(String skill,Location l,Vector v,double d,double hr){
        return spawn(null,skill,l,v,d,hr);
    }
    public static Entity spawn(@Nullable Entity caster,String skill,Location l,Vector v,double d,double hr){
        Projectile projectile = new Projectile(caster,skill,l,v,d,hr);
        projectile.startTick();
        return projectile.projectileEntity;
    }

    public void startTick(){
        if(!tick0()){
            return;
        }
        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                if(!tick()){
                    this.cancel();
                }
            }
        }.runTaskTimer(that, 1, 1);
    }
    protected boolean tick(){
        if(this.duration <= 0 || !this.projectileEntity.isValid() || this.projectileEntity.isDead()){
            this.deadLocation = this.projectileEntity.getLocation();
            if(!this.projectileEntity.isDead() && this.projectileEntity.isValid()){
                if(!Timestop.isTimestopped(this.projectileEntity)){
                    makecollision0();
                }
                if(!this.projectileEntity.isDead() && this.projectileEntity.isValid()) {
                    Bukkit.getPluginManager().callEvent(new ProjectileEndEvent(this));
                }
            }
            this.projectileEntity.remove();
            projectiles.remove(this.projectileEntity);
            return false;
        }

        if(!Timestop.isTimestopped(this.projectileEntity)){
            if(this.hitRadius >= 0){
                makecollision();
            }else{
                makeBlockcollision();
            }
            if(!Timestop.isTimestopped(this.projectileEntity)){
                if(this.duration >= 1){
                    this.projectileEntity.teleport(this.projectileEntity.getLocation().add(this.velocity.clone().multiply(0.05)));
                }else{
                    this.projectileEntity.teleport(this.projectileEntity.getLocation().add(this.velocity.clone().multiply(0.05*this.duration)));
                }
                this.duration--;
            }
        }
        return true;
    }
    protected void makecollision(){
        Set<Entity> hitsave = new HashSet();
        Predicate<Entity> pred;
        pred = ent -> !hitsave.contains(ent) && checkTarget(this.projectileEntity,ent);
        World w = this.projectileEntity.getLocation().getWorld();
        RayTraceResult temp = null;
        boolean hittedblock = false;
        do{
            if(temp != null){
                if(temp.getHitEntity() != null){
                    ProjectileHitEntityEvent event = new ProjectileHitEntityEvent(this,temp);
                    Bukkit.getPluginManager().callEvent(event);
                    hitsave.add(temp.getHitEntity());
                    if(!event.isCancelled()){
                        hittedEntity.add(temp.getHitEntity());
                    }
                }else if(temp.getHitBlock() != null){
                    ProjectileHitBlockEvent event = new ProjectileHitBlockEvent(this,temp);
                    Bukkit.getPluginManager().callEvent(event);
                    hittedblock = true;
                }
            }
            if(this.duration <= 0 || !this.projectileEntity.isValid() || this.velocity.length() < Vector.getEpsilon() || Timestop.isTimestopped(this.projectileEntity)){
                return;
            }

            double length;
            if(this.duration >= 1) {
                length = velocity.length() * 0.05;
            }else{
                length = velocity.length() * 0.05 * this.duration;
            }
            if(hittedblock){
                temp = w.rayTraceEntities(this.projectileEntity.getLocation(),this.velocity,length,hitRadius,pred);
            }else {
                temp = w.rayTrace(this.projectileEntity.getLocation(), this.velocity,
                        length, FluidCollisionMode.NEVER, true, hitRadius, pred);
            }
        }while (temp != null);
    }
    protected void makeBlockcollision(){
        World w = this.projectileEntity.getLocation().getWorld();
        RayTraceResult temp;
        if(this.duration >= 1){
            temp = w.rayTraceBlocks(this.projectileEntity.getLocation(),this.velocity,
                    this.velocity.length()*0.05,FluidCollisionMode.NEVER);
        }else{
            temp = w.rayTraceBlocks(this.projectileEntity.getLocation(),this.velocity,
                    this.velocity.length()*0.05*this.duration,FluidCollisionMode.NEVER);
        }
        if(temp !=null){
            ProjectileHitBlockEvent event = new ProjectileHitBlockEvent(this,temp);
            Bukkit.getPluginManager().callEvent(event);
        }
    }
    protected boolean tick0(){
        if(this.duration <= 0 || !this.projectileEntity.isValid()){
            this.deadLocation = this.projectileEntity.getLocation();
            this.projectileEntity.remove();
            return false;
        }
        if(!Timestop.isTimestopped(this.projectileEntity)){
            if(this.hitRadius >= 0){
                makecollision0();
            }else{
                makeBlockcollision0();
            }
        }
        return true;
    }
    protected void makecollision0(){
        Set<Entity> hitsave = new HashSet();
        Predicate<Entity> pred;
        pred = ent -> !hitsave.contains(ent) && checkTarget(this.projectileEntity,ent);
        World w = this.projectileEntity.getLocation().getWorld();
        RayTraceResult temp = null;
        do{
            if(temp != null){
                if(temp.getHitEntity() != null){
                    ProjectileHitEntityEvent event = new ProjectileHitEntityEvent(this,temp);
                    Bukkit.getPluginManager().callEvent(event);
                    hitsave.add(temp.getHitEntity());
                    if(!event.isCancelled()){
                        hittedEntity.add(temp.getHitEntity());
                    }
                }else if(temp.getHitBlock() != null){
                    ProjectileHitBlockEvent event = new ProjectileHitBlockEvent(this,temp);
                    Bukkit.getPluginManager().callEvent(event);
                }
            }
            if(!this.projectileEntity.isValid() || this.velocity.length() < Vector.getEpsilon() || Timestop.isTimestopped(this.projectileEntity)){
                return;
            }

            temp = w.rayTrace(this.projectileEntity.getLocation(),this.velocity, Vector.getEpsilon(),
                    FluidCollisionMode.NEVER,true,hitRadius,pred);

        }while (temp != null);
    }
    protected void makeBlockcollision0(){
        World w = this.projectileEntity.getLocation().getWorld();
        RayTraceResult temp = w.rayTraceBlocks(this.projectileEntity.getLocation(),
                this.velocity,Vector.getEpsilon(),FluidCollisionMode.NEVER);
        if(temp !=null){
            ProjectileHitBlockEvent event = new ProjectileHitBlockEvent(this,temp);
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    public Entity getCaster(){
        return this.owner;
    }
    public Entity getProjectileEntity(){
        return this.projectileEntity;
    }
    public String getSkill(){
        return this.skill;
    }

    public Location getDeadLocation(){
        return this.deadLocation;
    }

    public static boolean isProjectile(Entity projectile){
        return projectiles.containsKey(projectile);
    }

    public static void setTarget(Entity projectile,@Nullable Entity target){
        if(!isProjectile(projectile)){return;}
        Projectile p = projectiles.get(projectile);
        if (target == null){
            p.target = null;
        }else{
            p.target = target;
        }
    }
    public static void setVector(Entity projectile,Vector v){
        if(!isProjectile(projectile)){return;}
        Projectile p = projectiles.get(projectile);
        p.velocity = v;
        if (p.velocity.length() <= 0){
            p.velocity = new Vector(0,Vector.getEpsilon(),0);
        }
    }
    public static void setSpeed(Entity projectile,double spd){
        if(!isProjectile(projectile)){return;}
        Projectile p = projectiles.get(projectile);
        if (p.velocity.length() <= Vector.getEpsilon()){
            p.velocity = new Vector(0,Vector.getEpsilon(),0);
        }else{
            p.velocity.normalize().multiply(spd);
        }
    }
    public static void setDuration(Entity projectile,double d){
        if(!isProjectile(projectile)){return;}
        Projectile p = projectiles.get(projectile);
        p.duration = d;
    }
    public static void setHitRadius(Entity projectile,double d){
        if(!isProjectile(projectile)){return;}
        Projectile p = projectiles.get(projectile);
        p.hitRadius = d;
    }
    public static void setCaster(Entity projectile,Entity caster){
        if(!isProjectile(projectile)){return;}
        Projectile p = projectiles.get(projectile);
        p.owner = caster;
    }

    public static Entity getTarget(Entity projectile){
        if(!isProjectile(projectile)){return null;}
        Projectile p = projectiles.get(projectile);
        return p.target;
    }
    public static Vector getVector(Entity projectile){
        if(!isProjectile(projectile)){return null;}
        Projectile p = projectiles.get(projectile);
        return p.velocity.clone();
    }
    public static double getSpeed(Entity projectile){
        if(!isProjectile(projectile)){return 0;}
        Projectile p = projectiles.get(projectile);
        return p.velocity.length();
    }
    public static double getDuration(Entity projectile){
        if(!isProjectile(projectile)){return 0;}
        Projectile p = projectiles.get(projectile);
        return p.duration;
    }

    public static double getMaxDuration(Entity projectile){
        if(!isProjectile(projectile)){return 0;}
        Projectile p = projectiles.get(projectile);
        return p.maxduration;
    }
    public static double getHitRadius(Entity projectile){
        if(!isProjectile(projectile)){return 0;}
        Projectile p = projectiles.get(projectile);
        return p.hitRadius;
    }
    public static Entity getCaster(Entity projectile){
        if(!isProjectile(projectile)){return null;}
        Projectile p = projectiles.get(projectile);
        return p.owner;
    }
    public static String getSkill(Entity projectile){
        if(!isProjectile(projectile)){return null;}
        Projectile p = projectiles.get(projectile);
        return p.skill;
    }
    public static UUID getID(Entity projectile){
        if(!isProjectile(projectile)){return null;}
        Projectile p = projectiles.get(projectile);
        return p.projectileEntity.getUniqueId();
    }

    public static boolean isHittedEntity(Entity projectile,Entity e){
        if(!isProjectile(projectile)){return false;}
        Projectile p = projectiles.get(projectile);
        return p.hittedEntity.contains(e);
    }

    public static void clearHittedEntity(Entity projectile){
        if(!isProjectile(projectile)){return;}
        Projectile p = projectiles.get(projectile);
        p.hittedEntity.clear();
    }

    public static void reset(Entity projectile){
        if(!isProjectile(projectile)){return;}
        Projectile p = projectiles.get(projectile);
        p.hittedEntity.clear();
        p.duration = p.maxduration;
    }

    public static boolean checkTarget(Entity p,Entity v){
        if(p.equals(v)){
            return false;
        }
        if(v.isDead()){
            return false;
        }
        EntityType type = v.getType();
        if (!type.isAlive()){
            if(type == EntityType.INTERACTION){
                return (v.getName().toLowerCase().contains("projectile") || v.getName().equalsIgnoreCase("all"));
            }
            return false;
        }
        else if (type == EntityType.PLAYER){
            if (((HumanEntity)v).getGameMode() == GameMode.SPECTATOR){
                return false;
            }
        }else if(type == EntityType.ARMOR_STAND) {
            if (((ArmorStand)v).isMarker()) {
                return false;
            }
        }
        BoundingBox b = v.getBoundingBox();
        return b.getMax().distanceSquared(b.getMin()) > Vector.getEpsilon();
    }

    public static void clear(){
        for(Entity e:projectiles.keySet()){
            e.remove();
        }
    }

    public static class ProjectileHandler implements Listener {
        public ProjectileHandler() {
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onDeath(EntityDeathEvent e) {
            if(isProjectile(e.getEntity())){
                Projectile p = projectiles.get(e);
                p.task.cancel();
                e.getEntity().remove();
            }
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onUnload(EntitiesLoadEvent e){
            if(!projectiles.isEmpty()){
                for(Entity entity: e.getEntities()){
                    if(isProjectile(entity)){
                        entity.getLocation().getChunk().load();
                        entity.remove();
                        Projectile p = projectiles.get(entity);
                        p.task.cancel();
                    }
                }
            }
        }
    }
}
