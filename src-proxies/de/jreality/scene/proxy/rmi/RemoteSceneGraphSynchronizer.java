/*
 * Created on 12-Nov-2004
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

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.event.AppearanceEvent;
import de.jreality.scene.event.AppearanceListener;
import de.jreality.scene.event.GeometryEvent;
import de.jreality.scene.event.GeometryListener;
import de.jreality.scene.event.SceneContainerEvent;
import de.jreality.scene.event.SceneContainerListener;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;

/**
 * 
 * This class registers itself to all nodes for keeping the remote scenegraph up-to-date.
 * 
 * TODO: implement this as 1-1 in the factory
 * 
 * @author weissman
 */
public class RemoteSceneGraphSynchronizer extends SceneGraphVisitor implements TransformationListener, AppearanceListener, GeometryListener, SceneContainerListener {
		
	RemoteMirrorScene rmc;
	RemoteMirrorFactory factory;
	private boolean debug;

	public RemoteSceneGraphSynchronizer(RemoteMirrorScene rmc) {
		this.rmc = rmc;
		factory = (RemoteMirrorFactory) rmc.getProxyFactory();
	}
	
	public void visit(final Transformation t) {
		t.addTransformationListener(this);
	}

	public void visit(final Appearance a) {
		a.addAppearanceListener(this);
	}

	public void visit(final Geometry g) {
		g.addGeometryListener(this);
	}
	
	public void visit(SceneGraphComponent sg) {
		sg.addSceneContainerListener(this);
	}
	
	public void transformationMatrixChanged(TransformationEvent ev) {
		try {
			((RemoteTransformation) rmc.getProxy(ev.getSource())).setMatrix(ev.getTransformationMatrix());
		} catch (RemoteException e) {
			ev.getTransformation().removeTransformationListener(this);
			e.printStackTrace();
		}
	}

	public void appearanceChanged(AppearanceEvent ev) {
		Appearance src = (Appearance) ev.getSourceNode();
		RemoteAppearance dst = (RemoteAppearance) rmc.getProxy(src);
        List lst= src.getChildNodes();
        try {
        	for (int ix= 0, num= lst.size(); ix < num; ix++) {
        		de.jreality.scene.AppearanceAttribute aa= (de.jreality.scene.AppearanceAttribute)lst.get(ix);
				dst.setAttribute(
				    aa.getAttributeName(),
				    aa.getValue(),
				    aa.getAttributeType());
            }
		} catch (RemoteException e) {
			src.removeAppearanceListener(this);
			e.printStackTrace();
		}
	}

	public void geometryChanged(GeometryEvent ev) {
//		System.out.println("geometryChanged: ");
		Geometry src = ev.getGeometry();
		RemoteGeometry dst = (RemoteGeometry) rmc.getProxy(src);
		try {
			for (Iterator i = ev.getChangedFaceAttributes().iterator(); i.hasNext();  ) {
				Attribute a = (Attribute) i.next();
				((RemoteIndexedFaceSet) dst).setAndCheckFaceCountAndAttributes(a, ((IndexedFaceSet) src).getFaceAttributes(a));
				if (debug) System.out.println("setFaceCountAndAttributes "+a+"\n"+((IndexedFaceSet) src).getFaceAttributes(a));
			}
			for (Iterator i = ev.getChangedEdgeAttributes().iterator(); i.hasNext();  ) {
				Attribute a = (Attribute) i.next();
				((RemoteIndexedLineSet) dst).setAndCheckEdgeCountAndAttributes(a, ((IndexedLineSet) src).getEdgeAttributes(a));
				if (debug) System.out.println("setEdgeCountAndAttributes "+a+"\n"+((IndexedLineSet) src).getEdgeAttributes(a));
			}
			for (Iterator i = ev.getChangedVertexAttributes().iterator(); i.hasNext();  ) {
				Attribute a = (Attribute) i.next();
				((RemotePointSet) dst).setAndCheckVertexCountAndAttributes(a, ((PointSet) src).getVertexAttributes(a));
				if (debug) System.out.println("setVertexCountAndAttributes "+a+"\n"+((PointSet) src).getVertexAttributes(a));
			}
			for (Iterator i = ev.getChangedGeometryAttributes().iterator(); i.hasNext();  ) {
				Attribute a = (Attribute) i.next();
				((RemoteGeometry) dst).setGeometryAttributes(a, ((Geometry) src).getGeometryAttributes(a));
				if (debug) System.out.println("setGeometryAttributes "+a+"\n"+((Geometry) src).getGeometryAttributes(a));
			}
//			if (!ev.getChangedFaceAttributes().isEmpty()) ((RemoteIndexedFaceSet) dst).setFaceCountAndAttributes(((IndexedFaceSet) src).getFaceAttributes());
//			if (!ev.getChangedEdgeAttributes().isEmpty()) ((RemoteIndexedFaceSet) dst).setEdgeCountAndAttributes(((IndexedLineSet) src).getEdgeAttributes());
//			if (!ev.getChangedVertexAttributes().isEmpty()) ((RemoteIndexedFaceSet) dst).setVertexCountAndAttributes(((PointSet) src).getVertexAttributes());
//			if (!ev.getChangedGeometryAttributes().isEmpty()) ((RemoteIndexedFaceSet) dst).setGeometryAttributes(((Geometry) src).getGeometryAttributes());
		} catch (RemoteException e) {
			src.removeGeometryListener(this);
			e.printStackTrace();
		}
//		System.out.println("geometryChanged finished. ");
	}

	public void childAdded(SceneContainerEvent ev) {
		//System.out.println("child added:"+ev.getNewChildElement().getName());
		RemoteSceneGraphComponent parent = (RemoteSceneGraphComponent) rmc.getProxy(ev.getParentElement());
		SceneGraphNode child = ev.getNewChildElement();
		RemoteSceneGraphNode remoteChild = (RemoteSceneGraphNode) rmc.createProxyScene(child);
		try {
			parent.add(remoteChild);
		} catch (RemoteException e) {
			ev.getParentElement().removeSceneContainerListener(this);
			e.printStackTrace();
		}
	}

	public void childRemoved(SceneContainerEvent ev) {
		//System.out.println("child REMOVED:"+ev.getOldChildElement().getName());
		RemoteSceneGraphComponent parent = (RemoteSceneGraphComponent) rmc.getProxy(ev.getParentElement());
		SceneGraphNode child = ev.getOldChildElement();
		try {
			parent.remove((RemoteSceneGraphNode) rmc.getProxy(child));
		} catch (RemoteException e) {
			ev.getParentElement().removeSceneContainerListener(this);
			e.printStackTrace();
		}
	}

  public void childReplaced(SceneContainerEvent ev) {
    childRemoved(ev); childAdded(ev);
  }
  
}
