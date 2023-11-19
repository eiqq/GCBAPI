package org.EIQUI.GCBAPI.Core;

import org.EIQUI.GCBAPI.Core.BehavioralEffect.*;
import org.EIQUI.GCBAPI.Core.BeneficialEffect.*;
import org.EIQUI.GCBAPI.Core.CC.*;
import org.EIQUI.GCBAPI.Core.projectile.Projectile;
import org.EIQUI.GCBAPI.Core.skill.Skill;
import org.EIQUI.GCBAPI.Core.skill.SkillStacker;
import org.EIQUI.GCBAPI.Core.stat.Stat;

import static org.EIQUI.GCBAPI.main.that;

public class Core {

    public static void Initialize(){
        that.getServer().getPluginManager().registerEvents(new Bound.BoundHandler(), that);
        that.getServer().getPluginManager().registerEvents(new Silent.SilentHandler(), that);
        that.getServer().getPluginManager().registerEvents(new Slow.SlowHandler(), that);
        that.getServer().getPluginManager().registerEvents(new Stun.StunHandler(), that);
        that.getServer().getPluginManager().registerEvents(new Suspend.SuspendHandler(), that);
        that.getServer().getPluginManager().registerEvents(new Timestop.TimestopHandler(), that);

        that.getServer().getPluginManager().registerEvents(new Invincible.InvincibleHandler(), that);
        that.getServer().getPluginManager().registerEvents(new Stealth.StealthHandler(), that);
        that.getServer().getPluginManager().registerEvents(new Swift.SwiftHandler(), that);
        that.getServer().getPluginManager().registerEvents(new Unstopable.UnstopableHandler(), that);
        that.getServer().getPluginManager().registerEvents(new Untargetable.UntargetableHandler(), that);

        that.getServer().getPluginManager().registerEvents(new CannotAttack.CannotAttackHandler(), that);
        that.getServer().getPluginManager().registerEvents(new CannotBaseAttack.CannotBaseAttackHandler(), that);
        that.getServer().getPluginManager().registerEvents(new Floating.FloatingHandler(), that);
        that.getServer().getPluginManager().registerEvents(new Hold.HoldHandler(), that);
        that.getServer().getPluginManager().registerEvents(new JumpBlock.JumpBlockHandler(), that);
        that.getServer().getPluginManager().registerEvents(new ScreenHold.ScreenHoldHandler(), that);
        that.getServer().getPluginManager().registerEvents(new SelfMovementSpeedControl.SelfMovementSpeedControlHandler(), that);

        that.getServer().getPluginManager().registerEvents(new Buff.BuffHandler(), that);
        that.getServer().getPluginManager().registerEvents(new Shield.ShieldHandler(), that);

        that.getServer().getPluginManager().registerEvents(new Projectile.ProjectileHandler(), that);
        that.getServer().getPluginManager().registerEvents(new Stat.StatHandler(),that);

        that.getServer().getPluginManager().registerEvents(new Skill.SkillHandler(), that);
        that.getServer().getPluginManager().registerEvents(new SkillStacker.SkillStackerHandler(),that);
    }
}
