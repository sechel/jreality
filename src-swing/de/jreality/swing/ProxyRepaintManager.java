package de.jreality.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.RepaintManager;

/**
 * this class simply delegates all method calls to 
 * the delegated RepaintManager. This class is used
 * in JRJComponent.
 * 
 * @author Steffen Weissmann
 *
 */
class ProxyRepaintManager extends RepaintManager {
  
  private RepaintManager delegated;
  
  public ProxyRepaintManager(RepaintManager delegated) {
    this.delegated = delegated;
  }
  public static RepaintManager currentManager(Component c) {
    return RepaintManager.currentManager(c);
  }
  public static RepaintManager currentManager(JComponent c) {
    return RepaintManager.currentManager(c);
  }
  public static void setCurrentManager(RepaintManager aRepaintManager) {
    RepaintManager.setCurrentManager(aRepaintManager);
  }
  public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
    delegated.addDirtyRegion(c, x, y, w, h);
  }
  public void addInvalidComponent(JComponent invalidComponent) {
    delegated.addInvalidComponent(invalidComponent);
  }
  public boolean equals(Object obj) {
    return delegated.equals(obj);
  }
  public Rectangle getDirtyRegion(JComponent aComponent) {
    return delegated.getDirtyRegion(aComponent);
  }
  public Dimension getDoubleBufferMaximumSize() {
    return delegated.getDoubleBufferMaximumSize();
  }
  public Image getOffscreenBuffer(Component c, int proposedWidth,
      int proposedHeight) {
    return delegated.getOffscreenBuffer(c, proposedWidth, proposedHeight);
  }
  public Image getVolatileOffscreenBuffer(Component c, int proposedWidth,
      int proposedHeight) {
    return delegated.getVolatileOffscreenBuffer(c, proposedWidth,
        proposedHeight);
  }
  public int hashCode() {
    return delegated.hashCode();
  }
  public boolean isCompletelyDirty(JComponent aComponent) {
    return delegated.isCompletelyDirty(aComponent);
  }
  public boolean isDoubleBufferingEnabled() {
    return delegated.isDoubleBufferingEnabled();
  }
  public void markCompletelyClean(JComponent aComponent) {
    delegated.markCompletelyClean(aComponent);
  }
  public void markCompletelyDirty(JComponent aComponent) {
    delegated.markCompletelyDirty(aComponent);
  }
  public void paintDirtyRegions() {
    delegated.paintDirtyRegions();
  }
  public void removeInvalidComponent(JComponent component) {
    delegated.removeInvalidComponent(component);
  }
  public void setDoubleBufferingEnabled(boolean aFlag) {
    delegated.setDoubleBufferingEnabled(aFlag);
  }
  public void setDoubleBufferMaximumSize(Dimension d) {
    delegated.setDoubleBufferMaximumSize(d);
  }
  public String toString() {
    return delegated.toString();
  }
  public void validateInvalidComponents() {
    delegated.validateInvalidComponents();
  }
}
