package org.EIQUI.GCBAPI.Core.stat;

import org.EIQUI.GCBAPI.Core.stat.structure.NonBasedAttribute;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

public class CooldownReduce implements NonBasedAttribute {
    public static final String NAME = "CDR";
    private static final Map<LivingEntity,Double> TOTAL = new HashMap<>();
    private static final Map<LivingEntity,Double> BONUS_MULTIPLIER = new HashMap<>();

    @Override
    public double getTotal(LivingEntity e) {
        if(TOTAL.containsKey(e)){
            return TOTAL.get(e);
        }
        return 0;
    }

    @Override
    public double getMultiplierBonus(LivingEntity e) {
        if(BONUS_MULTIPLIER.containsKey(e)){
            return BONUS_MULTIPLIER.get(e);
        }
        return 0;
    }

    @Override
    public double getMultiplierBonus_AsFixed(LivingEntity e) {
        double multib = getMultiplierBonus(e)+1;
        double total = getTotal(e);
        return total - (total/multib);
    }

    @Override
    public void setTotal(LivingEntity e, double value) {
        TOTAL.put(e,value);
    }

    @Override
    public void setMultiplierBonus(LivingEntity e, double value) {
        BONUS_MULTIPLIER.put(e,value);
        setTotal(e,getTotal(e)*getMultiplierBonus(e));
    }

    @Override
    public void clear() {
        TOTAL.clear();
        BONUS_MULTIPLIER.clear();
    }
    @Override
    public void clear(LivingEntity e) {
        TOTAL.remove(e);
        BONUS_MULTIPLIER.remove(e);
    }

    public double calculateCooldownReduce(LivingEntity e,double cooldown){
        return Stat.roundToPlace((1.0d - (getTotal(e)/100))*cooldown , 2);
    }
}
