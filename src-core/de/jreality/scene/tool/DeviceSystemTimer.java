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
