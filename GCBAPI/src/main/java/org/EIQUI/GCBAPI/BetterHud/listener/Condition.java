package org.EIQUI.GCBAPI.BetterHud.listener;

import kr.toxicity.hud.api.BetterHud;
import kr.toxicity.hud.api.manager.PlaceholderManager;
import kr.toxicity.hud.api.placeholder.HudPlaceholder;
import org.EIQUI.GCBAPI.Core.BeneficialEffect.Invincible;
import org.EIQUI.GCBAPI.Core.BeneficialEffect.Stealth;
import org.EIQUI.GCBAPI.Core.BeneficialEffect.Unstopable;
import org.EIQUI.GCBAPI.Core.BeneficialEffect.Untargetable;

public class Condition {
    public static final PlaceholderManager MANAGER = BetterHud.getInstance().getPlaceholderManager();

    public static void Initialize(){
        MANAGER.getBooleanContainer().addPlaceholder("isInvincible",
                HudPlaceholder.of((args, event) -> p -> Invincible.isInvincible(p.getBukkitPlayer())));

        MANAGER.getBooleanContainer().addPlaceholder("isStealth",
                HudPlaceholder.of((args, event) -> p -> Stealth.isStealth(p.getBukkitPlayer())));

        MANAGER.getBooleanContainer().addPlaceholder("isUntargetable",
                HudPlaceholder.of((args, event) -> p -> Untargetable.isUntargetable(p.getBukkitPlayer())));

        MANAGER.getBooleanContainer().addPlaceholder("isUnstopable",
                HudPlaceholder.of((args, event) -> p -> Unstopable.isUnstopable(p.getBukkitPlayer())));
    }

}
