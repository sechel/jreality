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

package de.jreality.portal;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import szg.framework.distscenegraph.DistSceneGraphFramework;
import szg.framework.distscenegraph.DistSceneGraphFrameworkContext;
import szg.framework.event.HeadEvent;
import szg.framework.event.HeadMotionListener;
import szg.framework.event.WandEvent;
import szg.framework.event.WandListener;
import szg.framework.event.WandMotionListener;
import szg.java.FrameworkFactory;
import de.jreality.portal.util.Debug;
import de.jreality.portal.util.INetUtilities;
import de.jreality.scene.Transformation;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.Lock;
import de.jreality.util.P3;
import de.jreality.util.Rn;

/**
 * @author gollwas
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class RemoteServerImpl extends UnicastRemoteObject implements RemoteServer, WandListener, WandMotionListener,
		HeadMotionListener {


	public static void main(String[] args) throws RemoteException {
		String hostname = INetUtilities.getHostname();
		try {
		RemoteServerImpl rsi = new RemoteServerImpl();
		// Bind this object instance to the name "HelloServer"
		Naming.rebind(ConfigurationAttributes.getSharedConfiguration().getProperty("server.uri"), rsi);
		System.out.println("RemoteServerImpl bound in registry "+ "rmi://" + hostname + "/jRealityRemoteServer");
		} catch (Exception e) {
			System.out.println("RemoteServerImpl err: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private HashMap clientMap = new HashMap();
	private ClientThread[] clients;
	private ConfigurationAttributes config;
	boolean swapBuffers = false;
    private final HashMap busyClients = new HashMap();
    private final Object busyClientsSynch = new Object();
    
    private Lock locker = new Lock();
	
	private long timestamp;
	private boolean autoSwap;
	private String worldClassname;

	public RemoteServerImpl() throws RemoteException {
		Thread.currentThread().setName("RemoteServerImpl");
		config = ConfigurationAttributes.getDefaultConfiguration();
		autoSwap = config.getBool("viewer.autoswap");
		
		headTransform = new Transformation();
		worldTransform = new Transformation();
		wandStartMatrix = new Transformation();
		currentWandMatrix = new Transformation();
		wandDeltaMatrix = new Transformation();
		wandDeltaFractionMatrix = new Transformation();
		wandTransformation = new Transformation();
		actOnStartTransformation = new Transformation(this.worldTransform
				.getMatrix());
		DistSceneGraphFramework framework = FrameworkFactory
				.createSceneGraphFramework(
						new DistSceneGraphFrameworkContext(),
						new String[] { "createInstance<JsyzygyViewer>" });
		framework.start();
		framework.startEvents();
		framework.addHeadMotionListener(this);
		framework.addWandListener(this);
		framework.addWandMotionListener(this);
	}

	private ClientThread init(String uri) {
		try {
			JoglRender home = (JoglRender) Naming.lookup(uri);
			ClientThread ret = new ClientThread(this, home);
			ret.start();
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Lookup failed. " + uri);
		}
	}

	/* 
	 * clients call this method to register as client
	 * 
	 * @param clientURI the rmi URI of the clients JoglRender 
	 */
	public synchronized boolean connect(String clientURI) throws RemoteException {
		System.out.println("client connect: "+clientURI);
		try {
			addClient(clientURI);
		} catch (Exception e) {
			System.out.println("client connect failed.");
			return false;
		}
		System.out.println("client connect succeeded.");
		return true;
	}

	/* (non-Javadoc)
	 * @see de.jreality.portal.RemoteServer#disconnect(de.jreality.portal.JoglRender)
	 */
	public synchronized boolean disconnect(String clientURI) throws RemoteException {
		System.out.println("client disconnect: "+clientURI);
		try {
			removeClient(clientURI);
		} catch (Exception e) {
			System.out.println("client disconnect failed.");
			return false;
		}
		System.out.println("client disconnect succeeded.");
		return true;
	}

	private void addClient(String name) {
		ClientThread newClient = init(name);
		clientMap.put(name, newClient);
		clients = new ClientThread[clientMap.size()];
		Iterator i = clientMap.values().iterator();
		int index = 0;
		while (i.hasNext()) clients[index++] = (ClientThread) i.next();
//		if (worldClassname != null) {
//			newClient.loadWorld(worldClassname);
//		}
//		newClient.sendHeadTransformation();
//		waitForAll();
//		newClient.sendNavigationMatrix();
//		waitForAll();
	}

	private void removeClient(String name) {
		ClientThread removed = (ClientThread) clientMap.remove(name);
		clients = new ClientThread[clientMap.size()];
		Iterator i = clientMap.values().iterator();
		int index = 0;
		while (i.hasNext()) clients[index++] = (ClientThread) i.next();
		removed.quit();
	}

	/* (non-Javadoc)
	 * @see de.jreality.portal.RemoteServer#loadWorld(java.lang.String)
	 */
	public void loadWorld(String classname) throws RemoteException {
		lock();
		worldClassname = new String(classname);
		for (int i = 0; clients != null && i < clients.length; i++) {
				clients[i].loadWorld(classname);
		}
		waitAndRepaint();
		unlock();
		}
	
	private void sendWorldTransform() {
		lock();
		for (int i = 0; clients != null && i < clients.length; i++)
			clients[i].sendNavigationMatrix();
		waitAndRepaint();
		unlock();
		}
	
	private void sendHeadTransform() {
		lock();
		for (int i = 0; clients != null && i < clients.length; i++)
			clients[i].sendHeadTransformation();
		waitAndRepaint();
		unlock();
	}

	/**
	 * 
	 */
	private void waitAndRepaint() {
		waitForAll();
		if (!autoSwap) {
			swapBuffers();
			waitForAll();
		}
		render();
		waitForAll();
	}

	private void render() {
		for (int i = 0; clients != null && i < clients.length; i++)
			clients[i].render();
	}
	
	public void swapBuffers() {
		for (int i = 0; clients != null && i < clients.length; i++)
			clients[i].swapBuffers();
	}

	/**
	 * @param near
	 * @param far
	 */
	private void setClippingPlanes(double near, double far) {
			for (int i = 0; clients != null && i < clients.length; i++) {
				try {
					clients[i].renderClient.setNear(near);
					clients[i].renderClient.setFar(far);
				} catch (RemoteException re) {
					re.printStackTrace();
				}
			}	
	}

	
	/*************** portal event listener implementation ***************/
	
	Transformation actOnStartTransformation;
	Transformation currentWandMatrix;
	Transformation headTransform;
	Transformation wandDeltaFractionMatrix;
	Transformation wandDeltaMatrix;
	Transformation wandStartMatrix;
	Transformation wandTransformation;
	Transformation worldTransform;
	private boolean move;
	private boolean rotate;
	private boolean showSkybox;
	double[] wandStartPoint = new double[4];
	private static final boolean DEBUG = true;

	public void axisMoved(WandEvent event) {
		if (DEBUG) Debug.debug("begin");
		if (false) { //event.buttonPressed(1)) { // zoom
			Transformation wand = new Transformation(P3.transposeF2D(
					new double[16], event.getMatrix()));
			Transformation scale = new Transformation();
			scale.setStretch(1. + (event.getMainAxisValue() * 0.01));
				worldTransform.multiplyOnLeft(wand.getInverse());
				worldTransform.multiplyOnLeft(scale);
				worldTransform.multiplyOnLeft(wand);
				sendWorldTransform();
		} else { // move
			Transformation wand = new Transformation(P3.transposeF2D(
					new double[16], event.getMatrix()));
			Transformation move = new Transformation();
			double x = event.getAxisValue(0);
			double y = event.getAxisValue(1);
			x *= 1.1;
			y *= 1.1;
			move.setTranslation(x * x * x * 0.1, 0, y * y * y * 0.1);
				worldTransform.multiplyOnLeft(wand.getInverse());
				worldTransform.multiplyOnLeft(move);
				worldTransform.multiplyOnLeft(wand);
				if (DEBUG) Debug.debug("sending world transform");
				sendWorldTransform();
				if (DEBUG) Debug.debug("sending world transform done.");
			if (DEBUG) Debug.debug("end.");
		}
	}

	public void buttonPressed(WandEvent arg0) {
		if (arg0.getButton() == 0) {
			timestamp = System.currentTimeMillis();
			showSkybox = false;
		}
			wandStartMatrix.setMatrix(P3.transposeF2D(new double[16], arg0
				.getMatrix()));
			wandStartPoint = wandStartMatrix.getTranslation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see szg.framework.event.WandListener#buttonReleased(szg.framework.event.WandEvent)
	 */
	public void buttonReleased(WandEvent arg0) {
	}

	public void buttonTipped(WandEvent arg0) {
		if (arg0.getButton() == 2) {
				worldTransform.setMatrix(actOnStartTransformation.getMatrix());
				sendWorldTransform();
		}
		if (arg0.getButton() == 1) { // framerate
			new Thread() {
				int counter = 0;

				public void run() {
					while (counter++ < 100) {
						try {
							Thread.sleep(1000);
							clients[0].renderClient.showFrameRate();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}.start();
		}
	}

	long totHeadTime = 0;
	long totHeadSent = 0;
	
	public void headMoved(HeadEvent event) {
			headTransform.setMatrix(P3.transposeF2D(null, event.getMatrix()));
			//System.out.println("->shead moved:"+wandTransform);
			long start = System.currentTimeMillis();
			sendHeadTransform();
			totHeadTime += (System.currentTimeMillis() - start);
			if ((++totHeadSent) % 1000 == 0) System.out.println("average time for sending head transform: "+( totHeadTime / totHeadSent));
			
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see szg.framework.event.WandMotionListener#wandDragged(szg.framework.event.WandEvent)
	 */
	public void wandDragged(WandEvent arg0) {
		// rotate & translate
		if (arg0.getButton() == 0) {
				currentWandMatrix.setMatrix(P3.transposeF2D(new double[16], arg0
						.getMatrix()));
				double[] wandDeltaM = new double[16];
				Rn.times(wandDeltaM, currentWandMatrix.getMatrix(), wandStartMatrix
						.getInverse().getMatrix());
				wandDeltaMatrix.setMatrix(wandDeltaM);
				wandStartMatrix.setMatrix(currentWandMatrix.getMatrix());
				worldTransform.multiplyOnLeft(wandDeltaM);
				sendWorldTransform();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see szg.framework.event.WandMotionListener#wandMoved(szg.framework.event.WandEvent)
	 */
	public void wandMoved(WandEvent arg0) {
		wandTransformation.setMatrix(P3.transposeF2D(new double[16], arg0
				.getMatrix()));
	}

    void waitForAll() {
        if (DEBUG) {
        	Debug.debug(2, " waiting for all clients.");
        }
    	if (DEBUG) timestamp = System.currentTimeMillis();
        synchronized(busyClientsSynch)
        {
        	dumpBusy();
            while(!busyClients.isEmpty()) try {
            	busyClientsSynch.wait();
            	dumpBusy();
            } catch (InterruptedException e) {}
        }
        if (DEBUG) {
        	Debug.debug(2, " ------> waited "+(System.currentTimeMillis()-timestamp)+" ms for all clients.");
        }
    }

    // only use in synchronized block!!!!!!!!
    private void dumpBusy() {
    	Iterator i = busyClients.keySet().iterator();
    	while (i.hasNext()) {
    		ClientThread ct = (ClientThread) i.next();
    		Debug.debug(2, "waiting for "+ct.getName()+ " put by "+busyClients.get(ct));
    	}
    }
    
    void addBusy(ClientThread ct) {
        synchronized(busyClientsSynch)
        {
        	Debug.debug("putting client "+ct.getName());
        	if (busyClients.containsKey(ct)) {
            	String putter = (String) busyClients.get(ct);
        		throw new Error(ct.getName()+" inserted by "+putter+" again added by "+Thread.currentThread().getName());
        	}
            busyClients.put(ct, Thread.currentThread().getName());
//            busyClientsSynch.notify();
        }
    }
    
    void removeBusy(ClientThread ct) {
        synchronized(busyClientsSynch)
        {
            String putter = (String) busyClients.remove(ct);
            Debug.debug(ct.getName()+" inserted by "+putter+" removed by "+Thread.currentThread().getName());
            busyClientsSynch.notify();
        }
    }
    
    /* these methods assure that events are sent one by one, not cuncurrently */
    private void lock() {
    	locker.writeLock();
    }
    
    private void unlock() {
    	locker.writeUnlock();
    }
}