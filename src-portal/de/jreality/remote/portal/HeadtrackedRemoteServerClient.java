/*
 * Created on 20-Nov-2004
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
package de.jreality.remote.portal;

import java.rmi.RemoteException;

import de.jreality.remote.ClientDisconnectedException;
import de.jreality.remote.RemoteServerClient;

/**
 * @author gollwas
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public final class HeadtrackedRemoteServerClient extends RemoteServerClient {

	static int idCounter;
	int id = idCounter++;
	/**
	 * @param viewer
	 */
	public HeadtrackedRemoteServerClient(HeadtrackedRemoteViewer viewer, String clientURI) {
		super(viewer, clientURI);
		// TODO Auto-generated constructor stub
	}

	public void sendHeadTransformation(double[] matrix) throws ClientDisconnectedException {
		try {
			((HeadtrackedRemoteViewer)viewer).sendHeadTransformation(matrix);
		} catch (RemoteException e) {
			throw new ClientDisconnectedException(getClientURI());
		}
	}

	public void waitForRenderFinish() throws ClientDisconnectedException {
		try {
			((HeadtrackedRemoteViewer)viewer).waitForRenderFinish();
		} catch (RemoteException e) {
			throw new ClientDisconnectedException(getClientURI());
		}
	}

	public void swapBuffers() throws ClientDisconnectedException {
		try {
			((HeadtrackedRemoteViewer)viewer).swapBuffers();
		} catch (RemoteException e) {
			throw new ClientDisconnectedException(getClientURI());
		}
	}
	
	public void setBackgroundColor(java.awt.Color color) throws ClientDisconnectedException {
		try {
			((HeadtrackedRemoteViewer)viewer).setBackgroundColor(color);
		} catch (RemoteException e) {
			throw new ClientDisconnectedException(getClientURI());
		}
	}

	public void setManualSwapBuffers(boolean b) throws ClientDisconnectedException {
		try {
			((HeadtrackedRemoteViewer)viewer).setManualSwapBuffers(b);
		} catch (RemoteException e) {
			throw new ClientDisconnectedException(getClientURI());
		}
	}
	public void setUseDisplayLists(boolean b) throws ClientDisconnectedException {
		try {
			((HeadtrackedRemoteViewer)viewer).setUseDisplayLists(b);
		} catch (RemoteException e) {
			throw new ClientDisconnectedException(getClientURI());
		}
	}

}
