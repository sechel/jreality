/*
 * Created on 13-Nov-2004
 *
 * This file is part of the jReality package.
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
package de.jreality.remote;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.proxy.rmi.RemoteMirrorScene;
import de.jreality.scene.proxy.rmi.RemoteSceneGraphComponent;

/**
 * This class encapsulates a remote viewer together with its factory and the local Proxies.
 * It's responsible for updating the remote Objects and their local remoteReferences.
 * Sould react on all SceneGraphEvents by calling corresponding copyAttr Methods from the factory.
 *  
 * @author weissman
 * 
 */
public class RemoteServerClient {
	
	RemoteMirrorScene rmc;
	protected RemoteViewer viewer;
	final String clientURI;
	
	public RemoteServerClient(RemoteViewer viewer, String clientURI) {
		this.viewer = viewer;
		this.clientURI = clientURI;
	}
	
	public void setSceneRoot(SceneGraphComponent root) throws ClientDisconnectedException {
		if ( root == null ) {
			try {
				viewer.setRemoteSceneRoot(null);
			} catch (RemoteException e) {
				throw new ClientDisconnectedException(clientURI);
			}
			rmc = null;
		}
		else {
			try {
				rmc = new RemoteMirrorScene(viewer.getFactory());
			} catch (RemoteException e) {
				e.printStackTrace();
				throw new ClientDisconnectedException(clientURI);
			}
			RemoteSceneGraphComponent remoteRoot = (RemoteSceneGraphComponent) rmc.createProxyScene(root);
//			synchronizer = new SceneGraphSynchronizer(rmc);
//			root.accept(synchronizer);
			try {
				viewer.setRemoteSceneRoot(remoteRoot);
			} catch (RemoteException e) {
				throw new ClientDisconnectedException(clientURI);
			}
		}
		
	}
	
	/**
	 * sets the given Path as cameraPath for all viewers that have the given
	 * preferredCameraName.
	 * @param sp
	 * @param cameraName
	 */
	public void setCameraPathByName(SceneGraphPath sp, String cameraName) throws ClientDisconnectedException {
		try {
			if (viewer.getPreferredCameraName().equals(cameraName)) viewer.setRemoteCameraPath(rmc.getProxies(sp.toList()));
		} catch (RemoteException e) {
			throw new ClientDisconnectedException(clientURI);
		}
	}
	
	public void disconnect() throws ClientDisconnectedException {
		//setSceneRoot(null);
		try {
			viewer.quit();
		} catch (RemoteException e) {
			throw new ClientDisconnectedException(clientURI);
		}
	}
	
	public void render() throws ClientDisconnectedException {
		try {
			viewer.render();
		} catch (RemoteException e) {
			throw new ClientDisconnectedException(clientURI);
		}
	}

	public void setSignature(int sig) throws ClientDisconnectedException {
		try {
			viewer.setSignature(sig);
		} catch (RemoteException e) {
			throw new ClientDisconnectedException(clientURI);
		}
	}
	
	protected String getClientURI() {
		return clientURI;
	}

	/**
	 * @param cameraPaths
	 * @throws ClientDisconnectedException
	 */
	public void initClientCameraPath(Map cameraPaths) throws ClientDisconnectedException {
		try {
			List l = (List) cameraPaths.get(viewer.getPreferredCameraName());
			if (l == null) throw new IllegalStateException("no matching cam path for viewer "+clientURI);
		    viewer.setRemoteCameraPath(rmc.getProxies(l));
		} catch (RemoteException e) {
			throw new ClientDisconnectedException(clientURI);
		}
		
	}

}
