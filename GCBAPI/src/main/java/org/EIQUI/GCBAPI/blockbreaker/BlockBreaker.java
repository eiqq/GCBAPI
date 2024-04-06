package org.EIQUI.GCBAPI.blockbreaker;

import org.EIQUI.GCBAPI.Core.HitboxAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class BlockBreaker {
    private static ConcurrentHashMap<Location,BlockSaver> saved =new ConcurrentHashMap<>();

    public static void Break(Location l, double radius){
        double rr = (radius * radius) - Vector.getEpsilon();
        Collection<Block> blocks = HitboxAPI.getBlocksIn(l, radius, radius, radius);
        blocks.removeIf(block ->
        {
            Material type = block.getState().getType();
            return type.isAir() ||
                    type.equals(Material.COMMAND_BLOCK) ||
                    type.equals(Material.REPEATING_COMMAND_BLOCK) ||
                    type.equals(Material.CHAIN_COMMAND_BLOCK) ||
                    type.equals(Material.BEDROCK) ||
                    type.equals(Material.BARRIER) ||
                    type.equals(Material.STRUCTURE_VOID) ||
                    block.getLocation().distanceSquared(l) > rr;
        });
        for (Block b : blocks) {
            saved.put(b.getLocation(), new BlockSaver(b.getType(), b.getBlockData()));
            if (!b.getChunk().isLoaded()) {
                b.getChunk().load();
            }
            b.setType(Material.AIR,false);
        }
    }

    public static void Break(Location l, double radius,double per){
        double rr = (radius * radius) - Vector.getEpsilon();
        Collection<Block> blocks = HitboxAPI.getBlocksIn(l, radius, radius, radius);
        blocks.removeIf(block ->
        {
            Material type = block.getState().getType();
            return type.isAir() ||
                    type.equals(Material.COMMAND_BLOCK) ||
                    type.equals(Material.REPEATING_COMMAND_BLOCK) ||
                    type.equals(Material.CHAIN_COMMAND_BLOCK) ||
                    type.equals(Material.BEDROCK) ||
                    type.equals(Material.BARRIER) ||
                    type.equals(Material.STRUCTURE_VOID) ||
                    block.getLocation().distanceSquared(l) > rr;
        });
        for (Block b : blocks) {
            if(Math.random() < per){
                saved.put(b.getLocation(), new BlockSaver(b.getType(), b.getBlockData()));
                if (!b.getChunk().isLoaded()) {
                    b.getChunk().load();
                }
                b.setType(Material.AIR,false);
            }
        }
    }

    public static void repairAt(Location l){
        if(!saved.containsKey(l)){
            return;
        }
        BlockSaver saver = saved.get(l);
        l.getChunk().load();
        l.getBlock().setType(saver.material,false);
        l.getBlock().setBlockData(saver.blockData,false);
        saved.remove(l);
    }

    public static void repairAll(){
        for(Location l:saved.keySet()){
            BlockSaver saver = saved.get(l);
            l.getChunk().load();
            l.getBlock().setType(saver.material,false);
            l.getBlock().setBlockData(saver.blockData,false);
        }
        saved.clear();
    }
}
