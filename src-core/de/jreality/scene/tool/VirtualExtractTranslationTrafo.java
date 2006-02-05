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
public class VirtualExtractTranslationTrafo implements VirtualDevice {

    InputSlot inSlot;
    InputSlot outSlot;
    
    double slotValue[] = new double[16];
    DoubleArray outTrafo = new DoubleArray(slotValue);
    
    public ToolEvent process(VirtualDeviceContext context)
            throws MissingSlotException {
        try {
            slotValue[3]  = context.getTransformationMatrix(inSlot).getValueAt(3);
            slotValue[7]  = context.getTransformationMatrix(inSlot).getValueAt(7);
            slotValue[11] = context.getTransformationMatrix(inSlot).getValueAt(11);
            return new ToolEvent(context.getEvent().getSource(), outSlot, outTrafo);
        } catch (Exception e) {
            throw new MissingSlotException(inSlot);
        }

    }

    public void initialize(List inputSlots, InputSlot result,
            Map configuration) {
        inSlot = (InputSlot) inputSlots.get(0);
        outSlot = result;
        Rn.setIdentityMatrix(slotValue);
    }

    public void dispose() {
        // TODO Auto-generated method stub

    }

    public String getName() {
        return "ExtractTranslationTrafo";
    }

}
