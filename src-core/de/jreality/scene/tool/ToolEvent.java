/*
 * Created on Mar 21, 2005
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

import java.util.EventObject;

import de.jreality.scene.data.DoubleArray;

/**
 * @author weissman
 *
 **/
public class ToolEvent extends EventObject {
    
	  InputSlot device;
    
    // these must be accessable from sublasses replace-methods
    protected AxisState axis;
    protected DoubleArray trafo;
    protected long time;

    public ToolEvent(Object source, InputSlot device, AxisState axis) {
        this(source, device, axis, null);
    }
    
    public ToolEvent(Object source, InputSlot device, DoubleArray trafo) {
        this(source, device, null, trafo);
    }
    
    public ToolEvent(Object source, InputSlot device, AxisState axis, DoubleArray trafo) {
    	super(source);
    	time=System.currentTimeMillis();
    	this.device=device;
      this.axis=axis;
      this.trafo=trafo;
    }

    public InputSlot getInputSlot() {
    	return device;
    }
    
	public AxisState getAxisState() {
		return axis;
	}

	public DoubleArray getTransformation() {
		return trafo;
	}
		
	public long getTimeStamp() {
		return time;
	}
  
  public String toString() {
    return "ToolEvent source="+getSource()+" device="+device+" "+axis+" trafo="+trafo;
  }
  
  /**
   * sets the 
   * @param replacement
   */
  void replaceWith(ToolEvent replacement) {
      this.axis = replacement.axis;
      this.trafo = replacement.trafo;
      this.time = replacement.time;
  }
/**
 * 
 *  TODO improve this!
 */
  boolean canReplace(ToolEvent e) {
      return (device == e.device) 
          && (getSource() == e.getSource())
          && compareTransformation(trafo, e.trafo)
          && compareAxisStates(axis, e.axis);
  }

    boolean compareAxisStates(AxisState axis1, AxisState axis2) {
        if (axis1 == axis2) return true;
        if (axis1 == null || axis2 == null) return false;
        // sign changed
        if ( (axis1.doubleValue() * axis2.doubleValue()) <= 0 ) return false;
        // one state changed
        if (   (axis1.isPressed() && !axis2.isPressed())
            || (!axis1.isPressed() && axis2.isPressed())
            || (axis1.isReleased() && !axis2.isReleased())
            || (!axis1.isReleased() && axis2.isReleased()) )
            return false;
        return (axis1.doubleValue() - axis2.doubleValue() < 0.0001);
    }
    
    boolean compareTransformation(DoubleArray trafo1, DoubleArray trafo2) {
        if (trafo1 == trafo2) return true;
        if (trafo1 == null || trafo1 == null) return false;
        //return Rn.equals(trafo1.toDoubleArray(null), trafo2.toDoubleArray(null), 0.00000001);
        //return false;
        return true;
    }

}
