package org.EIQUI.GCBAPI.particle;
import com.github.fierioziy.particlenativeapi.api.ParticleNativeAPI;
import com.github.fierioziy.particlenativeapi.api.particle.ParticleList_1_13;
import com.github.fierioziy.particlenativeapi.api.particle.type.ParticleType;
import org.EIQUI.GCBAPI.fabric.Fabric;
import org.EIQUI.GCBAPI.fabric.FabricC2S;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Particle.DustOptions;

import static ch.njol.util.VectorMath.getYaw;
import static org.EIQUI.GCBAPI.Util.getVector;
import static org.EIQUI.GCBAPI.main.that;
import static org.bukkit.util.NumberConversions.round;
import org.bukkit.Particle.DustTransition;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class ParticleAPI{
    public static ParticleList_1_13 particle_1_13;

    public static final double NOMODREDUCED = 0.5;

//--------------------------------------------------------------------------------------------------------
    public static void Particle(String particle,final Location l
            ,double x,double y,double z,int amount,double speed,boolean force){
        l.getWorld().spawnParticle(Particle.valueOf(particle),
                l,amount,x,y,z,speed,null,force);
    }
    public static void ParticleDot(String particle,final Location l,boolean force){
        l.getWorld().spawnParticle(Particle.valueOf(particle),
                l,0,0,0,0,0,null,force);
    }
    public static void RedstoneParticleDot(final Location l,int r,int g,int b,float size,boolean force){
        l.getWorld().spawnParticle(Particle.valueOf("REDSTONE"),
                l,0,0,0,0,0,new DustOptions(Color.fromRGB(r,g,b), size),force);
    }
    public static void RedstoneParticle(final Location l,double x,double y,double z, int amount
            ,int r,int g,int b,float size,boolean force){
        l.getWorld().spawnParticle(Particle.valueOf("REDSTONE"),l,amount,x,y,z,0
                ,new DustOptions(Color.fromRGB(r,g,b),size),force);
    }

    public static void RedstoneParticle_ToPlayers(Player p,final Location l,double x,double y,double z, int amount
            ,int r,int g,int b,float size){
        p.spawnParticle(Particle.valueOf("REDSTONE"),l,amount,x,y,z,0,
                new DustOptions(Color.fromRGB(r,g,b), size));
    }
//--------------------------------------------------------------------------------------------------------------------------------
    public static void RSline(Player p,Location start, Location end,
                                            int r, int g, int b,int r2, int g2, int b2,
                                            float size, float density, boolean force){
        Fabric.sendPacketToFabric(p,"gcb:gcb","RSLine:" +
                start.getX()+","+start.getY()+","+start.getZ()+":"
                +end.getX()+","+end.getY()+","+end.getZ()+":"
                +r+","+g+","+b+":"
                +r2+","+g2+","+b2+":"
                +size+":"+density+":"+force);
    }
    public static void RedstoneParticleLine(Location start, Location end,
                                            int r, int g, int b,
                                            float size, float density, boolean force){
        RedstoneParticleLine(start,end,r,g,b,r,g,b,size,density,force);
    }
    public static void RedstoneParticleLine(Location start, Location end,
                                            int r, int g, int b, int r2, int g2, int b2,
                                            float size, float density, boolean force){
        ArrayList<Player> col = new ArrayList<Player>(Bukkit.getOnlinePlayers());
        if (col.size() == 0 || start.getWorld() != end.getWorld()){
            return;
        }
        Location finalStart = start;
        col.removeIf(ent -> (ent.getWorld() != finalStart.getWorld()));
        for(Player p: col){
            if (FabricC2S.hasMod(p)) {
                RSline(p, start, end, r, g, b, r2, g2, b2, size, density, force);
            }
        }
        col.removeIf(ent -> (FabricC2S.hasMod(ent)));
        if (col.size() == 0){
            return;
        }
        density /= NOMODREDUCED;
        start = start.clone();
        end = end.clone();
        Vector v = (end.clone().subtract(start).toVector()).normalize().multiply(density);
        double distance = end.distance(start);
        int loop = round(distance / density);
        ParticleType part = particle_1_13.DUST_COLOR_TRANSITION.
                color(Color.fromRGB(r, g, b), Color.fromRGB(r2, g2, b2), size);
        for (short i = 0; i < loop; i++) {
            part.packet(force, start).sendTo(col);
            start.add(v);
        }
    }

//--------------------------------------------------------------------------------------------------------------------------------
    public static void RedstoneParticleLine_ToPlayers(Player p, Location start, Location end,
                                                      int r, int g, int b,
                                                      float size, float density,boolean force){
        RedstoneParticleLine_ToPlayers(p,start,end,r,g,b,r,g,b,size,density,force);
    }
    public static void RedstoneParticleLine_ToPlayers(Player p, Location start, Location end,
                                                      int r, int g, int b, int r2, int g2, int b2,
                                                      float size, float density, boolean force) {
        if (FabricC2S.hasMod(p)) {
            RSline(p, start, end, r, g, b, r2, g2, b2, size, density, force);
            return;
        }
        density /= NOMODREDUCED;
        start = start.clone();
        end = end.clone();
        Vector v = (end.clone().subtract(start).toVector()).normalize().multiply(density);
        double distance = end.distance(start);
        int loop = round(distance / density);
        ParticleType part = particle_1_13.DUST_COLOR_TRANSITION.
                color(Color.fromRGB(r, g, b), Color.fromRGB(r2, g2, b2), size);
        for (short i = 0; i < loop; i++) {
            part.packet(force, start).sendTo(p);
            start.add(v);
        }
    }
//--------------------------------------------------------------------------------------------------------------------------------
    public static void RedstoneParticleLine_ToPlayers_Exclude(Player p, Location start, Location end,
                                                              int r, int g, int b,
                                                              float size, float density, boolean force){
        RedstoneParticleLine_ToPlayers_Exclude(p,start,end,r,g,b,r,g,b,size,density,force);
    }
    public static void RedstoneParticleLine_ToPlayers_Exclude(Player p, Location start, Location end,
                                                              int r, int g, int b, int r2, int g2, int b2,
                                                              float size, float density, boolean force){
        ArrayList<Player> col = new ArrayList<Player>(Bukkit.getOnlinePlayers());
        if (col.size() == 0 || start.getWorld() != end.getWorld()){
            return;
        }
        Location finalStart = start;
        col.removeIf(ent -> (ent.getWorld() != finalStart.getWorld()));
        col.remove(p);
        for(Player player: col){
            if (FabricC2S.hasMod(player)) {
                RSline(player, start, end, r, g, b, r2, g2, b2, size, density, force);
            }
        }
        col.removeIf(ent -> (FabricC2S.hasMod(ent)));
        if (col.size() == 0){
            return;
        }
        density /= NOMODREDUCED;
        start = start.clone();
        end = end.clone();
        Vector v = (end.clone().subtract(start).toVector()).normalize().multiply(density);
        double distance = end.distance(start);
        int loop = round(distance / density);
        ParticleType part = particle_1_13.DUST_COLOR_TRANSITION.
                color(Color.fromRGB(r, g, b), Color.fromRGB(r2, g2, b2), size);
        for (short i = 0; i < loop; i++) {
            part.packet(force, start).sendTo(col);
            start.add(v);
        }
    }

    public static void RedstoneParticleLine_ToPlayers_Exclude(Player p, Location start, Location end,
                                                              int r, int g, int b,
                                                              float size, float density, boolean force, double vdis){
        RedstoneParticleLine_ToPlayers_Exclude(p,start,end,r,g,b,r,g,b,size,density,force);
    }
    public static void RedstoneParticleLine_ToPlayers_Exclude(Player p, Location start, Location end,
                                                              int r, int g, int b, int r2, int g2, int b2,
                                                              float size, float density, boolean force, double vdis){
        RedstoneParticleLine_ToPlayers_Exclude(p,start,end,r,g,b,r2,g2,b2,size,density,force);
    }

//--------------------------------------------------------------------------------------------------------------------------------
    public static void RSCircle(Player p, Location l,double radius,int points
            , int r, int g, int b,int r2, int g2, int b2,
                                float size,boolean force){
        Fabric.sendPacketToFabric(p,"gcb:gcb","RSCircle:" +
                l.getX()+","+l.getY()+","+l.getZ()+":"
                +radius+":"+points+":"
                +r+","+g+","+b+":"
                +r2+","+g2+","+b2+":"
                +size+":"+force);
    }
    public static void RedstoneParticleCircle(final Location c,double radius,int points,
                                              int r, int g, int b, float size,boolean force){
        RedstoneParticleCircle(c,radius,points,r,g,b,r,g,b,size,force);
    }
    public static void RedstoneParticleCircle(final Location c,double radius,int points,
                                              int r, int g, int b,int r2, int g2, int b2,
                                              float size,boolean force){
        ArrayList<Player> col = new ArrayList<Player>(Bukkit.getOnlinePlayers());
        if (col.size() <= 0){
            return;
        }
        col.removeIf(ent -> (ent.getWorld() != c.getWorld()));
        for(Player p: col){
            if (FabricC2S.hasMod(p)) {
                RSCircle(p, c, radius, points, r, g, b, r2, g2, b2, size, force);
            }
        }
        col.removeIf(ent -> (FabricC2S.hasMod(ent)));
        if (col.size() <= 0){
            return;
        }
        points = round(points*NOMODREDUCED);
        Vector v = new Vector(radius,0,0);
        double rotate = Math.PI/(points*0.5);
        ParticleType part = particle_1_13.DUST_COLOR_TRANSITION.
                color(Color.fromRGB(r, g, b),Color.fromRGB(r2, g2, b2), size);
        for(short i=0; i<points ;i++){
            v.rotateAroundY(rotate);
            part.packet(force,c.clone().add(v)).sendTo(col);
        }
    }

    public static void RedstoneParticleCircle_ToPlayers(Player p, final Location c,double radius,int points,
                                                        int r, int g, int b, float size,boolean force){
        RedstoneParticleCircle_ToPlayers(p,c,radius,points,r,g,b,r,g,b,size,force);
    }
    public static void RedstoneParticleCircle_ToPlayers(Player p, final Location c,double radius,int points,
                                                        int r, int g, int b,int r2, int g2, int b2, float size,boolean force){
        if (FabricC2S.hasMod(p)) {
            RSCircle(p, c,radius,points, r, g, b, r2, g2, b2, size, force);
            return;
        }
        points = round(points*NOMODREDUCED);
        Vector v = new Vector(radius,0,0);
        double rotate = Math.PI/(points*0.5);
        ParticleType part = particle_1_13.DUST_COLOR_TRANSITION.
                color(Color.fromRGB(r, g, b),Color.fromRGB(r2, g2, b2), size);
        for(short i=0; i<points ;i++){
            v.rotateAroundY(rotate);
            part.packet(force,c.clone().add(v)).sendTo(p);
        }
    }

//--------------------------------------------------------------------------------------------------------------------------------
    public static void RSCycle(Player p,Location l, double radius, Vector v, Vector rot,
                               int points, double degree, double time, boolean clockwise,
                               int r, int g, int b, int r2, int g2, int b2, float size, boolean force){
        Fabric.sendPacketToFabric(p,"gcb:gcb","RSCycle:" +
                l.getX()+","+l.getY()+","+l.getZ()+":"
                +radius+":"
                +v.getX()+","+v.getY()+","+v.getZ()+":"
                +rot.getX()+","+rot.getY()+","+rot.getZ()+":"
                +points+":"+degree+":"+time+":"+clockwise+":"
                +r+","+g+","+b+":"
                +r2+","+g2+","+b2+":"
                +size+":"+force);
    }

    public static void RedstoneParticleCycle(final Location c, double radius, Vector v, Vector rot,
                                             int points, double degree, double time, boolean clockwise,
                                             int r, int g, int b, float size, boolean force){
        RedstoneParticleCycle(c,radius,v,rot,points,degree,time,clockwise,r,g,b,r,g,b,size,force);
    }
    public static void RedstoneParticleCycle(final Location c, double radius, Vector v, Vector rot,
                                             int points, double degree, double time, boolean clockwise,
                                             int r, int g, int b, int r2, int g2, int b2, float size, boolean force){
        ArrayList<Player> col = new ArrayList<Player>(Bukkit.getOnlinePlayers());
        if (col.size() <= 0){
            return;
        }
        col.removeIf(ent -> (ent.getWorld() != c.getWorld()));
        for(Player p: col){
            if (FabricC2S.hasMod(p)) {
                RSCycle(p, c, radius, v, rot, points, degree, time, clockwise
                        , r, g, b, r2, g2, b2, size, force);
            }
        }
        col.removeIf(ent -> (FabricC2S.hasMod(ent)));
        if (col.size() <= 0){
            return;
        }
        points = round(points*NOMODREDUCED);
        Vector vc = v.clone();
        Vector rotc = rot.clone();
        vc.normalize().multiply(radius);
        if(!clockwise){
            degree *= -1;
        }
        double angle = (degree/points)*(Math.PI/180.0f);
        int delay = round((double)points/time);
        ParticleType part = particle_1_13.DUST_COLOR_TRANSITION.color(Color.fromRGB(r, g, b),Color.fromRGB(r2, g2, b2), size);
        for(int i = 1,j=0;i<= points;i++){
            if(i % delay == 0){
                j++;
            }
            new BukkitRunnable() {
                public void run() {
                    vc.rotateAroundAxis(rotc,angle);
                    part.packet(force,c.clone().add(vc)).sendTo(col);
                }
            }.runTaskLater(that, j);
        }
    }

    public static void RedstoneParticleCycle_ToPlayers(Player p, final Location c, double radius, Vector v, Vector rot,
                                                       int points, double degree, double time, boolean clockwise,
                                                       int r, int g, int b, float size, boolean force){
        RedstoneParticleCycle_ToPlayers(p,c,radius,v,rot,points,degree,time,clockwise,r,g,b,r,g,b,size,force);
    }
    public static void RedstoneParticleCycle_ToPlayers(Player p, final Location c, double radius, Vector v, Vector rot,
                                                       int points, double degree, double time, boolean clockwise,
                                                       int r, int g, int b, int r2, int g2, int b2, float size, boolean force){
        if (FabricC2S.hasMod(p)) {
            RSCycle(p, c,radius,v,rot,points,degree,time,clockwise, r, g, b, r2, g2, b2, size, force);
            return;
        }
        points = round(points*NOMODREDUCED);
        Vector vc = v.clone();
        Vector rotc = rot.clone();
        vc.normalize().multiply(radius);
        if(!clockwise){
            degree *= -1;
        }
        double angle = (degree/points)*(Math.PI/180.0f);
        int delay = round((double)points/time);
        ParticleType part = particle_1_13.DUST_COLOR_TRANSITION.color(Color.fromRGB(r, g, b),Color.fromRGB(r2, g2, b2), size);
        for(int i = 1,j=0;i<= points;i++){
            if(i % delay == 0){
                j++;
            }
            new BukkitRunnable() {
                public void run() {
                    vc.rotateAroundAxis(rotc,angle);
                    part.packet(force,c.clone().add(vc)).sendTo(p);
                }
            }.runTaskLater(that, j);
        }
    }

//--------------------------------------------------------------------------------------------------------------------------------
    public static void RedstoneParticleCycleAroundEntity(Entity e, double radius, double yoff, Vector v, Vector rot,
                                                         int points, double degree, double time, boolean clockwise,
                                                         int r, int g, int b, float size, boolean force){
        Vector vc = v.clone();
        Vector rotc = rot.clone();
        vc.normalize().multiply(radius);
        if(!clockwise){
            degree *= -1;
        }
        double angle = (degree/points)*(Math.PI/180.0f);
        World w = e.getWorld();
        int delay = round((double)points/time);
        DustOptions dust = new DustOptions(Color.fromRGB(r, g, b), size);
        for(int i = 1,j=0;i<= points;i++){
            if(i % delay == 0){
                j++;
            }
            new BukkitRunnable() {
                public void run() {
                    Location c = e.getLocation();
                    c.add(new Vector(0,yoff,0));
                    vc.rotateAroundAxis(rotc,angle);
                    w.spawnParticle(Particle.valueOf("REDSTONE"), c.clone().add(vc).add(e.getVelocity()),
                            0, 0, 0, 0, 0, dust , force);
                }
            }.runTaskLater(that, j);
        }
    }
    public static void RedstoneParticleCycleAroundEntity(Entity e, double radius, double yoff, Vector v, Vector rot,
                                                         int points, double degree, double time, boolean clockwise,
                                                         int r, int g, int b, int r2, int g2, int b2, float size, boolean force){
        Vector vc = v.clone();
        Vector rotc = rot.clone();
        vc.normalize().multiply(radius);
        if(!clockwise){
            degree *= -1;
        }
        double angle = (degree/points)*(Math.PI/180.0f);
        World w = e.getWorld();
        int delay = round((double)points/time);
        DustTransition dust = new DustTransition(Color.fromRGB(r,g,b), Color.fromRGB(r2,g2,b2), size);
        for(int i = 1,j=0;i<= points;i++){
            if(i % delay == 0){
                j++;
            }
            new BukkitRunnable() {
                public void run() {
                    Location c = e.getLocation();
                    c.add(new Vector(0,yoff,0));
                    vc.rotateAroundAxis(rotc,angle);
                    w.spawnParticle(Particle.DUST_COLOR_TRANSITION, c.clone().add(vc).add(e.getVelocity()),
                            0, 0, 0, 0, 0, dust , force);
                }
            }.runTaskLater(that, j);
        }
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------
    public static void RedstoneParticleCycleAroundEntity_ToPlayers(Player p, Entity e, double radius, double yoff, Vector v, Vector rot,
                                                                   int points, double degree, double time, boolean clockwise,
                                                                   int r, int g, int b, float size, boolean force){
        Vector vc = v.clone();
        Vector rotc = rot.clone();
        vc.normalize().multiply(radius);
        if(!clockwise){
            degree *= -1;
        }
        double angle = (degree/points)*(Math.PI/180.0f);
        int delay = round((double)points/time);
        ParticleType part = particle_1_13.DUST.color(Color.fromRGB(r, g, b), size);
        for(int i = 1,j=0;i<= points;i++){
            if(i % delay == 0){
                j++;
            }
            new BukkitRunnable() {
                public void run() {
                    Location c = e.getLocation();
                    c.add(new Vector(0,yoff,0));
                    vc.rotateAroundAxis(rotc,angle);
                    part.packet(force,c.clone().add(vc)).sendTo(p);
                }
            }.runTaskLater(that, j);
        }
    }

    public static void RedstoneParticleCycleAroundEntity_ToPlayers(Player p, Entity e, double radius, double yoff, Vector v, Vector rot,
                                                                   int points, double degree, double time, boolean clockwise,
                                                                   int r, int g, int b, int r2, int g2, int b2, float size, boolean force){
        Vector vc = v.clone();
        Vector rotc = rot.clone();
        vc.normalize().multiply(radius);
        if(!clockwise){
            degree *= -1;
        }
        double angle = (degree/points)*(Math.PI/180.0f);
        int delay = round((double)points/time);
        ParticleType part = particle_1_13.DUST_COLOR_TRANSITION.color(Color.fromRGB(r, g, b),Color.fromRGB(r2, g2, b2), size);
        for(int i = 1,j=0;i<= points;i++){
            if(i % delay == 0){
                j++;
            }
            new BukkitRunnable() {
                public void run() {
                    Location c = e.getLocation();
                    c.add(new Vector(0,yoff,0));
                    vc.rotateAroundAxis(rotc,angle);
                    part.packet(force,c.clone().add(vc)).sendTo(p);
                }
            }.runTaskLater(that, j);
        }
    }

//----------------------------------------------------------------------------------------------------------------------------------------------------------
    public static void RSSphere(Player p,Location l, double radius,int points,
                               int r, int g, int b, int r2, int g2, int b2,
                                float size, boolean force){
        Fabric.sendPacketToFabric(p,"gcb:gcb","RSSphere:" +
                +l.getX()+","+l.getY()+","+l.getZ()+":"
                +radius+":"+points+":"
                +r+","+g+","+b+":"
                +r2+","+g2+","+b2+":"
                +size+":"+force);
    }
    public static void RedstoneParticleSphere(final Location c,double radius,int points, int r, int g, int b, float size,boolean force){
        RedstoneParticleSphere(c,radius,points,r,g,b,r,g,b,size,force);
    }
    public static void RedstoneParticleSphere(final Location c,double radius,int points,
                                              int r, int g, int b, int r2, int g2, int b2, float size,boolean force){
        ArrayList<Player> col = new ArrayList<Player>(Bukkit.getOnlinePlayers());
        if (col.size() <= 0){
            return;
        }
        col.removeIf(ent -> (ent.getWorld() != c.getWorld()));
        for(Player p: col){
            if (FabricC2S.hasMod(p)) {
                RSSphere(p, c, radius, points, r, g, b, r2, g2, b2, size, force);
            }
        }
        col.removeIf(ent -> (FabricC2S.hasMod(ent)));
        if (col.size() <= 0){
            return;
        }
        points = round(points*NOMODREDUCED);
        Vector v = new Vector(radius,0,0);
        Vector v2 = v.clone().rotateAroundY(Math.PI/2);
        double rotate = Math.PI*2/points;
        ParticleType part = particle_1_13.DUST_COLOR_TRANSITION.
                color(Color.fromRGB(r, g, b),Color.fromRGB(r2, g2, b2), size);
        for(int i=0,k = points/2; i<k ;i++){
            for(int j=0; j<points ;j++){
                v.rotateAroundAxis(v2,rotate);
                part.packet(force,c.clone().add(v)).sendTo(col);
            }
            v.rotateAroundY(rotate);
            v2.rotateAroundY(rotate);
        }
    }
    public static void RedstoneParticleSphere_ToPlayers(Player p,final Location c,double radius,int points,
                                                        int r, int g, int b, int r2, int g2, int b2,
                                                        float size,boolean force){
        if (FabricC2S.hasMod(p)) {
            RSSphere(p, c,radius,points, r, g, b, r2, g2, b2, size, force);
            return;
        }
        points = round(points*NOMODREDUCED);
        Vector v = new Vector(radius,0,0);
        Vector v2 = v.clone().rotateAroundY(Math.PI/2);
        double rotate = Math.PI*2/points;
        ParticleType part = particle_1_13.DUST_COLOR_TRANSITION.
                color(Color.fromRGB(r, g, b),Color.fromRGB(r2, g2, b2), size);
        for(int i=0,k = points/2; i<k ;i++){
            for(int j=0; j<points ;j++){
                v.rotateAroundAxis(v2,rotate);
                part.packet(force,c.clone().add(v)).sendTo(p);
            }
            v.rotateAroundY(rotate);
            v2.rotateAroundY(rotate);
        }
    }
//-----------------------------------------------------------------------------------------------------------------------------------------------------------
    public static void RSSpiralLine(Player p, Location s, Location e, double radius, int points, double period,
                                    int r, int g, int b, int r2, int g2, int b2, float size, boolean force){
        Fabric.sendPacketToFabric(p,"gcb:gcb","RSSpiralLine:" +
                +s.getX()+","+s.getY()+","+s.getZ()+":"
                +e.getX()+","+e.getY()+","+e.getZ()+":"
                +radius+":"+points+":"+period+":"
                +r+","+g+","+b+":"
                +r2+","+g2+","+b2+":"
                +size+":"+force);
    }
    public static void RedstoneParticleSpiralLine(Location s, Location e, double radius, int points, double period,
                                                  int r, int g, int b, float size, boolean force){
        RedstoneParticleSpiralLine(s,e,radius,points,period,r,g,b,r,g,b,size,force);
    }
    public static void RedstoneParticleSpiralLine(Location s, Location e, double radius, int points, double period,
                                                  int r, int g, int b, int r2, int g2, int b2, float size, boolean force){
        ArrayList<Player> col = new ArrayList<Player>(Bukkit.getOnlinePlayers());
        if (col.size() == 0 || s.getWorld() != e.getWorld()){
            return;
        }
        Location finalS = s;
        col.removeIf(ent -> (ent.getWorld() != finalS.getWorld()));
        for(Player p: col){
            if (FabricC2S.hasMod(p)) {
                RSSpiralLine(p, s, e, radius, points, period, r, g, b, r2, g2, b2, size, force);
            }
        }
        col.removeIf(ent -> (FabricC2S.hasMod(ent)));
        if (col.size() <= 0){
            return;
        }
        points = round(points*NOMODREDUCED);
        s = s.clone();
        e = e.clone();
        Vector v = (e.clone().subtract(s)).toVector().normalize();
        double yaw = getYaw(v);
        Vector v2 = v.clone().rotateAroundAxis(getVector(yaw+90,0),Math.PI/2);
        v2.multiply(radius);
        double distance = e.distance(s);
        v.multiply(period/points);
        double rotate = (360.0d/points)*(Math.PI/180.0d);
        ParticleType part = particle_1_13.DUST_COLOR_TRANSITION.color(Color.fromRGB(r, g, b),Color.fromRGB(r2, g2, b2), size);
        for(int i =0, k = round(distance/period); i< k;i++){
            for(int j=0 ;j< points;j++){
                v2.rotateAroundAxis(v,rotate);
                part.packet(force,s.clone().add(v2)).sendTo(col);
                s.add(v);
            }
        }
    }

    public static void RedstoneParticleSpiralLine_ToPlayers(Player p, Location s, Location e, double radius, int points, double period,
                                                            int r, int g, int b, float size, boolean force){
        RedstoneParticleSpiralLine_ToPlayers(p,s,e,radius,points,period,r,g,b,r,g,b,size,force);
    }
    public static void RedstoneParticleSpiralLine_ToPlayers(Player p, Location s, Location e, double radius, int points, double period,
                                                            int r, int g, int b, int r2, int g2, int b2, float size, boolean force){
        if (FabricC2S.hasMod(p)) {
            RSSpiralLine(p, s,e,radius,points,period, r, g, b, r2, g2, b2, size, force);
            return;
        }
        points = round(points*NOMODREDUCED);
        s = s.clone();
        e = e.clone();
        Vector v = (e.clone().subtract(s)).toVector().normalize();
        double yaw = getYaw(v);
        Vector v2 = v.clone().rotateAroundAxis(getVector(yaw+90,0),Math.PI/2);
        v2.multiply(radius);
        double distance = e.distance(s);
        v.multiply(period/points);
        double rotate = (360.0f/points)*(Math.PI/180.0f);
        ParticleType part = particle_1_13.DUST_COLOR_TRANSITION.color(Color.fromRGB(r, g, b),Color.fromRGB(r2, g2, b2), size);
        for(int i =0, k = round(distance/period); i< k;i++){
            for(int j=0 ;j< points;j++){
                v2.rotateAroundAxis(v,rotate);
                part.packet(force,s.clone().add(v2)).sendTo(p);
                s.add(v);
            }
        }
    }

//-----------------------------------------------------------------------------------------------------------------------------------------------------------
    public static void RSSpiralLine_Delay(Player p, Location s, Location e, double radius, int points, double period,
                                          int r, int g, int b, int r2, int g2, int b2, float size,
                                          double time, double stoff, boolean force){
        Fabric.sendPacketToFabric(p,"gcb:gcb","RSSpiralLine_Delay:" +
                +s.getX()+","+s.getY()+","+s.getZ()+":"
                +e.getX()+","+e.getY()+","+e.getZ()+":"
                +radius+":"+points+":"+period+":"
                +r+","+g+","+b+":"
                +r2+","+g2+","+b2+":"
                +size+":"+time+":"+stoff+":"+force);
    }
    public static void RedstoneParticleSpiralLine_Delay(final Location s, Location e, double radius, int points, double period,
                                                        int r, int g, int b, int r2, int g2, int b2, float size,
                                                        double time, double stoff, boolean force){
        ArrayList<Player> col = new ArrayList<Player>(Bukkit.getOnlinePlayers());
        if (col.size() == 0 || s.getWorld() != e.getWorld()){
            return;
        }
        col.removeIf(ent -> (ent.getWorld() != s.getWorld()));
        for(Player p: col){
            if (FabricC2S.hasMod(p)) {
                RSSpiralLine_Delay(p, s, e, radius, points, period,
                        r, g, b, r2, g2, b2, size, time, stoff, force);
            }
        }
        col.removeIf(ent -> (FabricC2S.hasMod(ent)));
        if (col.size() <= 0){
            return;
        }
        points = round(points*NOMODREDUCED);
        Location st = s.clone();
        Location et = e.clone();
        Vector v = (et.clone().subtract(st)).toVector().normalize();
        double yaw = getYaw(v);
        Vector v2 = v.clone().rotateAroundAxis(getVector(yaw+90,0),Math.PI/2);
        v2.multiply(radius);
        double distance = et.distance(st);
        double rotate = (360.0f/points)*(Math.PI/180.0f);
        int delay = round(((distance/period)*points)/time);
        v.multiply(period/points);
        ParticleType part = particle_1_13.DUST_COLOR_TRANSITION.color(Color.fromRGB(r, g, b),Color.fromRGB(r2, g2, b2), size);
        v2.rotateAroundAxis(v,stoff*(Math.PI/180.0f));
        for(int i =0,j = 0, k = round((distance/period)*points); i< k;i++){
            if(i % delay == 0){
                j++;
            }
            new BukkitRunnable() {
                public void run() {
                    v2.rotateAroundAxis(v,rotate);
                    part.packet(force,st.clone().add(v2)).sendTo(col);
                    st.add(v);
                }
            }.runTaskLater(that, j);
        }
    }
    public static void RedstoneParticleSpiralLine_Delay_ToPlayers(Player p,Location s, Location e, double radius, int points, double period,
                                                        int r, int g, int b, int r2, int g2, int b2, float size,
                                                        double time, double stoff, boolean force){
        if (FabricC2S.hasMod(p)) {
            RSSpiralLine_Delay(p, s,e,radius,points,period, r, g, b, r2, g2, b2, size,time,stoff, force);
            return;
        }
        points = round(points*NOMODREDUCED);
        Location st = s.clone();
        Location et = e.clone();
        Vector v = (et.clone().subtract(st)).toVector().normalize();
        double yaw = getYaw(v);
        Vector v2 = v.clone().rotateAroundAxis(getVector(yaw+90,0),Math.PI/2);
        v2.multiply(radius);
        double distance = et.distance(st);
        double rotate = (360.0f/points)*(Math.PI/180.0f);
        int delay = round(((distance/period)*points)/time);
        v.multiply(period/points);
        ParticleType part = particle_1_13.DUST_COLOR_TRANSITION.color(Color.fromRGB(r, g, b),Color.fromRGB(r2, g2, b2), size);
        v2.rotateAroundAxis(v,stoff*(Math.PI/180.0f));
        for(int i =0,j = 0, k = round((distance/period)*points); i< k;i++){
            if(i % delay == 0){
                j++;
            }
            new BukkitRunnable() {
                public void run() {
                    v2.rotateAroundAxis(v,rotate);
                    part.packet(force,st.clone().add(v2)).sendTo(p);
                    st.add(v);
                }
            }.runTaskLater(that, j);
        }
    }
//-------------------------------------------------------------------------------------------------------------------------------------
    public static void RedstoneParticleTetrahedron(Location center,Vector face, double length,
                                                                  int r, int g, int b, int r2, int g2, int b2, float size,
                                                    float density, double rotate, boolean force){
        Vector top = new Vector(0,length,0);
        double dist = length*Math.pow(3,0.5)/2;
        Vector bot1 = new Vector(0,0,dist).add(new Vector(0,length/-2.0f,0));
        Vector bot2 = bot1.clone().rotateAroundY(Math.PI*(2.0f/3.0f));
        Vector bot3 = bot2.clone().rotateAroundY(Math.PI*(2.0f/3.0f));
        Vector temp = face.clone();
        temp.setY(0);
        double angle = top.angle(face);
        if(temp.length() == 0){
            temp = new Vector(0,0,1);
        }else{
            temp = temp.rotateAroundY(Math.PI/2.0f);
        }
        top = top.rotateAroundAxis(temp,angle);
        bot1 = bot1.rotateAroundAxis(temp,angle).rotateAroundAxis(face,rotate*Math.PI/180.0d);
        bot2 = bot2.rotateAroundAxis(temp,angle).rotateAroundAxis(face,rotate*Math.PI/180.0d);
        bot3 = bot3.rotateAroundAxis(temp,angle).rotateAroundAxis(face,rotate*Math.PI/180.0d);
        RedstoneParticleLine(center.clone().add(top),center.clone().add(bot1),r,g,b,r2,g2,b2,size,density,force);
        RedstoneParticleLine(center.clone().add(top),center.clone().add(bot2),r,g,b,r2,g2,b2,size,density,force);
        RedstoneParticleLine(center.clone().add(top),center.clone().add(bot3),r,g,b,r2,g2,b2,size,density,force);
        RedstoneParticleLine(center.clone().add(bot1),center.clone().add(bot2),r,g,b,r2,g2,b2,size,density,force);
        RedstoneParticleLine(center.clone().add(bot2),center.clone().add(bot3),r,g,b,r2,g2,b2,size,density,force);
        RedstoneParticleLine(center.clone().add(bot3),center.clone().add(bot1),r,g,b,r2,g2,b2,size,density,force);
    }

    public static void RedstoneParticleHexahedron(final Location center,final Vector face, double length,
                                                   int r, int g, int b, int r2, int g2, int b2, float size,
                                                   float density, double rotate, boolean force){
        double A = length*2.0f/Math.pow(3,0.5f); //변길이
        double B = A*Math.pow(2,0.5f); //변정사각형대각선길이
        Vector top = new Vector(0,A/2.0f,0);
        Vector dir = new Vector(1,0,1).multiply(B/2.0f);
        List<Vector> vertices = new ArrayList<>();
        for(int i = 0;i<8;i++){
            vertices.add(top.clone().add(dir));
            dir.rotateAroundY(Math.PI/2.0f);
        }
        for(int i = 4;i<8;i++){
            vertices.get(i).add(new Vector(0,-A,0));
        }
        Vector temp = face.clone();
        double angle = top.angle(face);
        if(temp.length() == 0){
            temp = new Vector(0,0,1);
        }else{
            temp = temp.rotateAroundY(Math.PI/2.0f);
        }
        temp.setY(0);
        Vector finalTemp = temp;
        vertices.forEach(vector -> vector.rotateAroundAxis(finalTemp,angle));
        vertices.forEach(vector -> vector.rotateAroundAxis(face,rotate*Math.PI/180.0d));
        RedstoneParticleLine(center.clone().add(vertices.get(0)),center.clone().add(vertices.get(1)),r,g,b,r2,g2,b2,size,density,force);
        RedstoneParticleLine(center.clone().add(vertices.get(1)),center.clone().add(vertices.get(2)),r,g,b,r2,g2,b2,size,density,force);
        RedstoneParticleLine(center.clone().add(vertices.get(2)),center.clone().add(vertices.get(3)),r,g,b,r2,g2,b2,size,density,force);
        RedstoneParticleLine(center.clone().add(vertices.get(3)),center.clone().add(vertices.get(0)),r,g,b,r2,g2,b2,size,density,force);

        RedstoneParticleLine(center.clone().add(vertices.get(0)),center.clone().add(vertices.get(4)),r,g,b,r2,g2,b2,size,density,force);
        RedstoneParticleLine(center.clone().add(vertices.get(1)),center.clone().add(vertices.get(5)),r,g,b,r2,g2,b2,size,density,force);
        RedstoneParticleLine(center.clone().add(vertices.get(2)),center.clone().add(vertices.get(6)),r,g,b,r2,g2,b2,size,density,force);
        RedstoneParticleLine(center.clone().add(vertices.get(3)),center.clone().add(vertices.get(7)),r,g,b,r2,g2,b2,size,density,force);

        RedstoneParticleLine(center.clone().add(vertices.get(4)),center.clone().add(vertices.get(5)),r,g,b,r2,g2,b2,size,density,force);
        RedstoneParticleLine(center.clone().add(vertices.get(5)),center.clone().add(vertices.get(6)),r,g,b,r2,g2,b2,size,density,force);
        RedstoneParticleLine(center.clone().add(vertices.get(6)),center.clone().add(vertices.get(7)),r,g,b,r2,g2,b2,size,density,force);
        RedstoneParticleLine(center.clone().add(vertices.get(7)),center.clone().add(vertices.get(4)),r,g,b,r2,g2,b2,size,density,force);
    }
//-------------------------------------------------------------------------------------------------------------------------------------

    public static void FallingDustParticleLine(final Location start, final Location end, BlockData b, float density,boolean force){
        Vector v = (end.clone().subtract(start).toVector()).normalize().multiply(density);
        double distance = end.distance(start);
        World w =  start.getWorld();
        int loop = round(distance/density);
        for(short i = 0;i<loop ;i++){
            w.spawnParticle(Particle.FALLING_DUST,start,0,0,0,0,0,b,force);
            start.add(v);
        }
    }

}
