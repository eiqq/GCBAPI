package org.EIQUI.GCBAPI.ModelEngine;

import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.manager.BehaviorManager;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.api.model.bone.type.Mount;
import com.ticxo.modelengine.api.mount.controller.MountControllerType;
import com.ticxo.modelengine.api.mount.controller.impl.AbstractMountController;
import com.ticxo.modelengine.api.nms.entity.wrapper.LookController;
import com.ticxo.modelengine.api.nms.entity.wrapper.MoveController;
import org.EIQUI.GCBAPI.Core.CC.Timestop;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Optional;

public class METitanMountController extends AbstractMountController {
    // Type class used to specify the controller when mounting entity
    // NEVER SUPPLY A STATIC INSTANCE
    public static final MountControllerType CUSTOM = new MountControllerType(METitanMountController::new);
    public METitanMountController(Entity entity, Mount mount) {
        super(entity, mount);
        this.setCanInteractMount(false);
        this.setCanDamageMount(false);
    }

    @Override
    public void updateDirection(LookController controller, ActiveModel model) {
        if(Timestop.isTimestopped(this.getEntity())){
            return;
        }
        Location location = this.getEntity().getLocation();
        controller.setHeadYaw(location.getYaw());
        controller.setPitch(location.getPitch());
    }

    @Override
    public void updateDriverMovement(MoveController controller, ActiveModel model) {
        Optional maybeMount = model.getMountManager();
        if (!maybeMount.isEmpty()) {
            BehaviorManager manager = (BehaviorManager)maybeMount.get();
            if (this.input.isSneak() || !entity.isValid() || entity.isDead()) {
                ((MountManager)manager).dismountDriver();
            } else {
                controller.move(this.input.getSide(), 0.0F, this.input.getFront(), 1.0F);
            }
        }
    }

    @Override
    public void updatePassengerMovement(MoveController controller, ActiveModel model) {
        Optional maybeMount = model.getMountManager();
        if (!maybeMount.isEmpty()) {
            BehaviorManager manager = (BehaviorManager)maybeMount.get();
            if (this.input.isSneak() || !entity.isValid() || entity.isDead()) {
                ((MountManager)manager).dismountRider(this.entity);
            }
        }
    }

}
