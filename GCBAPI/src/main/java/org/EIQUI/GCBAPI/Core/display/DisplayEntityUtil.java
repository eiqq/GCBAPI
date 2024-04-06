package org.EIQUI.GCBAPI.Core.display;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class DisplayEntityUtil {
    private static final double RADIAN = 0.017453292519943295;
    private static final Set<Display> TRANSFORMATIONS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static void applyTransformation(Display e,TransSaver saver){
        if(saver.Interport <= 0 && e.getTicksLived() < 2){
            saver.apply(e);
        }
        else if(!TRANSFORMATIONS.contains(e)){
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (e.getTicksLived() >= 2) {
                        saver.apply(e);
                        TRANSFORMATIONS.remove(e);
                        cancel();
                    }
                }
            }.runTaskTimer(that, 0L, 1L);
        }else{
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!TRANSFORMATIONS.contains(e)) {
                        saver.apply(e);
                        cancel();
                    }
                }
            }.runTaskTimer(that, 0L, 1L);
        }
    }

    public static Display spawnItemDisplay(@Nullable Location l){
        if(l == null){return null;}
        Display d = (Display) l.getWorld().spawnEntity(l,EntityType.ITEM_DISPLAY);
        ((ItemDisplay)d).setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD);
        d.setDisplayWidth(0);
        d.setDisplayWidth(0);
        d.setViewRange(200);
        d.setInterpolationDelay(-1);
        d.setBrightness(new Display.Brightness(15,15));
        return d;
    }

    public static Display spawnBlockDisplay(@Nullable Location l){
        if(l == null){return null;}
        return (Display) l.getWorld().spawnEntity(l,EntityType.BLOCK_DISPLAY);
    }
    public static Display spawnTextDisplay(@Nullable Location l){
        if(l == null){return null;}
        return (Display) l.getWorld().spawnEntity(l,EntityType.TEXT_DISPLAY);
    }

    public static void setTranslation(@Nullable Display e, double x, double y , double z, int time){
        if(e == null || !e.isValid()){
            return;
        }
        Vector3f vec = new Vector3f((float) x, (float) y, (float) z);
        applyTransformation(e,new TransSaver(vec,null,null,null,time));
    }

    public static void setSize(@Nullable Display e,double x, double y ,double z,int time){
        if(e == null || !e.isValid()){
            return;
        }
        Vector3f vec = new Vector3f((float) x, (float) y, (float) z);
        applyTransformation(e,new TransSaver(null,null,vec,null,time));
    }

    public static void setTranformation(@Nullable Display e,double sizex,double sizey,double sizez,
                                        double tlx,double tly,double tlz,
                                        double spinx,double spiny,double spinz,double degree,
                                        int time){
        if(e == null || !e.isValid()){
            return;
        }
        double angle = (degree/4.0d);
        Quaternionf rotation = new Quaternionf(spinx,spiny,spinz,Math.sin(angle*RADIAN)/Math.cos(angle*RADIAN));
        rotation = rotation.normalize();

        Vector3f scale = new Vector3f((float) sizex, (float) sizey, (float) sizez);

        Vector3f translation = new Vector3f((float) tlx, (float) tly, (float) tlz);

        if(spinx == 0 && spiny == 0 && spinz ==0){
            applyTransformation(e,new TransSaver(translation,null,scale,null,time));
        }else{
            applyTransformation(e,new TransSaver(translation,rotation,scale,rotation,time));
        }
    }

    public static void setSpinX(@Nullable Display e,double degree,int time){
        if(e == null || !e.isValid()){
            return;
        }
        double angle = (degree/4.0d);
        Quaternionf vec = new Quaternionf(1,0,0,Math.sin(angle*RADIAN)/Math.cos(angle*RADIAN)).normalize();
        applyTransformation(e,new TransSaver(null,vec,null,vec,time));
    }
    public static void setSpinY(@Nullable Display e,double degree,int time){
        if(e == null || !e.isValid()){
            return;
        }
        double angle = (degree/4.0d);
        Quaternionf vec = new Quaternionf(0,1,0,Math.sin(angle*RADIAN)/Math.cos(angle*RADIAN)).normalize();
        applyTransformation(e,new TransSaver(null,vec,null,vec,time));
    }
    public static void setSpinZ(@Nullable Display e,double degree,int time){
        if(e == null || !e.isValid()){
            return;
        }
        double angle = (degree/4.0d);
        Quaternionf vec = new Quaternionf(0,0,1,Math.sin(angle*RADIAN)/Math.cos(angle*RADIAN)).normalize();
        applyTransformation(e,new TransSaver(null,vec,null,vec,time));
    }

    public static void setSpin(@Nullable Display e,double yaw,double pitch,double roll,int time){
        if(e == null || !e.isValid()){
            return;
        }
        yaw = yaw*RADIAN*0.5;
        pitch = pitch*RADIAN*0.5;
        roll = roll*RADIAN*0.5;
        Quaternionf vec = new Quaternionf().rotationYXZ((float) yaw, (float) pitch, (float) roll).normalize();
        applyTransformation(e,new TransSaver(null,vec,null,vec,time));
    }

    public static void setInterpolationDelay(@Nullable Display e,int time){
        if(e == null || !e.isValid()){
            return;
        }
        e.setInterpolationDelay(time);
    }
    public static void setInterpolationDuration(@Nullable Display e,int time){
        if(e == null || !e.isValid()){
            return;
        }
        e.setInterpolationDuration(time);
    }
    public static void setTeleportDuration(@Nullable Display e,int time){
        if(e == null || !e.isValid()){
            return;
        }
        e.setTeleportDuration(time);
    }

    public static void setBillboard(@Nullable Display e,String x){
        if(e == null || !e.isValid()){
            return;
        }
        if(x.equalsIgnoreCase("CENTER")){
            e.setBillboard(Display.Billboard.CENTER);
        }else if(x.equalsIgnoreCase("HORIZONTAL")){
            e.setBillboard(Display.Billboard.HORIZONTAL);
        }else if(x.equalsIgnoreCase("VERTICAL")){
            e.setBillboard(Display.Billboard.VERTICAL);
        }else{
            e.setBillboard(Display.Billboard.FIXED);
        }
    }


    public static void setGlowColor(@Nullable Display e,int r,int g,int b){
        if(e == null || !e.isValid()){
            return;
        }
        e.setGlowColorOverride(Color.fromRGB(r,g,b));
    }

    public static void setCullHeight(@Nullable Display e,double n){
        if(e == null || !e.isValid()){
            return;
        }
        e.setDisplayHeight((float) n);
    }

    public static void setCullWidth(@Nullable Display e,double n){
        if(e == null || !e.isValid()){
            return;
        }
        e.setDisplayWidth((float) n);
    }
    public static void setViewRange(@Nullable Display e,double n){
        if(e == null || !e.isValid()){
            return;
        }
        e.setViewRange((float) n);
    }

    public static void setBright(@Nullable Display e,int l,int sky){
        if(e == null || !e.isValid()){
            return;
        }
        e.setBrightness(new Display.Brightness(l,sky));
    }


    public static void setItem(@Nullable Display e, ItemStack item){
        if(e == null || !e.isValid()){
            return;
        }
        if(e.getType().equals(EntityType.ITEM_DISPLAY)){
            ((ItemDisplay)e).setItemStack(item);
        }
    }
    public static void setBlock(@Nullable Display e, Block b){
        if(e == null || !e.isValid()){
            return;
        }
        if(e.getType().equals(EntityType.BLOCK_DISPLAY)){
            ((BlockDisplay)e).setBlock(b.getBlockData());
        }
    }


    public static void setText(@Nullable Display e,String text){
        if(e == null || !e.isValid()){
            return;
        }
        if(e.getType().equals(EntityType.TEXT_DISPLAY)){
            ((TextDisplay)e).setText(text);
        }
    }

    public static void setTextOpacity(@Nullable Display e,byte i){
        if(e == null || !e.isValid()){
            return;
        }
        if(e.getType().equals(EntityType.TEXT_DISPLAY)){
            ((TextDisplay)e).setTextOpacity(i);
        }
    }

    public static void setBackgroundColor(@Nullable Display e,int r,int g, int b,int a){
        if(e == null || !e.isValid()){
            return;
        }
        if(e.getType().equals(EntityType.TEXT_DISPLAY)){
            ((TextDisplay)e).setBackgroundColor(Color.fromARGB(a,r,g,b));
        }
    }
    public static void setAlign(@Nullable Display e,String align){
        if(e == null || !e.isValid()){
            return;
        }
        if(e.getType().equals(EntityType.TEXT_DISPLAY)){
            if(align.equalsIgnoreCase("CENTER")){
                ((TextDisplay)e).setAlignment(TextDisplay.TextAlignment.CENTER);
            }
            else if(align.equalsIgnoreCase("RIGHT")){
                ((TextDisplay)e).setAlignment(TextDisplay.TextAlignment.RIGHT);
            }
            else if(align.equalsIgnoreCase("LEFT")){
                ((TextDisplay)e).setAlignment(TextDisplay.TextAlignment.LEFT);
            }
        }
    }

    public static class DisplayHandler implements Listener {
        public DisplayHandler() {
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onDeath(EntityDeathEvent e) {
            if(e.getEntity() instanceof Display){
                TRANSFORMATIONS.remove(e.getEntity());
            }
        }
    }
}
