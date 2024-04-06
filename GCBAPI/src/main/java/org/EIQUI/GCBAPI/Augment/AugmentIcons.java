package org.EIQUI.GCBAPI.Augment;

import kr.toxicity.hud.api.placeholder.HudPlaceholder;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.BetterHud.listener.TextPlaceHolder.MANAGER;

public class AugmentIcons {

    public static final int MAX_AUG = 47;

    public static final ConcurrentHashMap<Player, Set<Integer>> AugIcons = new ConcurrentHashMap<>();

    public static void clear(Player p) {
        AugIcons.computeIfPresent(p, (key, value) -> {
            value.clear();
            return value;
        });
    }

    public static void addIcon(Player p, int i) {
        AugIcons.compute(p, (key, value) -> {
            if (value == null) {
                value = Collections.newSetFromMap(new ConcurrentHashMap<>());
            }
            value.add(i);
            return value;
        });
    }

    public static void removeIcon(Player p, int i) {
        AugIcons.computeIfPresent(p, (key, value) -> {
            value.remove(i); // 아이콘 제거
            return value;
        });
    }
    public static boolean hasIcon(Player p, int i) {
        Set<Integer> icons = AugIcons.getOrDefault(p, Collections.emptySet());
        return icons.contains(i);
    }
    public static void Initialize(){

        for(int i = 1; i <= MAX_AUG ; i++){
            final int finalI = i;
            MANAGER.getBooleanContainer().addPlaceholder("Augment_Condition_"+finalI,
                    HudPlaceholder.of((args, event) -> p ->
                            hasIcon(p.getBukkitPlayer(), finalI)));
        }

    }


}
