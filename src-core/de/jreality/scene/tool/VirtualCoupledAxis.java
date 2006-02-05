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
 * An axis that has state == pressed iff both sources have state pressed.
 * 
 * 
 * @author weissman
 *
 **/
public class VirtualCoupledAxis implements VirtualDevice {
    
    InputSlot in1;
    InputSlot in2;

    InputSlot out;
    
    boolean currentState;
    boolean initialized;
    
    public ToolEvent process(VirtualDeviceContext context)
            throws MissingSlotException {
      if (!initialized) {
        initialized = true;
        return new ToolEvent(context.getEvent().getSource(), out, AxisState.ORIGIN);
      }
        ToolEvent e = context.getEvent();
        boolean state = context.getAxisState(in1).isPressed() && context.getAxisState(in2).isPressed();
        if (state != currentState) {
          currentState = state;
          return new ToolEvent(context.getEvent().getSource(), out, currentState ? AxisState.PRESSED : AxisState.ORIGIN);
        }
        return null;
    }

    public void initialize(List inputSlots, InputSlot result,
            Map configuration) {
      in1 = (InputSlot) inputSlots.get(0);
      in2 = (InputSlot) inputSlots.get(1);
      out = result;
    }

    public void dispose() {
        // TODO Auto-generated method stub

    }

    public String getName() {
        return "CoupledAxis";
    }

    public String toString() {
        return "Virtual Device: "+getName();
    }
}
