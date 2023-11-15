package org.EIQUI.GCBAPI;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;

import static org.EIQUI.GCBAPI.Util.getVector;
import static org.EIQUI.GCBAPI.main.that;

public final class PacketAPI {

    public static void sendEntityRidePacket(final Entity e) throws InvocationTargetException {
        List<Entity> passengers = e.getPassengers();
        if (passengers.size() == 0){
            return;
        }
        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = pm.createPacket(PacketType.Play.Server.MOUNT);
        int[] intarr = new int[passengers.size()];

        int i = 0;
        for(Entity ent: passengers){
            intarr[i] = ent.getEntityId();
            i++;
        }

        packet.getIntegers().write(0,e.getEntityId());
        packet.getIntegerArrays().write(0, intarr);
        for(Player player: Bukkit.getOnlinePlayers()){
            pm.sendServerPacket(player,packet);
        }
    }

    public static void sendRotation(final Player p,double yaw,double pitch) throws InvocationTargetException {
        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = pm.createPacket(PacketType.Play.Server.POSITION);
        packet.getDoubles().write(0, 0.0);
        packet.getDoubles().write(1, 0.0);
        packet.getDoubles().write(2, 0.0);
        packet.getFloat().write(0, (float) (yaw));
        packet.getFloat().write(1, (float) (pitch));
        pm.sendServerPacket(p, packet);
    }
    public static void sendLocation(final Player p, final Location l) throws InvocationTargetException {
        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = pm.createPacket(PacketType.Play.Server.POSITION);
        packet.getDoubles().write(0, l.getX()-p.getLocation().getX());
        packet.getDoubles().write(1, l.getY()-p.getLocation().getY());
        packet.getDoubles().write(2, l.getZ()-p.getLocation().getZ());
        packet.getFloat().write(0, 0f);
        packet.getFloat().write(1, 0f);
        pm.sendServerPacket(p, packet);
    }
    public static void sendVelocity(final Player p, final Vector v) throws InvocationTargetException {
        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_VELOCITY);
        packet.getIntegers().write(0,p.getEntityId());
        packet.getIntegers().write(1,(int)v.getX()*8000);
        packet.getIntegers().write(2,(int)v.getY()*8000);
        packet.getIntegers().write(3,(int)v.getZ()*8000);
        pm.sendServerPacket(p, packet);
    }
    public static void sendLookAt(final Player p,double yaw,double pitch){
        try{
            ProtocolManager pm = ProtocolLibrary.getProtocolManager();
            PacketContainer packet = pm.createPacket(PacketType.Play.Server.LOOK_AT);
            Location l = p.getEyeLocation().add(getVector(yaw+90,-pitch).multiply(999));
            packet.getIntegers().write(0, 1);
            packet.getDoubles().write(0, l.getX());
            packet.getDoubles().write(1, l.getY());
            packet.getDoubles().write(2, l.getZ());
            packet.getBooleans().write(0,false);
            pm.sendServerPacket(p, packet);
        }catch (Exception e){
            new RuntimeException(e);
        }
    }

    public static void sendGlow(Player player, Entity entity, String color) {
        if(entity.isDead() || !entity.isValid()){
            return;
        }
        //sendTeamCreate(player, entity, color);
        sendColor(player, entity, color);
        byte def = 0x00;
        byte fire = 0x01;
        byte crouching = 0x02;
        byte unused = 0x04;
        byte sprinting = 0x08;
        byte swimming = 0x10;
        byte invisible = 0x20;
        byte glowing = 0x40;
        byte flying = (byte) 0x80;
        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, entity.getEntityId());
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class);
        WrappedDataValue wrappedDataValue = new WrappedDataValue(
                0,
                serializer,
                glowing
        );
        packet.getDataValueCollectionModifier().write(0, Arrays.asList(wrappedDataValue));
        pm.sendServerPacket(player, packet);
    }
    public static void sendGlowRemove(Player player, Entity entity) {
        if(entity.isDead() || !entity.isValid()){
            return;
        }
        byte def = 0x00;
        byte fire = 0x01;
        byte crouching = 0x02;
        byte unused = 0x04;
        byte sprinting = 0x08;
        byte swimming = 0x10;
        byte invisible = 0x20;
        byte glowing = 0x40;
        byte flying = (byte) 0x80;
        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, entity.getEntityId());
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class);
        WrappedDataValue wrappedDataValue = new WrappedDataValue(
                0,
                serializer,
                def
        );
        packet.getDataValueCollectionModifier().write(0, Arrays.asList(wrappedDataValue));
        pm.sendServerPacket(player, packet);
    }
    public static void sendColor(Player player, Entity entity, String color) {
        if(entity.isDead() || !entity.isValid()){
            return;
        }
        int team_created = 0;
        int team_removed = 1;
        int team_updated = 2;
        int players_added = 3;
        int players_removed = 4;
        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = pm.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        packet.getIntegers().write(0, players_added);
        packet.getStrings().write(0, "LD_Color_" + color);

        if (entity instanceof Player) {
            packet.getSpecificModifier(Collection.class).write(0, Arrays.asList(entity.getName()));
        } else {
            packet.getSpecificModifier(Collection.class).write(0, Arrays.asList(entity.getUniqueId().toString()));
        }
        pm.sendServerPacket(player, packet);
    }
    public static void sendTeamCreate(Player player, Entity entity, String color) {
        if(entity.isDead()){
            return;
        }
        int team_created = 0;
        int team_removed = 1;
        int team_updated = 2;
        int players_added = 3;
        int players_removed = 4;
        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = pm.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        packet.getIntegers().write(0, team_created);
        packet.getStrings().write(0, "LD_Color_" + color);

        Optional<InternalStructure> optional = packet.getOptionalStructures().read(0);
        if (optional.isPresent()) {
            InternalStructure internalStructure = optional.get();
            internalStructure.getEnumModifier(ChatColor.class, MinecraftReflection.getMinecraftClass("EnumChatFormat")).write(0, ChatColor.getByChar(color));
            packet.getOptionalStructures().write(0, Optional.of(internalStructure));
        }
        if (entity instanceof Player) {
            packet.getSpecificModifier(Collection.class).write(0, Arrays.asList(entity.getName()));
        } else {
            packet.getSpecificModifier(Collection.class).write(0, Arrays.asList(entity.getUniqueId().toString()));
        }
        pm.sendServerPacket(player, packet);
    }
    public static void sendTeamRemove(Player player, Entity entity, String color) {
        int team_created = 0;
        int team_removed = 1;
        int team_updated = 2;
        int players_added = 3;
        int players_removed = 4;
        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = pm.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        packet.getIntegers().write(0, players_removed);
        packet.getStrings().write(0, "LD_Color_" + color);
        if (entity instanceof Player) {
            packet.getSpecificModifier(Collection.class).write(0, Arrays.asList(entity.getName()));
        } else {
            packet.getSpecificModifier(Collection.class).write(0, Arrays.asList(entity.getUniqueId().toString()));
        }
        pm.sendServerPacket(player, packet);
    }
}
