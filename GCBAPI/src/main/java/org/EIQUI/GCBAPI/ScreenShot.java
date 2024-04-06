package org.EIQUI.GCBAPI;

import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ScreenShot {
    private static final Map<Player, LinkedHashSet<ScreenShotObject>> SCREENSHOTS = new ConcurrentHashMap<>();

    public static void saveFromPacket(Player p, String[] data){
        SCREENSHOTS.computeIfAbsent(p, k -> new LinkedHashSet<>());
        ScreenShotObject obj = new ScreenShotObject(data[0],Integer.parseInt(data[1]));
        SCREENSHOTS.get(p).add(obj);
    }
    public static String getShoots(Player p){
        if(SCREENSHOTS.containsKey(p)){
            if(SCREENSHOTS.get(p) != null){
                StringBuilder ret = new StringBuilder();
                for(ScreenShotObject s : SCREENSHOTS.get(p)){
                    ret.append(s.pictrues).append(",");
                }
                return ret.toString();
            }
        }
        return "";
    }
    public static void clear(@Nullable Player p){
        if(p == null){
            for(Set<ScreenShotObject> set : SCREENSHOTS.values()){
                set.clear();
            }
            SCREENSHOTS.clear();
        }else if(SCREENSHOTS.containsKey(p)){
            SCREENSHOTS.get(p).clear();
        }
    }

    public static class ScreenShotObject{
        String pictrues;
        int where;
        public ScreenShotObject(String base64,int part){
            where = part;
            pictrues = base64;
        }
    }
}
