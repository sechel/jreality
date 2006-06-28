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


package de.jreality.scene.tool;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.Timer;

import de.jreality.scene.Viewer;

/**
 * @author weissman
 *
 **/
public class DeviceJinputJoystick implements RawDevice, ActionListener {

    private ToolEventQueue queue;
    
    volatile int delay = 20; // delay in ms
    
	Timer timer = new Timer(delay,this);
	private HashMap componentMap = new HashMap();
	private HashMap lastValues = new HashMap();
    private InputSlot device;
    
    private Controller controllers[];
	private Component[][] components;
	
	
	public DeviceJinputJoystick() {
		ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
		controllers = env.getControllers();
		components = new net.java.games.input.Component[controllers.length][];
		for (int i = 0; i < controllers.length; i++) {
			components[i] = controllers[i].getComponents();
		}
		 
		
	}
	
    public ToolEvent mapRawDevice(String rawDeviceName, InputSlot inputDevice) {
		String[] nums = rawDeviceName.split("_");
		if(nums.length != 3) throw new IllegalArgumentException("no such raw axis");
		try {
			int i = Integer.parseInt(nums[1]);
			int j = Integer.parseInt(nums[2]);
			Component c = components[i][j];
			componentMap.put(c,rawDeviceName);
			return new ToolEvent(this, inputDevice, AxisState.ORIGIN);
		} catch (Exception e) {
			throw new IllegalArgumentException("no such raw axis");
		}
    }

    public void setEventQueue(ToolEventQueue queue) {
        this.queue = queue;
    }

    public void dispose() {
		timer.stop();
    }

    public void initialize(Viewer viewer) {
      timer.start();
    }

    public String getName() {
        return "jinputJoystick";
    }
    
    public String toString() {
      return "RawDevice: jinputJoystick";
    }

	public void actionPerformed(ActionEvent ae) {
		if (queue == null) return;
		
		//Set keys = componentMap.keySet();
		 Set entries = componentMap.entrySet();
		for (Iterator iter = entries.iterator(); iter.hasNext();) {
			Map.Entry element = (Map.Entry) iter.next();
			Component c = (Component) element.getKey();
			InputSlot inputDevice = (InputSlot) element.getValue();
			float data = c.getPollData();
			ToolEvent oldEvent = (ToolEvent) lastValues.get(inputDevice);
			if(data != oldEvent.getAxisState().doubleValue()) {
				queue.addEvent(
						new ToolEvent(this, inputDevice, new AxisState((double)data))
						);
			}
		}
	}

}
