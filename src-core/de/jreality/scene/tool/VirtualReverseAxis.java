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
 * Changes the sign of the axis value of the given slot
 * 
 * usage: Virtual.Map[src-slot-name]: target-slot-name
 * 
 * @author weissman
 *
 **/
public class VirtualReverseAxis implements VirtualDevice {
    
    InputSlot in;
    InputSlot out;
    
    public ToolEvent process(VirtualDeviceContext context)
            throws MissingSlotException {
        ToolEvent e = context.getEvent();
        return new ToolEvent(context.getEvent().getSource(), out, new AxisState(-e.getAxisState().doubleValue()));
    }

    public void initialize(List inputSlots, InputSlot result,
            Map configuration) {
        in = (InputSlot) inputSlots.get(0);
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
