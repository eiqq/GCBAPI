package org.EIQUI.GCBAPI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import java.lang.String;
import org.bukkit.attribute.Attribute;
import static org.bukkit.util.NumberConversions.round;

public final class HealthbarAPI{

    static char backsp = '耀';
    static String fill = ""+backsp;

    public static String getPlayerHealthBarText(LivingEntity p,short size){
        double maxhealth = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double health = p.getHealth();

        if(health > maxhealth){
            health = maxhealth;
        }
        String color = "§a";
        if (health/maxhealth <= 0.25){
            color = "§c";
        }else if (health/maxhealth <= 0.5){
            color = "§6";
        }
        String r = "";
        int phm = round(size*health/maxhealth);
        for(short i = 1;  i<=size ; i++){
            if(i <= phm){
                r+= color+fill;
            }else{
                r+="§7"+fill;
            }
            if(i != 1 && i%10 == 0 && i != size){
                r+="§0"+fill;
            }
        }
        return color+"["+round(health)+"]"+" "+r;
    }
    public static String getPlayerHealthBarText(LivingEntity p, short size, float shield){
        if(shield <= 0){
            return getPlayerHealthBarText(p,size);
        }
        double maxhealth = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double health = p.getHealth();
        if(health > maxhealth){
            health = maxhealth;
        }

        String color = "§a";
        if (health/maxhealth <= 0.25){
            color = "§c";
        }else if (health/maxhealth <= 0.5){
            color = "§6";
        }
        if(health + shield > maxhealth){
            maxhealth = health + shield;
        }
        String r = "";
        int phm = round(size*health/maxhealth);
        int pspm = phm + round(size*shield/maxhealth);
        for(short i = 1;  i<=size ; i++){
            if(i <= phm){
                r+= color+fill;
            }else if(i <= pspm){
                r+= "§9"+fill;
            }
            else{
                r+="§7"+fill;
            }
            if(i != 1 && i%10 == 0 && i != size){
                r+="§0"+fill;
            }
        }
        return "§9["+round(health)+"]"+" "+r;
    }
}
