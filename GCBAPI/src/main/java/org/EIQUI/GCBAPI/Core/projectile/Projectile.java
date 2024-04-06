package org.EIQUI.GCBAPI.Core.projectile;

import org.EIQUI.GCBAPI.Core.CC.Timestop;
import org.EIQUI.GCBAPI.Core.projectile.Event.ProjectileEndEvent;
import org.EIQUI.GCBAPI.Core.projectile.Event.ProjectileHitBlockEvent;
import org.EIQUI.GCBAPI.Core.projectile.Event.ProjectileHitEntityEvent;
import org.EIQUI.GCBAPI.Util;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
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

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    protected final Set<Entity> hittedEntity = Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected BukkitTask task;
    protected Entity target;
    protected static final Map<Entity,Projectile> SAVED_PROJECTILES = new ConcurrentHashMap<>();

    protected static final double DELTA = 0.05d;

    public Projectile(String skill,Location l,Vector v,double d,double hr){
        this(null,skill,l,v,d,hr);
    }
    public Projectile(@Nullable Entity caster, String skill, Location l, Vector v, double d, double hr){
        this.projectileEntity = l.getWorld().spawnEntity(l, EntityType.MARKER);
        this.velocity = Util.setInfiniteTo(v.clone(),0);
        this.skill = skill;
        this.spawnedLoc = l.clone();
        this.duration = d;
        this.maxduration = d;
        this.hitRadius = hr;
        this.owner = caster;

        SAVED_PROJECTILES.put(this.projectileEntity,this);
    }

    public static Entity spawn(String skill,Location l,Vector v,double d,double hr){
        return spawn(null,skill,l,v,d,hr);
    }

    public static Entity spawn(@Nullable Entity caster,String skill,Location l,Vector v,double d,double hr){
        Projectile projectile = new Projectile(caster,skill,l,v,d,hr);
        Entity e = projectile.projectileEntity;
        projectile.startTick();
        return e;
    }

    public static Entity spawnWithoutTick(@Nullable Entity caster,String skill,Location l,Vector v,double d,double hr){
        Projectile projectile = new Projectile(caster,skill,l,v,d,hr);
        projectile.checkTaskAndRemove();
        return projectile.projectileEntity;
    }

    protected void checkTaskAndRemove(){
        new BukkitRunnable() {
            @Override
            public void run() {
                if(task == null){
                    markForRemoval();
                }
            }
        }.runTaskLater(that,5);
    }
    public void startTick(){
        this.task = new BukkitRunnable() {
            boolean b = true;
            @Override
            public void run() {
                if(b){
                    b = false;
                    if(!zeroLengthTick()){
                        cancel();
                    }
                }else if(!tick()){
                    cancel();
                }
            }
        }.runTaskTimer(that, 0, 1);
    }
    public boolean isValid(){
        return this.duration > 0.0d && this.projectileEntity.isValid() && !this.projectileEntity.isDead();
    }
    protected boolean tick(){
        if(!isValid()){
            this.deadLocation = this.projectileEntity.getLocation();
            if(!this.projectileEntity.isDead() && this.projectileEntity.isValid() ){
                Bukkit.getPluginManager().callEvent(new ProjectileEndEvent(this));
            }
            markForRemoval();
            return false;
        }
        if(this.hitRadius >= 0.0d){
            entityCollision();
        }else{
            blockCollision();
        }
        if(!this.projectileEntity.isValid() || this.projectileEntity.isDead()){
            markForRemoval();
            return false;
        }
        if(!Timestop.isTimestopped(this.projectileEntity)){
            if(this.duration >= 1.0d){
                this.projectileEntity.teleport(this.projectileEntity.getLocation().add(this.velocity.clone().multiply(DELTA)));
            }else{
                this.projectileEntity.teleport(this.projectileEntity.getLocation().add(this.velocity.clone().multiply(DELTA*this.duration)));
            }
            this.duration--;
        }
        return true;
    }
    protected void entityCollision(){
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
                    if(isValid()){
                        break;
                    }else{
                        return;
                    }
                }
            }
            if(!isValid() || this.velocity.length() == 0 || Timestop.isTimestopped(this.projectileEntity)){
                return;
            }

            double length;
            if(this.duration >= 1.0d) {
                length = velocity.length() * DELTA;
            }else{
                length = velocity.length() * DELTA * this.duration;
            }
            temp = w.rayTrace(this.projectileEntity.getLocation(), checkZeroVector(),
                    length, FluidCollisionMode.NEVER, true, hitRadius, pred);
        }while (temp != null);
        temp = null;
        do{
            if(temp != null){
                ProjectileHitEntityEvent event = new ProjectileHitEntityEvent(this,temp);
                Bukkit.getPluginManager().callEvent(event);
                hitsave.add(temp.getHitEntity());
                if(!event.isCancelled()){
                    hittedEntity.add(temp.getHitEntity());
                }
            }
            if(!isValid() || this.velocity.length() <= Vector.getEpsilon() || Timestop.isTimestopped(this.projectileEntity)){
                return;
            }

            double length;
            if(this.duration >= 1) {
                length = velocity.length() * DELTA;
            }else{
                length = velocity.length() * DELTA * this.duration;
            }
            temp = w.rayTraceEntities(this.projectileEntity.getLocation(),this.velocity,length,hitRadius,pred);
        }while (temp != null);

    }
    protected void blockCollision(){
        if(!isValid() || this.velocity.length() <= Vector.getEpsilon() || Timestop.isTimestopped(this.projectileEntity)){
            return;
        }
        World w = this.projectileEntity.getLocation().getWorld();
        RayTraceResult temp;
        if(this.duration >= 1.0d){
            temp = w.rayTraceBlocks(this.projectileEntity.getLocation(),checkZeroVector(),
                    this.velocity.length()*DELTA,FluidCollisionMode.NEVER);
        }else{
            temp = w.rayTraceBlocks(this.projectileEntity.getLocation(),checkZeroVector(),
                    this.velocity.length()*DELTA*this.duration,FluidCollisionMode.NEVER);
        }
        if(temp !=null){
            ProjectileHitBlockEvent event = new ProjectileHitBlockEvent(this,temp);
            Bukkit.getPluginManager().callEvent(event);
        }
    }
    protected boolean zeroLengthTick(){
        if(this.hitRadius >= 0.0d){
            zeroLengthEntityCollision();
        }else{
            zeroLengthBlockCollision();
        }
        if(Timestop.isTimestopped(this.projectileEntity)){
            return true;
        }
        if(!isValid()){
            this.deadLocation = this.projectileEntity.getLocation();
            if (!this.projectileEntity.isDead() && this.projectileEntity.isValid()) {
                Bukkit.getPluginManager().callEvent(new ProjectileEndEvent(this));
            }
            markForRemoval();
            return false;
        }
        return true;
    }
    protected void zeroLengthEntityCollision(){
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
                    if(isValid()){
                        break;
                    }else{
                        return;
                    }
                }
            }
            if(!isValid() || this.velocity.length() == 0 || Timestop.isTimestopped(this.projectileEntity)){
                return;
            }
            temp = w.rayTrace(this.projectileEntity.getLocation(), checkZeroVector(),0.0d,
                    FluidCollisionMode.NEVER, true, hitRadius, pred);
        }while (temp != null);
        temp = null;
        do{
            if(temp != null){
                ProjectileHitEntityEvent event = new ProjectileHitEntityEvent(this,temp);
                Bukkit.getPluginManager().callEvent(event);
                hitsave.add(temp.getHitEntity());
                if(!event.isCancelled()){
                    hittedEntity.add(temp.getHitEntity());
                }
            }
            if(!isValid() || this.velocity.length() <= Vector.getEpsilon() || Timestop.isTimestopped(this.projectileEntity)){
                return;
            }
            temp = w.rayTraceEntities(this.projectileEntity.getLocation(),checkZeroVector(),0.0d,hitRadius,pred);
        }while (temp != null);

    }
    protected void zeroLengthBlockCollision(){
        if(!isValid() || this.velocity.length() <= Vector.getEpsilon() || Timestop.isTimestopped(this.projectileEntity)){
            return;
        }
        World w = this.projectileEntity.getLocation().getWorld();
        RayTraceResult temp;
        temp = w.rayTraceBlocks(this.projectileEntity.getLocation(),
                checkZeroVector(),0.0d,FluidCollisionMode.NEVER);
        if(temp !=null){
            ProjectileHitBlockEvent event = new ProjectileHitBlockEvent(this,temp);
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    protected void markForRemoval(){
        if(this.task != null){
            this.task.cancel();
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                SAVED_PROJECTILES.remove(projectileEntity);
                projectileEntity.remove();
                duration = 0;
            }
        }.runTaskLater(that,0);
    }
    protected Vector checkZeroVector(){
        Util.NaNToZero(this.velocity);
        return this.velocity;
    }
    public static boolean checkTarget(Entity p,Entity v){
        if(p.equals(v) || v.isDead()){
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
        return b.getMax().distanceSquared(b.getMin()) >= Vector.getEpsilon();
    }
//---------------------------------------------------------------------------------------------------------------
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

    public static Projectile getProjectileObject(@Nullable Object e){
        if(e == null){
            return null;
        }
        if(e instanceof Projectile){
            return (Projectile) e;
        }
        if(!(e instanceof Entity)){
            return null;
        }
        return SAVED_PROJECTILES.get((Entity) e);
    }

    public static boolean isProjectile(Entity projectile){
        return SAVED_PROJECTILES.containsKey(projectile);
    }

    public static void setTarget(Entity projectile,@Nullable Entity target){
        if(!isProjectile(projectile)){return;}
        Projectile p = SAVED_PROJECTILES.get(projectile);
        p.target = Optional.ofNullable(target).orElse(null);
    }
    public static void setVector(Entity projectile,Vector v){
        if(!isProjectile(projectile)){return;}
        Util.NaNToZero(v);
        Projectile p = SAVED_PROJECTILES.get(projectile);
        p.velocity = v;
        if (p.velocity.length() < Vector.getEpsilon()){
            p.velocity = new Vector(0,0,0);
        }
    }
    public static void setSpeed(Entity projectile,double spd){
        if(!isProjectile(projectile)){return;}
        Projectile p = SAVED_PROJECTILES.get(projectile);
        if (p.velocity.length() < Vector.getEpsilon()){
            p.velocity = new Vector(0,Vector.getEpsilon(),0);
        }else{
            p.velocity.normalize().multiply(spd);
        }
    }
    public static void setDuration(Entity projectile,double d){
        if(!isProjectile(projectile)){return;}
        Projectile p = SAVED_PROJECTILES.get(projectile);
        p.duration = d;
    }
    public static void setHitRadius(Entity projectile,double d){
        if(!isProjectile(projectile)){return;}
        Projectile p = SAVED_PROJECTILES.get(projectile);
        p.hitRadius = d;
    }
    public static void setCaster(Entity projectile,Entity caster){
        if(!isProjectile(projectile)){return;}
        Projectile p = SAVED_PROJECTILES.get(projectile);
        p.owner = caster;
    }

    public static Entity getTarget(Entity projectile){
        if(!isProjectile(projectile)){return null;}
        Projectile p = SAVED_PROJECTILES.get(projectile);
        return p.target;
    }
    public static Vector getVector(Entity projectile){
        if(!isProjectile(projectile)){return null;}
        Projectile p = SAVED_PROJECTILES.get(projectile);
        return p.velocity.clone();
    }
    public static double getSpeed(Entity projectile){
        if(!isProjectile(projectile)){return 0;}
        Projectile p = SAVED_PROJECTILES.get(projectile);
        return p.velocity.length();
    }
    public static double getDuration(Entity projectile){
        if(!isProjectile(projectile)){return 0;}
        Projectile p = SAVED_PROJECTILES.get(projectile);
        return p.duration;
    }

    public static double getMaxDuration(Entity projectile){
        if(!isProjectile(projectile)){return 0;}
        Projectile p = SAVED_PROJECTILES.get(projectile);
        return p.maxduration;
    }
    public static double getHitRadius(Entity projectile){
        if(!isProjectile(projectile)){return 0;}
        Projectile p = SAVED_PROJECTILES.get(projectile);
        return p.hitRadius;
    }
    public static Entity getCaster(Entity projectile){
        if(!isProjectile(projectile)){return null;}
        Projectile p = SAVED_PROJECTILES.get(projectile);
        return p.owner;
    }
    public static String getSkill(Entity projectile){
        if(!isProjectile(projectile)){return null;}
        Projectile p = SAVED_PROJECTILES.get(projectile);
        return p.skill;
    }
    public static UUID getID(Entity projectile){
        if(!isProjectile(projectile)){return null;}
        Projectile p = SAVED_PROJECTILES.get(projectile);
        return p.projectileEntity.getUniqueId();
    }

    public static boolean isHittedEntity(Entity projectile,Entity e){
        if(!isProjectile(projectile)){return false;}
        Projectile p = SAVED_PROJECTILES.get(projectile);
        return p.hittedEntity.contains(e);
    }

    public static void clearHittedEntity(Entity projectile){
        if(!isProjectile(projectile)){return;}
        Projectile p = SAVED_PROJECTILES.get(projectile);
        p.hittedEntity.clear();
    }

    public static void reset(Entity projectile){
        if(!isProjectile(projectile)){return;}
        Projectile p = SAVED_PROJECTILES.get(projectile);
        p.hittedEntity.clear();
        p.duration = p.maxduration;
    }

    public static void clear(){
        for(Entity e: SAVED_PROJECTILES.keySet()){
            e.remove();
        }
    }
    public static class ProjectileHandler implements Listener {
        public ProjectileHandler() {
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onDeath(EntityDeathEvent e) {
            if(isProjectile(e.getEntity())){
                Projectile p = SAVED_PROJECTILES.get(e);
                p.markForRemoval();
            }
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onUnload(EntitiesLoadEvent e){
            if(!SAVED_PROJECTILES.isEmpty()){
                for(Entity entity: e.getEntities()){
                    if(isProjectile(entity)){
                        entity.getLocation().getChunk().load();
                        Projectile p = SAVED_PROJECTILES.get(entity);
                        p.markForRemoval();
                    }
                }
            }
        }
    }
}
