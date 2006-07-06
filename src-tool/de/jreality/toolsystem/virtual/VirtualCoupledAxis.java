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


package de.jreality.toolsystem.virtual;

import java.util.List;
import java.util.Map;

import de.jreality.scene.tool.AxisState;
import de.jreality.scene.tool.InputSlot;
import de.jreality.toolsystem.MissingSlotException;
import de.jreality.toolsystem.ToolEvent;
import de.jreality.toolsystem.VirtualDevice;
import de.jreality.toolsystem.VirtualDeviceContext;

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
        boolean state = context.getAxisState(in1).isPressed() && context.getAxisState(in2).isPressed();
        if (state != currentState) {
          currentState = state;
          ToolEvent te = new ToolEvent(context.getEvent().getSource(), out, currentState ? AxisState.PRESSED : AxisState.ORIGIN);
          if (context.getEvent().getInputSlot() == in1 || context.getAxisState(in1).isPressed())
            context.getEvent().consume();
          return te;
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
