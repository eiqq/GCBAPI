package org.EIQUI.GCBAPI;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

import static org.bukkit.util.NumberConversions.round;

public final class HealthbarAPI {
    private static final char BACKSPACE = '耀';
    private static final String FILL = "" + BACKSPACE;
    private static final String COLOR_HEALTHY = "§a";
    private static final String COLOR_DANGER = "§c";
    private static final String COLOR_WARNING = "§6";
    private static final String COLOR_SHIELD = "§9";
    private static final String COLOR_EMPTY = "§7";
    private static final String COLOR_SEPARATOR = "§0";
    private static final double LOW_HEALTH_THRESHOLD = 0.25;
    private static final double MEDIUM_HEALTH_THRESHOLD = 0.5;

    public static String getPlayerHealthBarText(LivingEntity p, int size) {
        double health = p.getHealth();
        double maxhealth = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        health = Math.min(health, maxhealth);

        String color = getColorByHealthFraction(health / maxhealth);
        return color+"["+(int)Math.ceil(health)+"] "+buildBar(size, color, round(size * health / maxhealth));
    }

    public static String getPlayerHealthBarText(LivingEntity p, int size, float shield) {
        double health = p.getHealth();
        double maxhealth = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        health = Math.min(health, maxhealth);

        if (health + shield > maxhealth) {
            maxhealth = health + shield;
        }

        String color = getColorByHealthFraction(health / maxhealth);
        int phm = round(size * health / maxhealth);
        int pshm = phm + round(size * shield / maxhealth);
        if(shield > 0){
            return COLOR_SHIELD+"["+(int)Math.ceil(health)+"] "+buildBarAndShieldBar(size, color, phm, pshm);
        }
        return color+"["+(int)Math.ceil(health)+"] "+buildBarAndShieldBar(size, color, phm, 0);
    }

    private static String getColorByHealthFraction(double fraction) {
        if (fraction <= LOW_HEALTH_THRESHOLD) {
            return COLOR_DANGER;
        } else if (fraction <= MEDIUM_HEALTH_THRESHOLD) {
            return COLOR_WARNING;
        }
        return COLOR_HEALTHY;
    }

    private static String buildBar(int size, String color, int filledSize) {
        StringBuilder healthBar = new StringBuilder();
        for (int i = 1; i <= size; i++) {
            healthBar.append((i <= filledSize) ? color : COLOR_EMPTY).append(FILL);
            if (i != 1 && i % 10 == 0 && i != size) {
                healthBar.append(COLOR_SEPARATOR).append(FILL);
            }
        }
        return healthBar.append(" ").toString();
    }

    private static String buildBarAndShieldBar(int size, String color, int healthMark, int shieldMark) {
        StringBuilder healthBar = new StringBuilder();
        for (int i = 1; i <= size; i++) {
            if (i <= healthMark) {
                healthBar.append(color);
            } else if (i <= shieldMark) {
                healthBar.append(COLOR_SHIELD);
            } else {
                healthBar.append(COLOR_EMPTY);
            }
            healthBar.append(FILL);
            if (i != 1 && i % 10 == 0 && i != size) {
                healthBar.append(COLOR_SEPARATOR).append(FILL);
            }
        }
        return healthBar.append(" ").toString();
    }
}
