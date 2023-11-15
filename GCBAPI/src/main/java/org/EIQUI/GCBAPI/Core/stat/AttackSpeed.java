package org.EIQUI.GCBAPI.Core.stat;

import org.EIQUI.GCBAPI.Core.stat.structure.BasedAttribute;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;


public class AttackSpeed implements BasedAttribute {
    public static final Attribute TYPE = Attribute.GENERIC_ATTACK_SPEED;
    public static final String NAME = "AS";
    public static final String NAMESPACE_Fixed = "State::"+NAME+"_FixedBonus";
    public static final String NAMESPACE_Multiplier = "State::"+NAME+"_MultiplierBonus";

    @Override
    public double getTotal(LivingEntity e) {
        return getBase(e)*(getFixedBonus(e)+1)*(getMultiplierBonus(e)+1);
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
        return 0;
    }

    @Override
    public double getScalarBonus_AsFixed(LivingEntity e) {
        return getFixedBonus(e);
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
        double total = base*(getMultiplierBonus(e)+1);
        return (total-base);
    }

    @Override
    public void setBase(LivingEntity e,double value) {
        e.getAttribute(TYPE).setBaseValue(value);
    }
    @Override
    public void setFixedBonus(LivingEntity e,double value) {
        Stat.setAttributeValue(e,TYPE,NAMESPACE_Fixed,value, AttributeModifier.Operation.ADD_SCALAR);
    }
    @Override
    public void setScalarBonus(LivingEntity e, double value) {}
    @Override
    public void setMultiplierBonus(LivingEntity e,double value) {
        Stat.setAttributeValue(e,TYPE,NAMESPACE_Multiplier,value, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
    }

    @Override
    public void update(LivingEntity e) {

    }

    @Override
    public void clear() {

    }

    @Override
    public void clear(LivingEntity e) {}

}
