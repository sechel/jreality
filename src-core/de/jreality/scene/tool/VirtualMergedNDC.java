/*
 * Created on Apr 4, 2005
 *
 * This file is part of the jReality package.
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
import de.jreality.scene.data.DoubleArray;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class VirtualMergedNDC implements VirtualDevice {

  InputSlot inX, inY, outSlot;
  DoubleArray da;
  Matrix mat = new Matrix();
  
  public VirtualMergedNDC() {
    da = new DoubleArray(mat.getArray());
    // set Z to -1
    mat.setEntry(2, 3, -1);
  }
  
  public ToolEvent process(VirtualDeviceContext context) throws MissingSlotException {
    try {
    mat.setEntry(0, 3, context.getAxisState(inX).doubleValue());
    mat.setEntry(1, 3, context.getAxisState(inY).doubleValue());
    return new ToolEvent(context.getEvent().getSource(), outSlot, da);
    } catch (NullPointerException ne) {
      return null;
    }
  }

  public void initialize(List inputSlots, InputSlot result, Map configuration) {
    inX = (InputSlot) inputSlots.get(0);
    inY = (InputSlot) inputSlots.get(1);
    outSlot = result;
  }

  public void dispose() {
  }

  public String getName() {
    return "MergedNDC";
  }
  
  public String toString() {
    return "Virtual Device: "+getName();
  }

}
