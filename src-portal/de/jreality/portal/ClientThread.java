	/*
 * Created on Jul 27, 2004
 *
 * This file is part of the de.jreality.portal package.
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

import java.rmi.RemoteException;
import java.util.logging.Logger;
import java.util.logging.Level;

import de.jreality.portal.util.Debug;
import de.jreality.util.Lock;

/**
 * @author weissman
 * 
 * This Thread manages communication with one client.
 *  
 */
public class ClientThread extends Thread {

	private boolean ack;

	JoglRender renderClient;

	RemoteServerImpl server;

	private final Lock lock = new Lock();

	final static int SEND_NAVIGATION = 0;

	final static int SEND_HEAD_MATRIX = 1;

	final static int SWAP_BUFFERS = 2;

	final static int RENDER = 3;

	final static int LOAD_WORLD = 11;

	final static int DISCONNECT = 99;

	int currentAction;

	boolean running;

	static int counter;

	int id;

	public ClientThread(RemoteServerImpl server, JoglRender renderClient) {
		this.renderClient = renderClient;
		this.server = server;
		currentAction = -1;
		id = ++counter;
		setName("ClientThread-" + id);
	}

	public void start() {
		// prevent send calls before waiting...
		lock.readLock();
		running = true;
		super.start();
	}

	public void quit() {
		send(DISCONNECT);
		running = false;
		synchronized (this) {
			notify();
		}
	}

	/**
	 * following steps are executed:
	 * 
	 * 1.
	 */
	public void run() {
		while (running) {
			Debug.debug("(" + id + ") now unlocking...(1)");
			try {
				synchronized (this) {
					Debug.debug("(" + id + ") now unlocking...(2)");
					lock.readUnlock();
					Debug.debug("(" + id + ") unlocked, now waiting");
					wait();
				}
				Debug.debug("(" + id + ") woke up.");
			} catch (InterruptedException e) {
				throw new Error();
			} finally {
				Debug.debug("(" + id + ") getting readLock...");
				lock.readLock();
				Debug.debug("(" + id + ") ... readLock received.");
			}
			Debug.debug("(" + id + ") sending action " + currentAction);
			switch (currentAction) {
			case SEND_NAVIGATION:
				try {
					renderClient
							.setNavigationTransformation(server.worldTransform);
				} catch (RemoteException e) {
					Logger.getLogger("de.jreality").log(Level.SEVERE,
							"ClientThread - remote", e);
				}
				break;
			case SEND_HEAD_MATRIX:
				try {
					renderClient.setHeadPosition(server.headTransform);
				} catch (RemoteException e) {
					Logger.getLogger("de.jreality").log(Level.SEVERE,
							"ClientThread - remote", e);
				}
				break;
			case SWAP_BUFFERS:
				try {
					renderClient.swapBuffers();
				} catch (RemoteException e) {
					Logger.getLogger("de.jreality").log(Level.SEVERE,
							"ClientThread - remote", e);
				}
				break;
			case LOAD_WORLD:
				try {
					renderClient.loadWorld(classname);
				} catch (RemoteException e) {
					Logger.getLogger("de.jreality").log(Level.SEVERE,
							"ClientThread - remote", e);
				}
				break;
			case RENDER:
				try {
					renderClient.render();
				} catch (RemoteException e) {
					Logger.getLogger("de.jreality").log(Level.SEVERE,
							"ClientThread - remote", e);
				}
				break;
			case DISCONNECT:
				try {
					renderClient.quit();
				} catch (RemoteException e) {
					Logger.getLogger("de.jreality").log(Level.SEVERE,
							"ClientThread - remote", e);
				}
				break;
			default:
				Logger.getLogger("de.jreality").log(Level.WARNING,
						"ClientThread - unknown action " + currentAction, this);
			}
			server.removeBusy(this);
			Debug.debug("(" + id + ") removed");
		}
	}

	private void send(int action) {
		lock.writeLock();
		synchronized (this) {
			currentAction = action;
			Debug.debug("(" + id + ") sending action " + action);
			server.addBusy(this);
			lock.writeUnlock();
			Debug.debug("(" + id + ") writeLock for action " + action
					+ " removed.");
			notify();
		}
	}

	public void sendHeadTransformation() {
		send(SEND_HEAD_MATRIX);
	}

	public void sendNavigationMatrix() {
		send(SEND_NAVIGATION);
	}

	public void swapBuffers() {
		send(SWAP_BUFFERS);
	}

	public void render() {
		send(RENDER);
	}

	String classname = "";

	public void loadWorld(String classname) {
		this.classname = classname;
		send(LOAD_WORLD);
	}

}