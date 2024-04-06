package org.EIQUI.GCBAPI.ModelEngine;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.BlueprintAnimation;
import com.ticxo.modelengine.api.animation.handler.AnimationHandler;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.entity.data.BukkitEntityData;
import com.ticxo.modelengine.api.generator.blueprint.BlueprintBone;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.nms.entity.wrapper.BodyRotationController;
import com.ticxo.modelengine.api.utils.OffsetMode;
import org.EIQUI.GCBAPI.Core.CC.Timestop;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.*;

import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static org.EIQUI.GCBAPI.main.that;

public class MEAPI {
    private static final List<String> PLAYERLIMB = List.of("head", "body", "left_arm", "right_arm", "right_leg", "left_leg");
    public static ModeledEntity addModel(Entity e, String modelID) {
        return addModel(e,modelID,true);
    }
    public static ModeledEntity addModel(Entity e, String modelID,boolean b) {
        return addModel(e,modelID,b,true);
    }
    public static ModeledEntity addModel(Entity e, String modelID,boolean b, boolean auto) {
        if (!isValidEntity(e) || !isValidModelID(modelID)) {
            return null;
        }
        boolean isPlayer = e.getType().equals(EntityType.PLAYER);
        ModeledEntity modeledEntity;
        if(isPlayer && ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            if(ModelEngineAPI.getModeledEntity(e).getModels().size() == 0){
                ModelEngineAPI.getModeledEntity(e).destroy();
            }
            modeledEntity = ModelEngineAPI.createModeledEntity(e);
        }else{
            modeledEntity = ModelEngineAPI.getOrCreateModeledEntity(e);
        }

        ActiveModel activeModel = ModelEngineAPI.createActiveModel(ModelEngineAPI.getBlueprint(modelID));
        activeModel.setAutoRendererInitialization(auto);
        if (isPlayer) {
            modeledEntity.addModel(activeModel, false);
            ((BukkitEntityData) modeledEntity.getBase().getData()).getTracked().addForcedPairing((Player) e);
        } else {
            modeledEntity.addModel(activeModel, b);
            modeledEntity.setBaseEntityVisible(false);
            setDamageTint(e,modelID,255,255,255);
        }
        modeledEntity.getBase().setRenderRadius(160);
        return modeledEntity;
    }
    private static boolean isValidEntity(Entity e) {
        return e != null && e.isValid() && !e.isDead();
    }
    protected static boolean isValidModelID(String modelID) {
        return ModelEngineAPI.getBlueprint(modelID) != null;
    }
    public static boolean hasModel(Entity e,String modelID){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return false;
        }
        Optional<ActiveModel> activeModel = ModelEngineAPI.getModeledEntity(e).getModel(modelID);
        return activeModel.isPresent();
    }

    public static void removeModel(Entity e,String modelID){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        Optional<ActiveModel> activeModel = modeledEntity.getModel(modelID);
        modeledEntity.removeModel(modelID);
        if(activeModel.isPresent()){
            ActiveModel am = activeModel.get();
            am.setRemoved(true);
            am.destroy();
        }
        if(modeledEntity.getModels().size() == 0){
            modeledEntity.destroy();
            ModelEngineAPI.removeModeledEntity(e);
        }
    }
    public static void removeModelAll(Entity e){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        for(ActiveModel activeModel : modeledEntity.getModels().values()){
            if(!activeModel.isRemoved()){
                activeModel.setRemoved(true);
                activeModel.destroy();
            }
        }
        for(String modelid : modeledEntity.getModels().keySet()){
            modeledEntity.removeModel(modelid);
        }
        modeledEntity.destroy();
        ModelEngineAPI.removeModeledEntity(e);
    }

    public static void setHitbox(Entity e,String modelID,boolean b){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        Optional<ActiveModel> activeModel = modeledEntity.getModel(modelID);
        if (activeModel.isEmpty()) return;
        activeModel.get().setMainHitbox(b);
    }
    public static void playAnimation(Entity e,String modelID,String ani,double li,double lo,double sp,boolean b){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        Optional<ActiveModel> activeModel = modeledEntity.getModel(modelID);
        if(activeModel.isEmpty()) {
            Map<String, ActiveModel> tempmodels = modeledEntity.getModels();
            if(tempmodels.size() == 0){
                return;
            }
            activeModel = tempmodels.values().stream().findAny();
        }
        AnimationHandler handler = activeModel.get().getAnimationHandler();
        if(handler == null) return;
        if(!b && handler.isPlayingAnimation(ani)){
            IAnimationProperty aniobject = handler.getAnimation(ani);
            if(!aniobject.isFinished() && aniobject.getLoopMode().equals(BlueprintAnimation.LoopMode.LOOP)){
                aniobject.setSpeed(sp);
                return;
            }
        }
        handler.playAnimation(ani,li,lo,sp,b);
        manageAnimationSpeedDuringTimestop(e,activeModel.get(),ani,sp);
    }
    private static void manageAnimationSpeedDuringTimestop(Entity e,ActiveModel activeModel, String ani,double sp) {
        new BukkitRunnable() {
            boolean isTimestopped = false;
            final AnimationHandler handler = activeModel.getAnimationHandler();
            @Override
            public void run() {
                if (!handler.isPlayingAnimation(ani) || activeModel.isDestroyed() || activeModel.isRemoved()) {
                    if(handler.getAnimation(ani) != null){
                        handler.getAnimation(ani).setSpeed(sp);
                    }
                    cancel();
                    return;
                }
                if (Timestop.isTimestopped(e)) {
                    if (!isTimestopped) {
                        isTimestopped = true;
                        handler.getAnimation(ani).setSpeed(0);
                    }
                } else if (isTimestopped) {
                    isTimestopped = false;
                    handler.getAnimation(ani).setSpeed(sp);
                }
            }
        }.runTaskTimer(that, 0L, 1);
    }
    public static void stopAnimation(Entity e,String modelID,String ani){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        Optional<ActiveModel> activeModel = modeledEntity.getModel(modelID);
        if(activeModel.isEmpty()) {
            Map<String, ActiveModel> tempmodels = modeledEntity.getModels();
            if(tempmodels.size() == 0){
                return;
            }
            activeModel = tempmodels.values().stream().findAny();
        }
        activeModel.get().getAnimationHandler().stopAnimation(ani);
    }

    public static Collection<String> getPlayingAnimation(Entity e, String modelID){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return new ArrayList<>();
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        if(modeledEntity == null) return new ArrayList<>();
        Optional<ActiveModel> activeModel = modeledEntity.getModel(modelID);
        if (activeModel.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(activeModel.get().getAnimationHandler().getAnimations().keySet());
    }

    public static void setAnimationSpeed(Entity e,String modelID,String ani,double sp){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        Optional<ActiveModel> activeModel = modeledEntity.getModel(modelID);
        if (activeModel.isEmpty()) return;
        IAnimationProperty animationobj = activeModel.get().getAnimationHandler().getAnimation(ani);
        if(animationobj == null) return;
        animationobj.setSpeed(sp);
    }

    public static void setSkin(Entity e, String modelID, @Nullable String boneID, OfflinePlayer p){
        setSkin(e,modelID,boneID,p.getPlayer());
    }
    public static void setSkin(Entity e, String modelID, @Nullable String boneID, Player p){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        Optional<ActiveModel> activeModel = modeledEntity.getModel(modelID);
        if (activeModel.isEmpty()) return;
        if(boneID == null){
            boneID = "all";
        }
        boneID = boneID.toLowerCase();
        if(!PLAYERLIMB.contains(boneID)){
            boneID = "all";
        }
        if(!boneID.equals("all")){
            Optional<ModelBone> bone = activeModel.get().getBone(boneID);
            if (bone.isEmpty()) return;
            bone.get().getBoneBehavior(BoneBehaviorTypes.PLAYER_LIMB).get().setTexture(p);
        }else{
            for(String id : PLAYERLIMB){
                Optional<ModelBone> bone = activeModel.get().getBone(id);
                if (!bone.isPresent()) return;
                bone.get().getBoneBehavior(BoneBehaviorTypes.PLAYER_LIMB).get().setTexture(p);
            }
        }
    }

    public static Location getBoneLocation(Entity e, String modelID,String boneID){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return null;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        Optional<ActiveModel> activeModel = modeledEntity.getModel(modelID);
        if (activeModel.isEmpty()) return null;
        Optional<ModelBone> bone = activeModel.get().getBone(boneID);
        if (bone.isEmpty()) return null;
        Location bonepos = bone.get().getLocation(OffsetMode.GLOBAL,new Vector3f(),false);
        Quaternionf bonerot = bone.get().getTrueGlobalLeftRotation();

        float pitch = (float) atan2(2 * (bonerot.w * bonerot.x + bonerot.y * bonerot.z),
                1 - 2 * (bonerot.x * bonerot.x + bonerot.y * bonerot.y));
        float yaw = (float) asin(2 * (bonerot.w * bonerot.y - bonerot.z * bonerot.x));

        bonepos.setYaw(yaw);
        bonepos.setPitch(pitch);
        return bonepos;
    }

    public static void setVisible(Entity e,Player p,boolean vis){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        BukkitEntityData data = (BukkitEntityData) modeledEntity.getBase().getData();
        if(vis){
            data.getTracked().removeForcedHidden(p);
        }else{
            data.getTracked().addForcedHidden(p);
        }
    }

    public static void setLook(Entity e,double x,double y, double z){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        modeledEntity.getBase().getLookController().lookAt(x,y,z);
    }
    public static void setPitch(Entity e,double pitch){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        modeledEntity.getBase().getLookController().setPitch((float) pitch);
    }
    public static void setXHeadRot(Entity e,double pitch){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        modeledEntity.getBase().getBodyRotationController().setXHeadRot((float) pitch);
    }
    public static void setYHeadRot(Entity e,double pitch){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        modeledEntity.getBase().getBodyRotationController().setYHeadRot((float) pitch);
    }
    public static void setNoRotDelay(Entity e){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        BodyRotationController bcont = modeledEntity.getBase().getBodyRotationController();
        bcont.setRotationDuration(0);
        bcont.setRotationDelay(0);
        bcont.setStableAngle(0);
        bcont.setMinBodyAngle(0);
        bcont.setMaxBodyAngle(0);
        bcont.setMinHeadAngle(0);
        bcont.setMaxHeadAngle(0);
    }
    public static void setYBodyRot(Entity e,double yaw){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        modeledEntity.getBase().getBodyRotationController().setYBodyRot((float) yaw);
    }

    public static void changePart(Entity e, String modelID,String boneID,String newmodelID,String newboneID){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        Optional<ActiveModel> activeModel = modeledEntity.getModel(modelID);
        if (activeModel.isEmpty()) return;
        Optional<ModelBone> bone = activeModel.get().getBone(boneID);
        if (bone.isEmpty()) return;
        ModelBlueprint newPrint = ModelEngineAPI.getBlueprint(newmodelID);
        if(newPrint == null) return;
        BlueprintBone newbone = newPrint.getFlatMap().get(newboneID);
        bone.get().setModelScale(newbone.getScale());
        bone.get().setModel(newbone.getDataId());
    }
    public static Collection<String> getAllBonesName(Entity e, String modelID){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return new ArrayList<>();
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        Optional<ActiveModel> activeModel = modeledEntity.getModel(modelID);
        if (activeModel.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(activeModel.get().getBones().keySet());
    }
    public static void setBoneVisible(Entity e,String modelID,String boneID,boolean b){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        Optional<ActiveModel> activeModel = modeledEntity.getModel(modelID);
        if (activeModel.isEmpty()) return;
        Optional<ModelBone> bone = activeModel.get().getBone(boneID);
        if (bone.isEmpty()) return;
        bone.get().setVisible(b);
    }

    public static void setColor(Entity e,String modelID,String boneID,int r,int g ,int b, boolean em){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        Optional<ActiveModel> activeModel = modeledEntity.getModel(modelID);
        if (activeModel.isEmpty()) return;
        Map<String, ModelBone> bones = activeModel.get().getBones();
        Color color = Color.fromRGB(r,g,b);
        if(em){
            if(bones.containsKey(boneID)){
                bones.get(boneID).setDefaultTint(color);
            }
        }else{
            for (ModelBone ids : bones.values()){
                ids.setDefaultTint(color);
            }
        }
    }

    public static void setDamageTint(Entity e,String modelID,int r,int g ,int b){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        Optional<ActiveModel> activeModel = modeledEntity.getModel(modelID);
        if (activeModel.isEmpty()) return;
        Color color = Color.fromRGB(r,g,b);
        activeModel.get().setDamageTint(color);
    }

    public static void setGlowing(Entity e,String modelID,boolean b){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        Optional<ActiveModel> activeModel = modeledEntity.getModel(modelID);
        if (activeModel.isEmpty()) return;
        activeModel.get();
    }

    public static void setBoneBehavior(Entity e,String modelID){
        if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
        Optional<ActiveModel> activeModel = modeledEntity.getModel(modelID);
        if (activeModel.isEmpty()) return;
        ModelBone d;
    }

    public static boolean mountUsingTitanContoller(Entity driver,Entity vehicle,@Nullable String modelID){
        if(!ModelEngineAPI.isModeledEntity(vehicle.getUniqueId()) || driver.isDead()
                || vehicle.isDead() || !driver.isValid() || !vehicle.isValid()){
            return false;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(vehicle);
        Optional<ActiveModel> activeModel;
        if (modelID == null){
            activeModel = modeledEntity.getModels().values().stream().findFirst();
        }else{
            activeModel = modeledEntity.getModel(modelID);
        }
        if (activeModel.isEmpty()) return false;
        activeModel.get().getMountManager().ifPresent(mountManager -> {
            mountManager.mountDriver(driver, METitanMountController.CUSTOM);
        });
        return true;
    }
}
