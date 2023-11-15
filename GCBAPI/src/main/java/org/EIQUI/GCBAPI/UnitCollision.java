package org.EIQUI.GCBAPI;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;

import static java.lang.Math.ceil;
import static org.EIQUI.GCBAPI.main.that;

public final class UnitCollision {
    public static Collection<Entity> WithUnit(Entity e, Vector v, double hr, double vrp, double vrn, Predicate<Entity> pred){
        Set<Entity> col = new HashSet<Entity>();

        Location l = e.getLocation();
        double s = v.length();
        double size = ceil(s)*2;
        if(size < 2) {
            size = 2;
        }
        Vector move = v.clone().multiply(1/(size-1));
        BoundingBox box = new BoundingBox(-hr,-vrn,-hr, hr,vrp,hr).shift(l);
        for(int i = 0;i<size;i++){
            col.addAll(HitboxAPI.getEntity_inCube(e.getWorld(),box,true));
            box.shift(move);
        }
        col.removeIf(pred);
        return col.stream().toList();
    }

    public static Collection<Entity> WithUnit(Entity e, Vector v, double hr, double vrp, double vrn){
        Predicate<Entity> pred = ent -> !checktarget(e,ent);
        return WithUnit(e,v,hr,vrp,vrn,pred);
    }

    //---------------------------------------------------------------------------------------------------------

    public static Location WithUnit_Location(Entity e, Vector v, double hr, double vrp, double vrn, Predicate<Entity> pred){
        Location l = e.getLocation();
        double size = ceil(v.length())*4;
        if(size < 4) {
            size = 4;
        }
        Vector move = v.clone().multiply(1/(size-1));
        BoundingBox box = new BoundingBox(-hr,-vrn,-hr, hr,vrp,hr).shift(l);
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
        Set<Block> col = new HashSet<Block>();
        Location l = e.getLocation();
        double size = ceil(v.length())*3;
        if(size < 3) {
            size = 3;
        }
        Vector move = v.clone().multiply(1/(size-1));
        BoundingBox box = new BoundingBox(-hr,-vrn,-hr,hr,vrp,hr).shift(l);
        World w = e.getWorld();

        for(int i = 0;i<size;i++){
            int dx = (int)ceil(box.getWidthX());
            int dy = (int)ceil(box.getHeight());
            int dz = (int)ceil(box.getWidthZ());
            Vector tv = box.getMin();
            int minX = tv.getBlockX();
            int minY = tv.getBlockY();
            int minZ = tv.getBlockZ();
            for(int x = 0; x <= dx; x++){
                for(int y = 0 ; y <= dy ; y++){
                    for(int z = 0; z <= dz ; z++){
                        Block b = w.getBlockAt(minX+x,minY+y,minZ+z);
                        if(b.isPassable()){
                            continue;
                        }
                        if(HitboxAPI.isHitboxCollide_AABBWithBlockAABB(box,b)){
                            if (e.isDead()) {
                                return col.stream().toList();
                            }else{
                                col.add(b);
                            }
                        }
                    }
                }
            }
            box.shift(move);
            l.add(move);
        }

        return col.stream().toList();
    }

    public static Location WithBlock_Location(Entity e, Vector v, double hr, double vrp, double vrn){
        Location l = e.getLocation();
        double size = ceil(v.length())*6;
        if(size < 6) {
            size = 6;
        }
        Vector move = v.clone().multiply(1/(size-1));
        BoundingBox box = new BoundingBox(-hr,-vrn,-hr,hr,vrp,hr).shift(l);
        World w = e.getWorld();

        for(int i = 0;i<size;i++){
            int dx = (int)ceil(box.getWidthX());
            int dy = (int)ceil(box.getHeight());
            int dz = (int)ceil(box.getWidthZ());
            Vector tv = box.getMin();
            int minX = tv.getBlockX();
            int minY = tv.getBlockY();
            int minZ = tv.getBlockZ();
            for(int x = 0; x <= dx; x++){
                for(int y = 0 ; y <= dy ; y++){
                    for(int z = 0; z <= dz ; z++){
                        Block b = w.getBlockAt(minX+x,minY+y,minZ+z);
                        if(b.isPassable()){
                            continue;
                        }
                        if(HitboxAPI.isHitboxCollide_AABBWithBlockAABB(box,b)){
                            Vector tmove = move.clone().multiply(-1.0d/16);
                            return check(box,l,tmove);
                        }
                    }
                }
            }
            box.shift(move);
            l.add(move);
        }
        return l;

    }
    protected static Location check(BoundingBox box,Location l,Vector v){
        Set<Block> col = new HashSet<Block>();
        World w = l.getWorld();
        int dx = (int)ceil(box.getWidthX());
        int dy = (int)ceil(box.getHeight());
        int dz = (int)ceil(box.getWidthZ());
        Vector tv = box.getMin();
        int minX = tv.getBlockX();
        int minY = tv.getBlockY();
        int minZ = tv.getBlockZ();
        for(int x = 0; x <= dx; x++){
            for(int y = 0 ; y <= dy ; y++){
                for(int z = 0; z <= dz ; z++){
                    Block b = w.getBlockAt(minX+x,minY+y,minZ+z);
                    if(!b.isPassable()){
                        col.add(b);
                    }
                }
            }
        }
        int cols = col.size();
        for(int size = 0 ; size < cols;){
            size = 0;
            for(Block tb: col){
                if (HitboxAPI.isHitboxCollide_AABBWithBlockAABB(box,tb) ){
                    box.shift(v);
                    l.add(v);
                    break;
                }else{
                    size++;
                }
            }
        }
        return l;
    }

    public static Location findEmptySpace(Location l,double hr, double vrp, double vrn,Vector dir,int max){
        World w = l.getWorld();
        Set<Block> save = new HashSet<>();
        boolean b = true;
        Location r = l.clone();
        BoundingBox box = new BoundingBox(-hr,-vrn,-hr,hr,vrp,hr).shift(l);
        double length = dir.length();
        double i = 0;
        for(i = 0; b && i<max ;){
            b = false;
            save.clear();
            int dx = (int)ceil(box.getWidthX());
            int dy = (int)ceil(box.getHeight());
            int dz = (int)ceil(box.getWidthZ());
            Vector tv = box.getMin();
            int minX = tv.getBlockX();
            int minY = tv.getBlockY();
            int minZ = tv.getBlockZ();
            for(int x = 0; x <= dx; x++){
                for(int y = 0 ; y <= dy ; y++){
                    for(int z = 0; z <= dz ; z++){
                        Block bl = w.getBlockAt(minX+x,minY+y,minZ+z);
                        if(!bl.isPassable()){
                            save.add(bl);
                        }
                    }
                }
            }
            for(Block block: save){
                if(block.getBoundingBox().overlaps(box)){
                    b = true;
                    box.shift(dir);
                    r.add(dir);
                    i += length;
                    break;
                }
            }
        }
        if(i<=max){
            return r;
        }
        return null;
    }
    //---------------------------------------------------------------------------------------------------------
    public static boolean checktarget(Entity e,Entity target){
        if(e.equals(target)){
            return false;
        }
        EntityType type = target.getType();
        if (!type.isAlive()){
            if(type == EntityType.INTERACTION){
                return (target.getName().toLowerCase().contains("collision") || target.getName().equalsIgnoreCase("all"));
            }
            return false;
        }
        else if(type == EntityType.ARMOR_STAND){
            if(((ArmorStand)target).isMarker()){
                return false;
            }
        }
        return true;
    }

}
