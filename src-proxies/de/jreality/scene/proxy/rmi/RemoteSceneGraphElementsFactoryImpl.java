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
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class provides creation of remote SceneGraphNodes. A client that wants to distribute its
 * sceneGraph can create a mirror with that Factory and gets the remote references to these objects back.
 * 
 * @author weissman
 */
public class RemoteSceneGraphElementsFactoryImpl extends java.rmi.server.UnicastRemoteObject
		implements RemoteSceneGraphElementsFactory {

  static HashMap REMOTE_STUB_2_LOCAL = new HashMap();
  public static Object getLocal(RemoteSceneGraphNode stub)
  {
    Object obj=REMOTE_STUB_2_LOCAL.get(stub);
    if(obj!=null) return obj;
    throw new IllegalStateException("not local");
  }
  public static List convertToLocal(List l) {
  	List ret = new ArrayList();
  	for (int i = 0; i < l.size(); i++ ) ret.add(getLocal((RemoteSceneGraphNode)l.get(i)));
  	return ret;
  }
  static void prepare(Remote r) throws RemoteException
  {
    REMOTE_STUB_2_LOCAL.put(UnicastRemoteObject.exportObject(r), (Remote)r);
  }

	public RemoteSceneGraphElementsFactoryImpl() throws RemoteException {
		super();
	}

	public RemoteAppearance createRemoteAppearance() throws RemoteException {
		Appearance x=new Appearance();
		prepare(x);
		return x;
	}

	public RemoteCamera createRemoteCamera() throws RemoteException {
		Camera x=new Camera();
		prepare(x);
		return x;
	}

	public RemoteClippingPlane createRemoteClippingPlane()
			throws RemoteException {
		ClippingPlane x=new ClippingPlane();
		prepare(x);
		return x;
	}

	public RemoteCylinder createRemoteCylinder() throws RemoteException {
		Cylinder x=new Cylinder();
		prepare(x);
		return x;
	}

	public RemoteDirectionalLight createRemoteDirectionalLight()
			throws RemoteException {
		DirectionalLight x=new DirectionalLight();
		prepare(x);
		return x;
	}

	public RemoteGeometry createRemoteGeometry() throws RemoteException {
		Geometry x=new Geometry();
		prepare(x);
		return x;
	}

	public RemoteIndexedFaceSet createRemoteIndexedFaceSet()
			throws RemoteException {
		IndexedFaceSet x=new IndexedFaceSet();
		prepare(x);
		return x;
	}

	public RemoteIndexedLineSet createRemoteIndexedLineSet()
			throws RemoteException {
		IndexedLineSet x=new IndexedLineSet();
		prepare(x);
		return x;
	}

	public RemoteLight createRemoteLight() throws RemoteException {
		Light x=new Light();
		prepare(x);
		return x;
	}

	public RemotePointLight createRemotePointLight() throws RemoteException {
		PointLight x=new PointLight();
		prepare(x);
		return x;
	}

	public RemotePointSet createRemotePointSet() throws RemoteException {
		PointSet x=new PointSet();
		prepare(x);
		return x;
	}

//  private SceneGraphComponent first;
	public RemoteSceneGraphComponent createRemoteSceneGraphComponent()
			throws RemoteException {
		SceneGraphComponent x=new SceneGraphComponent();
//    if(first==null) {
//      first=x;
//          new Timer(2000, new ActionListener()
//    {
//      public void actionPerformed(ActionEvent e)
//      {
//        PrintScene.print(first);
//      }
//    }).start();
//
//    }
		prepare(x);
		return x;
	}

	public RemoteSceneGraphNode createRemoteSceneGraphNode()
			throws RemoteException {
		SceneGraphNode x=new SceneGraphNode();
		prepare(x);
		return x;
	}

	public RemoteSphere createRemoteSphere() throws RemoteException {
		Sphere x=new Sphere();
		prepare(x);
		return x;
	}

	public RemoteSpotLight createRemoteSpotLight() throws RemoteException {
		SpotLight x=new SpotLight();
		prepare(x);
		return x;
	}

	public RemoteTransformation createRemoteTransformation()
			throws RemoteException {
		Transformation x=new Transformation();
		prepare(x);
		return x;
	}

	public RemoteSceneGraphPath createRemoteSceneGraphPath() throws RemoteException {
		SceneGraphPath x=new SceneGraphPath();
		prepare(x);
		return x;
	}

	public RemoteSceneGraphPath createRemoteSceneGraphPath(List l) throws RemoteException {
    	SceneGraphPath path = (SceneGraphPath) SceneGraphPath.fromList(l);
    prepare(path);
    	return path;
	}
	
	public void foo() {
		System.out.println("foo");
	}

}
