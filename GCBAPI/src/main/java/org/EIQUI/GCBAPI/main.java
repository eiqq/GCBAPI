package org.EIQUI.GCBAPI;


import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.github.fierioziy.particlenativeapi.core.ParticleNativeCore;
import com.ticxo.modelengine.api.ModelEngineAPI;
import org.EIQUI.GCBAPI.Augment.AugmentIcons;
import org.EIQUI.GCBAPI.BetterHud.listener.Condition;
import org.EIQUI.GCBAPI.BetterHud.listener.GageListener;
import org.EIQUI.GCBAPI.BetterHud.listener.TextPlaceHolder;
import org.EIQUI.GCBAPI.Core.Core;
import org.EIQUI.GCBAPI.ModelEngine.METitanMountController;
import org.EIQUI.GCBAPI.events.PlayerControlVehicle;
import org.EIQUI.GCBAPI.events.PlayerMeleeAttack;
import org.EIQUI.GCBAPI.fabric.FabricC2S;
import org.EIQUI.GCBAPI.particle.ParticleAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;



public final class main extends JavaPlugin {
    public static main that; //in your case "plugin" would be "Main.that"
    public static final FabricC2S FABRIC = new FabricC2S();
    public static ProtocolManager protocolManager;
    @Override
    public void onEnable(){
        super.onEnable();
        this.that = this;
        this.getLogger().log(Level.INFO,"EIQUI: GCBAPI 로드됨");
        ParticleAPI.particle_1_13 = ParticleNativeCore.loadAPI(this).LIST_1_13;
        protocolManager = ProtocolLibrary.getProtocolManager();
        PlayerMeleeAttack.registerProtocolLibPacketListener();
        PlayerControlVehicle.registerProtocolLibPacketListener();
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "gcb:gcb");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "gcb:gcb", FABRIC);
        this.getServer().getPluginManager().registerEvents(FABRIC, this);
        Core.Initialize();
        ModelEngineAPI.getMountControllerTypeRegistry().register("custom_controller", METitanMountController.CUSTOM);

        TextPlaceHolder.ApplyDefaultListeners();
        GageListener.ApplyDefaultListeners();
        AugmentIcons.Initialize();
        Condition.Initialize();
    }

    public static void doNothing(){}
    @Override
    public void onDisable(){
        that = null; //Set to null to prevent memory leaks
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this);
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this);
    }

    public static Plugin getGCBPlugin(){
        return that;
    }

  
}
