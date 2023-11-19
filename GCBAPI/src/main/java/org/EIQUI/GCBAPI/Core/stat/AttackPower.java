package org.EIQUI.GCBAPI.Core.stat;

import org.EIQUI.GCBAPI.Core.stat.structure.NonBasedAttribute;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;


public class AttackPower implements NonBasedAttribute {
    public static final String NAME = "AP";
    private static final Map<LivingEntity,Double> TOTAL = new HashMap<>();
    private static final Map<LivingEntity,Double> BONUS_FIXED = new HashMap<>();
    private static final Map<LivingEntity,Double> BONUS_MULTIPLIER = new HashMap<>();

    @Override
    public double getTotal(LivingEntity e) {
        if(TOTAL.containsKey(e)){
            return TOTAL.get(e);
        }
        return 0;
    }

    @Override
    public double getFixedBonus(LivingEntity e) {
        if(BONUS_FIXED.containsKey(e)){
            return BONUS_FIXED.get(e);
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
    public void setFixedBonus(LivingEntity e, double value) {
        BONUS_FIXED.put(e,value);
        update(e);
    }

    @Override
    public void setMultiplierBonus(LivingEntity e, double value) {
        BONUS_MULTIPLIER.put(e,value);
        update(e);
    }

    @Override
    public void update(LivingEntity e) {
        TOTAL.put(e,getFixedBonus(e)*(1+getMultiplierBonus(e)));
    }


    @Override
    public void clear() {
        TOTAL.clear();
        BONUS_FIXED.clear();
        BONUS_MULTIPLIER.clear();
    }
    @Override
    public void clear(LivingEntity e) {
        TOTAL.remove(e);
        BONUS_FIXED.remove(e);
        BONUS_MULTIPLIER.remove(e);
    }

    public double calculateAttackPowerDamage(LivingEntity e,double damage){
        return Stat.roundToPlace( (1+(getTotal(e)/100))*damage, 2);
    }

}
