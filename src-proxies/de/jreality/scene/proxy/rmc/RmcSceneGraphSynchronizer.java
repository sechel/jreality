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
package de.jreality.scene.proxy.rmc;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
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
import de.jreality.scene.proxy.rmi.RemoteGeometry;
import de.jreality.scene.proxy.rmi.RemoteIndexedFaceSet;
import de.jreality.scene.proxy.rmi.RemoteIndexedLineSet;
import de.jreality.scene.proxy.rmi.RemotePointSet;

/**
 * 
 * This class registers itself to all nodes for keeping the remote scenegraph up-to-date.
 * 
 * TODO: implement this as 1-1 in the factory
 * 
 * @author weissman
 */
public class RmcSceneGraphSynchronizer extends SceneGraphVisitor implements TransformationListener, AppearanceListener, GeometryListener, SceneContainerListener {
		
	RmcMirrorScene rmc;
	RmcMirrorFactory factory;
	private boolean debug;

	public RmcSceneGraphSynchronizer(RmcMirrorScene rmc) {
		this.rmc = rmc;
		factory = (RmcMirrorFactory) rmc.getProxyFactory();
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
      	factory.execute("setMatrix", ev.getSource(), new Object[]{ev.getTransformationMatrix()});
	}

	public void appearanceChanged(AppearanceEvent ev) {
		Appearance src = (Appearance) ev.getSourceNode();
        List lst= src.getChildNodes();
        for (int ix= 0, num= lst.size(); ix < num; ix++) {
            de.jreality.scene.AppearanceAttribute aa= (de.jreality.scene.AppearanceAttribute)lst.get(ix);
			factory.execute("setAppearanceAttribute", src, 
				new Object[]{
					aa.getAttributeName(),
				    aa.getValue(),
				    aa.getAttributeType()
			});
        }
	}

//	public void geometryChanged(GeometryEvent ev) {
//		System.out.println("geometryChanged: ");
//		Geometry src = ev.getGeometry();
//		if (!ev.getChangedFaceAttributes().isEmpty()) factory.execute("setFaceCountAndAttributes", src, new Object[]{((IndexedFaceSet) src).getFaceAttributes()});
//		if (!ev.getChangedEdgeAttributes().isEmpty()) factory.execute("setEdgeCountAndAttributes", src, new Object[]{((IndexedLineSet) src).getEdgeAttributes()});
//		if (!ev.getChangedVertexAttributes().isEmpty()) factory.execute("setVertexCountAndAttributes", src, new Object[]{((PointSet) src).getVertexAttributes()});
//		if (!ev.getChangedGeometryAttributes().isEmpty()) factory.execute("setGeometryAttributes", src, new Object[]{src.getGeometryAttributes()});
//		System.out.println("geometryChanged finished. ");
//	}

	public void geometryChanged(GeometryEvent ev) {
		Geometry src = ev.getGeometry();
		for (Iterator i = ev.getChangedFaceAttributes().iterator(); i.hasNext();  ) {
			Attribute a = (Attribute) i.next();
			factory.execute("setAndCheckFaceCountAndAttributes", src, new Object[]{a, ((IndexedFaceSet) src).getFaceAttributes(a)});
		}
		for (Iterator i = ev.getChangedEdgeAttributes().iterator(); i.hasNext();  ) {
			Attribute a = (Attribute) i.next();
			factory.execute("setAndCheckEdgeCountAndAttributes", src, new Object[]{a, ((IndexedLineSet) src).getEdgeAttributes(a)});
		}
		for (Iterator i = ev.getChangedVertexAttributes().iterator(); i.hasNext();  ) {
			Attribute a = (Attribute) i.next();
			factory.execute("setAndCheckVertexCountAndAttributes", src, new Object[]{a, ((PointSet) src).getVertexAttributes(a)});
		}
		for (Iterator i = ev.getChangedGeometryAttributes().iterator(); i.hasNext();  ) {
			Attribute a = (Attribute) i.next();
			factory.execute("setGeometryAttributes", src, new Object[]{a, ((Geometry) src).getGeometryAttributes(a)});
		}
	}

		public void childAdded(SceneContainerEvent ev) {
		factory.execute("add", new Object[] {
   			rmc.getProxyImpl(ev.getParentElement()),
   			rmc.createProxyScene(ev.getNewChildElement())
    	});
	}

	public void childRemoved(SceneContainerEvent ev) {
		factory.execute("remove", new Object[] {
   			rmc.getProxyImpl(ev.getParentElement()),
   			rmc.getProxyImpl(ev.getOldChildElement())
    	});
	}

	public void childReplaced(SceneContainerEvent ev) {
		childRemoved(ev); childAdded(ev);
	}
  
}
