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

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Canvas;

import de.jreality.jogl.SwtQueue;
import de.jreality.jogl.SwtViewer;
import de.jreality.math.Matrix;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.DoubleArray;

/**
 * @author weissman
 *
 **/
public class DeviceMouseSWT implements RawDevice, MouseListener, MouseMoveListener {

  private ToolEventQueue queue;
  private Canvas component;

  private HashMap usedSources = new HashMap();
  static HashSet knownSources = new HashSet();
  static {
    knownSources.add("left");
    knownSources.add("center");
    knownSources.add("right");
    knownSources.add("left_shift");
    knownSources.add("center_shift");
    knownSources.add("right_shift");
    knownSources.add("axes");
  }
  private Matrix axesMatrix = new Matrix();
  private DoubleArray da = new DoubleArray(axesMatrix.getArray());

  public void mouseDown(MouseEvent e) {
    InputSlot button = findButton(e);
    ToolEvent toolEvent = new ToolEvent(DeviceMouseSWT.this, button,
            AxisState.PRESSED);
    if (button != null) {
     queue.addEvent(toolEvent);
     System.out.println(toolEvent);
    }
  }

  public void mouseUp(MouseEvent e) {
    InputSlot button = findButton(e);
    if (button != null)
        queue.addEvent(new ToolEvent(DeviceMouseSWT.this, button, AxisState.ORIGIN));
  }

  public void mouseMove(MouseEvent e) {
    InputSlot slot = (InputSlot) usedSources.get("axes");
    if (slot == null) return;
    double xndc = -1. + 2. * e.x / component.getBounds().width;
    double yndc = 1. - 2. * e.y / component.getBounds().height;

    axesMatrix.setEntry(0, 3, xndc);
    axesMatrix.setEntry(1, 3, yndc);
    axesMatrix.setEntry(2, 3, -1);

    queue.addEvent(new ToolEvent(DeviceMouseSWT.this, slot, da));
  }

//  public void mouseWheelMoved(MouseWheelEvent e) {
//    int count = e.getWheelRotation();
//    if (count > 0) {
//      InputSlot slot = (InputSlot) usedSources.get("wheel_up");
//      if (slot == null) return;
//      for (int i = 0; i < count; i++) {
//        queue.addEvent(new ToolEvent(DeviceMouseSWT.this, slot, AxisState.PRESSED));
//        queue.addEvent(new ToolEvent(DeviceMouseSWT.this, slot, AxisState.ORIGIN));
//      }
//    }
//    if (count < 0) {
//      InputSlot slot = (InputSlot) usedSources.get("wheel_down");
//      if (slot == null) return;
//      for (int i = 0; i > count; i--) {
//        queue
//            .addEvent(new ToolEvent(DeviceMouseSWT.this, slot, AxisState.PRESSED));
//        queue.addEvent(new ToolEvent(DeviceMouseSWT.this, slot, AxisState.ORIGIN));
//      }
//    }
//  }

  private InputSlot findButton(MouseEvent e) {
    String mods = "";
    //if ((e.stateMask | SWT.SHIFT) != 0) {
    //  mods += "_shift";
    //}
    if (e.button == 1)
        return (InputSlot) usedSources.get("left"+mods);
    if (e.button == 3)
        return (InputSlot) usedSources.get("right"+mods);
    if (e.button == 2)
        return (InputSlot) usedSources.get("center"+mods);
    return null;
  }

  public void setComponent(final Canvas component) {
    this.component = component;
    Runnable r = new Runnable() {
      public void run() {
        System.out.println("DeviceMouseSWT.attatching listeners");
        component.addMouseListener(DeviceMouseSWT.this);
        component.addMouseMoveListener(DeviceMouseSWT.this);
      }
    };
    SwtQueue.getInstance().waitFor(r);
  }

  public ToolEvent mapRawDevice(String rawDeviceName, InputSlot inputDevice) {
    if (!knownSources.contains(rawDeviceName))
        throw new IllegalArgumentException("no such raw device");
    usedSources.put(rawDeviceName, inputDevice);
    if (rawDeviceName.equals("axes")) return new ToolEvent(this, inputDevice, new DoubleArray(new Matrix().getArray()));
    return new ToolEvent(this, inputDevice, AxisState.ORIGIN);
  }

  public void setEventQueue(ToolEventQueue queue) {
    this.queue = queue;
  }

  public void dispose() {
    Runnable r = new Runnable() {
      public void run() {
        component.removeMouseListener(DeviceMouseSWT.this);
        component.removeMouseMoveListener(DeviceMouseSWT.this);
      }
    };
    component.getDisplay().syncExec(r);
  }

  public void initialize(Viewer viewer) {
    if (!(viewer instanceof SwtViewer)) throw new RuntimeException("only for SWT viewer!");
    setComponent(((SwtViewer)viewer).getGLCanvas());
  }

  public String getName() {
    return "Mouse";
  }

  public String toString() {
    return "RawDevice: Mouse";
  }

  public void mouseDoubleClick(MouseEvent arg0) {
  }
  
}
