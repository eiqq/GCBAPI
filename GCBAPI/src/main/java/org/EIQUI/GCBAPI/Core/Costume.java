package org.EIQUI.GCBAPI.Core;

import org.EIQUI.GCBAPI.Core.CC.Suspend;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class Costume {

    private Entity assignedEntity;
    private ArmorStand costumeEntity;
    private int modeldata;
    private ItemStack displayitem;
    private String nameid;

    private Location lastLocationTracked;
    private static final Map<Entity, Set<Costume>> COSTUMES = new ConcurrentHashMap<>();

    public Costume(Entity e,String nameid){
        this(e,0,nameid);
    }
    public Costume(Entity e, int model,String nameid){
        this.assignedEntity = e;
        this.modeldata = model;
        this.costumeEntity = (ArmorStand) e.getWorld().spawnEntity(e.getLocation()
                .add(0,e.getBoundingBox().getHeight()*0.75,0), EntityType.ARMOR_STAND);
        this.displayitem = new ItemStack(Material.SPLASH_POTION);
        ItemMeta meta = this.displayitem.getItemMeta();
        meta.setCustomModelData(this.modeldata);
        this.displayitem.setItemMeta(meta);
        this.nameid = nameid;
        this.costumeEntity.getEquipment().setHelmet(this.displayitem);
        this.costumeEntity.setMarker(true);
        this.costumeEntity.setVisible(false);
        this.costumeEntity.setSilent(true);
        this.costumeEntity.setInvulnerable(true);

        e.addPassenger(this.costumeEntity);

        if(COSTUMES.containsKey(this.assignedEntity)){
            for(Costume t :COSTUMES.get(this.assignedEntity)){
                if(t.nameid.equals(this.nameid)){
                    t.remove();
                }
            }
        }

        COSTUMES.computeIfAbsent(assignedEntity, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
        COSTUMES.get(this.assignedEntity).add(this);

        new BukkitRunnable() {
            @Override
            public void run() {
                if(!assignedEntity.isValid()){
                    remove();
                    cancel();
                    return;
                }
                lastLocationTracked = costumeEntity.getLocation();
            }
        }.runTaskTimer(that, 0L, 1L);
    }
    private void remove(){
        COSTUMES.get(this.assignedEntity).remove(this);
        lastLocationTracked.getChunk().load(true);
        this.costumeEntity.remove();
    }



    public static class CostumeHandler implements Listener {
        public CostumeHandler() {
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onDeath(EntityDeathEvent e) {
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onQuit(PlayerQuitEvent e){
        }
    }
}
