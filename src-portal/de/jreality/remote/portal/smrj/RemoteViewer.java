/*
 * Created on 09-Nov-2004
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
package de.jreality.remote.portal.smrj;

import java.util.List;
import java.rmi.Remote;
import java.rmi.RemoteException;

import de.jreality.scene.proxy.rmi.RemoteSceneGraphComponent;
import de.jreality.scene.proxy.rmi.RemoteSceneGraphElementsFactory;
import de.jreality.scene.proxy.rmi.RemoteSceneGraphPath;

/**
 * Interface in the spirit of de.jreality.scene.Viewer adapted to remote use.
 * 
 * @author weissman
 *
 */
public interface RemoteViewer extends de.jreality.remote.RemoteViewer {
	public void setRemoteSceneRoot(RemoteSceneGraphComponent r);
	public void setRemoteCameraPath(List list);
	public void render();
	public void setSignature(int sig);
}
