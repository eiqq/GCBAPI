package org.EIQUI.GCBAPI;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.util.BoundingBox;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.function.Predicate;

import static java.lang.Math.*;

public final class HitboxAPI{
    private static final int InitialSize = 24;

    /**
     * F3+B 를 눌렀을때 나오는 AABB박스와 구 형 히트박스가 겹치는지 확인한다
     * @param spc 구 형 히트박스의 중심위치 location
     * @param spr 구 형 히트박스의 반지름
     * @param boxmax 충돌시킬 AABB히트박스의 max값 vector
     * @param boxmin AABB히트박스의 min값 vector
     * @return boolean
     */
    public static boolean isHitboxCollide_SphereWithAABB(Location spc, double spr, Vector boxmax, Vector boxmin) {
        Vector centerbox = (boxmax.clone().add(boxmin.clone())).multiply(0.5);
        double boxlength = abs(boxmax.getX() - boxmin.getX());
        double boxheight = abs(boxmax.getY() - boxmin.getY());

        Vector boxpoint = new Vector(0, 0, 0);
        if (spc.getX() < centerbox.getX() - boxlength / 2) {
            boxpoint.setX(centerbox.getX() - boxlength / 2);
        } else if (spc.getX() > centerbox.getX() + boxlength / 2) {
            boxpoint.setX(centerbox.getX() + boxlength / 2);
        } else {
            boxpoint.setX(spc.getX());
        }

        if (spc.getY() < centerbox.getY() - boxheight / 2) {
            boxpoint.setY(centerbox.getY() - boxheight / 2);
        } else if (spc.getY() > centerbox.getY() + boxheight / 2) {
            boxpoint.setY(centerbox.getY() + boxheight / 2);
        } else {
            boxpoint.setY(spc.getY());
        }

        if (spc.getZ() < centerbox.getZ() - boxlength / 2) {
            boxpoint.setZ(centerbox.getZ() - boxlength / 2);
        } else if (spc.getZ() > centerbox.getZ() + boxlength / 2) {
            boxpoint.setZ(centerbox.getZ() + boxlength / 2);
        } else {
            boxpoint.setZ(spc.getZ());
        }
        if (spc.toVector().distanceSquared(boxpoint) > pow(spr, 2)) {
            return false;
        }
        return true;
    }

    /**
     *  F3+B 를 눌렀을때 나오는 AABB박스와 구 형 히트박스가 겹치는지 확인한다
     * @param spc 구 형 히트박스의 중심위치 location
     * @param spr 구 형 히트박스의 반지름
     * @param box 충돌시킬 AABB히트박스(Entity.getBoundingBox()로 구할수있다)
     * @return boolean
     */
    public static boolean isHitboxCollide_SphereWithAABB(Location spc, double spr, BoundingBox box) {
        return isHitboxCollide_SphereWithAABB(spc,spr,box.getMax(),box.getMin());
    }
    /**
     * F3+B 를 눌렀을때 나오는 AABB박스와 구 형 히트박스가 겹치는지 확인한다
     * @param spc 구 형 히트박스의 중심위치 location
     * @param spr 구 형 히트박스의 반지름
     * @param e 엔티티
     * @return boolean
     */
    public static boolean isHitboxCollide_SphereWithEntityAABB(Location spc, double spr, Entity e) {
        if(spc.getWorld() != e.getWorld()){
            return false;
        }
        return isHitboxCollide_SphereWithAABB(spc,spr,e.getBoundingBox().getMax(),e.getBoundingBox().getMin());
    }


