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
public class VirtualExtractNegative implements VirtualDevice {

  InputSlot inAxis, outSlot;
  
  AxisState state = AxisState.ORIGIN;
  
  double min;
  
  public ToolEvent process(VirtualDeviceContext context) throws MissingSlotException {
    double val = context.getAxisState(inAxis).doubleValue();
    if (updateState(val)) return new ToolEvent(context.getEvent().getSource(), outSlot, state);
    else return null;
  }

  /**
   * @param val
   * @return true if the state has changed
   */
  protected boolean updateState(double val) {
    if (state == AxisState.ORIGIN && val < min) {
      state = AxisState.PRESSED;
      return true;
    }
    if (state == AxisState.PRESSED && val >= min) {
      state = AxisState.ORIGIN;
      return true;
    }
    return false;
  }

  public void initialize(List inputSlots, InputSlot result, Map configuration) {
    inAxis = (InputSlot) inputSlots.get(0);
    outSlot = result;
    min = ((Double)configuration.get("threshold")).doubleValue();
  }

  public void dispose() {
  }

  public String getName() {
    return "ExtractNegative";
  }
  
  public String toString() {
    return "Virtual Device: "+getName();
  }

}
