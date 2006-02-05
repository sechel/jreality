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

import de.jreality.scene.Viewer;

/**
 * @author weissman
 *
 **/
public class DeviceSystemTimer implements RawDevice {

    private ToolEventQueue queue;
    
    volatile int delay = 20; // delay in ms
    volatile boolean running = true;
    
    String myDeviceName = "tick";
    
    private Thread timer = new Thread(new Runnable() {

      public void run() {
        while (running) {
          try {
            Thread.sleep(delay);
          } catch (InterruptedException e) {
            throw new Error();
          }
          if (running) generateEvent();
        }
      }
      
    });
    
    private InputSlot device;
    
    long lastEvent = -1l;
    
    public ToolEvent mapRawDevice(String rawDeviceName, InputSlot inputDevice) {
        if (!rawDeviceName.equals(myDeviceName)) throw new IllegalArgumentException("no such raw axis");
        device = inputDevice;
        return new ToolEvent(this, inputDevice, AxisState.ORIGIN);
    }

    protected void generateEvent() {
      if (queue == null) return;
      long ct = System.currentTimeMillis();
      int delta = (int)(lastEvent == -1l ? 0 : ct - lastEvent);
      lastEvent = ct;
      ToolEvent e = new ToolEvent(this, device, new AxisState(delta)) {
          boolean compareAxisStates(AxisState axis1, AxisState axis2) {
              return true;
          }
        void replaceWith(ToolEvent replacement) {
            this.axis = new AxisState(this.axis.intValue() + replacement.axis.intValue());
            this.trafo = replacement.trafo;
            this.time = replacement.time;
        }
      };
      queue.addEvent(e);
    }

    public void setEventQueue(ToolEventQueue queue) {
        this.queue = queue;
    }

    public void dispose() {
        running = false;
    }

    public void initialize(Viewer viewer) {
      timer.setName("jReality ToolSystem Timer");
      timer.start();
    }

    public String getName() {
        return "SystemTimer";
    }
    
    public String toString() {
      return "RawDevice: SystemTimer";
    }

}