    /**
     *  F3+B 를 눌렀을때 나오는 AABB박스와 원기둥형 히트박스가 겹치는지 확인한다
     * @param spc 원기둥의 위치
     * @param cylr 원기둥의 반지름
     * @param up 원기둥의 위치로부터 위로 길이
     * @param down 윈기둥의 위치로부터 아래로 길이(+를 사용해야 아래로 늘어남)
     * @param e 엔티티
     * @return boolean
     */
    public static boolean isHitboxCollide_CylinderWithEntityAABB(Location spc, double cylr,double up,double down, Entity e) {
        if(spc.getWorld() != e.getWorld()){
            return false;
        }
        Vector centerbox = e.getBoundingBox().getCenter();
        double boxlength = e.getBoundingBox().getWidthX();
        double boxheight = e.getBoundingBox().getHeight();

        Vector boxpoint = new Vector(0, 0, 0);
        if (spc.getX() < centerbox.getX() - boxlength / 2) {
            boxpoint.setX(centerbox.getX() - boxlength / 2);
        } else if (spc.getX() > centerbox.getX() + boxlength / 2) {
            boxpoint.setX(centerbox.getX() + boxlength / 2);
        } else {
            boxpoint.setX(spc.getX());
        }

        if (spc.getY() < centerbox.getY() - boxheight / 2) {
            boxpoint.setY(centerbox.getY() - boxheight / 2);
        } else if (spc.getY() > centerbox.getY() + boxheight / 2) {
            boxpoint.setY(centerbox.getY() + boxheight / 2);
        } else {
            boxpoint.setY(spc.getY());
        }

        if (spc.getZ() < centerbox.getZ() - boxlength / 2) {
            boxpoint.setZ(centerbox.getZ() - boxlength / 2);
        } else if (spc.getZ() > centerbox.getZ() + boxlength / 2) {
            boxpoint.setZ(centerbox.getZ() + boxlength / 2);
        } else {
            boxpoint.setZ(spc.getZ());
        }

        Vector hbox = boxpoint.clone().setY(spc.getY());
        if (spc.toVector().distanceSquared(hbox) > pow(cylr, 2)) {
            return false;
        }
        if(spc.getY()+up < boxpoint.getY()){
            return false;
        }
        if(spc.getY()-down > boxpoint.getY()){
            return false;
        }
        return true;
    }

    /**
     * F3+B 를 눌렀을때 나오는 AABB박스와 큐브형 히트박스가 겹치는지 확인한다
     * @param l 위치
     * @param x x
     * @param y y
     * @param z z
     * @param e e
     * @return
     */
    public static boolean isHitboxCollide_CubeWithEntityAABB(Location l, double x,double y,double z, Entity e) {
        if(l.getWorld() != e.getWorld()){
            return false;
        }
        BoundingBox box = new BoundingBox(-x,-y,-z, x,y,z).shift(l);
        return e.getBoundingBox().overlaps(box);
    }


    /**
     *  구 형 히트박스와 구 형 히트박스가 겹치는지 확인한다
     * @param spc 1번 구의 중심위치
     * @param spr 1번 구의 반지름
     * @param spc2 2번 구의 중심위치
     * @param spr2 2번 구의 반지름
     * @return boolean
     */
    public static boolean isHitboxCollide_SphereWithSphere(Location spc, double spr, Location spc2, double spr2){
        if(spc.getWorld() != spc2.getWorld()){
            return false;
        }
        if (spc.distanceSquared(spc2) > pow(spr+spr2,2)){
            return false;
        }
        return true;
    }

    /**
     * 사용할필요없음
     * @param spc
     * @param spr
     * @param e
     * @return boolean
     */
    public static boolean isHitboxCollide_SphereWithEntitySphere(Location spc,double spr,Entity e){
        if(spc.getWorld() != e.getWorld()){
            return false;
        }
        double boxlength = e.getBoundingBox().getWidthX();
        double boxheight = e.getBoundingBox().getHeight();
        Vector len = new Vector(boxlength/2,boxheight,boxlength/2);
        if (spc.toVector().distanceSquared(e.getBoundingBox().getCenter()) > pow(spr+len.length(),2)){
            return false;
        }
        return true;
    }

