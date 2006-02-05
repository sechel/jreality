/*
 * Created on Apr 11, 2005
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
public class VirtualEvolutionOperator implements VirtualDevice {

  private boolean isAxis=false; 
  
    InputSlot inSlot;
    InputSlot outSlot;
    
    double oldAxis;
    boolean isFirst = true;
    
    double[] oldTrafo = null;    
    double slotValue[] = new double[16];
    DoubleArray outTrafo = new DoubleArray(slotValue);
    
    public ToolEvent process(VirtualDeviceContext context) throws MissingSlotException {
    if (!isAxis) {
      double[] newTrafo;
      try {
        newTrafo = context.getTransformationMatrix(inSlot).toDoubleArray(null);
     } catch (NullPointerException ne) {
       throw new MissingSlotException(inSlot);
     }
      if (oldTrafo == null) {
        oldTrafo = newTrafo;
        return null;
      }
      oldTrafo = Rn.inverse(oldTrafo, oldTrafo);
      // slotValue = Rn.times(slotValue, oldTrafo, newTrafo);
      slotValue = Rn.times(slotValue, newTrafo, oldTrafo);
      oldTrafo = newTrafo;
      return new ToolEvent(context.getEvent().getSource(), outSlot, outTrafo);
    } else {
      double newVal;
      try {
        newVal = context.getAxisState(inSlot).doubleValue();
      } catch (NullPointerException ne) {
        throw new MissingSlotException(inSlot);
      }
      double dval = newVal - oldAxis;
      oldAxis = newVal;
      return new ToolEvent(context.getEvent().getSource(), outSlot, new AxisState(dval));
    }
  }

    public void initialize(List inputSlots, InputSlot result,
            Map configuration) {
        inSlot = (InputSlot) inputSlots.get(0);
        outSlot = result;
        try {
          if (((String)configuration.get("slottype")).equalsIgnoreCase("axis")) {
            isAxis=true;
          }
        } catch (Exception e) {
          // assume is Transformation
        }
    }

    public void dispose() {
        // TODO Auto-generated method stub

    }

    public String getName() {
        return "DeltaTrafo";
    }

}
