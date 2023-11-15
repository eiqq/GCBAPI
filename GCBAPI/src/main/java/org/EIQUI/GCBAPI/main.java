package org.EIQUI.GCBAPI;


import com.comphenix.protocol.ProtocolLibrary;

import com.comphenix.protocol.ProtocolManager;
import com.github.fierioziy.particlenativeapi.core.ParticleNativeCore;

import org.EIQUI.GCBAPI.Core.Core;
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
    public static FabricC2S li = new FabricC2S();
    public static ProtocolManager protocolManager;
    @Override
    public void onEnable(){
        super.onEnable();
        this.getLogger().log(Level.INFO,"EIQUI: GCBAPI 로드됨");
        this.that = this; //Main.that is now equal to this class
        ParticleAPI.particle_1_13 = ParticleNativeCore.loadAPI(this).LIST_1_13;
        protocolManager = ProtocolLibrary.getProtocolManager();
        PlayerMeleeAttack.registerProtocolLibPacketListener();
        PlayerControlVehicle.registerProtocolLibPacketListener();
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "gcb:gcb");
        this.getServer().getMessenger().
                registerIncomingPluginChannel(this, "gcb:gcb", li);
        this.getServer().getPluginManager().registerEvents(new FabricC2S(), this);
        Core.Initialize();
    }
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
