/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.toolsystem;

import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.tool.AxisState;
import de.jreality.scene.tool.InputSlot;

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

    private List overwrites=new LinkedList();

    private boolean consumed;

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
        return false;
        //return true;
    }

    public void consume() {
      consumed=true;
    }
    
    public boolean isConsumed() {
      return consumed;
    }
}
