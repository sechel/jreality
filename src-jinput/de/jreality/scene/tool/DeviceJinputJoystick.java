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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.Timer;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
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
