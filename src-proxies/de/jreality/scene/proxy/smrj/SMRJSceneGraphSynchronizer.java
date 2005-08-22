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

import java.util.Iterator;

import de.jreality.scene.*;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Light;
import de.jreality.scene.PointLight;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SpotLight;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.ByteBufferList;
import de.jreality.scene.data.DataList;
import de.jreality.scene.event.*;
import de.jreality.scene.proxy.scene.*;

/**
 * 
 * This class registers itself to all nodes for keeping the remote scenegraph up-to-date.
 * 
 * TODO: implement this as 1-1 in the factory
 * 
 * @author weissman
 */
public class SMRJSceneGraphSynchronizer extends SceneGraphVisitor implements TransformationListener, AppearanceListener, GeometryListener, SceneGraphComponentListener, CameraListener, LightListener {
		
	SMRJMirrorScene rmc;
	SMRJMirrorFactory factory;

	public SMRJSceneGraphSynchronizer(SMRJMirrorScene rmc) {
		this.rmc = rmc;
		factory = (SMRJMirrorFactory) rmc.getProxyFactory();
	}
	
  public void visit(Camera c) {
    c.addCameraListener(this);
  }
  
  public void visit(Light l) {
    l.addLightListener(this);
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
		sg.addSceneGraphComponentListener(this);
	}
	
	public void transformationMatrixChanged(TransformationEvent ev) {
      	((RemoteTransformation)rmc.getProxy(ev.getSourceNode())).setMatrix(ev.getTransformationMatrix());
	}

	public void appearanceChanged(AppearanceEvent ev) {
    Appearance src = (Appearance) ev.getSourceNode();
    RemoteAppearance dst = (RemoteAppearance) rmc.getProxy(src);
    Object aa = src.getAppearanceAttribute(ev.getKey());
    dst.setAttribute(ev.getKey(), aa);
  }

	public void geometryChanged(GeometryEvent ev) {
        Geometry src = ev.getGeometry();
        RemoteGeometry dst = (RemoteGeometry) rmc.getProxy(src);
        for (Iterator i = ev.getChangedFaceAttributes().iterator(); i.hasNext();) {
            Attribute a = (Attribute) i.next();
            DataList dl = ((IndexedFaceSet) src).getFaceAttributes(a);
            if (ByteBufferList.canCopy(dl)) {
            	ByteBufferList copy = ByteBufferList.createByteBufferCopy(dl);
                ((RemoteIndexedFaceSet) dst).setFaceAttributes(a, copy);
                ByteBufferList.releaseList(copy);
            } else {
                ((RemoteIndexedFaceSet) dst).setFaceAttributes(a, dl);
            }            
        }
        for (Iterator i = ev.getChangedEdgeAttributes().iterator(); i.hasNext();) {
            Attribute a = (Attribute) i.next();
            DataList dl = ((IndexedLineSet) src).getEdgeAttributes(a);
            if (ByteBufferList.canCopy(dl)) {
            	ByteBufferList copy = ByteBufferList.createByteBufferCopy(dl);
                ((RemoteIndexedLineSet) dst).setEdgeAttributes(a, copy);
                ByteBufferList.releaseList(copy);
            } else {
                ((RemoteIndexedLineSet) dst).setEdgeAttributes(a, dl);
            }
        }
        for (Iterator i = ev.getChangedVertexAttributes().iterator(); i
                .hasNext();) {
            Attribute a = (Attribute) i.next();
            DataList dl = ((PointSet) src).getVertexAttributes(a);
            if (ByteBufferList.canCopy(dl)) {
            	ByteBufferList copy = ByteBufferList.createByteBufferCopy(dl);
                ((RemotePointSet) dst).setVertexAttributes(a, copy);
                ByteBufferList.releaseList(copy);
            } else {
                ((RemotePointSet) dst).setVertexAttributes(a, dl);
            }
        }
        for (Iterator i = ev.getChangedGeometryAttributes().iterator(); i
                .hasNext();) {
            Attribute a = (Attribute) i.next();
            dst.setGeometryAttributes(a, src.getGeometryAttributes(a));
        }
    }

		public void childAdded(SceneGraphComponentEvent ev) {
   			((RemoteSceneGraphComponent)rmc.getProxyImpl(ev.getSceneGraphComponent()))
            .add((RemoteSceneGraphNode) rmc.createProxyScene(ev.getNewChildElement()));
	}

	public void childRemoved(SceneGraphComponentEvent ev) {
        ((RemoteSceneGraphComponent)rmc.getProxyImpl(ev.getSceneGraphComponent()))
        .remove((RemoteSceneGraphNode) rmc.getProxyImpl(ev.getOldChildElement()));
	}

	public void childReplaced(SceneGraphComponentEvent ev) {
		childRemoved(ev); childAdded(ev);
	}

  public void visibilityChanged(SceneGraphComponentEvent ev) {
    ((RemoteSceneGraphComponent)rmc.getProxyImpl(ev.getSceneGraphComponent())).setVisible(ev.getSceneGraphComponent().isVisible());
  }

  public void cameraChanged(CameraEvent ev) {
    Camera src = ev.getCamera();
    RemoteCamera dst = (RemoteCamera) rmc.getProxyImpl(ev.getCamera());
    dst.setEyeSeparation(src.getEyeSeparation());
    dst.setFar(src.getFar());
    dst.setFieldOfView(src.getFieldOfView());
    dst.setFocus(src.getFocus());
    dst.setNear(src.getNear());
    dst.setOnAxis(src.isOnAxis());
    dst.setOrientationMatrix(src.getOrientationMatrix());
    dst.setPerspective(src.isPerspective());
//    dst.setSignature(src.getSignature());
    dst.setStereo(src.isStereo());
    dst.setViewPort(src.getViewPort().getX(), src.getViewPort().getY(),
        src.getViewPort().getWidth(), src.getViewPort().getHeight());
  }

  public void lightChanged(LightEvent ev) {
    Light src = ev.getLight();
    RemoteLight dst = (RemoteLight) rmc.getProxyImpl(src);
    if (src instanceof SpotLight) {
      SpotLight src1 = (SpotLight) src;
      RemoteSpotLight dst1 = (RemoteSpotLight) dst;
      dst1.setConeAngle(src1.getConeAngle());
      dst1.setConeDeltaAngle(src1.getConeDeltaAngle());
      dst1.setDistribution(src1.getDistribution());
    }
    if (src instanceof PointLight) {
      PointLight src1 = (PointLight) src;
      RemotePointLight dst1 = (RemotePointLight) dst;
      dst1.setFalloffA0(src1.getFalloffA0());
      dst1.setFalloffA1(src1.getFalloffA1());
      dst1.setFalloffA2(src1.getFalloffA2());
      dst1.setUseShadowMap(src1.isUseShadowMap());
      dst1.setShadowMapX(src1.getShadowMapX());
      dst1.setShadowMapY(src1.getShadowMapY());
      dst1.setShadowMap(src1.getShadowMap());
    }
    dst.setColor(src.getColor());
    dst.setIntensity(src.getIntensity());
  }
  
}
