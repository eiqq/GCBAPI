package org.EIQUI.GCBAPI.Core.display;


import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;

public class TransSaver {
    public Vector3f Translation;
    public Quaternionf LeftRotation;
    public Vector3f Scale;
    public Quaternionf RightRotation;
    public int Interport;

    public TransSaver(@Nullable Vector3f trans,@Nullable Quaternionf left,@Nullable Vector3f s,@Nullable Quaternionf right,int interport){
        Translation = trans;
        LeftRotation = left;
        Scale = s;
        RightRotation = right;
        Interport = interport;
    }

    public void apply(Display e){
        if(!e.isValid()){
            return;
        }
        Transformation tf = e.getTransformation();

        Vector3f trans = (Translation == null) ? tf.getTranslation() : Translation;
        Quaternionf left = (LeftRotation == null) ? tf.getLeftRotation() : LeftRotation;
        Vector3f scale = (Scale == null) ? tf.getScale() : Scale;
        Quaternionf right = (RightRotation == null) ? tf.getRightRotation(): RightRotation;

        Transformation newTrans = new Transformation(trans,left,scale,right);
        e.setInterpolationDelay(-1);
        e.setInterpolationDuration(Interport);
        e.setTransformation(newTrans);
    }
}
