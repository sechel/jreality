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

import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;

/**
 * @author weissman
 *
 **/
public class ShipRotateTool extends Tool {
  
  InputSlot leftRightSlot = InputSlot.getDevice("LeftRightAxis");
  InputSlot timerSlot = InputSlot.getDevice("SystemTime");
  List used = new LinkedList();
  
  double angle;
  boolean isRotating;
  
  double gain=1;
  
  public ShipRotateTool() {
	  used.add(leftRightSlot);
  }
  
  public List getActivationSlots() {
    return Collections.EMPTY_LIST;
  }

  public List getCurrentSlots() {
    return used;
  }

  public List getOutputSlots() {
    return Collections.EMPTY_LIST;
  }

  double[] tmp = new double[16];
  public void perform(ToolContext tc) {
	if (tc.getSource() == leftRightSlot) {
		angle = tc.getAxisState(leftRightSlot).doubleValue();
		angle = angle*angle*angle;
		if (tc.getAxisState(leftRightSlot).isReleased()) {
			isRotating = false;
			used.remove(timerSlot);
			return;
		}
		if (!isRotating) {
			isRotating = true;
			used.add(timerSlot);
		}
		return;
	}
    SceneGraphComponent ship = tc.getRootToToolComponent().getLastComponent();

    Matrix shipMatrix = new Matrix();
    if (ship.getTransformation() != null) shipMatrix.assignFrom(ship.getTransformation());

    double val = tc.getAxisState(timerSlot).intValue()*0.001;
    MatrixBuilder.euclidean(shipMatrix).rotateY(val*angle*gain).assignTo(ship);
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

}
