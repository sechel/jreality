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

import de.jreality.math.MatrixBuilder;
import de.jreality.scene.SceneGraphComponent;

/**
 * @author weissman
 *  
 */
public class HeadTransformationTool extends Tool {

  private InputSlot rotateActivation = InputSlot.getDevice("ShipRotateActivation");
  private final InputSlot verticalRotation = InputSlot.getDevice("VerticalHeadRotationAngleEvolution");

  List myDevs = new LinkedList();

  private double maxAngle = Math.PI*0.35;
  private double minAngle = -Math.PI*0.35;
  
  private double headHeight=1.7;
  
  private double currentAngle;
  private boolean rotate;
  
  public HeadTransformationTool() {
    myDevs.add(rotateActivation);
  }

  public void perform(ToolContext tc) {
    if (rotate) {
      if (!tc.getAxisState(rotateActivation).isPressed()) {
        myDevs.remove(verticalRotation);
        rotate = false;
      }
    } else {
      if (tc.getAxisState(rotateActivation).isPressed()) {
        myDevs.add(verticalRotation);
        rotate = true;
      }
    }
    if (tc.getSource() == rotateActivation) return;

    double dAngle = tc.getAxisState(verticalRotation).doubleValue();
    if (currentAngle + dAngle > maxAngle || currentAngle + dAngle < minAngle) return;
    SceneGraphComponent myComponent = tc.getRootToLocal().getLastComponent();
    MatrixBuilder.euclidean(tc.getRootToLocal().getLastComponent().getTransformation()).translate(0, -headHeight, 0).rotateX(dAngle).translate(0, headHeight, 0).assignTo(myComponent);
    currentAngle+=dAngle;
  }

  public List getActivationSlots() {
    return Collections.EMPTY_LIST;
  }

  public List getCurrentSlots() {
    return myDevs;
  }

  public List getOutputSlots() {
    return null;
  }

  public void activate(ToolContext tc) {
  }

  public void deactivate(ToolContext tc) {
  }

  public double getHeadHeight() {
    return headHeight;
  }

  public void setHeadHeight(double headHeight) {
    this.headHeight = headHeight;
  }

  public double getMaxAngle() {
    return maxAngle;
  }

  public void setMaxAngle(double maxAngle) {
    this.maxAngle = maxAngle;
  }

  public double getMinAngle() {
    return minAngle;
  }

  public void setMinAngle(double minAngle) {
    this.minAngle = minAngle;
  }
}