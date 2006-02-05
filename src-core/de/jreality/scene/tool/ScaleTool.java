/*
 * Created on May 30, 2005
 *
 * This file is part of the de.jreality.scene.tool package.
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
import de.jreality.scene.SceneGraphComponent;

/**
 * @author weissman
 *
 **/
public class ScaleTool extends Tool {
  
  InputSlot scaleActivation = InputSlot.getDevice("ScaleActivation");
  InputSlot scaleSlot = InputSlot.getDevice("ScaleAxis");
  InputSlot timerSlot = InputSlot.getDevice("SystemTime");
  List used = new LinkedList();
  List activation;
  
  double factor;
  boolean isScaling;
  
  boolean moveChilderen;
  boolean slotPolling;
  
  double gain=1;
  
  public ScaleTool() {
	  used.add(scaleSlot);
    activation = Collections.singletonList(scaleActivation);
  }
  
  public List getActivationSlots() {
    return activation;
  }

  public List getCurrentSlots() {
    return used;
  }

  public List getOutputSlots() {
    return Collections.EMPTY_LIST;
  }

  double[] tmp = new double[16];
  public void perform(ToolContext tc) {
    if (tc.getSource() == scaleSlot) {
      factor = tc.getAxisState(scaleSlot).doubleValue();
      factor = factor*factor*factor;
    }
    if (!slotPolling) {
    	if (tc.getSource() == scaleSlot) {
    		if (tc.getAxisState(scaleSlot).isReleased()) {
    			isScaling = false;
    			used.remove(timerSlot);
    			return;
    		}
    		if (!isScaling) {
    			isScaling = true;
    			used.add(timerSlot);
    		}
    		return;
    	}
    }
    SceneGraphComponent scale = (isMoveChilderen() ? tc.getRootToLocal() : tc.getRootToToolComponent()).getLastComponent();

    Matrix shipMatrix = new Matrix();
    if (scale.getTransformation() != null) shipMatrix.assignFrom(scale.getTransformation());

    double dt = slotPolling ? 1 : tc.getAxisState(timerSlot).intValue()*0.001;
    dt *=acceleration(tc);
    MatrixBuilder.euclidean(shipMatrix).scale(1+(factor*dt*gain)).assignTo(scale);
  }

  private double acceleration(ToolContext tc) {
    try {
      if (tc.getAxisState(InputSlot.getDevice("Accelertation")).isPressed()) return 50;
    } catch (Exception e) {
    }
    return 1;
  }

  public void activate(ToolContext tc) {
  }

  public void deactivate(ToolContext tc) {
  }

public double getGain() {
	return gain;
}

public void setGain(double gain) {
	this.gain = gain;
}

public boolean isMoveChilderen() {
  return moveChilderen;
}

public void setMoveChilderen(boolean moveChilderen) {
  this.moveChilderen = moveChilderen;
}

public boolean isSlotPolling() {
  return slotPolling;
}

public void setSlotPolling(boolean slotPolling) {
  this.slotPolling = slotPolling;
}

}