    /**
     * AABB박스와 블록의 히트박스가 겹치는지 확인한다
     * @param boxmax AABB박스 Max 벡터
     * @param boxmin AABB박스 Min 벡터
     * @param b 블록
     * @return boolean
     */
    public static boolean isHitboxCollide_AABBWithBlockAABB(Vector boxmax , Vector boxmin,Block b){

        BoundingBox box = BoundingBox.of(boxmax,boxmin);
        double len = boxmax.distance(boxmin)/2;
        Location bl = b.getLocation();
        if(!isHitboxCollide_SphereWithSphere(bl,len,box.getCenter().toLocation(bl.getWorld()),1.75)){
            return false;
        }
        for(BoundingBox tempo: b.getCollisionShape().getBoundingBoxes()){
            if(tempo.shift(bl).overlaps(box)){
                return true;
            }
        }
        return false;
    }
    /**
     * AABB박스와 블록의 히트박스가 겹치는지 확인한다
     * @param box AABB박스
     * @param b 블록
     * @return boolean
     */
    public static boolean isHitboxCollide_AABBWithBlockAABB(BoundingBox box,Block b){
        return isHitboxCollide_AABBWithBlockAABB(box.getMax(),box.getMin(),b);
    }



    public static boolean isHitboxCollide_ConeWithDot(Location coneOrigin, Vector direction, double height,
                                                      double startRadius, double endRadius,
                                                      BoundingBox b) {
        return isHitboxCollide_ConeWithDot(coneOrigin,direction,height,startRadius,endRadius,
                b.getCenter().toLocation(coneOrigin.getWorld()));
    }
    public static boolean isHitboxCollide_ConeWithDot(Location coneOrigin, Vector direction, double height,
                                                      double startRadius, double endRadius,
                                                      Entity e) {
        return isHitboxCollide_ConeWithDot(coneOrigin,direction,height,startRadius,endRadius,
                e.getLocation().add(new Vector(0,e.getBoundingBox().getHeight()*0.5,0)));
    }
    public static boolean isHitboxCollide_ConeWithDot(Location coneOrigin, Vector direction, double height,
                                                      double startRadius, double endRadius,
                                                      Location dotlocation) {
        dotlocation = dotlocation.clone();
        // 원뿔의 끝 점을 계산합니다.
        Vector heightVector = direction.clone().normalize().multiply(height); // 방향 벡터를 복제한 후 정규화하고 높이를 곱합니다.
        Location coneEnd = coneOrigin.clone().add(heightVector);
        // 점이 원뿔의 높이 범위 내에 있는지 확인합니다.
        Vector toPoint = dotlocation.toVector().subtract(coneOrigin.toVector()); // 원점에서 점까지의 벡터
        double dotHeight = toPoint.dot(direction.clone().normalize()); // 점까지의 벡터와 방향 벡터 사이의 내적을 사용하여 '높이'를 계산합니다.
        if (dotHeight < 0 || dotHeight > height) {
            return false; // 점이 원뿔의 높이 범위를 벗어남
        }
        // 점과 원뿔의 축 사이의 최소 거리를 계산합니다.
        Location closestPointOnLine = minDistanceLocation_DotWithLine(coneOrigin, coneEnd, dotlocation);
        double actualDistance = closestPointOnLine.distance(dotlocation); // 실제 거리 계산
        // 점이 원뿔 내에 있는지 확인하기 위해 현재 높이에서의 반지름을 계산합니다.
        double lerpFactor = dotHeight / height; // 선형 보간을 위한 계수
        double radiusAtHeight = startRadius + (endRadius - startRadius) * lerpFactor; // 현재 높이에서의 반지름
        // 점이 원뿔의 섹션 내에 있는지 확인합니다.
        return actualDistance <= radiusAtHeight;
    }
//-------------------------------------------------------------------------------------------------------
    public static Collection<Entity> getEntity_inSphere(Location spc, double spr,boolean removezero) {
        double sprs = pow(spr+InitialSize,2);
        Collection<Entity> ret = spc.getWorld().getEntities();
        Predicate<Entity> pred2 = ent -> (ent.getLocation().distanceSquared(spc) > sprs);
        Predicate<Entity> pred = ent -> !isHitboxCollide_SphereWithEntityAABB(spc,spr,ent);
        if (removezero){
            ret.removeIf(ent -> (ent.isDead()) ||
                    (ent.getType() == EntityType.PLAYER && ((Player)ent).getGameMode() == GameMode.SPECTATOR ) ||
                    (ent.getType() != EntityType.INTERACTION && !ent.getType().isAlive())  );
            Predicate<Entity> pred3 = ent -> (ent.getBoundingBox().getMax().distanceSquared(ent.getBoundingBox().getMin()) <= 0);
            ret.removeIf(pred3);
        }
        ret.removeIf(pred2);
        ret.removeIf(pred);
        return ret;
    }
    public static Collection<Entity> getEntity_inCylinder(Location spc, double cylr,double up,double down,boolean removezero) {
        Vector v;
        if (up > down){
            v = new Vector(cylr,up,0);
        }else{
            v = new Vector(cylr,down,0);
        }
        double sprs = pow(v.length()+InitialSize,2);
        Collection<Entity> ret = spc.getWorld().getEntities();
        Predicate<Entity> pred2 = ent -> (ent.getLocation().distanceSquared(spc) > sprs);
        Predicate<Entity> pred = ent -> !isHitboxCollide_CylinderWithEntityAABB(spc,cylr,up,down,ent);
        if (removezero){
            ret.removeIf(ent -> (ent.isDead()) ||
                    (ent.getType() == EntityType.PLAYER && ((Player)ent).getGameMode() == GameMode.SPECTATOR ) ||
                    (ent.getType() != EntityType.INTERACTION && !ent.getType().isAlive())  );
            Predicate<Entity> pred3 = ent -> (ent.getBoundingBox().getMax().distanceSquared(ent.getBoundingBox().getMin()) <= 0);
            ret.removeIf(pred3);
        }
        ret.removeIf(pred2);
        ret.removeIf(pred);
        return ret;
    }


