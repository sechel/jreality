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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;

import javax.swing.ImageIcon;

import de.jreality.math.Matrix;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.DoubleArray;
import de.jreality.util.LoggingSystem;

/**
 * @author weissman
 *
 **/
public class DeviceMouse implements RawDevice, MouseListener,
    MouseMotionListener, MouseWheelListener {

  private ToolEventQueue queue;
  private Component component;
  
  private boolean center;
  private boolean sentCenter;
  
  private static Robot robot;
  
  private int lastX=-1, lastY=-1, centerX=200, centerY=200;
  
  private HashMap usedSources = new HashMap();
  static HashSet knownSources = new HashSet();
  static {
    try {
      robot = new Robot();
    } catch (Exception e) {
      e.printStackTrace();
    }
    knownSources.add("left");
    knownSources.add("center");
    knownSources.add("right");
    knownSources.add("axes");
    knownSources.add("axesEvolution");
    knownSources.add("wheel_up");
    knownSources.add("wheel_down");
  }
  private Matrix axesMatrix = new Matrix();
  private Matrix axesEvolutionMatrix = new Matrix();
  private DoubleArray da = new DoubleArray(axesMatrix.getArray());
  private DoubleArray daEvolution = new DoubleArray(axesEvolutionMatrix.getArray());

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

  private Cursor emptyCursor;
  public void mouseMoved(MouseEvent e) {
    InputSlot slot = (InputSlot) usedSources.get("axes");
    if (slot != null) {
      if (!isCenter()) {
        double xndc = -1. + (2. * e.getX()) / component.getWidth();
        double yndc = 1. - (2. * e.getY()) / component.getHeight();
    
        axesMatrix.setEntry(0, 3, xndc);
        axesMatrix.setEntry(1, 3, yndc);
        axesMatrix.setEntry(2, 3, -1);
        queue.addEvent(new ToolEvent(DeviceMouse.this, slot, da));
      } else if (!sentCenter) {
        axesMatrix.setEntry(0, 3, 0);
        axesMatrix.setEntry(1, 3, 0);
        axesMatrix.setEntry(2, 3, -1);
        queue.addEvent(new ToolEvent(DeviceMouse.this, slot, da));
      }
    }
    slot = (InputSlot) usedSources.get("axesEvolution");
    if (slot != null) {
      if (lastX==-1) { lastX=e.getX(); lastY=e.getY(); return; }
      
      int x, y, w, h;
      if (isCenter()) {
        Point p = MouseInfo.getPointerInfo().getLocation();
        x = p.x;
        y = p.y;
      } else { 
        x = e.getX();
        y = e.getY();
      }
      w = component.getWidth();
      h = component.getHeight();
      
      int dx = x - lastX;
      int dy = y - lastY;
      
      if (dx == 0 && dy == 0) return;
      
      double dxndc = (2. * dx) / w;
      double dyndc = -(2. * dy) / h;
  
      axesEvolutionMatrix.setEntry(0, 3, dxndc);
      axesEvolutionMatrix.setEntry(1, 3, dyndc);
      axesEvolutionMatrix.setEntry(2, 3, -1);
  
      ToolEvent evolutionEvent = new ToolEvent(DeviceMouse.this, slot, daEvolution);
      queue.addEvent(evolutionEvent);
      if (isCenter()) {
        try {
          robot.mouseMove(centerX, centerY);
        } catch (Exception ex) {
          LoggingSystem.getLogger(this).log(Level.CONFIG, "cannot use robot: ", ex);
        }
      } else {
        lastX=e.getX();
        lastY=e.getY();
      }
    }
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
  public static int getRealButton(MouseEvent e) {
    int button = e.getButton();
    if (button == 0)  {   // Linux!
      int mods = e.getModifiersEx();
      if ((mods & InputEvent.BUTTON1_DOWN_MASK) != 0)   button = 1;
      else if ((mods & InputEvent.BUTTON2_DOWN_MASK) != 0)  button = 2;
      else button = 3;
    } else {          // Mac OS X Laptop (no 3-mouse button)!!
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
    component.addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent e) {
      }
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_F11 || e.getKeyCode() == KeyEvent.VK_F10) {
          setCenter(!isCenter());
        }
      }
      public void keyReleased(KeyEvent e) {
      }
    });
  }

  public ToolEvent mapRawDevice(String rawDeviceName, InputSlot inputDevice) {
    if (!knownSources.contains(rawDeviceName))
        throw new IllegalArgumentException("no such raw device");
    usedSources.put(rawDeviceName, inputDevice);
    if (rawDeviceName.equals("axes")) return new ToolEvent(this, inputDevice, new DoubleArray(new Matrix().getArray()));
    if (rawDeviceName.equals("axesEvolution")) {
      Matrix initM = new Matrix();
      initM.setEntry(2, 3, -1);
      return new ToolEvent(this, inputDevice, new DoubleArray(initM.getArray()));
    }
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

  public boolean isCenter() {
    return center;
  }

  public void setCenter(boolean center) {
    if (this.center != center) sentCenter = false;
    this.center = center;
    if (center) {
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      centerX=lastX=dim.width/2; centerY=lastY=dim.height/2;
      robot.mouseMove(centerX, centerY);
      installGrabs();
    } else {
      uninstallGrabs();
    }
  }
  
  public void installGrabs() {
    try {
      if (emptyCursor == null) {
        ImageIcon emptyIcon = new ImageIcon(new byte[0]);
        emptyCursor = component.getToolkit().createCustomCursor(emptyIcon.getImage(), new Point(0, 0), "emptyCursor");
      }
      component.setCursor(emptyCursor);
    } catch (Exception e) {
      LoggingSystem.getLogger(this).log(Level.WARNING, "cannot grab mouse", e);
    }
  }
  
  public void uninstallGrabs()
  {
    try {
      component.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    } catch (Exception e) {
      LoggingSystem.getLogger(this).log(Level.WARNING, "cannot grab mouse", e);
    }
  }


}
