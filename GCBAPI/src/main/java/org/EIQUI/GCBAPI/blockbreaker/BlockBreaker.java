package org.EIQUI.GCBAPI.blockbreaker;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;

import java.util.HashMap;
import java.util.logging.Level;
import static org.EIQUI.GCBAPI.main.that;

public class BlockBreaker {

    private static HashMap<Location,BlockSaver> saved =new HashMap<>();

    public static void Break(Location l, double radius){
        int minX = (int) (l.getBlockX() -radius);
        int minY = (int) (l.getBlockY() -radius);
        int minZ = (int) (l.getBlockZ() -radius);
        World w = l.getWorld();
        double rr = Math.pow(radius,2) + 0.000001;
        for(int x = 0; x <= radius*2; x++){
            for(int y = 0 ; y <= radius*2 ; y++){
                for(int z = 0; z <= radius*2 ; z++){
                    Block b = w.getBlockAt(minX+x,minY+y,minZ+z);
                    if (!(b.getBlockData().getMaterial().equals(Material.BEDROCK)
                            || b.getBlockData().getMaterial().equals(Material.AIR)
                            || b.getState() instanceof TileState)){
                        Location tl = b.getLocation();
                        if(tl.distanceSquared(l) <= rr){
                            saved.put(tl,new BlockSaver(b.getType(),b.getBlockData()));
                            if(!w.getChunkAt(b).isForceLoaded()){
                                w.getChunkAt(b).setForceLoaded(true);
                                b.setType(Material.AIR,false);
                                w.getChunkAt(b).setForceLoaded(false);
                            }else{
                                b.setType(Material.AIR,false);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void repairAt(Location l){
        l.setX(l.getBlockX());
        l.setX(l.getBlockY());
        l.setX(l.getBlockZ());
        if(!saved.containsKey(l)){
            return;
        }
        BlockSaver saver = saved.get(l);
        Material mat = saver.material;
        BlockData data = saver.blockData;
        if(!l.getWorld().getChunkAt(l).isForceLoaded()){
            l.getWorld().getChunkAt(l).setForceLoaded(true);
            l.getWorld().setType(l,mat);
            l.getWorld().setBlockData(l,data);
            l.getWorld().getChunkAt(l).setForceLoaded(false);
        }else{
            l.getWorld().setType(l,mat);
            l.getWorld().setBlockData(l,data);
        }
        saved.remove(l);
    }

    public static void repairAll(){
        for(Location l:saved.keySet()){
            BlockSaver saver = saved.get(l);
            Material mat = saver.material;
            BlockData data = saver.blockData;
            if(!l.getWorld().getChunkAt(l).isForceLoaded()){
                l.getWorld().getChunkAt(l).setForceLoaded(true);
                l.getWorld().setType(l,mat);
                l.getWorld().setBlockData(l,data);
                l.getWorld().getChunkAt(l).setForceLoaded(false);
            }else{
                l.getWorld().setType(l,mat);
                l.getWorld().setBlockData(l,data);
            }
        }
        saved.clear();
    }
}