    public static Collection<Entity> getEntity_inCube(Location l, double x,double y,double z,boolean removezero) {
        BoundingBox box = new BoundingBox(-x,-y,-z,x,y,z).shift(l);
        Vector v = new Vector(x,y,z);
        double sprs = pow(v.length()+InitialSize,2);
        Collection<Entity> ret = l.getWorld().getEntities();
        Predicate<Entity> pred2 = ent -> (ent.getLocation().distanceSquared(l) > sprs);
        Predicate<Entity> pred = ent -> !ent.getBoundingBox().overlaps(box);
        if (removezero){
            ret.removeIf(ent -> (ent.isDead()) ||
                    (ent.getType() == EntityType.PLAYER && ((Player)ent).getGameMode() == GameMode.SPECTATOR ) ||
                    (ent.getType() != EntityType.INTERACTION && !ent.getType().isAlive())  );
            Predicate<Entity> pred3 = ent -> (ent.getBoundingBox().getMax().distanceSquared(ent.getBoundingBox().getMin()) <= 0);
            ret.removeIf(pred3);
        }
        ret.removeIf(pred2);
        ret.removeIf(pred);
        return ret;
    }

    public static Collection<Entity> getEntity_inCube(Location l,Location l2,boolean removezero) {
        BoundingBox box = BoundingBox.of(l,l2);
        Location center = l.clone().add(l2).multiply(0.5);
        double sprs = pow((l.distance(l2)/2)+InitialSize,2);
        Collection<Entity> ret = l.getWorld().getEntities();
        Predicate<Entity> pred2 = ent -> (ent.getLocation().distanceSquared(center) > sprs);
        Predicate<Entity> pred = ent -> !ent.getBoundingBox().overlaps(box);
        if (removezero){
            ret.removeIf(ent -> (ent.isDead()) ||
                    (ent.getType() == EntityType.PLAYER && ((Player)ent).getGameMode() == GameMode.SPECTATOR ) ||
                    (ent.getType() != EntityType.INTERACTION && !ent.getType().isAlive())  );
            Predicate<Entity> pred3 = ent -> (ent.getBoundingBox().getMax().distanceSquared(ent.getBoundingBox().getMin()) <= 0);
            ret.removeIf(pred3);
        }
        ret.removeIf(pred2);
        ret.removeIf(pred);
        return ret;
    }
    public static Collection<Entity> getEntity_inCube(World w, BoundingBox box,boolean removezero) {
        Vector length = box.getMax().add(box.getMin()).multiply(0.5);
        Location center = length.toLocation(w);
        double sprs = pow(length.length()+InitialSize,2);
        Collection<Entity> ret = w.getEntities();
        Predicate<Entity> pred2 = ent -> (ent.getLocation().distanceSquared(center) > sprs);
        Predicate<Entity> pred = ent -> !ent.getBoundingBox().overlaps(box);
        if (removezero){
            ret.removeIf(ent -> (ent.isDead()) ||
                    (ent.getType() == EntityType.PLAYER && ((Player)ent).getGameMode() == GameMode.SPECTATOR ) ||
                    (ent.getType() != EntityType.INTERACTION && !ent.getType().isAlive())  );
            Predicate<Entity> pred3 = ent -> (ent.getBoundingBox().getMax().distanceSquared(ent.getBoundingBox().getMin()) <= 0);
            ret.removeIf(pred3);
        }
        ret.removeIf(pred2);
        ret.removeIf(pred);
        return ret;
    }


//-------------------------------------------------------------------------------------------------------
    public static Location minDistanceLocation_DotWithAABB(Location l,BoundingBox b){
        Vector centerbox = b.getCenter();
        double boxlength = b.getWidthX()/2;
        double boxheight = b.getHeight()/2;
        Vector boxpoint = new Vector(0, 0, 0);
        if (l.getX() < centerbox.getX() - boxlength) {
            boxpoint.setX(centerbox.getX() - boxlength);
        } else if (l.getX() > centerbox.getX() + boxlength) {
            boxpoint.setX(centerbox.getX() + boxlength);
        } else {
            boxpoint.setX(l.getX());
        }

        if (l.getY() < centerbox.getY() - boxheight) {
            boxpoint.setY(centerbox.getY() - boxheight);
        } else if (l.getY() > centerbox.getY() + boxheight) {
            boxpoint.setY(centerbox.getY() + boxheight);
        } else {
            boxpoint.setY(l.getY());
        }

        if (l.getZ() < centerbox.getZ() - boxlength) {
            boxpoint.setZ(centerbox.getZ() - boxlength);
        } else if (l.getZ() > centerbox.getZ() + boxlength) {
            boxpoint.setZ(centerbox.getZ() + boxlength);
        } else {
            boxpoint.setZ(l.getZ());
        }
        return boxpoint.toLocation(l.getWorld());
    }

