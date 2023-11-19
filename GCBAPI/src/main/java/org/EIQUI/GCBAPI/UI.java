package org.EIQUI.GCBAPI;

import ch.njol.skript.variables.Variables;
import org.EIQUI.GCBAPI.Core.CC.*;
import org.EIQUI.GCBAPI.Core.UnitVector;
import org.EIQUI.GCBAPI.Core.skill.Skill;
import org.EIQUI.GCBAPI.Core.skill.SkillStacker;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;


public class UI {

    public static String CooldownActionBar(Player p){
        String result = "";
        result += getCCText(p);

        double tempcooldown = Skill.getCooldownAt(p,0);
        String tempskill = Skill.getSkill(p,0);
        if(tempskill != null && tempcooldown > 0){
            result += "§b§l";
            result += "[";
            result += (int) Math.ceil(tempcooldown);
            result += "]";
            int stack = SkillStacker.getStack(p,tempskill);
            if (stack > 0) {
                result += getSmallNumber(stack);
            }
            result += " ";
        }

        for(int i = 1;i<=4;i++){
            tempskill = Skill.getSkill(p,i);
            if(tempskill != null){
                tempcooldown = Skill.getCooldownAt(p,i);
                if(tempcooldown > 0){
                    result += "§a§l";
                }else{
                    result += "§2§l";
                }
                result += "[";
                if(SkillStacker.hasStacker(p,tempskill)){
                    result += (int) Math.ceil(SkillStacker.getCooldown(p,tempskill));
                }else{
                    result += (int) Math.ceil(tempcooldown);
                }
                result += "]";
                int stack = SkillStacker.getStack(p,tempskill);
                if (stack > 0) {
                    result += getSmallNumber(stack);
                }
                result += " ";
            }
        }
        return result;
    }

    private static String getCCText(Entity e){
        if (Timestop.isTimestopped(e)){
            return "§c§l[TIMESTOP]";
        }else if (UnitVector.isCC(e)) {
            return "§b§l[AIRBORNE]";
        } else if (Stun.isStuned(e)) {
            return "§6§l[STUN]";
        }else if (Suspend.isSuspended(e)) {
            return "§3§l[SUSPEND]";
        }else if (Silent.isSilented(e)) {
            return "§f§l[SILENT]";
        }else if (Bound.isBounded(e)) {
            return "§3§l[BOUND]";
        }else if (Slow.isSlowed(e)) {
            return "§7§l[SLOW]";
        } else{
            return "";
        }
    }
    private static String getSmallNumber(int i){
        if (i == 0){
            return "⁰";
        }
        if (i == 1){
            return "¹";
        }
        if (i == 2){
            return "²";
        }
        if (i == 3){
            return "³";
        }
        if (i == 4){
            return "⁴";
        }
        if (i == 5){
            return "⁵";
        }
        if (i == 6){
            return "⁶";
        }
        if (i == 7){
            return "⁷";
        }
        if (i == 8){
            return "⁸";
        }
        if (i == 9){
            return "⁹";
        }
        return "⁰";
    }
}
