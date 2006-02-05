/*
 * Created on May 27, 2005
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

import java.util.List;
import java.util.Map;

import de.jreality.math.Matrix;
import de.jreality.math.Rn;
import de.jreality.scene.data.DoubleArray;

/**
 * @author weissman
 *
 **/
public class VirtualRotation implements VirtualDevice {
  
  InputSlot pointerNDC;
  InputSlot cameraToWorld;
  
  InputSlot out;

  private double gain = 4;
  private double aspectRatio = 1;

  private Matrix result = new Matrix();
  private DoubleArray da = new DoubleArray(result.getArray());

  private double oldX = Integer.MAX_VALUE, oldY;
  
  public ToolEvent process(VirtualDeviceContext context)
      throws MissingSlotException {
    DoubleArray pointer = context.getTransformationMatrix(pointerNDC);
    if (pointer == null) throw new MissingSlotException(pointerNDC);
    if (oldX == Integer.MAX_VALUE) {
      oldX = pointer.getValueAt(3);
      oldY = pointer.getValueAt(7);
      return null;
    }
    double x = pointer.getValueAt(3);
    double y = pointer.getValueAt(7);
    double dist = x*x + y*y;
    double z = 2>dist?Math.sqrt(2 - dist) : 0;
    double[] mouseCoords = new double[]{x, y, z};

    double distOld = oldX*oldX + oldY*oldY;
    double oldZ = 2>distOld?Math.sqrt(2 - distOld) : 0;
    double[] mouseCoordsOld = new double[]{oldX, oldY, oldZ};
    
    mouseCoords = Rn.normalize(mouseCoords, mouseCoords);
    mouseCoordsOld = Rn.normalize(mouseCoordsOld, mouseCoordsOld);
    
    double[] cross = Rn.crossProduct(new double[3], mouseCoordsOld, mouseCoords);
    double angle = gain*Math.asin(Rn.euclideanNorm(cross));
    
    // TODO: can't we get rid of the camera position here?
    Matrix camToWorldRot = new Matrix(context.getTransformationMatrix(cameraToWorld)).getRotation();
    cross = camToWorldRot.multiplyVector(cross);
    
   double s = Math.sin(angle);
   double c = Math.cos(angle);
   double t = 1 - c;

   cross = Rn.normalize(cross, cross);

   double xv = cross[0];
   double yv = cross[1];
   double zv = cross[2];

   result.assignFrom(t * xv*xv+ c, t*xv*yv- s*zv, t*xv*zv + s*yv,0,
     t*xv*yv + s*zv, t*yv*yv +c, t*yv*zv - s*xv,0,
     t*xv*zv - s*yv, t*yv*zv + s*xv, t*zv*zv +c,0,
     0,0,0,1);

   if (false) {
     Matrix rrt = new Matrix(result);
     rrt.multiplyOnRight(result.getTranspose());
     System.out.println("Rotation ["+rrt.getDeterminant()+"]:\n"+rrt);
   }
    oldX = x;
    oldY = y;
    return new ToolEvent(this, out, da);
  }

  public void initialize(List inputSlots, InputSlot result, Map configuration) {
    pointerNDC = (InputSlot) inputSlots.get(0);
    cameraToWorld = (InputSlot) inputSlots.get(1);
    out = result;
    if (configuration != null)
    try {
      gain = ((Double)configuration.get("gain")).doubleValue();
    } catch (Exception e) {
      // than we have the default value
    }
  }

  public void dispose() {
  }

  public String getName() {
    return "Virtual: Rotation";
  }

}