    public static Location minDistanceLocation_DotWithEntityAABB(Location l,Entity e){
        if(l.getWorld() != e.getWorld()){
            return l;
        }
        return minDistanceLocation_DotWithAABB(l,e.getBoundingBox());
    }

    public static Location minDistanceLocation_DotWithLine(Location pointA, Location pointB, Location pointC){
        if(pointA.getWorld() != pointB.getWorld() || pointA.getWorld() != pointC.getWorld()){
            return null;
        }
        // A to B 벡터
        Vector lineAB = pointB.clone().subtract(pointA).toVector();
        // A to C 벡터
        Vector lineAC = pointC.clone().subtract(pointA).toVector();
        // A to B 벡터의 크기 제곱
        double lineABLengthSquared = lineAB.lengthSquared();
        // A to B 벡터와 A to C 벡터의 내적
        double dotProduct = lineAC.dot(lineAB);
        // 점 C가 선분 A to B의 시작점(A)보다 뒤에 있는 경우
        if (dotProduct <= 0) {
            return pointA; // 점 A 반환
        }
        // 점 C가 선분 A to B의 끝점(B)보다 앞에 있는 경우
        if (dotProduct >= lineABLengthSquared) {
            return pointB;
        }
        // 점 C가 선분 A to B 사이에 있는 경우
        double distanceAlongLine = dotProduct / lineABLengthSquared; // 선분 A to B 위의 거리 비율
        return lineAB.multiply(distanceAlongLine).
                add(pointA.toVector()).toLocation(pointC.getWorld());// 선분 A to B 위의 가장 가까운 점 반환
    }

//-------------------------------------------------------------------------------------------------------

}