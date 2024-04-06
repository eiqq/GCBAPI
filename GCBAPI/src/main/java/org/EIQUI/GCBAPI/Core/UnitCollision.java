package org.EIQUI.GCBAPI.Core;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static java.lang.Math.ceil;

public final class UnitCollision {
    private static final double FIND_EMPTYSPACE_MAXCAP = 120.0d;
    private static final double FIND_EMPTYSPACE_MINPERIOD = 1.0/16.0;
    private static final double MIN_SIZE_UNIT = 2.0;
    private static final double MIN_SIZE_UNIT_LOC = 4.0;

    private static final double MIN_SIZE_BLOCK = 3.0;
    private static final double MIN_SIZE_BLOCK_LOC = 6.0;
    private static BoundingBox calcBoundingBox(Location l, double hr, double vrp, double vrn) {
        return new BoundingBox(-hr, -vrn, -hr, hr, vrp, hr).shift(l);
    }
    public static Collection<Entity> WithUnit(Entity e, Vector v, double hr, double vrp, double vrn, Predicate<Entity> pred){
        Set<Entity> col = new HashSet<Entity>();
        Location l = e.getLocation();
        double size = ceil(v.length())*MIN_SIZE_UNIT;
        if(size < MIN_SIZE_UNIT) {
            size = MIN_SIZE_UNIT;
        }
        Vector move = v.clone().multiply(1/(size-1));
        BoundingBox box = calcBoundingBox(l, hr, vrp, vrn);
        for(int i = 0;i<size;i++){
            col.addAll(HitboxAPI.getEntity_inCube(e.getWorld(),box,true));
            box.shift(move);
        }
        col.removeIf(ent -> !checktarget(e,ent));
        col.removeIf(pred);
        return new ArrayList<>(col);
    }

    public static Collection<Entity> WithUnit(Entity e, Vector v, double hr, double vrp, double vrn){
        return WithUnit(e,v,hr,vrp,vrn,(ent -> !checktarget(e,ent)));
    }

    //---------------------------------------------------------------------------------------------------------

    public static Location WithUnit_Location(Entity e, Vector v, double hr, double vrp, double vrn, Predicate<Entity> pred){
        Location l = e.getLocation();
        double size = ceil(v.length())*MIN_SIZE_UNIT_LOC;
        if(size < MIN_SIZE_UNIT_LOC) {
            size = MIN_SIZE_UNIT_LOC;
        }
        Vector move = v.clone().multiply(1/(size-1));
        BoundingBox box = calcBoundingBox(l, hr, vrp, vrn);
        for(int i = 0;i<size;i++){
            for(Entity loop : HitboxAPI.getEntity_inCube(e.getWorld(),box,true)){
                if(pred.test(loop)){
                    if (i <= 0){
                        return l;
                    }
                    BoundingBox tempbox = loop.getBoundingBox();
                    Vector offmove = move.clone().normalize().multiply(-1.0d/16);
                    while(box.overlaps(tempbox)){
                        box.shift(offmove);
                        l.add(offmove);
                    }
                    return l;
                }
            }
            box.shift(move);
            l.add(move);
        }
        return e.getLocation().add(v);
    }

    public static Location WithUnit_Location(Entity e, Vector v, double hr, double vrp, double vrn){
        Predicate<Entity> pred = ent -> checktarget(e,ent);
        return WithUnit_Location(e,v,hr,vrp,vrn,pred);
    }

    //---------------------------------------------------------------------------------------------------------

