package org.EIQUI.GCBAPI.Core;

import org.EIQUI.GCBAPI.HealthbarAPI;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class GCBNameTag {
    private Entity assignedEntity;
    private Display nametagEntity;
    private UUID nametagEntityUUID;
    private String nameid;
    private Location lastLocationTracked;
    private double mountYOffset = 0.5;
    private boolean isHPNametag = false;
    private boolean isVisible = true;
    private int hpWidth = 40;
    private static final Map<Entity, Set<GCBNameTag>> NAMETAGS = new ConcurrentHashMap<>();
    private static final Map<Entity, LocalTime> LAST_VIEWTICK_TIME = new ConcurrentHashMap<>();
    private static final int VIEWTICK_COOLDOWN = 225;

    public GCBNameTag(Entity e, String name, @Nullable String text){
        e.getLocation().getChunk().load();
        assignedEntity = e;
        nametagEntity = (Display) e.getWorld().spawnEntity(e.getLocation()
                .add(0,e.getHeight(),0), EntityType.TEXT_DISPLAY);
        nametagEntityUUID = nametagEntity.getUniqueId();
        lastLocationTracked = nametagEntity.getLocation();
        nametagEntity.setBillboard(Display.Billboard.CENTER);
        ((TextDisplay)nametagEntity).setBackgroundColor(Color.fromRGB(0,0,0));
        ((TextDisplay)nametagEntity).setSeeThrough(false);
        ((TextDisplay)nametagEntity).setText(text);
        ((TextDisplay)nametagEntity).setAlignment(TextDisplay.TextAlignment.CENTER);
        nametagEntity.setTeleportDuration(2);
        nameid = name;
        assignedEntity.addPassenger(nametagEntity);
        NAMETAGS.computeIfAbsent(assignedEntity, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
        NAMETAGS.get(assignedEntity).add(this);
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
        return nametagEntity != null && assignedEntity != null && nametagEntity.isValid() && assignedEntity.isValid() &&
                nametagEntity.getUniqueId().equals(nametagEntityUUID);
    }
    private boolean tick(){
        if(!isValid()){
            remove();
            return false;
        }
        if(!lastLocationTracked.equals(nametagEntity.getLocation())){
            updateNameTagPosition();
        }
        lastLocationTracked = nametagEntity.getLocation();
        return true;
    }

    private void updateNameTagPosition() {
        if(!assignedEntity.equals(nametagEntity.getVehicle()) || !assignedEntity.getWorld().equals(nametagEntity.getWorld())){
            nametagEntity.eject();
            Location l = assignedEntity.getLocation().add(0,mountYOffset,0);
            l.setPitch(nametagEntity.getLocation().getPitch());
            l.setYaw(nametagEntity.getLocation().getYaw());
            nametagEntity.teleport(l);
            assignedEntity.addPassenger(nametagEntity);
        }else{
            mountYOffset = nametagEntity.getLocation().getY() - assignedEntity.getLocation().getY();
            assignedEntity.removePassenger(nametagEntity);
            assignedEntity.addPassenger(nametagEntity);
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
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!isVisible || !this.viewCondition(p)) {
                p.hideEntity(that, nametagEntity);
            } else {
                updateNameTagPosition();
                p.showEntity(that, nametagEntity);
            }
        }
    }

    private boolean viewCondition(Player otherPlayer){
        if(assignedEntity.equals(otherPlayer)){
            return false;
        }
        return otherPlayer.canSee(assignedEntity);
    }
    private void remove(){
        if (lastLocationTracked == null) {
            lastLocationTracked = nametagEntity.getLocation();
        }
        lastLocationTracked.getChunk().load(true);
        if(NAMETAGS.get(assignedEntity) != null){
            NAMETAGS.get(assignedEntity).remove(this);
        }
        nametagEntity.remove();
        if(NAMETAGS.get(assignedEntity).isEmpty()){
            NAMETAGS.get(assignedEntity).clear();
        }
    }

    //-----------------------------------------------------------------------------
    public static GCBNameTag createNameTag(Entity e, String name, String text){
        remove(e,name);
        GCBNameTag a = new GCBNameTag(e,name,text);
        a.startTick();
        return a;
    }

    public static Entity getNameTagEntity(GCBNameTag nametag){
        return nametag.nametagEntity;
    }

    public static GCBNameTag getNameTagByName(Entity e, String text){
        if(NAMETAGS.containsKey(e)) {
            for (GCBNameTag cos : NAMETAGS.get(e)) {
                if (cos.nameid.equals(text)) {
                    return cos;
                }
            }
        }
        return null;
    }

    public static Collection<GCBNameTag> getAllNameTags(Entity e){
        return new ArrayList<>(NAMETAGS.getOrDefault(e, Collections.emptySet()));
    }

    public static void setVisible(GCBNameTag nametag,boolean b){
        if(b){
            nametag.isVisible = true;
            nametag.viewTick();
        }else{
            nametag.isVisible = false;
            for(Player p : Bukkit.getOnlinePlayers()){
                p.hideEntity(that,nametag.nametagEntity);
            }
            nametag.viewTick();
        }
    }

    public static void setText(@Nullable GCBNameTag nametag,@Nullable String s){
        if (nametag == null){
            return;
        }
        if(s == null || s.isBlank() || s.isEmpty()){
            s = "";
        }
        TextDisplay td = (TextDisplay) Bukkit.getEntity(nametag.nametagEntityUUID);
        if(td == null){
            nametag.remove();
            return;
        }
        td.setText(s);
    }

    public static void hideFor(GCBNameTag nametag, Player p){
        p.hideEntity(that,nametag.nametagEntity);
    }

    public static void showFor(GCBNameTag nametag, Player p){
        nametag.updateNameTagPosition();
        p.showEntity(that,nametag.nametagEntity);
    }

    public static void update(GCBNameTag nametag){
        nametag.updateNameTagPosition();
        nametag.viewTick();
        nametag.tick();
    }

    public static void updateAll(Entity e){
        if(NAMETAGS.containsKey(e)){
            for(GCBNameTag nameTag : getAllNameTags(e)){
                update(nameTag);
            }
        }
    }

    public static void viewTickAll(Entity e){
        if(NAMETAGS.containsKey(e)){
            for(GCBNameTag tag : getAllNameTags(e)){
                tag.viewTick();
            }
        }
    }

    public static boolean hasNameTag(@Nullable Entity e, String name){
        if(e == null || !NAMETAGS.containsKey(e)){
            return false;
        }
        for(GCBNameTag nameTag : NAMETAGS.get(e)){
            if(nameTag.nameid.equals(name)){
                return true;
            }
        }
        return false;
    }

    public static void setToHPNameTag(GCBNameTag tag, int length){
        if(!tag.assignedEntity.getType().isAlive()){
            return;
        }
        tag.isHPNametag = true;
        tag.hpWidth = length;
        ((TextDisplay)tag.nametagEntity).setAlignment(TextDisplay.TextAlignment.RIGHT);
        new BukkitRunnable() {
            Entity a = tag.nametagEntity;
            Entity b = tag.assignedEntity;
            @Override
            public void run() {
                if(!a.isValid() || !b.isValid()){
                    cancel();
                    return;
                }
                ((TextDisplay)tag.nametagEntity).setText(
                        HealthbarAPI.getPlayerHealthBarText((LivingEntity) b, tag.hpWidth,
                                Shield.getTotalAmount(b)));
            }
        }.runTaskTimer(that, 0L, 20L);
    }
    public static void updateHPNameTag(Entity e){
        if(NAMETAGS.containsKey(e)) {
            for (GCBNameTag tag : NAMETAGS.get(e)) {
                if(tag.isHPNametag){
                    ((TextDisplay)tag.nametagEntity).setText(
                            HealthbarAPI.getPlayerHealthBarText((LivingEntity) tag.assignedEntity,
                                    tag.hpWidth,Shield.getTotalAmount(tag.assignedEntity)));
                    return;
                }
            }
        }
    }
    //-----------------------------------------------------------------------------

    public static void remove(Entity e,String name){
        if(!NAMETAGS.containsKey(e)){
            return;
        }
        for(GCBNameTag nametag : NAMETAGS.get(e)){
            if(nametag.nameid.equals(name)){
                nametag.remove();
            }
        }
    }

    public static void removeAll(Entity e){
        if(!NAMETAGS.containsKey(e)){
            return;
        }
        for(GCBNameTag nametag : NAMETAGS.get(e)){
            nametag.remove();
        }
    }

    public static void clear(){
        for(Entity e : NAMETAGS.keySet()){
            for(GCBNameTag nametag : NAMETAGS.get(e)){
                nametag.remove();
            }
        }
    }
    public static class GCBNameTagHandler implements Listener {
        public GCBNameTagHandler() {
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
            if(!NAMETAGS.containsKey(e.getPlayer())){
                return;
            }
            Collection<GCBNameTag> tags = getAllNameTags(e.getPlayer());
            if(tags.size() == 0){
                return;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    for(GCBNameTag nametag : tags){
                        nametag.updateNameTagPosition();
                        nametag.viewTick(false);
                        nametag.tick();
                    }
                }
            }.runTaskLater(that, 0);
        }
    }
}
