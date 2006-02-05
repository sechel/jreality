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

import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.DoubleArray;


/**
 *
 * TODO: comment this, add signature support etc...
 *
 * @author weissman
 *
 */
public class VirtualExtractRotationTrafo implements VirtualDevice {

    InputSlot inSlot;
    InputSlot outSlot;
    
    FactoredMatrix mat = new FactoredMatrix();
    Matrix rot = new Matrix();
    DoubleArray outTrafo = new DoubleArray(rot.getArray());
    double[] tmp = new double[16];
    
    public ToolEvent process(VirtualDeviceContext context)
            throws MissingSlotException {
        try {
            context.getTransformationMatrix(inSlot).toDoubleArray(tmp);
            mat.assignFrom(tmp);
            rot.assignFrom(mat.getRotation());
            return new ToolEvent(context.getEvent().getSource(), outSlot, outTrafo);
        } catch (Exception e) {
            return null;
        }

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
        return "ExtractRotationTrafo";
    }

}
