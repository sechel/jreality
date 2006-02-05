/*
 * Created on Apr 4, 2005
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

/**
 * changed the state of its output axis from ORIGIN to
 * PRESSED whenever the switch (first) axis is PRESSED.
 * inverts its current state as long as the invert axis is pressed
 * 
 * @author weissman
 *
 **/
public class VirtualSwitchAndInvertAxis implements VirtualDevice {
    
    InputSlot switchIS;
    InputSlot invertIS;
    InputSlot out;
    
    boolean lastVal;
    boolean invert;
    
    Boolean value;
    
    public ToolEvent process(VirtualDeviceContext context)
            throws MissingSlotException {
      if (value == null) {
        invert = context.getAxisState(invertIS).isPressed();
        value = Boolean.valueOf(context.getAxisState(switchIS).isPressed());
        lastVal = value.booleanValue();
        if (invert) lastVal = !lastVal;
        return new ToolEvent(this, out, lastVal ? AxisState.PRESSED : AxisState.ORIGIN);
      }

      if (context.getEvent().getInputSlot() == switchIS && context.getAxisState(switchIS).isPressed())
        value = Boolean.valueOf(!value.booleanValue());

      if (context.getEvent().getInputSlot() == invertIS)
        invert = context.getAxisState(invertIS).isPressed();
      
      boolean newVal = value.booleanValue();
      if (invert) newVal = !newVal;
      if (newVal != lastVal) {
        lastVal = newVal;
        return new ToolEvent(this, out, lastVal ? AxisState.PRESSED : AxisState.ORIGIN);
      }
      return null;
    }

    public void initialize(List inputSlots, InputSlot result,
            Map configuration) {
      switchIS = (InputSlot) inputSlots.get(0);
      invertIS = (InputSlot) inputSlots.get(1);
      out = result;
    }

    public void dispose() {
        // TODO Auto-generated method stub

    }

    public String getName() {
        return "InvertAxis";
    }

    public String toString() {
        return "Virtual Device: "+getName();
    }
}
