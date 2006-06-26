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
import java.awt.event.*;
import java.util.HashMap;
import java.util.HashSet;

import de.jreality.math.Matrix;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.DoubleArray;

/**
 * @author weissman
 *
 **/
public class DeviceMouse implements RawDevice, MouseListener,
    MouseMotionListener, MouseWheelListener {

  private ToolEventQueue queue;
  private Component component;

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
    knownSources.add("wheel_up");
    knownSources.add("wheel_down");
  }
  private Matrix axesMatrix = new Matrix();
  private DoubleArray da = new DoubleArray(axesMatrix.getArray());

  public void mouseClicked(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
    InputSlot button = findButton(e);
    if (button != null)
        queue.addEvent(new ToolEvent(DeviceMouse.this, button,
            AxisState.PRESSED));
  }

  public void mouseReleased(MouseEvent e) {
    InputSlot button = findButton(e);
    if (button != null)
        queue.addEvent(new ToolEvent(DeviceMouse.this, button, AxisState.ORIGIN));
  }

  public void mouseDragged(MouseEvent e) {
    mouseMoved(e);
  }

  public void mouseMoved(MouseEvent e) {
    InputSlot slot = (InputSlot) usedSources.get("axes");
    if (slot == null) return;
    double xndc = -1. + 2. * e.getX() / component.getWidth();
    double yndc = 1. - 2. * e.getY() / component.getHeight();

    axesMatrix.setEntry(0, 3, xndc);
    axesMatrix.setEntry(1, 3, yndc);
    axesMatrix.setEntry(2, 3, -1);

    queue.addEvent(new ToolEvent(DeviceMouse.this, slot, da));
  }

  public void mouseWheelMoved(MouseWheelEvent e) {
    int count = e.getWheelRotation();
    if (count > 0) {
      InputSlot slot = (InputSlot) usedSources.get("wheel_up");
      if (slot == null) return;
      for (int i = 0; i < count; i++) {
        queue.addEvent(new ToolEvent(DeviceMouse.this, slot, AxisState.PRESSED));
        queue.addEvent(new ToolEvent(DeviceMouse.this, slot, AxisState.ORIGIN));
      }
    }
    if (count < 0) {
      InputSlot slot = (InputSlot) usedSources.get("wheel_down");
      if (slot == null) return;
      for (int i = 0; i > count; i--) {
        queue
            .addEvent(new ToolEvent(DeviceMouse.this, slot, AxisState.PRESSED));
        queue.addEvent(new ToolEvent(DeviceMouse.this, slot, AxisState.ORIGIN));
      }
    }
  }

  // e.getButton() doesn't work properly on 1-button mouse, such as MacOS laptops
	public static int getRealButton(MouseEvent e)	{
		int button = e.getButton();
		if (button == 0)	{		// Linux!
			int mods = e.getModifiersEx();
			if ((mods & InputEvent.BUTTON1_DOWN_MASK) != 0)		button = 1;
			else if ((mods & InputEvent.BUTTON2_DOWN_MASK) != 0)  button = 2;
			else button = 3;
		} else {					// Mac OS X Laptop (no 3-mouse button)!!
			int mods = e.getModifiers();
			if (e.isAltDown() && ((mods & InputEvent.BUTTON2_MASK) != 0) ) button = 2;
			else if (button == 1 &&  ((mods & InputEvent.BUTTON3_MASK) != 0) ) button = 3;
		}
		return button;
	}

  private InputSlot findButton(MouseEvent e) {
    int button = getRealButton(e); //e.getButton();
    int modifiers = e.getModifiersEx();
    String mods = "";
    if (e.isShiftDown()) {
      mods += "_shift";
    }
    if (button == MouseEvent.BUTTON1)
        return (InputSlot) usedSources.get("left"+mods);
    if (button == MouseEvent.BUTTON3)
        return (InputSlot) usedSources.get("right"+mods);
    if (button == MouseEvent.BUTTON2)
        return (InputSlot) usedSources.get("center"+mods);
    return null;
  }

  public void setComponent(Component component) {
    this.component = component;
    component.addMouseListener(this);
    component.addMouseMotionListener(this);
    component.addMouseWheelListener(this);
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
    component.removeMouseListener(this);
    component.removeMouseMotionListener(this);
    component.removeMouseWheelListener(this);
  }

  public void initialize(Viewer viewer) {
    if (!viewer.hasViewingComponent() || viewer.getViewingComponent() == null) throw new UnsupportedOperationException("need AWT component");
    setComponent(viewer.getViewingComponent());
  }

  public String getName() {
    return "Mouse";
  }

  public String toString() {
    return "RawDevice: Mouse";
  }

}
