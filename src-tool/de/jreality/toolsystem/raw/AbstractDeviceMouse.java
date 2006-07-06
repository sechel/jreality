package de.jreality.toolsystem.raw;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;

import de.jreality.math.Matrix;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.tool.InputSlot;
import de.jreality.toolsystem.ToolEvent;
import de.jreality.toolsystem.ToolEventQueue;
import de.jreality.util.LoggingSystem;

public abstract class AbstractDeviceMouse {

  protected ToolEventQueue queue;
  private boolean center;
  private boolean sentCenter;
  protected static Robot robot;
  private int lastX = -1;
  private int lastY = -1;
  private int centerX = 200;
  private int centerY = 200;
  protected HashMap usedSources = new HashMap();
  protected static HashSet knownSources = new HashSet();
  private Matrix axesMatrix = new Matrix();
  private Matrix axesEvolutionMatrix = new Matrix();
  private DoubleArray da = new DoubleArray(axesMatrix.getArray());
  private DoubleArray daEvolution = new DoubleArray(axesEvolutionMatrix.getArray());

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

  protected void mouseMoved(int ex, int ey) {
    InputSlot slot = (InputSlot) usedSources.get("axes");
    if (slot != null) {
      if (!isCenter()) {
        double xndc = -1. + (2. * ex) / getWidth();
        double yndc = 1. - (2. * ey) / getHeight();
    
        axesMatrix.setEntry(0, 3, xndc);
        axesMatrix.setEntry(1, 3, yndc);
        axesMatrix.setEntry(2, 3, -1);
        queue.addEvent(new ToolEvent(AbstractDeviceMouse.this, slot, da));
      } else if (!sentCenter) {
        axesMatrix.setEntry(0, 3, 0);
        axesMatrix.setEntry(1, 3, 0);
        axesMatrix.setEntry(2, 3, -1);
        queue.addEvent(new ToolEvent(AbstractDeviceMouse.this, slot, da));
      }
    }
    slot = (InputSlot) usedSources.get("axesEvolution");
    if (slot != null) {
      if (lastX==-1) { lastX=ex; lastY=ey; return; }
      
      int x, y, w, h;
      if (isCenter()) {
        Point p = MouseInfo.getPointerInfo().getLocation();
        x = p.x;
        y = p.y;
      } else { 
        x = ex;
        y = ey;
      }
      w = getWidth();
      h = getHeight();
      
      int dx = x - lastX;
      int dy = y - lastY;
      
      if (dx == 0 && dy == 0) return;
      
      double dxndc = (2. * dx) / w;
      double dyndc = -(2. * dy) / h;
  
      axesEvolutionMatrix.setEntry(0, 3, dxndc);
      axesEvolutionMatrix.setEntry(1, 3, dyndc);
      axesEvolutionMatrix.setEntry(2, 3, -1);
  
      ToolEvent evolutionEvent = new ToolEvent(AbstractDeviceMouse.this, slot, daEvolution);
      queue.addEvent(evolutionEvent);
      if (isCenter()) {
        try {
          robot.mouseMove(centerX, centerY);
        } catch (Exception exc) {
          LoggingSystem.getLogger(this).log(Level.CONFIG, "cannot use robot: ", exc);
        }
      } else {
        lastX=ex;
        lastY=ey;
      }
    }
  }

  public void setEventQueue(ToolEventQueue queue) {
    this.queue = queue;
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

  protected abstract void uninstallGrabs();
  protected abstract void installGrabs();

  protected abstract int getWidth();
  protected abstract int getHeight();
  
  
  
}
