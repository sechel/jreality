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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
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


package de.jreality.tools;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.toolsystem.ToolUtility;

/**
 * @author weissman
 *
 **/
public class FlyTool extends AbstractTool {
  
  private final transient InputSlot forwardBackwardSlot = InputSlot.getDevice("ForwardBackwardAxis");
  private final transient InputSlot timerSlot = InputSlot.getDevice("SystemTime");
  
  private transient double velocity;
  private transient boolean isFlying;
  
  private double gain=1;
  private boolean raiseToThirdPower = true;
  
  public FlyTool() {
    super(null);
	  addCurrentSlot(forwardBackwardSlot);
  }
  
  EffectiveAppearance eap;
  
  public void perform(ToolContext tc) {
	if (tc.getSource() == forwardBackwardSlot) {
		velocity = tc.getAxisState(forwardBackwardSlot).doubleValue();
		// TODO make this transformation an option 
		if (raiseToThirdPower) velocity = velocity*velocity*velocity;
		if (tc.getAxisState(forwardBackwardSlot).isReleased()) {
			isFlying = false;
			removeCurrentSlot(timerSlot);
			return;
		}
		if (!isFlying) {
			isFlying = true;
			addCurrentSlot(timerSlot);
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
    //System.out.println("");
    //System.out.println("FlyTool: dir is "+Rn.toString(dir));
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
    //System.out.println("FlyTool: old position is "+Rn.toString(Pn.normalize(shipPosition, shipPosition,signature)));
    //System.out.println("FlyTool: new position is "+Rn.toString(Pn.normalize(newShipPosition,newShipPosition, signature)));
    MatrixBuilder.init(shipMatrix, signature).translateFromTo(shipPosition,newShipPosition).assignTo(ship);
    // demo madness: can't get render trigger to work, so do it by hand.  
    // TODO remove when demo is over
    tc.getViewer().render();
  }

  public double getGain() {
  	return gain;
  }
  
  public void setGain(double gain) {
  	this.gain = gain;
  }

}
