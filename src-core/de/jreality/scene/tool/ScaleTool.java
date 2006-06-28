/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ?AS IS?
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
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
    SceneGraphComponent scale = (isMoveChildren() ? tc.getRootToLocal() : tc.getRootToToolComponent()).getLastComponent();

    Matrix shipMatrix = new Matrix();
    if (scale.getTransformation() != null) shipMatrix.assignFrom(scale.getTransformation());

    double dt = slotPolling ? 1 : tc.getAxisState(timerSlot).intValue()*0.001;
    dt *=acceleration(tc);
    MatrixBuilder.euclidean(shipMatrix).scale(1+(factor*dt*gain)).assignTo(scale);
  }

  private double acceleration(ToolContext tc) {
    try {
      if (tc.getAxisState(InputSlot.getDevice("Acceleration")).isPressed()) return 50;
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

public boolean isMoveChildren() {
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
