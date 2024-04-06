package org.EIQUI.GCBAPI.ModelEngine;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.data.BukkitEntityData;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.nms.entity.wrapper.BodyRotationController;
import com.ticxo.modelengine.api.nms.entity.wrapper.LookController;
import org.EIQUI.GCBAPI.Util;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.EIQUI.GCBAPI.main.that;

public class Model {
    public static class Data {
        public UUID Uuid;
        public Vector direction;
        public ActiveModel activeModel;
        public String modelId = "";

        public Data(ActiveModel activeModel, String modelId,UUID modelUuid) {
            this.Uuid = modelUuid;
            this.activeModel = activeModel;
            this.modelId = modelId;
        }

    }
    private static final Map<Entity, Data> MODELDATA = new ConcurrentHashMap<>();

    public static boolean hasModel(Entity e) {
        return ModelEngineAPI.isModeledEntity(e.getUniqueId()) && MODELDATA.containsKey(e) ;
    }

    public static void add(Entity e, String modelID, UUID uuid) {
        if (MODELDATA.containsKey(e) && MODELDATA.get(e).modelId.equals(modelID) ) {
            Data data = MODELDATA.get(e);
            data.direction = null;
            data.Uuid = uuid;
            setSelfVisible(e, true);
            return;
        }
        remove(e,UUID.randomUUID(),true);

        ModeledEntity modeledEntity = MEAPI.addModel(e, modelID,true,false);
        ActiveModel activeModel = modeledEntity.getModel(modelID).get();
        activeModel.setAutoRendererInitialization(false);
        Data datat = new Data(activeModel,modelID,uuid);
        MODELDATA.put(e,datat);
        BodyRotationController bodyRotationController = activeModel.getModeledEntity().getBase().getBodyRotationController();
        bodyRotationController.setPlayerMode(true);
        LookController lookController = activeModel.getModeledEntity().getBase().getLookController();

        new BukkitRunnable() {
            boolean task = false;
            Data data = datat;
            ActiveModel am = activeModel;
            boolean tick = true;
            @Override
            public void run() {
                if(tick){
                    tick = false;
                    am.initializeRenderer();
                }
                if(e.isDead() || !e.isValid() || !(MODELDATA.containsKey(e) && MODELDATA.get(e).modelId.equals(modelID))
                       || data == null || am == null || am.isRemoved() || am.isDestroyed()){
                    if(am != null){
                        am.setRemoved(true);
                        am.destroy();
                    }
                    cancel();
                    return;
                }
                data = MODELDATA.get(e);
                am = data.activeModel;

                if(data.direction != null && !data.direction.isZero()){
                    if(task){
                        task = false;
                        bodyRotationController.setRotationDelay(99999999);
                        bodyRotationController.setRotationDuration(99999999);
                        am.setLockYaw(true);
                        am.setLockPitch(true);
                    }
                    Vector v = data.direction;
                    float yaw = Util.getPitch(v);
                    lookController.setPitch(Util.getPitch(v));
                    bodyRotationController.setYBodyRot(yaw);
                    lookController.setHeadYaw(yaw);
                    lookController.setBodyYaw(yaw);
                }else{
                    if(!task){
                        task = true;
                        am.setLockYaw(false);
                        am.setLockPitch(false);
                    }
                    float yaw = e.getLocation().getYaw();
                    lookController.setPitch(e.getLocation().getPitch());
                    bodyRotationController.setYBodyRot(yaw);
                    lookController.setHeadYaw(yaw);
                    lookController.setBodyYaw(yaw);
                }
            }
        }.runTaskTimer(that, 0L, 1);
    }


    public static boolean remove(Entity e, UUID uuid, boolean ignoreuuid) {
        if (ignoreuuid || (MODELDATA.containsKey(e) && MODELDATA.get(e).Uuid.equals(uuid))) {
            MEAPI.removeModel(e,getModelID(e));
            if( MODELDATA.get(e) != null){
                ActiveModel am = MODELDATA.get(e).activeModel;
                am.setRemoved(true);
                am.destroy();
            }
            MODELDATA.remove(e);

            return true;
        }
        return false;
    }

    public static UUID getModelUUID(Entity e) {
        if (hasModel(e)) {
            return MODELDATA.get(e).Uuid;
        }
        return null;
    }

    public static String getModelID(Entity e) {
        if (hasModel(e)) {
            return MODELDATA.get(e).modelId;
        }
        return "";
    }

    public static void playAnimation(Entity e,String ani,double li,double lo,double sp,boolean force,boolean removeprevanimation){
        if(!hasModel(e)){
            return;
        }
        String modelID = MODELDATA.get(e).modelId;
        if(removeprevanimation){
            for(String aid : MEAPI.getPlayingAnimation(e,modelID)){
                MEAPI.stopAnimation(e,modelID,aid);
            }
        }
        MEAPI.playAnimation(e,modelID,ani,li,lo,sp,force);
    }

    public static void setDirection(Entity e,@Nullable Vector v) {
        if (hasModel(e)) {
            MODELDATA.get(e).direction = v;
        }
    }

    public static void setDirection(Entity e, double yaw, double pitch) {
        if (hasModel(e)) {
            MODELDATA.get(e).direction = Util.getVector(yaw, pitch);
        }
    }

    public static void setSelfVisible(Entity e, boolean vis) {
        if (!hasModel(e) || !e.getType().equals(EntityType.PLAYER)) {
            return;
        }
        MEAPI.setVisible(e, (Player) e, vis);
        if (vis) {
            BukkitEntityData data = (BukkitEntityData) ModelEngineAPI.getModeledEntity(e).getBase().getData();
            data.getTracked().addForcedPairing((Player) e);
        }
    }
}
