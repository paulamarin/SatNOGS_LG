package com.lglab.diego.simple_cms.create.utility.model;

import android.util.Log;

import com.lglab.diego.simple_cms.create.utility.model.balloon.Balloon;
import com.lglab.diego.simple_cms.create.utility.model.movement.Movement;
import com.lglab.diego.simple_cms.create.utility.model.poi.POI;
import com.lglab.diego.simple_cms.create.utility.model.shape.Shape;

import java.util.ArrayList;
import java.util.List;

/**
 * It is the class base of the action
 */
public abstract class Action {

    private long id;
    private int type;

    public Action(int type){
        this.type = type;
    }

    public Action(long id, int type){
        this.id = id;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
