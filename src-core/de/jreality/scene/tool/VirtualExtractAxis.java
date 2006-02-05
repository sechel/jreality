/*
 * Created on Apr 11, 2005
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

import de.jreality.math.Rn;
import de.jreality.util.LoggingSystem;

/**
 * 
 * TODO: implement this ;-)
 *
 */
public class VirtualExtractAxis implements VirtualDevice {

	InputSlot inSlot, outSlot;
	
	private int index = -1;
	double maxVal=1;
	
	AxisState state;
	double lastVal=Double.MAX_VALUE;
	
	double gain = 1;
	
	public ToolEvent process(VirtualDeviceContext context) throws MissingSlotException {
		double newVal;
        newVal = gain * context.getTransformationMatrix(inSlot).getValueAt(index);
		if ( index == -1 || Math.abs(lastVal - newVal) < Rn.TOLERANCE ) return null;
		lastVal = newVal;
		return new ToolEvent(context.getEvent().getSource(), outSlot, new AxisState(newVal));
	}

	public void initialize(List inputSlots, InputSlot result,
			Map configuration) {
		inSlot = (InputSlot) inputSlots.get(0);
		outSlot = result;
		try {
			gain = ((Double)configuration.get("gain")).doubleValue();
		} catch (Exception e) {
			// no gain set...
		}
		if (configuration.get("axis").equals("translationX")) {
			index=3; return;
		}
		if (configuration.get("axis").equals("translationY")) {
			index=7; return;
		}
		if (configuration.get("axis").equals("translationZ")) {
			index=11; return;
		}
		try {
			index = ((Integer)configuration.get("index")).intValue();
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("unsupported config string");
		}
	}

	public void dispose() {
	}

	public String getName() {
		return "ExtractAxis";
	}

}
