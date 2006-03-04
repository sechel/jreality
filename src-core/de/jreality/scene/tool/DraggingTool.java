/*
 * Created on Apr 10, 2005
 *
 * This file is part of the jReality package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.scene.tool;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.LoggingSystem;


/**
 *
 * TODO: document this
 *
 * @author brinkman
 *
 */
public class DraggingTool extends Tool {

    transient List activationSlots = new LinkedList();
    transient List usedSlots = new LinkedList();
    
    private boolean moveChildren;
    transient private boolean dragInViewDirection;
    
    static InputSlot activationSlot = InputSlot.getDevice("DragActivation");
    static InputSlot alongPointerSlot = InputSlot.getDevice("DragAlongViewDirection");
    static InputSlot evolutionSlot = InputSlot.getDevice("PointerEvolution");
    
    public DraggingTool() {
        activationSlots.add(activationSlot);
        usedSlots.add(evolutionSlot);
        usedSlots.add(alongPointerSlot);
    }
    
    public List getActivationSlots() {
        return activationSlots;
    }

    public List getCurrentSlots() {
        return usedSlots;
    }

    public List getOutputSlots() {
        return Collections.EMPTY_LIST;
    }

    transient protected SceneGraphComponent comp;
    
    public void activate(ToolContext tc) {
      comp = (moveChildren ? tc.getRootToLocal() : tc.getRootToToolComponent()).getLastComponent();
      if (comp.getTransformation() == null) comp.setTransformation(new Transformation());
      try {
        if (tc.getAxisState(alongPointerSlot).isPressed()) {
          dragInViewDirection = true;
        }
        else {
          dragInViewDirection = false;
        }
      } catch (Exception me) {
        // no drag in zaxis
        dragInViewDirection = false;
      }
    }

    transient Matrix result = new Matrix();
    transient Matrix local2world = new Matrix();
    transient Matrix dragFrame;
    transient Matrix pointer = new Matrix();
    
    public void perform(ToolContext tc) {
      if (tc.getSource() == alongPointerSlot) {
        if (tc.getAxisState(alongPointerSlot).isPressed()) {
          dragInViewDirection = true;
        }
        else {
          dragInViewDirection = false;
        }
        return;
      }

      Matrix evolution = new Matrix(tc.getTransformationMatrix(evolutionSlot));
      
      (moveChildren ? tc.getRootToLocal():tc.getRootToToolComponent()).getMatrix(local2world.getArray());
      
      comp.getTransformation().getMatrix(result.getArray());
      
      if (dragInViewDirection) {
        tc.getTransformationMatrix(InputSlot.getDevice("CameraToWorld")).toDoubleArray(pointer.getArray());
        double dz = evolution.getEntry(0,3)+evolution.getEntry(1,3);
        evolution.assignIdentity();
        evolution.setColumn(3, Rn.times(null, dz, pointer.getColumn(2)));
        evolution.setEntry(3,3,1);
      }
      
      result.multiplyOnRight(local2world.getInverse());
      result.multiplyOnRight(evolution);
      result.multiplyOnRight(local2world);
      
      enforceConstraints(result);
      
      comp.getTransformation().setMatrix(result.getArray());
    }

    public void enforceConstraints(Matrix matrix) {
    	// do nothing, for now
    }
    
    public void deactivate(ToolContext tc) {
        //  do nothing
    }
    public boolean getMoveChildren() {
      return moveChildren;
    }
    public void setMoveChildren(boolean moveChildren) {
      this.moveChildren = moveChildren;
    }

}
