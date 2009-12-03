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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import de.jreality.scene.Viewer;
import de.jreality.scene.tool.AxisState;
import de.jreality.scene.tool.InputSlot;
import de.jreality.toolsystem.ToolEvent;
import de.jreality.toolsystem.ToolEventQueue;

/**
 * A device that utilizes the jinput library for polling mice and joysticks.
 * The k-th axis of the l-th device can be addressed via "axis_l_k"
 * 
 * TODO: rename this to DeviceJInput, since it provides access to ALL jinput devices
 * @author weissman/hoffmann
 *
 **/
public class DeviceJinputController implements RawDevice, PollingDevice {

    private ToolEventQueue queue;
    
	private HashMap<Component,InputSlot> componentMap = new HashMap<Component,InputSlot>();
	private HashMap<Component,AxisState> lastValues = new HashMap<Component,AxisState>();
	private HashMap<Component,Float> maxValues = new HashMap<Component,Float>();
    
    private Controller device;
    
	private net.java.games.input.Component[] components;
	
    public ToolEvent mapRawDevice(String rawDeviceName, InputSlot inputDevice) {
		String[] nums = rawDeviceName.split("_");
		if(nums.length < 2) throw new IllegalArgumentException("no such raw axis");
		try {
			int j = Integer.parseInt(nums[1]);
            net.java.games.input.Component c = components[j];
			componentMap.put(c,inputDevice);
            lastValues.put(c,AxisState.ORIGIN);
            float maxVal = 1;
            if (nums.length==3) try {
            	maxVal = Float.parseFloat(nums[2]);
            } catch (NumberFormatException nbfe) {
            	System.err.println("invalid max value: "+nums[2]);
            }
            maxValues.put(c, maxVal);
            System.out.println("Mapped "+device+"->"+c+" <=> "+inputDevice);
			return new ToolEvent(this, System.currentTimeMillis(), inputDevice, AxisState.ORIGIN);
		} catch (Exception e) {
			throw new IllegalArgumentException("no such raw axis");
		}
    }

    public void setEventQueue(ToolEventQueue queue) {
        this.queue = queue;
    }

    public void dispose() {
    }

    public void initialize(Viewer viewer, Map<String, Object> config) {
		ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
		Controller[] controllers = env.getControllers();
		String id_string = config.get("id_string").toString();
		for (Controller ctrl : controllers) {
			//System.out.println(ctrl+" :: "+Arrays.toString(ctrl.getComponents()));
			if (ctrl.getName().matches(id_string)) {
				System.out.println("Found "+ctrl+" :: "+Arrays.toString(ctrl.getComponents()));
				device = ctrl;
				break;
			} else {
				System.out.println("\""+id_string+"\" did not match \""+ctrl.getName()+"\"");
			}
		}
		components = device.getComponents();
		if (device == null) throw new IllegalStateException("no controller matching "+id_string);
    }

    public String getName() {
        return "jinput ["+(device == null ? "<null>":device.getName())+"]";
    }
    
	public void poll() {
		if (queue == null) return;
        device.poll();
		Set<Map.Entry<Component, InputSlot>> entries = componentMap.entrySet();
		for (Map.Entry<Component, InputSlot> element : entries) {
            Component c = element.getKey();
			float pollData = c.getPollData();
			float val = pollData/maxValues.get(c);
            // TODO google how to calibrate controllers
            //old controller calibration
            //if (val == 0.003921628f) val = 0f;
            //saitek: better controller, calibration
           // if (val == -0.043137252f) val = 0f;
            //if (val == -0.027450979f) val = 0f;
			//System.out.println("val: "+val);
            AxisState newState = new AxisState((double) val);
			AxisState oldState = lastValues.get(c);
			if(newState.intValue() != oldState.intValue()) {
				InputSlot slot = element.getValue();
				queue.addEvent(new ToolEvent(this, System.currentTimeMillis(), slot, newState));
                lastValues.put(c,newState);
                //if (slot.getName().endsWith("RZRaw")) System.out.println(slot+" val="+val+" pollData="+pollData);
			}
		}
	}

	public static void main(String[] args) {
		ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
		Controller[] controllers = env.getControllers();
		for (Controller ctrl : controllers) {
			System.out.println(ctrl+" :: "+Arrays.toString(ctrl.getComponents()));
		}
	}

}
