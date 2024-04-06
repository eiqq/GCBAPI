package org.EIQUI.GCBAPI.Core;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class Costume {
    private Entity assignedEntity;
    private Entity costumeEntity;
    private ItemStack displayitem;
    private String nameid;
    private Location lastLocationTracked;
    private double savedYOffset = 2;
    private boolean isVisible = true;
    private static final Map<Entity, Set<Costume>> COSTUMES = new ConcurrentHashMap<>();
    private static final Map<Entity, LocalTime> LAST_VIEWTICK_TIME = new ConcurrentHashMap<>();
    private static final int VIEWTICK_COOLDOWN = 225;
    public Costume(Entity e,String name,ItemStack item){
        e.getLocation().getChunk().load();
        this.assignedEntity = e;
        this.costumeEntity = e.getWorld().spawnEntity(e.getLocation()
                .add(0,e.getHeight(),0), EntityType.ARMOR_STAND);
        lastLocationTracked = costumeEntity.getLocation();
        this.displayitem = item;
        this.nameid = name;
        ((ArmorStand)costumeEntity).getEquipment().setHelmet(this.displayitem);
        ((ArmorStand)costumeEntity).setMarker(true);
        ((ArmorStand)costumeEntity).setVisible(false);
        costumeEntity.setSilent(true);
        costumeEntity.setInvulnerable(true);
        e.addPassenger(this.costumeEntity);
        COSTUMES.computeIfAbsent(assignedEntity, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
        COSTUMES.get(this.assignedEntity).add(this);
    }
    private void startTick(){
        new BukkitRunnable() {
            @Override
            public void run() {
                if(!tick()){
                    remove();
                    cancel();
                    return;
                }
                viewTick();
            }
        }.runTaskTimer(that, 0L, 20L);
    }
    private boolean isValid(){
        return costumeEntity != null && assignedEntity != null && costumeEntity.isValid() && assignedEntity.isValid();
    }
    private boolean tick(){
        if(!isValid()){
            remove();
            return false;
        }
        if(!lastLocationTracked.equals(costumeEntity.getLocation())){
            updateCostumePosition();
        }
        lastLocationTracked = costumeEntity.getLocation();
        return true;
    }

    private void updateCostumePosition() {
        if(!assignedEntity.equals(costumeEntity.getVehicle()) || !assignedEntity.getWorld().equals(costumeEntity.getWorld())){
            costumeEntity.eject();
            Location l = assignedEntity.getLocation().add(0,savedYOffset,0);
            l.setPitch(costumeEntity.getLocation().getPitch());
            l.setYaw(costumeEntity.getLocation().getYaw());
            assignedEntity.addPassenger(costumeEntity);
        }else{
            savedYOffset = costumeEntity.getLocation().getY() - assignedEntity.getLocation().getY();
            assignedEntity.removePassenger(costumeEntity);
            assignedEntity.addPassenger(costumeEntity);
        }
    }
    private void viewTick(){
        viewTick(true);
    }
    private void viewTick(boolean force){
        LocalTime now = LocalTime.now(); // 현재 시간을 한 번만 계산
        if (!force && LAST_VIEWTICK_TIME.containsKey(assignedEntity) &&
                Duration.between(LAST_VIEWTICK_TIME.get(assignedEntity), now).toMillis() < VIEWTICK_COOLDOWN) {
            return;
        }
        LAST_VIEWTICK_TIME.put(assignedEntity, now);
        for(Player p : Bukkit.getOnlinePlayers()){
            if (!isVisible || !this.viewCondition(p)) {
                p.hideEntity(that,costumeEntity);
            }else{
                updateCostumePosition();
                p.showEntity(that,costumeEntity);
            }
        }
    }
    private boolean viewCondition(Player otherPlayer){
        if(assignedEntity.equals(otherPlayer)){
            if(assignedEntity instanceof Player && !((Player)assignedEntity).getGameMode().equals(GameMode.SPECTATOR)){
                return true;
            }
            return false;
        }
        return otherPlayer.canSee(assignedEntity);
    }
    private void remove(){
        if(lastLocationTracked == null){
            lastLocationTracked = assignedEntity.getLocation();
        }
        lastLocationTracked.getChunk().load(true);
        if(COSTUMES.get(assignedEntity) != null){
            COSTUMES.get(assignedEntity).remove(this);
        }
        costumeEntity.remove();
        if(COSTUMES.get(assignedEntity).isEmpty()){
            COSTUMES.get(assignedEntity).clear();
        }
    }

    //-----------------------------------------------------------------------------
    public static Costume createCostume(Entity e,String name,ItemStack item){
        remove(e,name);
        Costume a = new Costume(e,name,item);
        a.startTick();
        return a;
    }
    public static Entity getCostumeEntity(Costume cos){
        return cos.costumeEntity;
    }

    public static Costume getCostumeByName(Entity e,String name){
        if(COSTUMES.containsKey(e)) {
            for (Costume cos : COSTUMES.get(e)) {
                if (cos.nameid.equals(name)) {
                    return cos;
                }
            }
        }
        return null;
    }
    public static Collection<Costume> getAllCostumes(Entity e){
        return new ArrayList<>(COSTUMES.getOrDefault(e, Collections.emptySet()));
    }
    public static void setItem(Costume cos,ItemStack item){
        ((ArmorStand)cos.costumeEntity).getEquipment().setHelmet(item);
    }

    public static ItemStack getItem(Costume cos){
        return ((ArmorStand)cos.costumeEntity).getEquipment().getHelmet();
    }
    public static void addRotator(Costume cos,int period){
        if (period < 1){
            period = 1;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                if(!cos.costumeEntity.isValid()){
                    cancel();
                    return;
                }
                cos.costumeEntity.setRotation(cos.assignedEntity.getLocation().getYaw(),0);
            }
        }.runTaskTimer(that, 0L, period);
    }
    public static void addRotator(Costume cos){
        addRotator(cos,1);
    }
    public static void addPitchHider(Costume cos,double minpitch,double maxpitch,int period){
        if(!cos.assignedEntity.getType().equals(EntityType.PLAYER)){
            return;
        }
        if (period < 1){
            period = 1;
        }
        Player p = (Player)cos.assignedEntity;
        new BukkitRunnable() {
            @Override
            public void run() {
                if(!cos.costumeEntity.isValid()){
                    cancel();
                    return;
                }
                double pitch = p.getLocation().getPitch();
                if(pitch < minpitch || pitch > maxpitch){
                    hideFor(cos,p);
                }else{
                    showFor(cos,p);
                }
            }
        }.runTaskTimer(that, 0L, period);
    }
    public static void addPitchHider(Costume cos,double minpitch,double maxpitch){
        addPitchHider(cos,minpitch,maxpitch,1);
    }

    public static void setVisible(Costume cos,boolean b){
        if(b){
            cos.isVisible = true;
            cos.viewTick();
        }else{
            cos.isVisible = false;
            for(Player p : Bukkit.getOnlinePlayers()){
                p.hideEntity(that,cos.costumeEntity);
            }
            cos.viewTick();
        }
    }
    public static void hideFor(Costume cos,Player p){
        p.hideEntity(that,cos.costumeEntity);
    }
    public static void showFor(Costume cos,Player p){
        cos.updateCostumePosition();
        p.showEntity(that,cos.costumeEntity);
    }

    public static void update(Costume cos){
        cos.updateCostumePosition();
        cos.viewTick();
        cos.tick();
    }

    public static void updateAll(Entity e){
        if(COSTUMES.containsKey(e)){
            for(Costume cos : getAllCostumes(e)){
                update(cos);
            }
        }
    }

    public static void viewTickAll(Entity e){
        if(COSTUMES.containsKey(e)){
            for(Costume cos : getAllCostumes(e)){
                cos.viewTick();
            }
        }
    }

    public static boolean hasCostume(@Nullable Entity e, String name){
        if(e == null || !COSTUMES.containsKey(e)){
            return false;
        }
        for(Costume cos : COSTUMES.get(e)){
            if(cos.nameid.equals(name)){
                return true;
            }
        }
        return false;
    }
    //-----------------------------------------------------------------------------
    public static void remove(Entity e,String name){
        if(!COSTUMES.containsKey(e)){
            return;
        }
        for(Costume cos : COSTUMES.get(e)){
            if(cos.nameid.equals(name)){
                cos.remove();
            }
        }
    }

    public static void removeAll(Entity e){
        if(!COSTUMES.containsKey(e)){
            return;
        }
        for(Costume cos : COSTUMES.get(e)){
            cos.remove();
        }
    }

    public static void clear(){
        for(Entity e : COSTUMES.keySet()){
            for(Costume cos : COSTUMES.get(e)){
                cos.remove();
            }
        }
    }
    public static class CostumeHandler implements Listener {
        public CostumeHandler() {
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onDeath(EntityDeathEvent e) {
            if(e.getEntityType().equals(EntityType.PLAYER)){
                updateAll(e.getEntity());
            }else{
                removeAll(e.getEntity());
            }
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onQuit(PlayerQuitEvent e){
            e.getPlayer().getLocation().getChunk().load();
            removeAll(e.getPlayer());
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onReSpawn(PlayerRespawnEvent e){
            updateAll(e.getPlayer());
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onGamemodeChange(PlayerGameModeChangeEvent e){
            updateAll(e.getPlayer());
        }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onTeleport(PlayerTeleportEvent e){
            if(!COSTUMES.containsKey(e.getPlayer())){
                return;
            }
            Collection<Costume> costumes = getAllCostumes(e.getPlayer());
            if(costumes.size() == 0){
                return;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    for(Costume cos : costumes){
                        cos.updateCostumePosition();
                        cos.viewTick(false);
                        cos.tick();
                    }
                }
            }.runTaskLater(that, 0);

        }
    }
}
