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

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;

import de.jreality.scene.Viewer;
import de.jreality.util.LoggingSystem;

/**
 * This class contains an ugly workaround for linux keyboard auto-repeat.
 * 
 * When a key released event arrives, it is noted and rescheduled,
 * whith a short sleep - so that there is time for the corresponding keyTyped
 * event to check in.
 * 
 * in the keyTyped method we mark a matching release event so that it is not executed.
 * so neither the keyPressed nor the keyReleased are processed.
 * 
 * This works for me much better than the previous version - anyway,
 * I guess one needs to tweak the sleep value depending on the machine...
 * 
 * TODO: use configuration attributes to configure raw devices if needed.
 * 
 * @author weissman
 *
 **/
public class DeviceKeyboard implements RawDevice, KeyListener {
  
    private HashMap keysToVirtual = new HashMap();
    
    private ToolEventQueue queue;
    private Component component;
    
    // maps InputDevices to Timers performing "keyReleased"
    private HashMap pendingReleases=new HashMap();

    public void initialize(Viewer viewer) {
      if (viewer.hasViewingComponent()) {
        this.component = viewer.getViewingComponent();
        component.addKeyListener(this);
      }
        else
          throw new IllegalStateException("device needs component");
    }

    // store last release events
    HashMap lastReleased = new HashMap();
    HashSet cancelEvents = new HashSet();
    HashSet seenReleases = new HashSet();
    
    public synchronized void keyPressed(KeyEvent e) {
        InputSlot id = (InputSlot) keysToVirtual.get(new Integer(e.getKeyCode()));
        // we assume that the released event is not older than 1 ms
        if (id != null) {
            Long timestamp = new Long(e.getWhen());
            if (((HashMap)lastReleased.get(id)).containsKey(timestamp)) {
                KeyEvent releaseEvent = (KeyEvent) ((HashMap)lastReleased.get(id)).get(timestamp);
                cancelEvents.add(releaseEvent);
                return;
            } 
            timestamp = new Long(e.getWhen()-1);
            if (((HashMap)lastReleased.get(id)).containsKey(timestamp)) {
                KeyEvent releaseEvent = (KeyEvent) ((HashMap)lastReleased.get(id)).get(timestamp);
                cancelEvents.add(releaseEvent);
                return;
            } 
            ToolEvent ev = new ToolEvent(this, id, AxisState.PRESSED);
            queue.addEvent(ev);
            LoggingSystem.getLogger(this).fine("added key pressed ["+id+"] "+e.getWhen());
        }
    }
    
    public synchronized void keyReleased(final KeyEvent e) {
        InputSlot id = (InputSlot) keysToVirtual.get(new Integer(e.getKeyCode()));
        if (id != null) {
            if (!seenReleases.contains(e)) {
                LoggingSystem.getLogger(this).log(Level.FINEST, "release first");
                seenReleases.add(e);
                ((HashMap)lastReleased.get(id)).put(new Long(e.getWhen()), e);
                try {
                    Thread.sleep(1);
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        DeviceKeyboard.this.keyReleased(e);
                    }
                });
            } else {
                LoggingSystem.getLogger(this).log(Level.FINEST, "release second");
                if (cancelEvents.contains(e)) cancelEvents.remove(id);
                else {
                    queue.addEvent(new ToolEvent(this, id, AxisState.ORIGIN));
                    LoggingSystem.getLogger(this).finer("added key released ["+id+"] "+e.getWhen());
                }
                ((HashMap)lastReleased.get(id)).remove(new Long(e.getWhen()));
            }
        }
    }

    public ToolEvent mapRawDevice(String rawDeviceName, InputSlot inputDevice) {
        // rawDeviceName = VK_W (e.g.)
        keysToVirtual.put(resolveKeyCode(rawDeviceName), inputDevice);
        lastReleased.put(inputDevice, new HashMap());
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

    public void keyTyped(KeyEvent e) {
        // not used
    }
    
}
