/*
 * Created on Jul 14, 2004
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

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import javax.swing.JFrame;

import de.jreality.remote.util.INetUtilities;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.proxy.rmi.RemoteSceneGraphComponent;
import de.jreality.scene.proxy.rmi.RemoteSceneGraphElementsFactory;
import de.jreality.scene.proxy.rmi.RemoteSceneGraphElementsFactoryImpl;
import de.jreality.scene.proxy.rmi.RemoteSceneGraphPath;
import de.jreality.scene.proxy.rmi.SceneGraphComponent;
import de.jreality.soft.DefaultViewer;
import de.jreality.util.ConfigurationAttributes;

/**
 * This is a simple remote viewer that encapsulates the Viewer given to the Constructor.
 * It reads window properties such as size and title from the Properies file set as "jreality.config"
 * default value for this file is "jreality.props" (in the current folder).
 * 
 * The attributes used are:<br>
 * <ul>
 * <li> frame.title
 * <li> frame.width
 * <li> frame.height
 * <li> camera.name (The default camera name is "defaultCamera"
 * so make sure to have such a camera path in the remote SceneGraph)
 * </ul>
 *
 * @author weissman
 *  
 */
public class RemoteViewerImpl implements RemoteViewer {

	String hostname;
	Viewer viewer;
	ConfigurationAttributes config;
	RemoteSceneGraphElementsFactoryImpl factory;
	JFrame f;
	java.awt.event.ActionListener connector = new java.awt.event.ActionListener() {
	      public void actionPerformed(java.awt.event.ActionEvent evt) {
	          connect();
	      }
	};
	javax.swing.Timer autoConnector = new javax.swing.Timer(500, connector);
	

	public RemoteViewerImpl(Viewer viewer) throws RemoteException {
		hostname = INetUtilities.getHostname();
		this.viewer = viewer;
		if (!viewer.hasViewingComponent()) throw new RuntimeException("expecting viewer with component!");
		Thread.currentThread().setName("RemoteViewerImpl");
		config = ConfigurationAttributes.getSharedConfiguration();

		// frame settings
		f = new JFrame(config.getProperty("frame.title", "no title"));
		if (config.getBool("frame.fullscreen")) {
			f.dispose();
			f.setUndecorated(true);
			f.getGraphicsConfiguration().getDevice().setFullScreenWindow(f);
		} else {
			f.setSize(config.getInt("frame.width"), config
					.getInt("frame.height"));
			f.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					disconnect();
				}
			});
		}
		f.getContentPane().add(viewer.getViewingComponent());
	}
	
	public RemoteSceneGraphElementsFactory getFactory() throws RemoteException {
        if (factory == null) factory = new RemoteSceneGraphElementsFactoryImpl();
		return factory;
	}

	public RemoteSceneGraphComponent getRemoteSceneRoot() {
		return (RemoteSceneGraphComponent) viewer.getSceneRoot();
	}

    public void setRemoteSceneRoot(RemoteSceneGraphComponent r) {
        System.out.println("Setting scene root to ["+r.toString()+"] ");
        viewer.setSceneRoot(getLocal(r));
    }

    protected de.jreality.scene.SceneGraphComponent getLocal(RemoteSceneGraphComponent r) {
        return (SceneGraphComponent)RemoteSceneGraphElementsFactoryImpl.getLocal(r);
    }

    public RemoteSceneGraphPath getRemoteCameraPath() {
		return (RemoteSceneGraphPath) viewer.getCameraPath();
	}

	public void setRemoteCameraPath(List list) {
		SceneGraphPath sgp = SceneGraphPath.fromList(RemoteSceneGraphElementsFactoryImpl.convertToLocal(list));
		System.out.println("[RemoteViewer->setCameraPath()] CameraPath: "+sgp.toString());
		viewer.setCameraPath(sgp);
		f.setVisible(list != null);
		try {
			Thread.sleep(10);
		} catch (Exception e) {}
		render();
		
	}

	public void render() {
		if (f.isVisible() && viewer.getSceneRoot() != null && viewer.getCameraPath() != null) viewer.render();
	}

	public int getSignature() {
		return viewer.getSignature();
	}

	public void setSignature(int sig) {
		viewer.setSignature(sig);
	}

	RemoteServer connectedHost;
	
	int conTryCount;
	protected void connect() {
		try {
			RemoteServer host = (RemoteServer) Naming.lookup(config.getProperty("server.uri"));
			if (connectedHost != null && connectedHost.equals(host)) return;
			host.connect(INetUtilities.getHostname(), config.getProperty("client.viewer.name"));
			connectedHost = host;
		} catch (Exception e) {
			connectedHost = null;
			if (conTryCount++%100 == 0) System.out.println("Server " + config.getProperty("server.uri") + " not found.");
			// throw new RuntimeException("RemoteViewerImpl Lookup failed. " + config.getProperty("server.uri"));
		}
		if (!autoConnector.isRunning()) {
			autoConnector.start();
		}
	}
	
	protected void disconnect() {
		autoConnector.stop();
		try {
			RemoteServer host = (RemoteServer) Naming.lookup(config.getProperty("server.uri"));
			host.disconnect(INetUtilities.getHostname(), config.getProperty("client.viewer.name"));
			connectedHost = null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("RemoteViewerImpl Lookup failed. " + config.getProperty("server.uri"));
		}
	}
	
	public void quit() {
		autoConnector.stop();
	  java.awt.event.ActionListener taskPerformer = new java.awt.event.ActionListener() {
	      public void actionPerformed(java.awt.event.ActionEvent evt) {
	          System.exit(0);
	      }
	  };
	  new javax.swing.Timer(50, taskPerformer).start();
	}
			
	public String getPreferredCameraName() {
		return config.getProperty("camera.name", "defaultCamera");
	}
		
	protected void bind() throws RemoteException, MalformedURLException {
        UnicastRemoteObject.exportObject(this);
		Naming.rebind("//"+hostname+"/"+config.getProperty("client.viewer.name"), this);
		System.out.println("RemoteViewer ["+"//"+hostname+"/"+config.getProperty("client.viewer.name")+"] bound in registry");
	}
		
	public static void main(String args[]) {
		String hostname = INetUtilities.getHostname();
		ConfigurationAttributes config = ConfigurationAttributes.getDefaultConfiguration();
		DefaultViewer viewer = new DefaultViewer(false); // new de.jreality.jogl.InteractiveViewer();
		viewer.setBackground(new Color(.4f,.5f,.8f));
//			de.jreality.jogl.InteractiveViewer viewer = new de.jreality.jogl.InteractiveViewer();
		try {
			RemoteViewerImpl obj = new RemoteViewerImpl(viewer);
			obj.bind();
			obj.connect();
		} catch (Exception e) {
			System.out.println("RemoteViewer err: " + e.getMessage());
			e.printStackTrace();
		}
		
	}
		
}