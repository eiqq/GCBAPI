package org.EIQUI.GCBAPI.Core.stat;


import org.EIQUI.GCBAPI.Core.stat.structure.BasedAttribute;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AttackDamage implements BasedAttribute {
    public static final Attribute TYPE = Attribute.GENERIC_ATTACK_DAMAGE;
    public static final String NAME = "AD";
    public static final String NAMESPACE_Fixed = "State::"+NAME+"_FixedBonus";
    public static final String NAMESPACE_Scalar = "State::"+NAME+"_ScalarBonus";
    private static final Map<LivingEntity,Double> BONUS_SCALAR = new ConcurrentHashMap<>();
    public static final String NAMESPACE_Multiplier = "State::"+NAME+"_MultiplierBonus";

    @Override
    public double getTotal(LivingEntity e) {
        return e.getAttribute(TYPE).getValue();
    }
    @Override
    public double getBase(LivingEntity e) {
        return e.getAttribute(TYPE).getBaseValue();
    }
    @Override
    public double getFixedBonus(LivingEntity e) {
        return Stat.getAttributeValue(e,TYPE,NAMESPACE_Fixed);
    }

    @Override
    public double getScalarBonus(LivingEntity e) {
        if(BONUS_SCALAR.containsKey(e)){
            return BONUS_SCALAR.get(e);
        }
        return 0;
    }

    @Override
    public double getScalarBonus_AsFixed(LivingEntity e) {
        return Stat.getAttributeValue(e,TYPE,NAMESPACE_Scalar);
    }

    @Override
    public double getMultiplierBonus(LivingEntity e) {
        return Stat.getAttributeValue(e,TYPE,NAMESPACE_Multiplier);
    }

    @Override
    public double getMultiplierBonus_AsFixed(LivingEntity e) {
        double multib = getMultiplierBonus(e)+1;
        double total = getBase(e)*(getScalarBonus(e)+1)*multib;
        return total - (total/multib);
    }

    @Override
    public double getTotalBonus(LivingEntity e) {
        double base = getBase(e);
        double total = base*(getScalarBonus(e)+1)*(getMultiplierBonus(e)+1);
        return (total-base);
    }

    @Override
    public void setBase(LivingEntity e,double value) {
        e.getAttribute(TYPE).setBaseValue(value);
        update(e);
    }
    @Override
    public void setFixedBonus(LivingEntity e,double value) {
        Stat.setAttributeValue(e,TYPE,NAMESPACE_Fixed,value, AttributeModifier.Operation.ADD_NUMBER);
        update(e);
    }
    @Override
    public void setScalarBonus(LivingEntity e, double value) {
        BONUS_SCALAR.put(e,value);
        update(e);
    }
    @Override
    public void setMultiplierBonus(LivingEntity e,double value) {
        Stat.setAttributeValue(e,TYPE,NAMESPACE_Multiplier,value, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
    }

    @Override
    public void update(LivingEntity e) {
        double bons = 0;
        if(BONUS_SCALAR.containsKey(e)){
            bons = BONUS_SCALAR.get(e);
        }
        Stat.setAttributeValue(e,TYPE,NAMESPACE_Scalar,getBase(e)*bons, AttributeModifier.Operation.ADD_NUMBER);
    }

    @Override
    public void clear() {
        BONUS_SCALAR.clear();
    }

    @Override
    public void clear(LivingEntity e) {
        BONUS_SCALAR.remove(e);
    }
}
