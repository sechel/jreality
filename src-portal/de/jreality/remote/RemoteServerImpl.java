/*
 * Created on Jul 5, 2004
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

import java.awt.Component;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.jreality.remote.util.INetUtilities;
import de.jreality.scene.Camera;
import de.jreality.scene.Drawable;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.LoadableScene;
import de.jreality.util.Lock;



/**
 * This class behaves like a usual viewer but distributes it for RemoteViewers.
 * RemoteViewers should know the name of the Camera / CameraPath they want to display.
 * 
 * TODO: Manage camera and cameraPath for LoadableScene
 *  
 * @author weissman
 *
 */
public class RemoteServerImpl extends UnicastRemoteObject implements RemoteServer, Viewer {


	private SceneGraphComponent root;
	private int signature;

	
	private ConfigurationAttributes config;
	
	private boolean swapBuffers = false;
	    
	protected final Lock clientMapLock = new Lock();
    private final Map clients = new HashMap(); 
	private final Map cameraPaths = new HashMap();
    	
	/**
	 * @return Returns the sWAP_BUFFERS.
	 */
	public boolean isSwapBuffers() {
		return swapBuffers;
	}
	/**
	 * @param swap_buffers The sWAP_BUFFERS to set.
	 */
	public void setSwapBuffers(boolean swap_buffers) {
		swapBuffers = swap_buffers;
	}

	/**
	 * @return Returns the clients.
	 */
	public Map getClients() {
		return clients;
	}
	/**
	 * @return Returns the config.
	 */
	public ConfigurationAttributes getConfig() {
		return config;
	}
	public RemoteServerImpl() throws RemoteException {
		Thread.currentThread().setName("RemoteServerImpl");
		config = ConfigurationAttributes.getDefaultConfiguration();
		swapBuffers = config.getBool("viewer.autoswap");		
	}

	/* 
	 * clients call this method to register as client
	 * 
	 * @param clientURI the rmi URI of the clients JoglRender 
	 */
	public boolean connect(String hostName, String viewerURL) throws RemoteException {
		System.out.println("client connect: "+hostName+"/"+viewerURL);
		String uri = new String("//"+hostName+"/"+viewerURL);
		RemoteViewer viewer = null;
		try {
			viewer = (RemoteViewer) Naming.lookup(uri);
			RemoteServerClient client = createServerClient(viewer, uri);
			// transfair / paint existing scene on new client
			if (getSceneRoot() != null) { 
				initClientScene(client);
				initClientCamera(client);
				client.render();
			}
			clientMapLock.writeLock();
			clients.put(uri, client);
			clientMapLock.writeUnlock();
		} catch (Exception e) {
			System.out.println("client connect failed.["+hostName+"/"+viewerURL+"] "+e.getMessage());
			return false;
		}
		System.out.println("client connect succeeded.["+hostName+"/"+viewerURL+"]");		
		return true;
	}
	
	protected void initClientScene(RemoteServerClient client) throws ClientDisconnectedException {
		client.setSceneRoot(getSceneRoot());
	}

	protected void initClientCamera(RemoteServerClient client) throws ClientDisconnectedException {
		client.initClientCameraPath(cameraPaths);
	}

	/**
	 * @param viewer
	 * @return
	 */
	protected RemoteServerClient createServerClient(RemoteViewer viewer, String uri) {
		return new RemoteServerClient(viewer, uri);
	}
	
	public boolean disconnect(String hostName, String viewerURL) throws RemoteException {
		String uri = new String("//"+hostName+"/"+viewerURL);
		System.out.println("client disconnect: "+uri);
		try {
			RemoteServerClient client = (RemoteServerClient) clients.get(uri);
			client.disconnect();
		} catch (Exception e) {
			System.out.println("client disconnect failed.");
			e.printStackTrace();
			return false;
		} finally {removeClient(uri);}
		System.out.println("client disconnect succeeded.");
		return true;
	}
		
	
    
    /******* Viewer INTERFACE Methods ***********/
    
	protected volatile boolean rendering = false;
	protected volatile boolean reRender = false;
	public void render() {
		if (rendering) { reRender = true; return; }
		rendering = reRender = true;
		while (reRender) {
			reRender = false;
			clientMapLock.writeLock();
			for (Iterator i = clients.values().iterator(); i.hasNext(); ) {
				try {
					((RemoteServerClient)i.next()).render();
				} catch (ClientDisconnectedException e) {
					//TODO: make this in a write lock
					i.remove();
				}
			}
			clientMapLock.writeUnlock();
		}
		rendering = false;
	}

