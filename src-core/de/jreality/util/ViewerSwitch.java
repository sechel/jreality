package de.jreality.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;

/**
 * hides several viewer implementations and provides the same
 * viewing component for all. Using this class an application
 * doesn't need to care about component replacement when switching
 * from one viewer to another...
 * 
 * @author gollwas
 *
 */
public class ViewerSwitch implements Viewer {
  
  private Viewer[] viewers;
  private String[] viewerNames;
  
  private Viewer currentViewer;
  private EventDispatcher eventDispatcher=new EventDispatcher();
  
  private JPanel component = new JPanel(new BorderLayout());

  private static String[] createViewerNames(Viewer[] viewers) {
    String[] viewerNames = new String[viewers.length];
    for (int i = 0; i < viewerNames.length; i++)
      viewerNames[i] = "["+i+"] "+viewers[i].getClass().getName();
    return viewerNames;
  }
  
  public ViewerSwitch(Viewer[] viewers) {
    this(viewers, createViewerNames(viewers));
  }
  
  public ViewerSwitch(Viewer[] viewers, String[] names) {
    this.viewers = viewers;
    this.viewerNames = names;
    currentViewer = viewers[0];
    if (currentViewer.hasViewingComponent()) {
      registerComponent(currentViewer.getViewingComponent());
    }
  }

  public Viewer getCurrentViewer() {
    return currentViewer;
  }
  
  private void registerComponent(Component c) {
    c.addKeyListener(eventDispatcher);
    c.addMouseListener(eventDispatcher);
    c.addMouseMotionListener(eventDispatcher);
    c.addMouseWheelListener(eventDispatcher);
    component.add("Center", c);
    component.revalidate();
  }
    
  private void unregisterComponent(Component c) {
    c.removeKeyListener(eventDispatcher);
    c.removeMouseListener(eventDispatcher);
    c.removeMouseMotionListener(eventDispatcher);
    c.removeMouseWheelListener(eventDispatcher);
    component.removeAll();
  }
  
  public void selectViewer(int i) {
    if (viewers[i] == currentViewer) return;
    Viewer newViewer = viewers[i];
    newViewer.setSceneRoot(currentViewer.getSceneRoot());
    newViewer.setCameraPath(currentViewer.getCameraPath());
    try {
      newViewer.setAuxiliaryRoot(currentViewer.getAuxiliaryRoot());
      newViewer.setSignature(currentViewer.getSignature());
    } catch (Exception e) {}
    
    currentViewer.setCameraPath(null);
    currentViewer.setSceneRoot(null);
    try {
      currentViewer.setAuxiliaryRoot(null);
    } catch (Exception e) {}

    if (currentViewer.hasViewingComponent()) unregisterComponent(currentViewer.getViewingComponent());
    if (newViewer.hasViewingComponent()) {
      if (currentViewer.hasViewingComponent())
        newViewer.getViewingComponent().setSize(currentViewer.getViewingComponent().getSize());
      registerComponent(newViewer.getViewingComponent());
    }
    currentViewer = newViewer;
  }
  
  public int getNumViewers() {
    return viewers.length;
  }
  
  public String[] getViewerNames() {
    return (String[]) viewerNames.clone();
  }
  
  public void selectViewer(String viewerName) {
    for (int i = 0; i < viewerNames.length; i++) {
      if (viewerNames[i].equals(viewerName)) {
        selectViewer(i);
        return;
      }
    }
    throw new IllegalArgumentException("no such viewer "+viewerName);
  }

  public SceneGraphComponent getSceneRoot() {
    return currentViewer.getSceneRoot();
  }

  public SceneGraphComponent getAuxiliaryRoot() {
    return currentViewer.getAuxiliaryRoot();
  }

  public SceneGraphPath getCameraPath() {
    return currentViewer.getCameraPath();
  }

  public int getSignature() {
    return currentViewer.getSignature();
  }

  public Component getViewingComponent() {
    return component;
  }

  public boolean hasViewingComponent() {
    return true;
  }

  public void render() {
    currentViewer.render();
  }

  public void setAuxiliaryRoot(SceneGraphComponent ar) {
    currentViewer.setAuxiliaryRoot(ar);
  }

  public void setCameraPath(SceneGraphPath p) {
    currentViewer.setCameraPath(p);
  }

  public void setSceneRoot(SceneGraphComponent r) {
    currentViewer.setSceneRoot(r);
  }

  public void setSignature(int sig) {
    currentViewer.setSignature(sig);
  }
  
  private class EventDispatcher implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
    
      public void keyPressed(KeyEvent e) {
        component.dispatchEvent(e);
      }
      public void keyReleased(KeyEvent e) {
        component.dispatchEvent(e);
      }
      public void keyTyped(KeyEvent e) {
        component.dispatchEvent(e);
      }
      public void mouseClicked(MouseEvent e) {
        component.dispatchEvent(e);
      }
      public void mouseEntered(MouseEvent e) {
        component.dispatchEvent(e);
      }
      public void mouseExited(MouseEvent e) {
        component.dispatchEvent(e);
      }
      public void mousePressed(MouseEvent e) {
        component.dispatchEvent(e);
      }
      public void mouseReleased(MouseEvent e) {
        component.dispatchEvent(e);
      }
      public void mouseDragged(MouseEvent e) {
        component.dispatchEvent(e);
      }
      public void mouseMoved(MouseEvent e) {
        component.dispatchEvent(e);
      }
      public void mouseWheelMoved(MouseWheelEvent e) {
        component.dispatchEvent(e);
      }
  }

}