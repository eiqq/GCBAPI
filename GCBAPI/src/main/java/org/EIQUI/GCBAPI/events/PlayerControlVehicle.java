package org.EIQUI.GCBAPI.events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

import static org.EIQUI.GCBAPI.main.protocolManager;
import static org.EIQUI.GCBAPI.main.that;


public class PlayerControlVehicle extends Event{
    private static Set<Player> noJumpPacket = new HashSet<>();
    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;
    private Entity vehicle;
    private float side;
    private float foward;
    private boolean jumped;

    public PlayerControlVehicle(Player p,Entity vehicle,float side,float foward, boolean b) {
        player = p;
        this.vehicle = vehicle;
        this.side  = side;
        this.foward = foward;
        this.jumped = b;
    }

    public Player getPlayer(){
        return this.player;
    }
    public Entity getVehicle(){
        return this.vehicle;
    }
    public float getSide(){
        return this.side;
    }
    public float getFoward(){
        return this.foward;
    }
    public boolean isJumped(){return this.jumped;}

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static void setIgnoreJumpPacket(Player p,boolean b){
        if(b){
            noJumpPacket.add(p);
        }else{
            noJumpPacket.remove(p);
        }
    }
    public static void reset(){
        noJumpPacket.clear();
    }
    public static void registerProtocolLibPacketListener() {
        protocolManager.addPacketListener(new PacketAdapter(that, ListenerPriority.LOWEST, PacketType.Play.Client.STEER_VEHICLE) {
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
                Entity vehicle = player.getVehicle();
                float side = packet.getFloat().read(0);
                float front = packet.getFloat().read(1);
                boolean jd = packet.getBooleans().read(0);
                if(noJumpPacket.contains(player)){
                    packet.getBooleans().write(0,false);
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        PlayerControlVehicle mevent = new PlayerControlVehicle(player, vehicle,side,front,jd);
                        Bukkit.getPluginManager().callEvent(mevent);
                    }
                }.runTask(that);
            }
        });
    }

}
