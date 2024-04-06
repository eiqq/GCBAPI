package org.EIQUI.GCBAPI.Core;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static java.lang.Math.*;

public final class HitboxAPI{
    private static final double BIGGEST_ENTITY_WIDTH = 5.0d;
    private static final double BIGGEST_ENTITY_HEIGHT = 10.0d;
    private static final double BIGGEST_ENTITY_WIDTH_HALF = BIGGEST_ENTITY_WIDTH/2.0d;
    private static final double BIGGEST_ENTITY_DIAGONAL_SIZE =
            Math.sqrt( (Math.pow(BIGGEST_ENTITY_WIDTH, 2)*2.0)  +
                    Math.pow(BIGGEST_ENTITY_HEIGHT, 2));


    /**
     *  F3+B 를 눌렀을때 나오는 AABB박스와 구 형 히트박스가 겹치는지 확인한다
     * @param spc 구 형 히트박스의 중심위치 location
     * @param spr 구 형 히트박스의 반지름
     * @param box 충돌시킬 AABB히트박스(Entity.getBoundingBox()로 구할수있다)
     * @return boolean
     */
    public static boolean isCollide_SphereWithAABB(Location spc, double spr, BoundingBox box) {
        Vector boxcenter = box.getCenter();
        double halfWidthX = box.getWidthX()/2.0;
        double halfHeight = box.getHeight()/2.0;
        double halfDepthZ = box.getWidthZ()/2.0;

        // Calculate the closest point on the AABB to the sphere center
        double closestX = Math.max(boxcenter.getX() - halfWidthX, Math.min(spc.getX(), boxcenter.getX() + halfWidthX));
        double closestY = Math.max(boxcenter.getY() - halfHeight, Math.min(spc.getY(), boxcenter.getY() + halfHeight));
        double closestZ = Math.max(boxcenter.getZ() - halfDepthZ, Math.min(spc.getZ(), boxcenter.getZ() + halfDepthZ));

        // Check if the closest point is within the sphere's radius
        return new Vector(closestX, closestY, closestZ).distanceSquared(spc.toVector()) <= Math.pow(spr, 2);
    }
    public static boolean isCollide_SphereWithAABB(Location spc, double spr, Entity e) {
        if(!spc.getWorld().equals(e.getWorld())){
            return false;
        }
        return isCollide_SphereWithAABB(spc,spr,e.getBoundingBox());
    }

    public static boolean isCollide_CylinderWithAABB(Location spc, double cylradius, double up, double down, BoundingBox box) {
        if( abs(spc.getY()-box.getMin().getY()) > abs(max(up,down)) + box.getHeight()){
            return false;
        }

        Vector centerbox = box.getCenter();
        double halfWidthX = box.getWidthX() / 2.0;
        double halfHeight = box.getHeight() / 2.0;

        // Calculate the closest point on the entity's bounding box to the cylinder center
        double closestX = Math.max(centerbox.getX() - halfWidthX, Math.min(spc.getX(), centerbox.getX() + halfWidthX));
        double closestZ = Math.max(centerbox.getZ() - halfWidthX, Math.min(spc.getZ(), centerbox.getZ() + halfWidthX));

        // Check if the closest point is within the cylinder's horizontal boundary
        double distanceSquared = new Vector(closestX, spc.getY(), closestZ).distanceSquared(spc.toVector());
        if (distanceSquared > Math.pow(cylradius, 2)) {
            return false;
        }

        // Check if the closest point is within the cylinder's vertical boundaries
        double closestY = Math.max(centerbox.getY() - halfHeight, Math.min(spc.getY(), centerbox.getY() + halfHeight));
        return !(closestY < spc.getY() - down || closestY > spc.getY() + up);
    }
    public static boolean isCollide_CylinderWithAABB(Location spc, double cylradius, double up, double down, Entity e) {
        if(!spc.getWorld().equals(e.getWorld())){
            return false;
        }
        return isCollide_CylinderWithAABB(spc,cylradius,up,down,e.getBoundingBox());
    }


    public static boolean isCollide_CubeWithAABB(BoundingBox bx, BoundingBox box) {
        return bx.overlaps(box);
    }

    public static boolean isCollide_CubeWithAABB(Location l, double x,double y,double z, Entity e) {
        if(l.getWorld() != e.getWorld()){
            return false;
        }
        return isCollide_CubeWithAABB(BoundingBox.of(l,x,y,z),e.getBoundingBox());
    }

