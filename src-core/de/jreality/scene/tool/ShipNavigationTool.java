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
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.pick.PickResult;
import de.jreality.util.LoggingSystem;

/**
 * @author weissman
 *  
 */
public class ShipNavigationTool extends Tool {

  private final InputSlot forwardBackward = InputSlot.getDevice("ForwardBackwardAxis");
  private final InputSlot leftRight = InputSlot.getDevice("LeftRightAxis");
  private final InputSlot increase = InputSlot.getDevice("IncreaseSpeed");
  private final InputSlot decrease = InputSlot.getDevice("DecreaseSpeed");
  private final InputSlot jump = InputSlot.getDevice("JumpActivation");
  private final InputSlot rotateActivation = InputSlot.getDevice("ShipRotateActivation");
  private final InputSlot horizontalRotation = InputSlot.getDevice("HorizontalShipRotationAngleEvolution");
  private final InputSlot timer = InputSlot.getDevice("SystemTime");
  
  protected List myDevs = new LinkedList();

  private double[] velocity = {0,0,0};
  private boolean touchGround;
  
  double gain = 3;
  private double gravity = 9.81;
  private double jumpSpeed = 8;
  private double lastJumpSpeed;
  private boolean rotate=false;

  boolean goTowardPointer;
  private boolean pollingDevice=true; // should be true for mouse look, false for some axis/button device TODO!!
  
  public ShipNavigationTool() {
    myDevs.add(forwardBackward);
    myDevs.add(leftRight);
    myDevs.add(increase);
    myDevs.add(decrease);
    myDevs.add(rotateActivation);
    myDevs.add(jump);
  }

  public void perform(ToolContext tc) {
    
    if (tc.getSource() == increase || tc.getSource() == decrease) {
      gain *= tc.getSource() == increase ? 1.05 : 1. / 1.05;
      return;
    }
    if (rotate) {
      if (!tc.getAxisState(rotateActivation).isPressed()) {
        myDevs.remove(horizontalRotation);
        rotate = false;
      }
    } else {
      if (tc.getAxisState(rotateActivation).isPressed()) {
        myDevs.add(horizontalRotation);
        rotate = true;
      }
    }
    if (tc.getSource() == rotateActivation) return;
    checkNeedTimer(tc);
    SceneGraphPath path = tc.getRootToLocal();
    SceneGraphComponent myComponent = path.getLastComponent();
    Matrix myMatrix = new Matrix(myComponent.getTransformation());
    if (pollingDevice && tc.getSource() == horizontalRotation) {
      MatrixBuilder.euclidean(myMatrix).rotateY(-tc.getAxisState(horizontalRotation).doubleValue());
      myComponent.getTransformation().setMatrix(myMatrix.getArray());
      return;
    }
    if (tc.getSource() == jump && tc.getAxisState(jump).isPressed() && gravity != 0) {
      velocity[1] = jumpSpeed;
    }
    if (tc.getSource() == timer) {
      AxisState axis;
      if ((axis = tc.getAxisState(forwardBackward)) != null) {
        velocity[2] = gain*axis.doubleValue();
      }
      if ((axis = tc.getAxisState(leftRight)) != null) {
        velocity[0] = -gain*axis.doubleValue();
      }
      if (!(touchGround && velocity[0] == 0 && velocity[1] == 0 && velocity[2] == 0)) {

        double sec = 0.001* tc.getAxisState(timer).intValue(); // time since
    	if (!pollingDevice) {
  	      MatrixBuilder.euclidean(myMatrix).rotateY(-tc.getAxisState(horizontalRotation).doubleValue()*sec);
  	      myComponent.getTransformation().setMatrix(myMatrix.getArray());
      	}
        double[] trans = new double[]{sec*velocity[0], sec*velocity[1], sec*velocity[2], 1};
        double[] dest;
        if (goTowardPointer) {
          Matrix pointerMatrix = new Matrix(tc.getTransformationMatrix(InputSlot.getDevice("PointerTransformation")));
          Matrix localPointer = ToolUtility.worldToTool(tc, pointerMatrix);
          // make a linear combination of x- and z-axis of the local pointer...
          // and project it onto the x-z-plane
          double dy = trans[1];
          trans = Rn.linearCombination(null, trans[0], localPointer.getColumn(0), trans[2], localPointer.getColumn(2));
          trans[1] = dy;
        }
        dest = myMatrix.multiplyVector(trans);

        double[] pickStart = new double[]{dest[0], dest[1]+1.7, dest[2], 1};
        List picks = Collections.EMPTY_LIST;
        if (gravity != 0) {
          try {
            picks = tc.getToolSystem().getPickSystem().computePick(pickStart, dest);
          } catch (Exception e) {
            LoggingSystem.getLogger(this).warning("pick system error");
            return;
          }
          if (!picks.isEmpty()) {
            PickResult pr = (PickResult) picks.get(0);
            double[] hit = pr.getWorldCoordinates();
            dest[1] = hit[1];
            velocity[1] = 0;
            touchGround = true;
          } else {
            velocity[1] -= sec*gravity ;
            touchGround = false;
          }
        }
        myMatrix.setColumn(3, dest);
        myComponent.getTransformation().setMatrix(myMatrix.getArray());
      }
    }
  }

  boolean timerOnline;
  
  private void checkNeedTimer(ToolContext tc) {
    boolean needTimer = false;
    if (tc.getAxisState(forwardBackward) != null && !tc.getAxisState(forwardBackward).isReleased()) {
      needTimer = true;
    }
    if (tc.getAxisState(leftRight) != null && !tc.getAxisState(leftRight).isReleased()) {
          needTimer = true;
        }
    if (!pollingDevice && tc.getAxisState(horizontalRotation) != null && !tc.getAxisState(horizontalRotation).isReleased()) {
          needTimer = true;
        }
    if (tc.getAxisState(jump) != null
        && tc.getAxisState(jump).isPressed())
      needTimer = true;
    if (needTimer || !touchGround) {
      if (!timerOnline) {
        myDevs.add(timer);
        timerOnline = true;
      }
    } else {
      if (timerOnline) {
        myDevs.remove(timer);
        timerOnline = false;
      }
    }
  }

  public double getGain() {
    return gain;
  }

  public void setGain(double gain) {
    this.gain = gain;
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

  public double getGravity() {
    return gravity;
  }

  public void setGravity(double gravity) {
    if (this.gravity == 0) {
      jumpSpeed = lastJumpSpeed;
      touchGround = false;
    }
    this.gravity = gravity;
    if (gravity==0) {
      touchGround=true;
      lastJumpSpeed=jumpSpeed;
      jumpSpeed=0;
    }
  }

  public double getJumpSpeed() {
    return jumpSpeed;
  }

  public void setJumpSpeed(double jumpSpeed) {
    this.jumpSpeed = jumpSpeed;
  }

  public boolean isPollingDevice() {
    return pollingDevice;
  }

  public void setPollingDevice(boolean pollingDevice) {
    this.pollingDevice = pollingDevice;
  }
}