    public static Collection<Block> WithBlock(Entity e, Vector v, double hr, double vrp, double vrn) {
        ArrayList<Block> ret = new ArrayList<>();
        Location l = e.getLocation();
        double size = ceil(v.length())*MIN_SIZE_BLOCK;
        if(size < MIN_SIZE_BLOCK) {
            size = MIN_SIZE_BLOCK;
        }
        Vector move = v.clone().multiply(1/(size-1));
        BoundingBox box = calcBoundingBox(l, hr, vrp, vrn);
        World w = e.getWorld();
        for(int i = 0;i<size;i++){
            Collection<Block> tempBlocks = HitboxAPI.getBlocksIn(w,box);
            for(Block b : tempBlocks){
                if(b.isPassable()){
                    continue;
                }
                if(HitboxAPI.isCollide_AABBWithBlock(box,b)){
                    if (e.isDead() || !e.isValid()) {
                        return ret;
                    }else{
                        ret.add(b);
                    }
                }
            }
            box.shift(move);
            l.add(move);
        }
        return ret;
    }
    public static Location WithBlock_Location(Entity e, Vector v, double hr, double vrp, double vrn){
        Location l = e.getLocation();
        double size = ceil(v.length())*MIN_SIZE_BLOCK_LOC;
        if(size < MIN_SIZE_BLOCK_LOC) {
            size = MIN_SIZE_BLOCK_LOC;
        }
        Vector move = v.clone().multiply(1/(size-1));
        BoundingBox box = calcBoundingBox(l, hr, vrp, vrn);
        World w = e.getWorld();
        for(int i = 0;i<size;i++){
            for(Block b : HitboxAPI.getBlocksIn(w,box)){
                if(b.isPassable()){
                    continue;
                }
                if(HitboxAPI.isCollide_AABBWithBlock(box,b)){
                    Location temp = findEmptySpace(l,box,move.multiply(-1.0),FIND_EMPTYSPACE_MINPERIOD,move.length());
                    if(temp == null){
                        return l;
                    }
                    return temp;
                }
            }
            box.shift(move);
            l.add(move);
        }
        return l;
    }
    public static Location findEmptySpace(Location l,BoundingBox box,Vector direction,double period,double maxdist){
        if(direction.length() <= Vector.getEpsilon()){
            return null;
        }
        if(maxdist > FIND_EMPTYSPACE_MAXCAP){
            maxdist = FIND_EMPTYSPACE_MAXCAP;
        }else if(maxdist <= 0){
            return  l.clone();
        }
        if(period < FIND_EMPTYSPACE_MINPERIOD){
            period = FIND_EMPTYSPACE_MINPERIOD;
        }
        box = box.clone();
        direction = direction.clone();
        Location currentLocation = l.clone();
        World world = l.getWorld();
        Vector stepVector = direction.normalize().multiply(period);
        double step = period;
        double totalDistanceMoved = 0.0;
        boolean exit = false;
        while (totalDistanceMoved <= maxdist) {
            Collection<Block> tempBlocks = HitboxAPI.getBlocksIn(world,box);
            tempBlocks.removeIf(block -> block.isPassable());
            BoundingBox tempbox = box.clone();
            tempBlocks.removeIf(block -> !HitboxAPI.isCollide_AABBWithBlock(tempbox,block));
            if(tempBlocks.size() == 0){
                return currentLocation;
            }
            if(exit){
                break;
            }
            if (step > maxdist - totalDistanceMoved) {
                step = maxdist - totalDistanceMoved;
                stepVector = stepVector.normalize().multiply(step);
                exit = true;
            }
            currentLocation.add(stepVector);
            box.shift(stepVector);
            totalDistanceMoved += step;
        }
        return null;
    }
    public static Location findEmptySpace(Location l,double hr, double vrp, double vrn,Vector direction,double period,double maxdist){
        BoundingBox box = new BoundingBox(-hr,-vrn,-hr,hr,vrp,hr).shift(l);
        return findEmptySpace(l,box,direction,period,maxdist);
    }
    //---------------------------------------------------------------------------------------------------------
    private static boolean checktarget(Entity e,Entity target){
        if(e.equals(target)){
            return false;
        }
        EntityType type = target.getType();
        if (!type.isAlive()){
            if(type.equals(EntityType.INTERACTION)){
                return (target.getName().toLowerCase().contains("collision") || target.getName().equalsIgnoreCase("all"));
            }
            return false;
        }
        else if(type.equals(EntityType.PLAYER)){
            if(((Player)target).getGameMode().equals(GameMode.SPECTATOR)){
                return false;
            }
        }
        else if(type.equals(EntityType.ARMOR_STAND)){
            if(((ArmorStand)target).isMarker()){
                return false;
            }
        }

        return true;
    }

}
