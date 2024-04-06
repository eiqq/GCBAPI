package org.EIQUI.GCBAPI;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ModChecker {

    private static final Map<Player, Set<String>> MODS = new ConcurrentHashMap<>();


    public static void saveFromPacket(Player p, String[] s){
        MODS.computeIfAbsent(p, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
        MODS.get(p).add(s[0]);
        //that.getLogger().log(Level.INFO,p.getName()+":"+s);
    }

    public static String getMods(Player p){
        if(MODS.containsKey(p)){
            if(MODS.get(p) != null){
                String ret = "";
                for(String s :MODS.get(p)){
                    ret += s+",";
                }
                return ret;
            }
        }
        return "";
    }

    public static class Handler implements Listener {
        public Handler() {
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onQuit(PlayerQuitEvent e) {
            MODS.remove(e.getPlayer());
        }
    }
}
