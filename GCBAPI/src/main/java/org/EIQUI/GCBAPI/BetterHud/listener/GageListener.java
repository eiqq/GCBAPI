package org.EIQUI.GCBAPI.BetterHud.listener;

import kr.toxicity.hud.api.BetterHud;
import kr.toxicity.hud.api.manager.ListenerManager;
import org.EIQUI.GCBAPI.Core.Shield;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class GageListener {
    private static final Map<String,Map<Player,Double>> VALUE = new ConcurrentHashMap<>();
    public static final ListenerManager MANAGER = BetterHud.getInstance().getListenerManager();

    public static void ApplyDefaultListeners(){

        MANAGER.addListener("shield", configurationSection -> updateEvent -> hudPlayer -> {
            Player p = hudPlayer.getBukkitPlayer();
            return Shield.getTotalAmount(p) / p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        });

        for(int i = 1 ;i <= 5 ; i++){
            String id = "def_Gage_"+i;
            MANAGER.addListener(id, configurationSection -> updateEvent -> hudPlayer -> {
                return VALUE.getOrDefault(id, Collections.emptyMap()).getOrDefault(hudPlayer.getBukkitPlayer(), 0.0);
            });
        }

    }

    public static void setValue(Player p,String id,double value){
        new BukkitRunnable() {
            @Override
            public void run() {
                VALUE.computeIfAbsent(id, k -> new ConcurrentHashMap<>());
                VALUE.get(id).put(p,value);
            }
        }.runTaskAsynchronously(that);
    }
    public static double getValue(Player p,String id){
        return VALUE.getOrDefault(id, Collections.emptyMap()).getOrDefault(p, 0.0);
    }

    public static void addListener(String id){
        if(id.length() == 0){
            return;
        }
        VALUE.computeIfAbsent(id, k -> new ConcurrentHashMap<>());
        MANAGER.addListener(id, configurationSection -> updateEvent -> hudPlayer -> {
            return VALUE.getOrDefault(id, Collections.emptyMap()).getOrDefault(hudPlayer.getBukkitPlayer(), 0.0);
        });
    }

}
