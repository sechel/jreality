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

import de.jreality.math.Rn;
import de.jreality.scene.data.DoubleArray;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class VirtualProductMatrix implements VirtualDevice {

  InputSlot leftSlot, rightSlot;
  InputSlot productSlot;
  double[] product = new double[16];
  
  double[] matrixL = new double[16];
  double[] matrixR = new double[16];
  
  public ToolEvent process(VirtualDeviceContext context) throws MissingSlotException {
    try {
      DoubleArray matrixLeft = context.getTransformationMatrix(leftSlot);
      matrixL = matrixLeft.toDoubleArray(matrixL);
    } catch (NullPointerException npe) {
      throw new MissingSlotException(leftSlot);
    }
    try {
    DoubleArray matrixRight = context.getTransformationMatrix(rightSlot);
      matrixR = matrixRight.toDoubleArray(matrixR);
    } catch (NullPointerException npe) {
      throw new MissingSlotException(rightSlot);
    }
      product = Rn.times(product, matrixL, matrixR);
      return new ToolEvent(context.getEvent().getSource(), productSlot, new DoubleArray(product));
  }

  public void initialize(List inputSlots, InputSlot result, Map configuration) {
    productSlot = result;
    leftSlot = (InputSlot) inputSlots.get(0);
    rightSlot = (InputSlot) inputSlots.get(1);
  }

  public void dispose() {
  }

  public String getName() {
    return "ProductMatrix";
  }

  public String toString() {
    return "VirtualDevice: "+getName();
  }
}
