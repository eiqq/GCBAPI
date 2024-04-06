package org.EIQUI.GCBAPI.Core.stat.structure;

import org.bukkit.entity.LivingEntity;

public interface NonBasedAttribute {


    double getTotal(LivingEntity e);
    double getFixedBonus(LivingEntity e);
    double getMultiplierBonus(LivingEntity e);
    double getMultiplierBonus_AsFixed(LivingEntity e);

    void setFixedBonus(LivingEntity e,double value);
    void setMultiplierBonus(LivingEntity e,double value);

    void update(LivingEntity e);
    void clear();
    void clear(LivingEntity e);
}
