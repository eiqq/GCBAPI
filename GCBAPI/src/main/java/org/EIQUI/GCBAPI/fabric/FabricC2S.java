package org.EIQUI.GCBAPI.fabric;

import org.EIQUI.GCBAPI.ModChecker;
import org.EIQUI.GCBAPI.ScreenShot;
import org.EIQUI.GCBAPI.events.PlayerKeyInput;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import static org.EIQUI.GCBAPI.main.doNothing;
import static org.EIQUI.GCBAPI.main.that;

public class FabricC2S implements PluginMessageListener, Listener {
    private static final HashMap<UUID, Boolean> HAS_MOD = new HashMap<>();
    private static final HashMap<UUID, String> VERSION = new HashMap<>();

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if ("gcb:gcb".equals(channel)) {
            HAS_MOD.put(player.getUniqueId(), true);
            String rec = new String(message);
            if(!rec.contains(":")){
                VERSION.put(player.getUniqueId(),rec);
                that.getLogger().log(Level.INFO, rec);
            }
            String[] packet = rec.split(":");
            String header = packet[0];
            header = header.replaceAll("[^a-zA-Z0-9가-힣]", "");
            String[] data = new String[packet.length - 1];
            System.arraycopy(packet, 1, data, 0, packet.length - 1);

            switch (header) {
                case "KEYINPUT" -> PlayerKeyInput.parsePacketAndCall(player, data);
                case "MOUSEINPUT" -> doNothing();
                case "MODS" -> ModChecker.saveFromPacket(player, data);
                case "SCREENSHOT" -> ScreenShot.saveFromPacket(player, data);
                default -> {
                    that.getLogger().log(Level.INFO, rec);
                }
            }
        }
    }



    public static void setMod(Player player, boolean hasMod) {
        HAS_MOD.put(player.getUniqueId(), hasMod);
    }

    public static boolean hasMod(Player player) {
        return HAS_MOD.getOrDefault(player.getUniqueId(), false);
    }

    public static String getVersion(Player player) {
        return VERSION.getOrDefault(player.getUniqueId(), "noMod");
    }

    public static void reset(Player player) {
        VERSION.remove(player.getUniqueId());
        HAS_MOD.remove(player.getUniqueId());
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        reset(player);
        that.getLogger().log(Level.INFO, player.getName() + ": 전송");
        Bukkit.getScheduler().runTaskLater(that, () -> Fabric.sendPacketToFabric(player, "gcb:gcb", "d"), 20);
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        that.getLogger().log(Level.INFO, player.getName() + ": 데이터삭제");
        reset(player);
    }
}
