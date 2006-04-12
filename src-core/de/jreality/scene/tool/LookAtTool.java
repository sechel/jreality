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
import de.jreality.math.Pn;
import de.jreality.shader.EffectiveAppearance;

/**
 * @author weissman
 *  
 */
public class LookAtTool extends Tool {

  private InputSlot myActivation = InputSlot.getDevice("ShipRotateActivation");
  private final InputSlot verticalRotation = InputSlot.getDevice("VerticalHeadRotationAngleEvolution");
  private final InputSlot horizontalRotation = InputSlot.getDevice("HorizontalShipRotationAngleEvolution");

  List myDevs = new LinkedList();

  private double currentAngleH;
  private double currentAngleV;

  private double minHorizontalAngle=-Double.MAX_VALUE;
  private double maxHorizontalAngle=Double.MAX_VALUE;
  private double minVerticalAngle=-0.2;
  private double maxVerticalAngle=-Math.PI/2-0.2;
  
  private double rodLength=5;
  
  private boolean rotate;
  
  public LookAtTool() {
    myDevs.add(myActivation);
    currentAngleV=(maxVerticalAngle+minVerticalAngle)/2;
  }

  public void perform(ToolContext tc) {
    if (rotate) {
      if (!tc.getAxisState(myActivation).isPressed()) {
        myDevs.remove(verticalRotation);
        myDevs.remove(horizontalRotation);
        rotate = false;
      }
    } else {
      if (tc.getAxisState(myActivation).isPressed()) {
        myDevs.add(verticalRotation);
        myDevs.add(horizontalRotation);
        rotate = true;
      }
    }
    if (tc.getSource() == myActivation) return;

    double dAngle = tc.getAxisState(verticalRotation).doubleValue();
    double hAngle = tc.getAxisState(horizontalRotation).doubleValue();

    if (currentAngleH-hAngle<=maxHorizontalAngle && currentAngleH-hAngle>=minHorizontalAngle)
      currentAngleH-=hAngle;
    if (currentAngleV+dAngle<=Math.max(minVerticalAngle, maxVerticalAngle) && currentAngleV+dAngle>=Math.min(minVerticalAngle, maxVerticalAngle)) {
      currentAngleV+=dAngle;
    }
    EffectiveAppearance eap = EffectiveAppearance.create(tc.getRootToToolComponent());
    int sig = eap.getAttribute("signature", Pn.EUCLIDEAN);
    
    Matrix m = MatrixBuilder.init(null, sig).rotateY(currentAngleH).rotateX(currentAngleV).translate(0,0,rodLength).getMatrix();

    m.assignTo(tc.getRootToLocal().getLastComponent());
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

  public double getMaxHorizontalAngle() {
    return maxHorizontalAngle;
  }

  public void setMaxHorizontalAngle(double maxHorizontalAngle) {
    this.maxHorizontalAngle = maxHorizontalAngle;
  }

  public double getMaxVerticalAngle() {
    return maxVerticalAngle;
  }

  public void setMaxVerticalAngle(double maxVerticalAngle) {
    this.maxVerticalAngle = maxVerticalAngle;
  }

  public double getMinHorizontalAngle() {
    return minHorizontalAngle;
  }

  public void setMinHorizontalAngle(double minHorizontalAngle) {
    this.minHorizontalAngle = minHorizontalAngle;
  }

  public double getMinVerticalAngle() {
    return minVerticalAngle;
  }

  public void setMinVerticalAngle(double minVerticalAngle) {
    this.minVerticalAngle = minVerticalAngle;
  }

  public double getRodLength() {
    return rodLength;
  }

  public void setRodLength(double rodLength) {
    this.rodLength = rodLength;
  }

}