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
package de.jreality.scene.proxy.smrj;

import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.Arrays;
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
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.event.AppearanceEvent;
import de.jreality.scene.event.AppearanceListener;
import de.jreality.scene.event.GeometryEvent;
import de.jreality.scene.event.GeometryListener;
import de.jreality.scene.event.SceneContainerEvent;
import de.jreality.scene.event.SceneContainerListener;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.scene.proxy.rmi.*;

/**
 * 
 * This class registers itself to all nodes for keeping the remote scenegraph up-to-date.
 * 
 * TODO: implement this as 1-1 in the factory
 * 
 * @author weissman
 */
public class SMRJSceneGraphSynchronizer extends SceneGraphVisitor implements TransformationListener, AppearanceListener, GeometryListener, SceneContainerListener {
		
	SMRJMirrorScene rmc;
	SMRJMirrorFactory factory;
	private boolean debug;

	public SMRJSceneGraphSynchronizer(SMRJMirrorScene rmc) {
		this.rmc = rmc;
		factory = (SMRJMirrorFactory) rmc.getProxyFactory();
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
            ((RemoteTransformation)rmc.getProxy(ev.getSourceNode())).setMatrix(ev.getTransformationMatrix());
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

	public void appearanceChanged(AppearanceEvent ev) {
		Appearance src = (Appearance) ev.getSourceNode();
        RemoteAppearance dst = (RemoteAppearance) rmc.getProxy(src);
        List lst= src.getChildNodes();
        for (int ix= 0, num= lst.size(); ix < num; ix++) {
            de.jreality.scene.AppearanceAttribute aa= (de.jreality.scene.AppearanceAttribute)lst.get(ix);
            try {
                dst.setAttribute(
                        aa.getAttributeName(),
                        aa.getValue(),
                        aa.getAttributeType()
                );
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
	}

	public void geometryChanged(GeometryEvent ev) {
        Geometry src = ev.getGeometry();
        RemoteGeometry dst = (RemoteGeometry) rmc.getProxy(src);
        for (Iterator i = ev.getChangedFaceAttributes().iterator(); i.hasNext();) {
            Attribute a = (Attribute) i.next();
            DataList dl = ((IndexedFaceSet) src).getFaceAttributes(a);
            try {
                ((RemoteIndexedFaceSet) dst).setAndCheckFaceCountAndAttributes(a,
                        dl);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        for (Iterator i = ev.getChangedEdgeAttributes().iterator(); i.hasNext();) {
            Attribute a = (Attribute) i.next();
            DataList dl = ((IndexedLineSet) src).getEdgeAttributes(a);
            try {
                ((RemoteIndexedLineSet) dst).setAndCheckEdgeCountAndAttributes(a,
                        dl);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        for (Iterator i = ev.getChangedVertexAttributes().iterator(); i
                .hasNext();) {
            Attribute a = (Attribute) i.next();
            DataList dl = ((PointSet) src).getVertexAttributes(a);
            try {
                if (a == Attribute.COORDINATES || a == Attribute.NORMALS) {
                    DoubleArray da = dl.toDoubleArray(); 
                    ByteBufferWrapper bbw = ByteBufferWrapper.getInstance();
                    ByteBuffer bb = bbw.createWriteBuffer(da.getLength()*8);
                    da.toNativeByteBuffer(bb);
                    if (bb.remaining()>0) throw new RuntimeException("not all read! "+bb);
                    if (bbw.getDoubleLength() != da.getLength()) throw new RuntimeException("length differs!");
                    if (a == Attribute.COORDINATES) ((RemotePointSet) dst).setVertices(bbw, dl.toDoubleArrayArray().getLengthAt(0));
                    else ((RemotePointSet) dst).setVertexNormals(bbw, dl.toDoubleArrayArray().getLengthAt(0));
                } else {
                    ((RemotePointSet) dst).setAndCheckVertexCountAndAttributes(a, dl);
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        for (Iterator i = ev.getChangedGeometryAttributes().iterator(); i
                .hasNext();) {
            Attribute a = (Attribute) i.next();
            try {
                dst.setGeometryAttributes(a, src.getGeometryAttributes(a));
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

		public void childAdded(SceneContainerEvent ev) {
   			try {
                ((RemoteSceneGraphComponent)rmc.getProxyImpl(ev.getParentElement()))
                .add((RemoteSceneGraphNode) rmc.createProxyScene(ev.getNewChildElement()));
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
	}

	public void childRemoved(SceneContainerEvent ev) {
        try {
            ((RemoteSceneGraphComponent)rmc.getProxyImpl(ev.getParentElement()))
            .remove((RemoteSceneGraphNode) rmc.getProxyImpl(ev.getOldChildElement()));
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

	public void childReplaced(SceneContainerEvent ev) {
		childRemoved(ev); childAdded(ev);
	}
  
}
