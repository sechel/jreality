package de.jreality.portal;

import java.awt.Component;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.smrj.RemoteFactory;
import de.smrj.tcp.TCPBroadcasterNIO;

public class DesktopPortalViewer implements Viewer {

  Viewer desktopViewer;
  PortalServerViewer portalViewer;
  
  public DesktopPortalViewer() {
    try {
      RemoteFactory factory = new TCPBroadcasterNIO(8868).getRemoteFactory();
      portalViewer = new PortalServerViewer(factory);
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e.getMessage());
    }
    desktopViewer = new de.jreality.jogl.Viewer();
  }
  
  public DesktopPortalViewer(RemoteFactory factory, Class portalViewerClass) throws MalformedURLException, RemoteException, IOException, NotBoundException {
    portalViewer = new PortalServerViewer(factory, portalViewerClass);
    try {
      desktopViewer =(Viewer) portalViewerClass.newInstance();
    } catch (Exception e) {
      
    }
  }
  
  public SceneGraphComponent getSceneRoot() {
    return portalViewer.getSceneRoot();
  }

  public void setSceneRoot(SceneGraphComponent r) {
    portalViewer.setSceneRoot(r);
    desktopViewer.setSceneRoot(r);
  }

  public SceneGraphPath getCameraPath() {
    return portalViewer.getCameraPath();
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
    return portalViewer.getSignature();
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
    return portalViewer.getAuxiliaryRoot();
  }

}
