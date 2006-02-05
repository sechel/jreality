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


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class VirtualMergedAxis implements VirtualDevice {

  InputSlot inPlus, inMinus, outSlot;
  
  public ToolEvent process(VirtualDeviceContext context) throws MissingSlotException {
    AxisState minus = context.getAxisState(inMinus) == null ? AxisState.ORIGIN : context.getAxisState(inMinus);
    AxisState plus = context.getAxisState(inPlus) == null ? AxisState.ORIGIN : context.getAxisState(inPlus);
    return new ToolEvent(context.getEvent().getSource(), outSlot, new AxisState(plus.intValue()-minus.intValue()));
  }

  public void initialize(List inputSlots, InputSlot result, Map configuration) {
    inMinus = (InputSlot) inputSlots.get(0);
    inPlus = (InputSlot) inputSlots.get(1);
    outSlot = result;
  }

  public void dispose() {
  }

  public String getName() {
    return "MergedAxis";
  }
  
  public String toString() {
    return "Virtual Device: "+getName();
  }

}
