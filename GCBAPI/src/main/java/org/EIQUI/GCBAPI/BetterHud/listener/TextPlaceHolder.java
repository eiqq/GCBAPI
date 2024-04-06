package org.EIQUI.GCBAPI.BetterHud.listener;

import kr.toxicity.hud.api.BetterHud;
import kr.toxicity.hud.api.manager.PlaceholderManager;
import kr.toxicity.hud.api.placeholder.HudPlaceholder;
import org.EIQUI.GCBAPI.Core.Shield;
import org.EIQUI.GCBAPI.Core.UI;
import org.EIQUI.GCBAPI.Core.stat.Stat;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TextPlaceHolder {
    public static final PlaceholderManager MANAGER = BetterHud.getInstance().getPlaceholderManager();

    private static final Map<String, Map<Player,String>> VALUE = new ConcurrentHashMap<>();

    public static void ApplyDefaultListeners(){

        HudPlaceholder<String> AD = HudPlaceholder.of((args, event) -> p ->
                String.valueOf(Math.ceil(Stat.ATTACK_DAMAGE.getTotal(p.getBukkitPlayer()))) );
        HudPlaceholder<String> AP = HudPlaceholder.of((args, event) -> p ->
                String.valueOf(Math.ceil(Stat.ATTACK_POWER.getTotal(p.getBukkitPlayer()))) );
        HudPlaceholder<String> AS = HudPlaceholder.of((args, event) -> p ->
                String.valueOf(Math.round(Stat.ATTACK_SPEED.getTotal(p.getBukkitPlayer())*100d)/100d));
        HudPlaceholder<String> ARMOR = HudPlaceholder.of((args, event) -> p ->
                String.valueOf(Math.ceil(Stat.ARMOR.getTotal(p.getBukkitPlayer()))) );
        HudPlaceholder<String> AR = HudPlaceholder.of((args, event) -> p ->
                String.valueOf(Math.round(Stat.ATTACK_RANGE.getTotal(p.getBukkitPlayer()))) );
        HudPlaceholder<String> CDR = HudPlaceholder.of((args, event) -> p ->
                String.valueOf(Math.ceil(Stat.COOLDOWN_REDUCE.getTotal(p.getBukkitPlayer()))) );
        HudPlaceholder<String> HEALTH_REGEN = HudPlaceholder.of((args, event) -> p ->
                String.valueOf(Math.ceil(Stat.HEALTH_REGEN.getTotal(p.getBukkitPlayer()))) );
        HudPlaceholder<String> MS = HudPlaceholder.of((args, event) -> p ->
                String.valueOf(Math.round(Stat.MOVEMENT_SPEED.getTotal(p.getBukkitPlayer())*100d)/100d));

        MANAGER.getStringContainer().addPlaceholder("Stat_AD",AD);
        MANAGER.getStringContainer().addPlaceholder("Stat_AP",AP);
        MANAGER.getStringContainer().addPlaceholder("Stat_AS",AS);
        MANAGER.getStringContainer().addPlaceholder("Stat_ARMOR",ARMOR);
        MANAGER.getStringContainer().addPlaceholder("Stat_AR",AR);
        MANAGER.getStringContainer().addPlaceholder("Stat_CDR",CDR);
        MANAGER.getStringContainer().addPlaceholder("Stat_HEALTH_REGEN",HEALTH_REGEN);
        MANAGER.getStringContainer().addPlaceholder("Stat_MS",MS);


        HudPlaceholder<String> COOLDOWN = HudPlaceholder.of((args, event) -> p -> UI.CooldownActionBar(p.getBukkitPlayer()));
        MANAGER.getStringContainer().addPlaceholder("COOLDOWN",COOLDOWN);

        HudPlaceholder<String> BAR_HEALTH = HudPlaceholder.of((args, event) -> p -> {
            Player player = p.getBukkitPlayer();
            if(Shield.isShielded(player)){
                return "<white>" + Math.ceil(player.getHealth()) + "/" +
                        Math.ceil(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())
                        +" <aqua>+"+Math.ceil(Shield.getTotalAmount(player));
            }
            return "<white>" + Math.ceil(player.getHealth()) + "/" + Math.ceil(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        });
        MANAGER.getStringContainer().addPlaceholder("BAR_HEALTH",BAR_HEALTH);

    }

    public static void addListener(String id){
        if(id.length() == 0){
            return;
        }
        VALUE.computeIfAbsent(id, k -> new ConcurrentHashMap<>());
        HudPlaceholder<String> h = HudPlaceholder.of(
                (args, event) -> p -> VALUE.getOrDefault(id, Collections.emptyMap()).getOrDefault(p, "")) ;
        MANAGER.getStringContainer().addPlaceholder(id, h);
    }

}
