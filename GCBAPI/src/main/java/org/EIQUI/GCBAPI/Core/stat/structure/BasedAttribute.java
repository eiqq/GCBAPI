package org.EIQUI.GCBAPI.Core.stat.structure;

import org.bukkit.entity.LivingEntity;

public interface BasedAttribute {

    double getTotal(LivingEntity e);
    double getBase(LivingEntity e);
    double getTotalBonus(LivingEntity e);
    double getFixedBonus(LivingEntity e);
    double getScalarBonus(LivingEntity e);
    double getScalarBonus_AsFixed(LivingEntity e);

    double getMultiplierBonus(LivingEntity e);
    double getMultiplierBonus_AsFixed(LivingEntity e);


    void setBase(LivingEntity e,double value);
    void setFixedBonus(LivingEntity e,double value);
    void setScalarBonus(LivingEntity e,double value);
    void setMultiplierBonus(LivingEntity e,double value);

    void update(LivingEntity e);

    void clear();
    void clear(LivingEntity e);
}
