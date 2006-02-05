/*
 * Created on May 31, 2005
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

import de.jreality.math.MatrixBuilder;
import de.jreality.scene.data.DoubleArray;
import de.jreality.util.LoggingSystem;

/**
 * @author weissman
 *
 **/
public class VirtualRotationAboutAxis implements VirtualDevice {

  InputSlot angle;
  InputSlot out;
  
  MatrixBuilder mb = MatrixBuilder.euclidian();
  DoubleArray da = new DoubleArray(mb.getMatrix().getArray());
  
  double[] axis;
  double gain = 1;
  
  public ToolEvent process(VirtualDeviceContext context) throws MissingSlotException {
    mb.reset().rotate(gain*context.getAxisState(angle).doubleValue(), axis);
    return new ToolEvent(this, out, da);
  }

  public void initialize(List inputSlots, InputSlot result, Map configuration) {
    angle = (InputSlot) inputSlots.get(0);
    out = result;
    if (configuration.containsKey("gain")) {
      try {
        gain = ((Double)configuration.get("gain")).doubleValue();
      } catch (NumberFormatException nfe) {
        LoggingSystem.getLogger(this).warning("unsupported config string");
      }
    }
  if (configuration.get("axis").equals("X-axis")) {
    axis=new double[]{1,0,0};
    return;
  }
  if (configuration.get("axis").equals("Y-axis")) {
    axis=new double[]{0,1,0};
    return;
  }
  if (configuration.get("axis").equals("Z-axis")) {
    axis=new double[]{0,0,1};
    return;
  }
  try {
    axis = (double[])configuration.get("axis");
  } catch (Exception nfe) {
    throw new IllegalArgumentException("unsupported config string");
  }
  }

  public void dispose() {
  }

  public String getName() {
    return "VirtualDevice: RotateAroundAxis";
  }

}
