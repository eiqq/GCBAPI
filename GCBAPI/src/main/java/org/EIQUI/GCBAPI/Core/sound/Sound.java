package org.EIQUI.GCBAPI.Core.sound;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;

import static org.EIQUI.GCBAPI.main.that;


public class Sound {

    public static void Play(Location l,String sound,float volume,float pitch){
        l.getWorld().playSound(l,sound,volume,pitch);
    }
    public static void PlayAt(Entity e, String sound, float volume, float pitch, double maxdistance){
        PlayAt(e.getLocation(),sound,volume,pitch,maxdistance);
    }
    public static void PlayAt(Location l,String sound,float volume,float pitch,double maxdistance){
        PlayAt(l,sound,volume,pitch,maxdistance,-1);
    }
    public static void PlayAt(Location l,String sound,float volume,float pitch,double maxDistance,long seed){
        if (volume <= 0 || maxDistance <= 0) {
            return;
        }

        // maxDistance를 기반으로 최대 거리 제곱 계산
        double maxDistanceSquared = maxDistance * maxDistance;

        // 비동기 작업을 위한 BukkitScheduler 사용
        Bukkit.getScheduler().runTaskAsynchronously(that, () -> {
            World world = l.getWorld();
            Collection<? extends Player> players = new ArrayList<>(Bukkit.getOnlinePlayers()); // 플레이어 목록 캐싱

            // 필터링된 플레이어에 대한 사운드 재생을 메인 스레드에서 실행
            if (seed < 0) {
                players.stream()
                        .filter(player -> player.getWorld().equals(world) &&
                                player.getEyeLocation().distanceSquared(l) <= maxDistanceSquared)
                        .forEach(player -> Bukkit.getScheduler().runTask(that, () ->
                                player.playSound(l, sound, SoundCategory.MASTER, volume, pitch)));
            }else{
                players.stream()
                        .filter(player -> player.getWorld().equals(world) &&
                                player.getEyeLocation().distanceSquared(l) <= maxDistanceSquared)
                        .forEach(player -> Bukkit.getScheduler().runTask(that, () ->
                                player.playSound(l, sound, SoundCategory.MASTER, volume, pitch,seed)));
            }
        });
    }

    public static void PlayFor(Player p,Location l, String sound,float volume, float pitch, double maxdistance,long seed){
        World w = l.getWorld();
        if(!p.getWorld().equals(w)){
            return;
        }
        if(volume <= 0){
            return;
        }
        if(maxdistance <= 0){
            maxdistance = volume*16.0d;
        }

        double maxd = maxdistance*maxdistance;
        if(p.getEyeLocation().distanceSquared(l) <= maxd) {
            if(seed < 0){
                p.playSound(l,sound,SoundCategory.MASTER,volume,pitch);
            }else{
                p.playSound(l,sound,SoundCategory.MASTER,volume,pitch,seed);
            }
        }
    }
}
