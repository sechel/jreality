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

package de.jreality.portal;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.JFrame;

import de.jreality.jogl.Viewer;
import de.jreality.portal.util.INetUtilities;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.util.CameraUtility;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.LoadableScene;

/**
 * @author weissman
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class JoglRenderImpl extends UnicastRemoteObject implements JoglRender {

	de.jreality.portal.ViewerPORTAL viewer;

	ConfigurationAttributes config;

	private boolean fixedHead;
	
	/**
	 * @throws RemoteException
	 */
	protected JoglRenderImpl() throws RemoteException {
		super();
		Thread.currentThread().setName("JoglRenderImpl");
		config = ConfigurationAttributes.getSharedConfiguration();
		viewer = de.jreality.portal.ViewerPORTAL.getSharedInstance();
		//iewer.getRenderer().setUseDisplayLists(false);

		// stereo settings
		viewer.setStereoType(Viewer.CROSS_EYED_STEREO);
		CameraUtility.getCamera(viewer).setStereo(config.getBool("camera.stereo"));
		CameraUtility.getCamera(viewer).setEyeSeparation(
				config.getDouble("camera.eyeSeparation"));
		CameraUtility.getCamera(viewer).setOnAxis(false);

		// frame settings
		JFrame f = new JFrame(config.getProperty("frame.title", "no title"));
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
		f.show();
		if (config.getBool("portal.fixedHead")) {
			fixedHead = true;
			Transformation t = new Transformation();
			t.setTranslation(config.getDoubleArray("portal.fixedHeadPosition"));
			viewer.setCameraPosition(t);
		}
		viewer.getCamera().setNear(config.getDouble("camera.nearPlane"));
		viewer.getCamera().setFar(config.getDouble("camera.farPlane"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.jreality.portal.JoglViewer#init()
	 */
	public void init() throws RemoteException {
		// TODO Auto-generated method stub

	}
	
	public void quit() throws RemoteException {
		new Thread() {
			public void run() {
				try {
					sleep(500);
				} catch (InterruptedException ie) {}
				System.exit(0);
			}
		}.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.jreality.portal.JoglViewer#setHeadPosition(de.jreality.scene.Transformation)
	 */
	public void setHeadPosition(Transformation t) throws RemoteException {
		if (fixedHead)	return;
		viewer.setCameraPosition(t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.jreality.portal.JoglViewer#setWorldTransformation(de.jreality.scene.Transformation)
	 */
	public void setNavigationTransformation(Transformation t)
			throws RemoteException {
		viewer.setNavigationTransformation(t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.jreality.portal.JoglViewer#render()
	 */
	public void render() throws RemoteException {
		viewer.render();
	}

	public void swapBuffers() throws RemoteException {
		viewer.swapBuffers();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.jreality.portal.JoglViewer#loadWorld(java.lang.String)
	 */
	public void loadWorld(String classname) throws RemoteException {
		LoadableScene wm = null;
		try {
			wm = (LoadableScene) Class.forName(classname).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// scene settings
		wm.setConfiguration(config);
		SceneGraphComponent world = wm.makeWorld();
		if (world != null)
			viewer.setWorld(world);
		if (world.getTransformation() == null)
			world.setTransformation(new Transformation());
		viewer.setSignature(wm.getSignature());
		System.out.println("loaded world "+classname+" successful.");
	}

	public void showFrameRate() throws RemoteException {
		System.out.println("Framerate: " + viewer.getRenderer().getFramerate());
	}

	public static void main(String args[]) {
		String hostname = INetUtilities.getHostname();
		ConfigurationAttributes config = ConfigurationAttributes.getDefaultConfiguration();
		try {
			JoglRenderImpl obj = new JoglRenderImpl();
			// Bind this object instance to the name "HelloServer"
			Naming.rebind("//"+hostname+"/"+config.getProperty("client.app.name"), obj);

			obj.connect();

			System.out.println("JoglRenderServer bound in registry");
		} catch (Exception e) {
			System.out.println("JoglRenderImpl err: " + e.getMessage());
			e.printStackTrace();
		}
	}


	/**
	 * @param args
	 * @param hostname
	 */
	private void connect() {
		try {
			RemoteServer host = (RemoteServer) Naming.lookup(config.getProperty("server.uri"));
			host.connect("//"+INetUtilities.getHostname()+"/"+config.getProperty("client.app.name"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Lookup failed. " + config.getProperty("server.uri"));
		}
	}
	
	private void disconnect() {
		try {
			RemoteServer host = (RemoteServer) Naming.lookup(config.getProperty("server.uri"));
			host.disconnect("//"+INetUtilities.getHostname()+"/"+config.getProperty("client.app.name"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Lookup failed. " + config.getProperty("server.uri"));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.jreality.portal.JoglRender#setWorldTransformation()
	 */
	public Transformation getWorldTransformation() throws RemoteException {
		try {
			return ViewerPORTAL.getSharedInstance().getSceneRoot()
					.getTransformation();
		} catch (NullPointerException ne) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.jreality.portal.JoglRender#setFar(double)
	 */
	public void setFar(double dist) throws RemoteException {
		viewer.getCamera().setFar(dist);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.jreality.portal.JoglRender#setNear(double)
	 */
	public void setNear(double dist) throws RemoteException {
		viewer.getCamera().setNear(dist);
	}

}