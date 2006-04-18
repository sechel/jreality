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
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.shader.EffectiveAppearance;

/**
 * @author weissman
 *
 **/
public class FlyTool extends Tool {
  
  private final transient InputSlot forwardBackwardSlot = InputSlot.getDevice("ForwardBackwardAxis");
  private final transient InputSlot timerSlot = InputSlot.getDevice("SystemTime");
  private final transient List used = new LinkedList();
  
  private transient double velocity;
  private transient boolean isFlying;
  
  private double gain=1;
  private boolean raiseToThirdPower = true;
  
  public FlyTool() {
	  used.add(forwardBackwardSlot);
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

  EffectiveAppearance eap;
  
  public void perform(ToolContext tc) {
	if (tc.getSource() == forwardBackwardSlot) {
		velocity = tc.getAxisState(forwardBackwardSlot).doubleValue();
		// TODO make this transformation an option 
		if (raiseToThirdPower) velocity = velocity*velocity*velocity;
		if (tc.getAxisState(forwardBackwardSlot).isReleased()) {
			isFlying = false;
			used.remove(timerSlot);
			return;
		}
		if (!isFlying) {
			isFlying = true;
			used.add(timerSlot);
		}
		return;
	}
    if (eap == null || !EffectiveAppearance.matches(eap, tc.getRootToToolComponent())) {
        eap = EffectiveAppearance.create(tc.getRootToToolComponent());
      }
    int signature = eap.getAttribute("signature", Pn.EUCLIDEAN);

    SceneGraphComponent ship = tc.getRootToToolComponent().getLastComponent();

    Matrix pointerMatrix = new Matrix(tc.getTransformationMatrix(InputSlot.getDevice("PointerTransformation")));
    Matrix localPointer = ToolUtility.worldToTool(tc, pointerMatrix);
    double[] dir = localPointer.getColumn(2); // z-axis ( modulo +/- )
    System.out.println("");
    System.out.println("FlyTool: dir is "+Rn.toString(dir));
 //   if (dir[3]*dir[2] > 0) for (int i = 0; i<3; ++i) dir[i] = -dir[i];
    double[] shipPosition = localPointer.getColumn(3);
   //System.out.println("FlyTool: dir is "+Rn.toString(dir));
        // don't need the following correction anymore
    //    if (signature == Pn.EUCLIDEAN) dir[3] = 1.0;
   
    Matrix shipMatrix = new Matrix();
    if (ship.getTransformation() != null) shipMatrix.assignFrom(ship.getTransformation());
      
    // the new position also depends on the signature;
    // val is the distance we have moved in the direction dir
    // use dragTowards to calculate the resulting point
    double val = tc.getAxisState(timerSlot).intValue()*0.001;    
    //Rn.times(dir, val*gain*velocity, dir);
    val = val*gain*velocity;
    double[] newShipPosition = P3.dragTowards(null, shipPosition, dir, val, signature);
    System.out.println("FlyTool: old position is "+Rn.toString(Pn.normalize(shipPosition, shipPosition,signature)));
    System.out.println("FlyTool: new position is "+Rn.toString(Pn.normalize(newShipPosition,newShipPosition, signature)));
    MatrixBuilder.init(shipMatrix, signature).translateFromTo(shipPosition,newShipPosition).assignTo(ship);
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
	System.err.println("Gain is "+gain);
}

}
