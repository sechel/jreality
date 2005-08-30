/*
 * Created on Apr 13, 2005
 *
 * This file is part of the  package.
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
package de.jreality.portal;

import java.awt.Component;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;

import szg.framework.event.HeadEvent;
import szg.framework.event.HeadMotionListener;
import szg.framework.event.remote.RemoteEventQueueImpl;
import de.jreality.math.Rn;
import de.jreality.scene.*;
import de.jreality.scene.proxy.scene.RemoteSceneGraphComponent;
import de.jreality.scene.proxy.smrj.SMRJMirrorScene;
import de.jreality.util.LoggingSystem;
import de.smrj.RemoteFactory;

/**
 * @author weissman
 * 
 */
public class PortalServerViewer implements Viewer {

  SceneGraphComponent root;
  SceneGraphComponent auxRoot;
	SceneGraphPath camPath;
  private int signature;

	RemoteFactory factory;
	RemoteJoglViewer clients;

	SMRJMirrorScene proxyScene;
  final Lock renderLock = new Lock();
  
	public PortalServerViewer(RemoteFactory factory) throws IOException,
			MalformedURLException, RemoteException, NotBoundException {
		this.factory = factory;
		clients = (RemoteJoglViewer) factory.createRemoteViaStaticMethod(
				PortalJoglClientViewer.class, PortalJoglClientViewer.class,
				"getInstance");
		proxyScene = new SMRJMirrorScene(factory, renderLock);
	}

	public SceneGraphComponent getSceneRoot() {
		return root;
	}

	public void setSceneRoot(SceneGraphComponent r) {
		this.root = r;
		RemoteSceneGraphComponent rsgc = (RemoteSceneGraphComponent) proxyScene.createProxyScene(root);
		clients.setRemoteSceneRoot(rsgc);
	}

	public SceneGraphPath getCameraPath() {
		return camPath;
	}

	public void setCameraPath(SceneGraphPath p) {
		camPath = p;
		clients.setRemoteCameraPath(proxyScene.getProxies(p.toList()));
	}

	/**
	 * TODO: open frame for keyboard/mouse input!?
	 */
	public boolean hasViewingComponent() {
		return false;
	}

	public Component getViewingComponent() {
		return null;
	}

	public boolean hasDrawable() {
		return false;
	}

	public Drawable getDrawable() {
		return null;
	}

	public void initializeFrom(Viewer v) {
	  setSceneRoot(v.getSceneRoot());
    setCameraPath(v.getCameraPath());
    setAuxiliaryRoot(v.getAuxiliaryRoot());
	}

	public int getSignature() {
		return signature;
	}

	public void setSignature(int sig) {
		this.signature = sig;
		clients.setSignature(this.signature);
	}

	public SceneGraphComponent getAuxiliaryRoot() {
	  return auxRoot;
	}

  public void setAuxiliaryRoot(SceneGraphComponent ar) {
    this.auxRoot = ar;
    RemoteSceneGraphComponent rsgc = (RemoteSceneGraphComponent) proxyScene.createProxyScene(auxRoot);
    clients.setRemoteAuxiliaryRoot(rsgc);
  }

  public void render() {
    renderLock.writeLock();
    clients.render();
    clients.waitForRenderFinish();
    renderLock.writeUnlock();
  }

}