	public SceneGraphComponent getSceneRoot() {
		return root;
	}

	public void setSceneRoot(SceneGraphComponent r) {
		root = r;
		clientMapLock.writeLock();
		for (Iterator i = clients.values().iterator(); i.hasNext(); ) {
			try {
				((RemoteServerClient)i.next()).setSceneRoot(getSceneRoot());
			} catch (ClientDisconnectedException e) {
				//TODO: make this in a write lock
				i.remove();
			}
		}
		clientMapLock.writeUnlock();		
	}

	public SceneGraphPath getCameraPath() {
		return (SceneGraphPath) cameraPaths.get("defaultCamera");
	}

	private SceneGraphPath getCameraPath(String cameraName) {
		return (SceneGraphPath) cameraPaths.get(cameraName);
	}

	public void setCameraPath(SceneGraphPath p) {
		setCameraPath(p, "defaultCamera");
	}

	/**
	 * maps the given cameraPath to cameraName
	 * @param p
	 * @param cameraName
	 */
	public void setCameraPath(SceneGraphPath p, String cameraName) {
		cameraPaths.put(cameraName, p);
	}
	
	/**
	 * traverses all known cameraPaths and passes them so the clients.
	 *
	 */
	public void sendCameraPaths() {
		clientMapLock.writeLock();
		for (Iterator i = clients.values().iterator(); i.hasNext(); ) {
			try {
				((RemoteServerClient) i.next()).initClientCameraPath(cameraPaths);
			} catch (ClientDisconnectedException e) {
				//TODO: make this in a write lock
				i.remove();
			}
		}
		clientMapLock.writeUnlock();		
	}
	
	/**
	 * this method creates all cameraPaths from root to each camera. the path is 
	 * named like the Camera.getName() value.
	 *
	 */
	public void generateCameraPaths() {
		SceneGraphVisitor camSearcher = new SceneGraphVisitor() {
			SceneGraphPath curPath = new SceneGraphPath();
			public void visit(Camera cam) {
				SceneGraphPath camPath = (SceneGraphPath) curPath.clone();
				camPath.push(cam);
				setCameraPath(camPath, cam.getName());
			}
			public void visit(SceneGraphComponent sgc) {
				curPath.push(sgc);
				sgc.childrenAccept(this);
				curPath.pop();
			}
		};
		getSceneRoot().accept(camSearcher);
	}

	
	public int getSignature() {
		return signature;
	}

	public void setSignature(int sig) {
		signature = sig;
		clientMapLock.writeLock();
		for (Iterator i = clients.values().iterator(); i.hasNext(); ) {
			try {
				((RemoteServerClient)i.next()).setSignature(getSignature());
			} catch (ClientDisconnectedException e) {
				//TODO: make this in a write lock
				i.remove();
			}
		}
		clientMapLock.writeUnlock();		
	}
	
	void removeClient(String clientURI) {
		clientMapLock.writeLock();
		clients.remove(clientURI);
		clientMapLock.writeUnlock();
	}

	/********** NOT USED *********/
	
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
		render();
	}
	
	/********      *********/
	
	
	public void loadWorld(String classname) {
		LoadableScene wm = null;
		try {
			wm = (LoadableScene) Class.forName(classname).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// scene settings
		wm.setConfiguration(config);
		de.jreality.scene.SceneGraphComponent world = wm.makeWorld();
		if (world != null)
			setSceneRoot(world);
		setSignature(wm.getSignature());
		System.out.println("loaded world "+classname+" successful.");
	}
	
	public void bind() {
		String hostname = INetUtilities.getHostname();
		try {
			Naming.rebind(ConfigurationAttributes.getSharedConfiguration().getProperty("server.uri"), this);
			System.out.println("RemoteServerImpl bound in registry "+ "rmi://" + hostname + "/jRealityRemoteServer");
		} catch (Exception e) {
			System.out.println("RemoteServerImpl err: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws RemoteException {
		RemoteServerImpl rsi = new RemoteServerImpl();
		rsi.bind();
		rsi.loadWorld(args[0]);
		rsi.generateCameraPaths();
	}

}