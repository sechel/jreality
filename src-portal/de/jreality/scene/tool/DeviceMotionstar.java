/*
 * Created on Apr 10, 2005
 *
 * This file is part of the jReality package.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;

import szg.framework.event.HeadEvent;
import szg.framework.event.HeadMotionListener;
import szg.framework.event.WandEvent;
import szg.framework.event.WandListener;
import szg.framework.event.remote.RemoteEventQueueImpl;
import de.jreality.math.Matrix;
import de.jreality.math.Rn;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.DoubleArray;
import de.jreality.util.LoggingSystem;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class DeviceMotionstar implements RawDevice {
	
	private static final boolean dump = false;
    
    private HashMap usedSources = new HashMap();
    static HashSet knownSources = new HashSet();
    private ToolEventQueue queue;
    static {
        knownSources.add("headMatrix");
        knownSources.add("wandMatrix");
        knownSources.add("wand_left");
        knownSources.add("wand_center");
        knownSources.add("wand_right");
        knownSources.add("wand_axes");
    }
    
    RemoteEventQueueImpl szgQueue;

    private class MyListener implements WandListener, HeadMotionListener {

        public void buttonPressed(WandEvent event) {
        	if (dump) System.out.println("MyListener.buttonPressed()");
        	try {
        		String button = resolveButton(event.getButton());
        		LoggingSystem.getLogger(this).log(Level.FINE, "button pressed: "+button);
				InputSlot slot = (InputSlot) usedSources.get(button);
        		if (slot == null) {
            		LoggingSystem.getLogger(this).log(Level.FINER, "button pressed = null");        			
        			return;
        		}
        		queue.addEvent(new ToolEvent(DeviceMotionstar.this, slot, AxisState.PRESSED));
        	} catch (Exception e) {
        		LoggingSystem.getLogger(this).log(Level.WARNING, "button pressed error", e);
        	}
        }

        private String resolveButton(int button) {
            switch (button) {
                case 0: return "wand_right";
                case 1: return "wand_left";
                case 2: return "wand_center";
                default: throw new IllegalArgumentException("unknown button id ["+button+"]");
            }
        }

        public void buttonReleased(WandEvent event) {
        	if (dump) System.out.println("MyListener.buttonReleased()");
        	try {
        		String button = resolveButton(event.getButton());
        		LoggingSystem.getLogger(this).log(Level.FINE, "button pressed: "+button);
				InputSlot slot = (InputSlot) usedSources.get(button);
	            if (slot == null) return;
	            queue.addEvent(new ToolEvent(DeviceMotionstar.this, slot, AxisState.ORIGIN));
	    	} catch (Exception e) {
	    		LoggingSystem.getLogger(this).log(Level.WARNING, "button pressed error", e);
	    	}
        }

        double[] axesMatrix = new Matrix().getArray();
        double oldX,oldY;
        public void axisMoved(WandEvent event) {
        	if (dump) System.out.println("MyListener.axisMoved()");
            InputSlot slot = (InputSlot) usedSources.get("wand_axes");
            if (slot == null) return;
            oldX=axesMatrix[3];
            oldY=axesMatrix[7];
            double x = event.getAxisValue(0);
            double y = event.getAxisValue(1);
            axesMatrix[3]  = trim(x);
            axesMatrix[7]  = trim(y);
            axesMatrix[11] = 1;
            if (oldX != axesMatrix[3] || oldY != axesMatrix[7])
            	queue.addEvent(new ToolEvent(DeviceMotionstar.this, slot, new DoubleArray(Rn.copy(null, axesMatrix))));
        }
        
        private double trim(double d) {
        	if (d > 0) {
        		if (d < 0.09) return 0;
        		if (d > 1) return 1;
        		return d;
        	}
    		if (d > -0.09) return 0;
    		if (d <  -1) return -1;
    		return d;
        }

        public void wandDragged(WandEvent event) {
        	if (dump) System.out.println("MyListener.wandDragged()");
          wandMoved(event);
        }

        double[] matrixWand = new double[16];
        public void wandMoved(WandEvent event) {
        	if (dump) System.out.println("MyListener.wandMoved()");
            InputSlot slot = (InputSlot) usedSources.get("wandMatrix");
            if (slot == null) return;
            matrixWand = Rn.transposeF2D(matrixWand, event.getMatrix());
            queue.addEvent(new ToolEvent(DeviceMotionstar.this, slot, new DoubleArray(Rn.copy(null, matrixWand))) {
              boolean compareTransformation(DoubleArray trafo1, DoubleArray trafo2) {
                return true;
              }
            });
        }

        double[] matrixHead = new double[16];
        public void headMoved(HeadEvent event) {
        	if (dump) System.out.println("MyListener.headMoved()");
            if (!usedSources.containsKey("headMatrix")) return;
            InputSlot slot = (InputSlot) usedSources.get("headMatrix");
            matrixHead = Rn.transposeF2D(matrixHead, event.getMatrix());
            queue.addEvent(new ToolEvent(DeviceMotionstar.this, slot, new DoubleArray(Rn.copy(null, matrixHead))) {
              boolean compareTransformation(DoubleArray trafo1, DoubleArray trafo2) {
                return true;
              }
            });
        }
    }
    
    private MyListener myListener = new MyListener();
    
    public void setEventQueue(ToolEventQueue queue) {
        this.queue=queue;
    }

    public void initialize(Viewer viewer) {
      try {
      	szgQueue = new RemoteEventQueueImpl();
        szgQueue.addWandListener(myListener);
        szgQueue.addHeadMotionListener(myListener);
      } catch (Exception mfe) {
        LoggingSystem.getLogger(this).log(Level.SEVERE, "error creating remote motionstar eventqueue", mfe);
      }
    }

    public ToolEvent mapRawDevice(String rawDeviceName, InputSlot inputDevice) {
        if (!knownSources.contains(rawDeviceName)) throw new IllegalArgumentException("no such raw device");
        usedSources.put(rawDeviceName, inputDevice);
        if (rawDeviceName.indexOf("Matrix") != -1 || rawDeviceName.equals("wand_axes"))
          return new ToolEvent(this, inputDevice, new DoubleArray(new Matrix().getArray()));
        return new ToolEvent(this, inputDevice, AxisState.ORIGIN);
    }

    public void dispose() {
        szgQueue.removeWandListener(myListener);
        szgQueue.removeHeadMotionListener(myListener);
    }

    public String getName() {
        return "Motionstar";
    }
    
    public String toString() {
    	return "RawDevice: "+getName();
    }

}