    public static boolean isCollide_CubeWithAABB(Location l, double widthhalf,double height, Entity e) {
        if(l.getWorld() != e.getWorld()){
            return false;
        }
        BoundingBox box = new BoundingBox(widthhalf,height,widthhalf,-widthhalf,0,-widthhalf).shift(l);
        return isCollide_CubeWithAABB(box,e.getBoundingBox());
    }

    public static boolean isCollide_SphereWithSphere(Location spc, double spr, Location spc2, double spr2){
        if(spc.getWorld() != spc2.getWorld()){
            return false;
        }
        return (spc.distanceSquared(spc2) <= pow(spr+spr2,2));
    }

    public static boolean isCollide_SphereWithSphere(Location spc,double spr,Entity e){
        BoundingBox b = e.getBoundingBox();
        Vector v = new Vector(b.getWidthX()/2,b.getHeight()/2,b.getWidthZ()/2);
        return isCollide_SphereWithSphere(spc,spr,e.getLocation().add(0,b.getHeight()/2,0),v.length());
    }

    public static boolean isCollide_AABBWithBlock(BoundingBox box,Block block){
        Location blockLocation = block.getLocation();
        if(!isCollide_SphereWithSphere(blockLocation,1.75,
                box.getCenter().toLocation(blockLocation.getWorld()),box.getMax().distance(box.getMin())/2)){
            return false;
        }
        if (block.getBoundingBox().overlaps(box)){
            Collection<BoundingBox> boxes = block.getCollisionShape().getBoundingBoxes();
            if(boxes.size() <= 1){
                return true;
            }
            for(BoundingBox tempo: boxes){
                if(tempo.shift(blockLocation).overlaps(box)){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isCollide_AABBWithBlock(Vector boxmax,Vector boxmin,Block block){
        return isCollide_AABBWithBlock(BoundingBox.of(boxmax,boxmin),block);
    }

    public static boolean isCollide_ConeWithDot(Location coneOrigin, Vector direction, double height,
                                                      double startRadius, double endRadius,
                                                      BoundingBox b) {
        return isCollide_ConeWithDot(coneOrigin,direction,height,startRadius,endRadius,
                b.getCenter().toLocation(coneOrigin.getWorld()));
    }
    public static boolean isCollide_ConeWithDot(Location coneOrigin, Vector direction, double height,
                                                      double startRadius, double endRadius,
                                                      Entity e) {
        if(!coneOrigin.getWorld().equals(e.getWorld())){
            return false;
        }
        return isCollide_ConeWithDot(coneOrigin,direction,height,startRadius,endRadius,
                e.getLocation().add(0,e.getBoundingBox().getHeight()*0.5,0));
    }
    public static boolean isCollide_ConeWithDot(Location coneOrigin, Vector direction, double height,
                                                      double startRadius, double endRadius,
                                                      Location dotlocation) {
        dotlocation = dotlocation.clone();
        // 원뿔의 끝 점을 계산합니다.
        // height means "range"
        Vector heightVector = direction.clone().normalize().multiply(height); // 방향 벡터를 복제한 후 정규화하고 높이를 곱합니다.
        Location coneEnd = coneOrigin.clone().add(heightVector);
        // 점이 원뿔의 높이 범위 내에 있는지 확인합니다.
        Vector toPoint = dotlocation.toVector().subtract(coneOrigin.toVector()); // 원점에서 점까지의 벡터
        double dotHeight = toPoint.dot(direction.clone().normalize()); // 점까지의 벡터와 방향 벡터 사이의 내적을 사용하여 '높이'를 계산합니다.
        if (dotHeight < 0 || dotHeight > height) {
            return false; // 점이 원뿔의 높이 범위를 벗어남
        }
        // 점과 원뿔의 축 사이의 최소 거리를 계산합니다.
        Location closestPointOnLine = getMinDistLoc_DotWithLine(dotlocation,coneOrigin, coneEnd);
        double actualDistance = closestPointOnLine.distance(dotlocation); // 실제 거리 계산
        // 점이 원뿔 내에 있는지 확인하기 위해 현재 높이에서의 반지름을 계산합니다.
        double lerpFactor = dotHeight / height; // 선형 보간을 위한 계수
        double radiusAtHeight = startRadius + (endRadius - startRadius) * lerpFactor; // 현재 높이에서의 반지름
        // 점이 원뿔의 섹션 내에 있는지 확인합니다.
        return actualDistance <= radiusAtHeight;
    }

//-------------------------------------------------------------------------------------------------------
    private static boolean zeroCondition(Entity e){
        if (e.isDead()
                || (e.getType() == EntityType.PLAYER && ((Player)e).getGameMode() == GameMode.SPECTATOR)
                || (e.getType() != EntityType.INTERACTION && !e.getType().isAlive())
                || (e.getBoundingBox().getWidthX() < Vector.getEpsilon()
                && e.getBoundingBox().getHeight() < Vector.getEpsilon()) ){
            return true;
        }
        return false;
    }

    public static Collection<Entity> getEntity_inSphere(Location spc, double spr,boolean removezero) {
        Collection<Entity> ret = getEntitiesInNearChunks(spc,spr);
        double sprs = pow(spr+BIGGEST_ENTITY_DIAGONAL_SIZE,2);
        Predicate<Entity> basic = ent -> (ent.getLocation().distanceSquared(spc) > sprs) ||
                !isCollide_SphereWithAABB(spc,spr,ent);
        if (removezero){
            ret.removeIf(ent -> zeroCondition(ent) || basic.test(ent));
        }else{
            ret.removeIf(basic);
        }
        return ret;
    }

    public static Collection<Entity> getEntity_inCylinder(Location spc, double cylr,double up,double down,boolean removezero) {
        Vector v;
        if (up > down){
            v = new Vector(cylr,up,0);
        }else{
            v = new Vector(cylr,down,0);
        }
        Collection<Entity> ret = getEntitiesInNearChunks(spc,cylr);
        double sprs = Math.pow(v.length()+BIGGEST_ENTITY_DIAGONAL_SIZE,2);
        Predicate<Entity> basic = ent ->
                (ent.getLocation().distanceSquared(spc) > sprs) ||
                !isCollide_CylinderWithAABB(spc,cylr,up,down,ent);
        if (removezero){
            ret.removeIf(ent -> zeroCondition(ent) || basic.test(ent));
        }else{
            ret.removeIf(basic);
        }
        return ret;
    }


    public static Collection<Entity> getEntity_inCube(Location l, double x,double y,double z,boolean removezero) {
        return getEntity_inCube(l.getWorld(),BoundingBox.of(l,x,y,z),removezero);
    }

    public static Collection<Entity> getEntity_inCube(Location l,Location l2,boolean removezero) {
        return getEntity_inCube(l.getWorld(),BoundingBox.of(l,l2),removezero);
    }
    public static Collection<Entity> getEntity_inCube(World w, BoundingBox box,boolean removezero) {
        Vector length = box.getMax().add(box.getMin()).multiply(0.5);
        Location center = length.toLocation(w);
        Collection<Entity> ret = getEntitiesInNearChunks(center,box.getWidthX()/2,box.getWidthZ()/2);
        Predicate<Entity> basic = ent -> !ent.getBoundingBox().overlaps(box);
        if (removezero){
            ret.removeIf(ent -> zeroCondition(ent) || basic.test(ent));
        }else{
            ret.removeIf(basic);
        }
        return ret;
    }

    public static Collection<Entity> getEntity_inCapsule(Location start,Location end,double radius,boolean stopAtBlock,boolean removezero) {
        Vector v = end.toVector().subtract(start.toVector());
        return getEntity_inCapsule(start,v,v.length(),radius,stopAtBlock,removezero);
    }
    public static Collection<Entity> getEntity_inCapsule(Location start,Vector v,double length,double radius,boolean stopAtBlock,boolean removezero) {
        Collection<Entity> ret = new ArrayList<>();
        RayTraceResult temp;
        World w = start.getWorld();
        Predicate<Entity> basic;
        if(stopAtBlock){
            RayTraceResult checkblock = w.rayTraceBlocks(start,v,length, FluidCollisionMode.NEVER,true);
            if(checkblock != null && checkblock.getHitBlock() != null){
                length = start.toVector().distance(checkblock.getHitPosition());
            }
        }
        if (removezero){
            basic = (ent -> !ret.contains(ent) && !zeroCondition(ent) );
        }else{
            basic = (ent -> !ret.contains(ent) );
        }
        int maxloop = 999;
        int i = 0;
        do{
            temp = w.rayTraceEntities(start,v,length,radius,  basic);
            if(temp == null){
                break;
            }
            if(temp.getHitEntity() != null){
                ret.add(temp.getHitEntity());
            }
            i++;
        }while (i < maxloop);
        return ret;
    }

    private static Collection<Entity> getEntitiesInNearChunks(Location location,double chunkrange) {
        return getEntitiesInNearChunks(location,chunkrange,chunkrange);
    }
    public static Collection<Entity> getEntitiesInNearChunks(Location location, double Xradius, double Zradius) {
        World world = location.getWorld();
        Collection<Entity> nearbyEntities = new ArrayList<>();
        if (world == null) {
            return nearbyEntities;
        }
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        Xradius += BIGGEST_ENTITY_WIDTH_HALF;
        Zradius += BIGGEST_ENTITY_WIDTH_HALF;
        int xRangeInChunks = (int) Math.ceil(Xradius / 16);
        int zRangeInChunks = (int) Math.ceil(Zradius / 16);

        for (int dx = -xRangeInChunks; dx <= xRangeInChunks; dx++) {
            for (int dz = -zRangeInChunks; dz <= zRangeInChunks; dz++) {
                Chunk chunk = world.getChunkAt(chunkX + dx, chunkZ + dz);
                if (!chunk.isLoaded()) {
                    continue;
                }
                for (Entity entity : chunk.getEntities()) {
                    nearbyEntities.add(entity);
                }
            }
        }
        return nearbyEntities;
    }


    public static Collection<Block> getBlocksIn(Location l,double x,double y,double z){
        BoundingBox box = BoundingBox.of(l,x,y,z);
        World w = l.getWorld();
        return getBlocksIn(box.getMin().toLocation(w),box.getMax().toLocation(w));
    }

    public static Collection<Block> getBlocksIn(World w, BoundingBox b){
        return getBlocksIn(b.getMin().toLocation(w),b.getMax().toLocation(w));
    }
    public static Collection<Block> getBlocksIn(Location loc1, Location loc2){
        List<Block> blocks = new ArrayList<>();
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return blocks;
        }
        World world = loc1.getWorld();
        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    blocks.add(world.getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }
//-------------------------------------------------------------------------------------------------------
    public static Location getMinDistLoc_DotWithAABB(Location l,BoundingBox box){
        Vector boxcenter = box.getCenter();
        double halfWidthX = box.getWidthX()/2.0;
        double halfHeight = box.getHeight()/2.0;
        double halfDepthZ = box.getWidthZ()/2.0;

        // Calculate the closest point on the AABB to the sphere center
        double closestX = Math.max(boxcenter.getX() - halfWidthX, Math.min(l.getX(), boxcenter.getX() + halfWidthX));
        double closestY = Math.max(boxcenter.getY() - halfHeight, Math.min(l.getY(), boxcenter.getY() + halfHeight));
        double closestZ = Math.max(boxcenter.getZ() - halfDepthZ, Math.min(l.getZ(), boxcenter.getZ() + halfDepthZ));

        // Check if the closest point is within the sphere's radius
        return new Location(l.getWorld(), closestX, closestY, closestZ,0,0);
    }

    public static Location getMinDistLoc_DotWithAABB(Location l,Entity e){
        if(!l.getWorld().equals(e.getWorld())){
            return null;
        }
        return getMinDistLoc_DotWithAABB(l,e.getBoundingBox());
    }

    public static Location getMinDistLoc_DotWithLine(Location pointC,Location Linestart, Location Lineend){
        if(Linestart.getWorld() != Lineend.getWorld() || Linestart.getWorld() != pointC.getWorld()){
            return null;
        }
        // A to B 벡터
        Vector lineAB = Lineend.clone().subtract(Linestart).toVector();
        // A to C 벡터
        Vector lineAC = pointC.clone().subtract(Linestart).toVector();
        // A to B 벡터의 크기 제곱
        double lineABLengthSquared = lineAB.lengthSquared();
        // A to B 벡터와 A to C 벡터의 내적
        double dotProduct = lineAC.dot(lineAB);
        // 점 C가 선분 A to B의 시작점(A)보다 뒤에 있는 경우
        if (dotProduct <= 0) {
            return Linestart; // 점 A 반환
        }
        // 점 C가 선분 A to B의 끝점(B)보다 앞에 있는 경우
        if (dotProduct >= lineABLengthSquared) {
            return Lineend;
        }
        // 점 C가 선분 A to B 사이에 있는 경우
        double distanceAlongLine = dotProduct / lineABLengthSquared; // 선분 A to B 위의 거리 비율
        return lineAB.multiply(distanceAlongLine).
                add(Linestart.toVector()).toLocation(pointC.getWorld());// 선분 A to B 위의 가장 가까운 점 반환
    }
//-------------------------------------------------------------------------------------------------------

}