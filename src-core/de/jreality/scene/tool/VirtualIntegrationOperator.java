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

import de.jreality.math.Matrix;
import de.jreality.scene.data.DoubleArray;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class VirtualIntegrationOperator implements VirtualDevice {

    InputSlot inSlot;
    InputSlot outSlot;
    
    Matrix mat = new Matrix();
    DoubleArray value = new DoubleArray(mat.getArray());
    
    double[] tmp = new double[16];
    public ToolEvent process(VirtualDeviceContext context)
            throws MissingSlotException {
        mat.multiplyOnRight(context.getTransformationMatrix(inSlot).toDoubleArray(tmp));
        
        return new ToolEvent(context.getEvent().getSource(), outSlot, value);
    }

    public void initialize(List inputSlots, InputSlot result,
            Map configuration) {
        inSlot = (InputSlot) inputSlots.get(0);
        outSlot = result;
    }

    public void dispose() {
        // TODO Auto-generated method stub

    }

    public String getName() {
        return "IntegrationOperator";
    }

}
