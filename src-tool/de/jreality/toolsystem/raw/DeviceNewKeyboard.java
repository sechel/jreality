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


package de.jreality.toolsystem.raw;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

import de.jreality.scene.Viewer;
import de.jreality.scene.tool.AxisState;
import de.jreality.scene.tool.InputSlot;
import de.jreality.toolsystem.ToolEvent;
import de.jreality.toolsystem.ToolEventQueue;
import de.jreality.util.LoggingSystem;

/**
 * @author weissman
 **/
public class DeviceNewKeyboard implements RawDevice, KeyListener {
  
    private HashMap<Integer, InputSlot> keysToVirtual = new HashMap<Integer, InputSlot>();
    
    private ToolEventQueue queue;
    private Component component;
    
    public void initialize(Viewer viewer) {
      if (!viewer.hasViewingComponent() || !(viewer.getViewingComponent() instanceof Component) ) throw new UnsupportedOperationException("need AWT component");
      this.component = (Component) viewer.getViewingComponent();
      this.component.addKeyListener(this);
    }

    public synchronized void keyPressed(KeyEvent e) {
    	if (e.isConsumed()) return;
        InputSlot id = (InputSlot) keysToVirtual.get(new Integer(e.getKeyCode()));
        if (id != null) {
          ToolEvent ev = new ToolEvent(this, id, AxisState.PRESSED);
          queue.addEvent(ev);
          LoggingSystem.getLogger(this).fine(this.hashCode()+" added key pressed ["+id+"] "+e.getWhen());
        }
    }
    
    public synchronized void keyReleased(final KeyEvent e) {
        InputSlot id = (InputSlot) keysToVirtual.get(e.getKeyCode());
        if (id != null) {
        	// check for a next key-pressed
        	KeyEvent nextEvent = (KeyEvent) Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent(KeyEvent.KEY_PRESSED);
        	if (nextEvent != null && nextEvent.getKeyCode() == e.getKeyCode()) {
        		nextEvent.consume();
        		return;
        	}
        	queue.addEvent(new ToolEvent(this, id, AxisState.ORIGIN));
            LoggingSystem.getLogger(this).finer("added key released ["+id+"] "+e.getWhen());
        }
    }

	public void keyTyped(KeyEvent e) {
	}

	public ToolEvent mapRawDevice(String rawDeviceName, InputSlot inputDevice) {
        // rawDeviceName = VK_W (e.g.)
        keysToVirtual.put(resolveKeyCode(rawDeviceName), inputDevice);
        return new ToolEvent(this, inputDevice, AxisState.ORIGIN);
    }
    
    private Integer resolveKeyCode(String fieldName) {
      try {
        int val = KeyEvent.class.getField(fieldName).getInt(KeyEvent.class);
        return new Integer(val);
      } catch (Exception e) {
        throw new IllegalArgumentException("no such key "+fieldName);
      }
      
    }

    public void setEventQueue(ToolEventQueue queue) {
        this.queue = queue; 
    }

    public void dispose() {
    	component.removeKeyListener(this);
    }
    
    public String getName() {
        return "Keyboard";
    }
    
    public String toString() {
      return "RawDevice: Keyboard";
    }

}
