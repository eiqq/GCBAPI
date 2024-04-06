package org.EIQUI.GCBAPI.particle;

import org.EIQUI.GCBAPI.Util;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

import static org.EIQUI.GCBAPI.Util.getVector;
import static org.EIQUI.GCBAPI.main.that;
import static org.bukkit.util.NumberConversions.round;

public class ComplexParticleAPI {

    public static void Line(String partictext, Location start, Location end,
                                            double x,double y,double z,int amount,double speed, float density, boolean force){
        if(!start.getWorld().equals(end.getWorld())){
            return;
        }
        Particle particle;
        try{
            particle = Particle.valueOf(partictext);
        }catch (IllegalArgumentException ex){
            particle = Particle.CLOUD;
        }
        start = start.clone();
        end = end.clone();
        Vector v = (end.clone().subtract(start).toVector()).normalize().multiply(density);
        World w = start.getWorld();
        double distance = end.distance(start);
        int loop = round(distance / density);
        for (short i = 0; i < loop; i++) {
            w.spawnParticle(particle,start,amount,x,y,z,speed,null,force);
            start.add(v);
        }
    }

    public static void Circle(String partictext, final Location c,double radius,int points,
                              double x,double y,double z,int amount,double speed,boolean force){
        Particle particle;
        try{
            particle = Particle.valueOf(partictext);
        }catch (IllegalArgumentException ex){
            particle = Particle.CLOUD;
        }
        Vector v = new Vector(0,0,radius);
        double rotate = (Math.PI*2.0f)/points;
        World w = c.getWorld();
        for(short i=0; i<points ;i++){
            v.rotateAroundY(rotate);
            w.spawnParticle(particle,c.clone().add(v),amount,x,y,z,speed,null,force);
        }
    }

    public static void Cycle(String partictext, final Location c, double radius, Vector v, Vector rot,
                                             int points, double degree, double time, boolean clockwise,
                             double x,double y,double z,int amount,double speed, boolean force){
        Particle particle;
        try{
            particle = Particle.valueOf(partictext);
        }catch (IllegalArgumentException ex){
            particle = Particle.CLOUD;
        }
        Vector vc = v.clone();
        Vector rotc = rot.clone();
        vc.normalize().multiply(radius);
        if(!clockwise){
            degree *= -1;
        }
        double angle = (degree/points)*(Math.PI/180.0f);
        int delay = round((double)points/time);
        World w = c.getWorld();
        for(int i = 1,j=0;i<= points;i++){
            if(i % delay == 0){
                j++;
            }
            Particle finalParticle = particle;
            new BukkitRunnable() {
                public void run() {
                    vc.rotateAroundAxis(rotc,angle);
                    w.spawnParticle(finalParticle,c.clone().add(vc),amount,x,y,z,speed,null,force);
                }
            }.runTaskLater(that, j);
        }
    }

    public static void Sphere(String partictext,final Location c,double radius,int points,
                              double x,double y,double z,int amount,double speed,boolean force){
        Particle particle;
        try{
            particle = Particle.valueOf(partictext);
        }catch (IllegalArgumentException ex){
            particle = Particle.CLOUD;
        }
        Vector v = new Vector(radius,0,0);
        Vector v2 = v.clone().rotateAroundY(Math.PI/2.0d);
        double rotate = (Math.PI*2.0f)/points;
        World w = c.getWorld();
        for(int i=0,k = points/2; i<k ;i++){
            for(int j=0; j<points ;j++){
                v.rotateAroundAxis(v2,rotate);
                w.spawnParticle(particle,c.clone().add(v),amount,x,y,z,speed,null,force);
            }
            v.rotateAroundY(rotate);
            v2.rotateAroundY(rotate);
        }
    }

    public static void RedstoneParticleSpiralLine(String partictext, Location s, Location e, double radius, int points, double period,
                                                  double x,double y,double z,int amount,double speed, boolean force){
        if(!s.getWorld().equals(e.getWorld())){
            return;
        }
        Vector v = (e.clone().subtract(s)).toVector();
        if(v.length() < Vector.getEpsilon()){
            return;
        }
        v.normalize();
        Particle particle;
        try{
            particle = Particle.valueOf(partictext);
        }catch (IllegalArgumentException ex){
            particle = Particle.CLOUD;
        }
        s = s.clone();
        e = e.clone();
        double yaw = Util.getYaw(v);
        Vector v2 = v.clone().rotateAroundAxis(getVector(yaw+90,0),Math.PI/2);
        v2.multiply(radius);
        double distance = e.distance(s);
        v.multiply(period/points);
        double rotate = (360.0d/points)*(Math.PI/180.0d);
        World w = s.getWorld();
        for(int i =0, k = round(distance/period); i< k;i++){
            for(int j=0 ;j< points;j++){
                v2.rotateAroundAxis(v,rotate);
                w.spawnParticle(particle,s.clone().add(v2),amount,x,y,z,speed,null,force);
                s.add(v);
            }
        }
    }

    public static void Tetrahedron(String partictext,Location center,Vector face, double length,
                                   double x,double y,double z,int amount,double speed,
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
        Line(partictext,center.clone().add(top),center.clone().add(bot1),x,y,z,amount,speed,density,force);
        Line(partictext,center.clone().add(top),center.clone().add(bot2),x,y,z,amount,speed,density,force);
        Line(partictext,center.clone().add(top),center.clone().add(bot3),x,y,z,amount,speed,density,force);
        Line(partictext,center.clone().add(bot1),center.clone().add(bot2),x,y,z,amount,speed,density,force);
        Line(partictext,center.clone().add(bot2),center.clone().add(bot3),x,y,z,amount,speed,density,force);
        Line(partictext,center.clone().add(bot3),center.clone().add(bot1),x,y,z,amount,speed,density,force);
    }

    public static void Hexahedron(String partictext, final Location center,final Vector face, double length,
                                  double x,double y,double z,int amount,double speed,
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
        Line(partictext,center.clone().add(vertices.get(0)),center.clone().add(vertices.get(1)),x,y,z,amount,speed,density,force);
        Line(partictext,center.clone().add(vertices.get(1)),center.clone().add(vertices.get(2)),x,y,z,amount,speed,density,force);
        Line(partictext,center.clone().add(vertices.get(2)),center.clone().add(vertices.get(3)),x,y,z,amount,speed,density,force);
        Line(partictext,center.clone().add(vertices.get(3)),center.clone().add(vertices.get(0)),x,y,z,amount,speed,density,force);

        Line(partictext,center.clone().add(vertices.get(0)),center.clone().add(vertices.get(4)),x,y,z,amount,speed,density,force);
        Line(partictext,center.clone().add(vertices.get(1)),center.clone().add(vertices.get(5)),x,y,z,amount,speed,density,force);
        Line(partictext,center.clone().add(vertices.get(2)),center.clone().add(vertices.get(6)),x,y,z,amount,speed,density,force);
        Line(partictext,center.clone().add(vertices.get(3)),center.clone().add(vertices.get(7)),x,y,z,amount,speed,density,force);

        Line(partictext,center.clone().add(vertices.get(4)),center.clone().add(vertices.get(5)),x,y,z,amount,speed,density,force);
        Line(partictext,center.clone().add(vertices.get(5)),center.clone().add(vertices.get(6)),x,y,z,amount,speed,density,force);
        Line(partictext,center.clone().add(vertices.get(6)),center.clone().add(vertices.get(7)),x,y,z,amount,speed,density,force);
        Line(partictext,center.clone().add(vertices.get(7)),center.clone().add(vertices.get(4)),x,y,z,amount,speed,density,force);
    }
}
