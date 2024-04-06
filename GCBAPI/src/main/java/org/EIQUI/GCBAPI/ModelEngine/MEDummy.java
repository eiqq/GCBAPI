package org.EIQUI.GCBAPI.ModelEngine;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Timer;
import java.util.TimerTask;

public class MEDummy extends MEAPI{

    public static Dummy createDummyModel(Location l,String modelID, boolean b) {
        if (!isValidModelID(modelID)) {
            return null;
        }
        Dummy<?> dummy = new Dummy<>();
        dummy.setVisible(false);
        dummy.syncLocation(l);
        dummy.setRenderRadius(160);
        dummy.setDetectingPlayers(false);
        ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(dummy);
        ActiveModel activeModel = ModelEngineAPI.createActiveModel(ModelEngineAPI.getBlueprint(modelID));
        modeledEntity.addModel(activeModel, b);
        modeledEntity.setBaseEntityVisible(false);
        dummy.setVisible(true);
        return dummy;
    }
    public static void appendToModeledEntity(Dummy dummy, Entity e){
       if(!ModelEngineAPI.isModeledEntity(e.getUniqueId())){
           return;
       }
       appendToModeledEntity(dummy,ModelEngineAPI.getModeledEntity(e));
    }
    public static void appendToModeledEntity(Dummy dummy, ModeledEntity me){
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if(me.isDestroyed() || !me.getBase().isAlive() || me.getBase().isRemoved()){
                    cancel();
                    remove(dummy);
                    return;
                }
                dummy.syncLocation(me.getBase().getLocation());
            }
        };
        long delay = 0;
        long intervalPeriod = 25;
        timer.scheduleAtFixedRate(task, delay, intervalPeriod);
    }
    public static void setLocation(Dummy dummy, Location l){
        dummy.setLocation(l);
    }
    public static void syncLocation(Dummy dummy, Location l){
        dummy.syncLocation(l);
    }

    public static void setVisible(Dummy dummy, Player p, boolean vis){
        if(!ModelEngineAPI.isModeledEntity(dummy.getUUID())){
            return;
        }
        dummy.setForceHidden(p,!vis);
        dummy.setForceViewing(p,vis);
    }

    public static void remove(Dummy e){
        e.setRemoved(true);
    }
}
