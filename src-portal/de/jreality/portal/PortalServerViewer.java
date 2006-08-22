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


package de.jreality.portal;

import java.awt.Dimension;
import java.io.IOException;
import java.net.ServerSocket;

import javax.swing.JPanel;

import de.jreality.scene.Lock;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.proxy.scene.RemoteSceneGraphComponent;
import de.jreality.scene.proxy.smrj.ClientFactory;
import de.jreality.scene.proxy.smrj.SMRJMirrorScene;
import de.smrj.Broadcaster;
import de.smrj.tcp.TCPBroadcasterNIO;
import de.smrj.tcp.management.JarServer;
import de.smrj.tcp.management.Local;

/**
 * @author weissman
 * 
 */
public class PortalServerViewer implements Viewer {

  SceneGraphComponent root;
  SceneGraphComponent auxRoot;
	SceneGraphPath camPath;
  private int signature;

	RemoteViewer clients;

	SMRJMirrorScene proxyScene;
  final Lock renderLock = new Lock();
  
  public PortalServerViewer() throws IOException {
    this(de.jreality.jogl.Viewer.class);
  }
  
	public PortalServerViewer(Class viewerClass) throws IOException {	
		int port = 8844;
		int cpPort = 8845;
		JarServer js = new JarServer(new ServerSocket(cpPort));
		Broadcaster bc = new TCPBroadcasterNIO(port);
		Local.sendStart(port, cpPort, ClientFactory.class);
		js.waitForDownloads();
		
    clients = bc.getRemoteFactory().createRemoteViaStaticMethod(
        HeadTrackedViewer.class, HeadTrackedViewer.class,
        "createFullscreen", new Class[]{Class.class}, new Object[]{viewerClass});
    proxyScene = new SMRJMirrorScene(bc.getRemoteFactory(), renderLock);
  }

  public SceneGraphComponent getSceneRoot() {
		return root;
	}

	public void setSceneRoot(SceneGraphComponent r) {
    if (root != null) {
      // dispose proxies for old root
      proxyScene.disposeProxy(root);
    }
		root = r;
    if (root != null) {
  		RemoteSceneGraphComponent rsgc = (RemoteSceneGraphComponent) proxyScene.createProxyScene(root);
	  	clients.setRemoteSceneRoot(rsgc);
    } else {
      clients.setRemoteSceneRoot(null);
    }
	}

	public SceneGraphPath getCameraPath() {
		return camPath;
	}

	public void setCameraPath(SceneGraphPath p) {
		camPath = p;
		clients.setRemoteCameraPath(p == null ? null : proxyScene.getProxies(p.toList()));
	}

	JPanel vcmp = new JPanel();
	
	public boolean hasViewingComponent() {
		//return false;
		return true;
	}

	public Object getViewingComponent() {
		//return null;
		return vcmp;
	}

    public Dimension getViewingComponentSize() {
		return vcmp.getSize();
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
    renderStart();
    renderEnd();
  }
  
  void renderStart() {
    renderLock.writeLock();
    clients.render();
  }

  void renderEnd() {
    clients.waitForRenderFinish();
    renderLock.writeUnlock();
  }

  public boolean canRenderAsync() {
    return false;
  }

  public void renderAsync() {
    throw new UnsupportedOperationException();
  }
  
}
