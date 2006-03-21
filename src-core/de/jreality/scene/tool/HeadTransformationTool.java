/*
 * Created on Mar 22, 2005
 *
 * This file is part of the de.jreality.portal.tools package.
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
import de.jreality.scene.Transformation;

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