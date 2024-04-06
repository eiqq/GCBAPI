package org.EIQUI.GCBAPI.Core;

import org.EIQUI.GCBAPI.Core.CC.*;
import org.EIQUI.GCBAPI.Core.skill.Skill;
import org.EIQUI.GCBAPI.Core.skill.SkillStacker;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UI {
    private static final Map<Player, Map<Integer, CooldownColor>> COLORS = new ConcurrentHashMap<>();

    public static void resetColorAt(Player player, int i) {
        if(COLORS.containsKey(player)){
            COLORS.get(player).remove(i);
        }
    }

    public static void resetColor(Player player) {
        Map<Integer, CooldownColor> playerColors = COLORS.get(player);
        if (playerColors != null) {
            playerColors.clear();
        }
        COLORS.remove(player);
    }
    public static void reset() {
        for (Map<Integer, CooldownColor> playerColorMap : COLORS.values()) {
            playerColorMap.clear();
        }
        COLORS.clear();
    }

    public static void setColorAt(Player player, int i, int r, int g, int b, int r2, int g2, int b2) {
        COLORS.computeIfAbsent(player, k -> new ConcurrentHashMap<>())
                .put(i, new CooldownColor(
                        String.format("<#%02X%02X%02X>", r, g, b),
                        String.format("<#%02X%02X%02X>", r2, g2, b2) ));
    }

    public static CooldownColor getColorAt(Player player, int i) {
        return COLORS.getOrDefault(player, new ConcurrentHashMap<>()).getOrDefault(i, CooldownColor.DEFAULT);
    }
    public static boolean hasColorAt(Player player, int i) {
        return COLORS.containsKey(player) && COLORS.get(player).containsKey(i);
    }
    public static class CooldownColor{
        public String cd = "<#55FF55>";
        public String ready = "<#00AA00>";
        public static final CooldownColor DEFAULT = new CooldownColor("<#55FF55>", "<#00AA00>");
        CooldownColor(){
        }
        CooldownColor(String cd,String ready){
            this.cd = cd;
            this.ready = ready;
        }
    }
//-------------------------------------------------------------------------------------------------------------

    public static String CooldownActionBar(Player p){
        StringBuilder result = new StringBuilder();
        result.append(getCCText(p));

        double tempcooldown = Skill.getCooldownAt(p, 0);
        String tempskill = Skill.getSkill(p, 0);
        if(tempskill != null && (tempcooldown > 0 || SkillStacker.hasStacker(p,tempskill)) ){
            int tempstack = SkillStacker.getStack(p, tempskill);
            result.append("<aqua>[")
                    .append((int) Math.ceil(tempcooldown))
                    .append("]")
                    .append(tempstack > 0 ? getSmallNumber(tempstack) : "")
                    .append(" ");
        }

        for(int i = 1; i <= 4; i++){
            tempskill = Skill.getSkill(p, i);
            if(tempskill != null){
                tempcooldown = Skill.getCooldownAt(p, i);
                CooldownColor color = new CooldownColor();
                if(hasColorAt(p,i)){
                    color = getColorAt(p,i);
                }
                if(SkillStacker.hasStacker(p,tempskill)){
                    int tempstack = SkillStacker.getStack(p, tempskill);
                    result.append(tempcooldown > 0 ? color.cd : color.ready)
                            .append("[")
                            .append((int) Math.ceil(SkillStacker.getCooldown(p,tempskill)))
                            .append("]")
                            .append(tempstack > 0 ? getSmallNumber(tempstack) : "");
                }else{
                    result.append(tempcooldown > 0 ? color.cd : color.ready)
                            .append("[")
                            .append((int) Math.ceil(tempcooldown))
                            .append("]");
                }
                if(i < 4){
                    result.append(" ");
                }
            }
        }
        return result.toString();
    }

    private static String getCCText(Entity e){
        if (Timestop.isTimestopped(e)){
            return "<#FF5555>[TIMESTOP] ";
        }else if (UnitVector.isCC(e)) {
            return "<#55FFFF>[AIRBORNE] ";
        } else if (Stun.isStuned(e)) {
            return "<#FFAA00>[STUN] ";
        }else if (Suspend.isSuspended(e)) {
            return "<#00AAAA>[SUSPEND] ";
        }else if (Silent.isSilented(e)) {
            return "<#FFFFFF>[SILENT] ";
        }else if (Boundd.isBounded(e)) {
            return "<#AA00AA>[BOUND] ";
        }else if (Slow.isSlowed(e)) {
            return "<#AAAAAA>[SLOW] ";
        } else{
            return "";
        }
    }
    private static final String[] SMALL_NUMBERS = {"⁰", "¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹"};
    private static String getSmallNumber(int number) {
        if (number < 0) {
            return SMALL_NUMBERS[0]; // 음수일 경우 빈 문자열 반환
        }
        StringBuilder smallNumberBuilder = new StringBuilder();
        do {
            int digit = number % 10;
            smallNumberBuilder.insert(0, SMALL_NUMBERS[digit]);
            number /= 10;
        } while (number > 0);

        return smallNumberBuilder.toString();
    }


}
