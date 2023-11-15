package org.EIQUI.GCBAPI.Core.stat.structure;

import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

public interface NonBasedAttribute {


    double getTotal(LivingEntity e);
    double getMultiplierBonus(LivingEntity e);
    double getMultiplierBonus_AsFixed(LivingEntity e);

    void setTotal(LivingEntity e,double value);
    void setMultiplierBonus(LivingEntity e,double value);

    void clear();
    void clear(LivingEntity e);
}