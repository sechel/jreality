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
package de.jreality.scene.proxy.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * @author weissman
 *
 * This interface provides creation of remote SceneGraphNodes. A client that wants to distribute its
 * sceneGraph can create a mirror with that Factory and gets the remote references to these objects back.
  */
public interface RemoteSceneGraphElementsFactory extends Remote {

	public RemoteAppearance createRemoteAppearance() throws RemoteException;
	public RemoteCamera createRemoteCamera() throws RemoteException;
	public RemoteClippingPlane createRemoteClippingPlane() throws RemoteException;
	public RemoteCylinder createRemoteCylinder() throws RemoteException;
	public RemoteDirectionalLight createRemoteDirectionalLight() throws RemoteException;
	public RemoteGeometry createRemoteGeometry() throws RemoteException;
	public RemoteIndexedFaceSet createRemoteIndexedFaceSet() throws RemoteException;
	public RemoteIndexedLineSet createRemoteIndexedLineSet() throws RemoteException;
	public RemoteLight createRemoteLight() throws RemoteException;
	public RemotePointLight createRemotePointLight() throws RemoteException;
	public RemotePointSet createRemotePointSet() throws RemoteException;
	public RemoteSceneGraphComponent createRemoteSceneGraphComponent() throws RemoteException;
	public RemoteSceneGraphNode createRemoteSceneGraphNode() throws RemoteException;
	public RemoteSphere createRemoteSphere() throws RemoteException;
	public RemoteSpotLight createRemoteSpotLight() throws RemoteException;
	public RemoteTransformation createRemoteTransformation() throws RemoteException;
	public RemoteSceneGraphPath createRemoteSceneGraphPath(List l) throws RemoteException;
}
