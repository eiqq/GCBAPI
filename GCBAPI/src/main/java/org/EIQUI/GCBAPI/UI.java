package org.EIQUI.GCBAPI;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.variables.Variables;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.Objects;


public class UI {

    public static String CooldownActionBar(Player p,String ID){
        String result = "";
        String uuid = String.valueOf(p.getUniqueId());

        String cc = (String) Variables.getVariable("파오캐::UI::"+uuid+"::cctext",null,false);
        if (cc != null){
            result += cc;
        }
        if(Variables.getVariable(ID+"::"+uuid+"::"+0,null,false) != null){
            String skill = (String) Variables.getVariable(ID+"::"+uuid+"::"+0,null,false);
            int cooldown = ((Long) Variables.getVariable(ID+"::"+uuid+"::cooldown::"+skill,null,false)).intValue();
            if(Variables.getVariable(ID+"::"+uuid+"::staker::"+skill,null,false) != null){
                int scd = ((Long) Variables.getVariable(ID+"::"+uuid+"::staker::"+skill+".cooldown",null,false)).intValue();
                if(cooldown > 0) {
                    result += "§b§l";
                    result += "[";
                    result += (int) Math.ceil(scd * 0.05);
                    result += "]";
                    Object stack = Variables.getVariable(ID + "::" + uuid + "::staker::" + skill + ".stack", null, false);
                    if (stack != null) {
                        int stackintvar = ((Long) stack).intValue();
                        if (stackintvar > 0) {
                            result += getSmallNumber(stackintvar);
                        }
                    }
                    result += " ";
                }
            }else{
                if(cooldown > 0) {
                    result += "§b§l";
                    result += "[";
                    result += (int) Math.ceil(cooldown * 0.05);
                    result += "]";
                    result += " ";
                }
            }
        }

        for(int i = 1;i<=4;i++){
            if(Variables.getVariable(ID+"::"+uuid+"::"+i,null,false) != null){
                String skill = (String) Variables.getVariable(ID+"::"+uuid+"::"+i,null,false);
                int cooldown = ((Long) Variables.getVariable(ID+"::"+uuid+"::cooldown::"+skill,null,false)).intValue();
                if(Variables.getVariable(ID+"::"+uuid+"::staker::"+skill,null,false) != null){
                    int scd = ((Long) Variables.getVariable(ID+"::"+uuid+"::staker::"+skill+".cooldown",null,false)).intValue();
                    if(cooldown > 0){
                        result += "§a§l";
                    }else{
                        result += "§2§l";
                    }
                    result += "[";
                    result += (int)Math.ceil(scd*0.05);
                    result += "]";
                    Object stack = Variables.getVariable(ID+"::"+uuid+"::staker::"+skill+".stack",null,false);
                    if(stack != null){
                        int stackintvar = ((Long) stack).intValue();
                        if(stackintvar > 0) {
                            result += getSmallNumber(stackintvar);
                        }
                    }
                    result += " ";

                }else{
                    if(cooldown > 0){
                        result += "§a§l";
                    }else{
                        result += "§2§l";
                    }
                    result += "[";
                    result += (int)Math.ceil(cooldown*0.05);
                    result += "]";
                    result += " ";
                }
            }
        }
        return result;
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
