package org.EIQUI.GCBAPI.Core.glow;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import static org.EIQUI.GCBAPI.main.that;

public class GlowAPI {

    public static final GlowingEntities GLOWING_ENTITY = new GlowingEntities(that);
    public static final GlowingBlocks GLOWING_BLOCK = new GlowingBlocks(that);

    public static void setGlow(Entity e, Player p, ChatColor color){
        if(!e.isValid() || e.isDead()){
            return;
        }
        try{
            GLOWING_ENTITY.setGlowing(e,p,color);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void unsetGlow(Entity e, Player p){
        try{
            GLOWING_ENTITY.unsetGlowing(e,p);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void unsetGlow(Entity e) throws ReflectiveOperationException {
        for(Player p : GLOWING_ENTITY.getAllGlowingSet(e)){
            GLOWING_ENTITY.unsetGlowing(e,p);
        }
    }
    public static boolean isGlowTo(Entity e,Player p){
        return GLOWING_ENTITY.isGlowingTo(e,p);
    }

    public static void clearEntity(){
        GLOWING_ENTITY.disable();
        GLOWING_ENTITY.enable();
    }


    public static void setGlow(Block b, Player p, ChatColor color) throws ReflectiveOperationException {
        GLOWING_BLOCK.setGlowing(b,p,color);
    }
    public static void unsetGlow(Block b, Player p) throws ReflectiveOperationException {
        GLOWING_BLOCK.unsetGlowing(b,p);
    }

    public static void unsetGlow(Block b) throws ReflectiveOperationException {
        for(Player p : GLOWING_BLOCK.getAllGlowingSet(b)){
            GLOWING_BLOCK.unsetGlowing(b,p);
        }
    }

    public static boolean isGlowTo(Block b,Player p){
        return GLOWING_BLOCK.getAllGlowingSet(b).contains(p);
    }

    public static boolean isGlowTo(Location l, Player p){
        return GLOWING_BLOCK.getAllGlowingSet(l).contains(p);
    }

    public static void clearBlock(){
        GLOWING_BLOCK.disable();
        GLOWING_BLOCK.enable();
    }

    public static ChatColor getColor(String s){
        ChatColor color = ChatColor.getByChar(s.toLowerCase());
        if(color.isColor()){
            return color;
        }
        return ChatColor.WHITE;
    }
}
