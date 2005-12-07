package de.jreality.portal;

import java.awt.Component;
import java.io.IOException;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;

public class DesktopPortalViewer implements Viewer {

  HeadTrackedViewer desktopViewer;
  PortalServerViewer portalViewer;
  
  public DesktopPortalViewer() {
    try {
      portalViewer = new PortalServerViewer();
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e.getMessage());
    }
    desktopViewer = new HeadTrackedViewer();
  }
  
  public DesktopPortalViewer(Class portalViewerClass) throws IOException {
    portalViewer = new PortalServerViewer(portalViewerClass);
    try {
      desktopViewer = new HeadTrackedViewer(portalViewerClass);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public SceneGraphComponent getSceneRoot() {
    return desktopViewer.getSceneRoot();
  }

  public void setSceneRoot(SceneGraphComponent r) {
    portalViewer.setSceneRoot(r);
    desktopViewer.setSceneRoot(r);
  }

  public SceneGraphPath getCameraPath() {
    return desktopViewer.getCameraPath();
  }

  public void setCameraPath(SceneGraphPath p) {
    portalViewer.setCameraPath(p);
    desktopViewer.setCameraPath(p);
  }

  public void render() {
    portalViewer.renderStart();
    desktopViewer.render();
    portalViewer.renderEnd();
  }

  public boolean hasViewingComponent() {
    return true;
  }

  public Component getViewingComponent() {
    return desktopViewer.getViewingComponent();
  }

  public int getSignature() {
    return desktopViewer.getSignature();
  }

  public void setSignature(int sig) {
    portalViewer.setSignature(sig);
    desktopViewer.setSignature(sig);
  }

  public void setAuxiliaryRoot(SceneGraphComponent ar) {
    portalViewer.setAuxiliaryRoot(ar);
    desktopViewer.setAuxiliaryRoot(ar);
  }

  public SceneGraphComponent getAuxiliaryRoot() {
    return desktopViewer.getAuxiliaryRoot();
  }

}
