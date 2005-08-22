package de.jreality.scene.proxy.smrj;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import de.jreality.geometry.QuadMeshShape;
import de.jreality.scene.data.ByteBufferList;
import de.jreality.scene.data.DataListSet;
import de.jreality.scene.proxy.ProxyFactory;
import de.jreality.scene.proxy.scene.*;
import de.smrj.RemoteFactory;

/**
 * this class should work like the inherited copy factory but copying objects on remote places
 * 
 * @author weissman
 */
public class SMRJMirrorFactory extends ProxyFactory {

    RemoteFactory rf;
    Object created;
    
    public SMRJMirrorFactory(RemoteFactory rf) {
        this.rf = rf;
     }
    
    public Object getProxy() {
        return created;
    }

    private Object createRemote(Class clazz) {
        try {
            return rf.createRemote(clazz);
        } catch (IOException ie) {
            throw new IllegalStateException("IO Error");
        }
    }

    public void visit(de.jreality.scene.Appearance a) {
        created=createRemote(Appearance.class);
        copyAttr(a, (RemoteAppearance) created);
    }

    public void visit(de.jreality.scene.Camera c) {
        created=createRemote(Camera.class);
        copyAttr(c, (RemoteCamera)created);
    }

    public void visit(de.jreality.scene.Cylinder c) {
        created=createRemote(Cylinder.class);
        copyAttr(c, (RemoteCylinder)created);
    }

    public void visit(de.jreality.scene.DirectionalLight l) {
        created=createRemote(DirectionalLight.class);
        copyAttr(l, (RemoteDirectionalLight) created);
    }

    public void visit(de.jreality.scene.IndexedFaceSet i) {
        created=createRemote(IndexedFaceSet.class);
        copyAttr(i, (RemoteIndexedFaceSet)created);
    }

    public void visit(de.jreality.scene.IndexedLineSet ils) {
        created=createRemote(IndexedLineSet.class);
        copyAttr(ils, (RemoteIndexedLineSet)created);
    }

    public void visit(de.jreality.scene.PointSet p) {
        created=createRemote(PointSet.class);
        copyAttr(p, (RemotePointSet)created);
    }

    public void visit(QuadMeshShape q) {
    	visit((de.jreality.scene.IndexedFaceSet) q);
    }    

    public void visit(de.jreality.scene.SceneGraphComponent c) {
        created=createRemote(SceneGraphComponent.class);
        copyAttr(c, (RemoteSceneGraphComponent)created);
    }

    public void visit(de.jreality.scene.Sphere s) {
        created=createRemote(Sphere.class);
        copyAttr(s, (RemoteSphere)created);
    }

    public void visit(de.jreality.scene.SpotLight l) {
        created=createRemote(SpotLight.class);
        copyAttr(l, (RemoteSpotLight)created);
    }

    public void visit(de.jreality.scene.ClippingPlane c) {
        created=createRemote(ClippingPlane.class);
        copyAttr(c, (RemoteClippingPlane)created);
    }

    public void visit(de.jreality.scene.PointLight l) {
        created=createRemote(PointLight.class);
        copyAttr(l, (RemotePointLight)created);
    }

    public void visit(de.jreality.scene.Transformation t) {
        created=createRemote(Transformation.class);
        copyAttr(t, (RemoteTransformation)created);
    }

    public void visit(de.jreality.scene.SceneGraphNode m) {
        throw new IllegalStateException(m.getClass() + " not handled by "
                + getClass().getName());
    }

    public void copyAttr(de.jreality.scene.SceneGraphNode src,
            RemoteSceneGraphNode dst) {
        dst.setName(src.getName());
    }

    public void copyAttr(de.jreality.scene.SceneGraphComponent src,
            RemoteSceneGraphComponent dst) {
        copyAttr((de.jreality.scene.SceneGraphNode) src,
                (RemoteSceneGraphNode) dst);
    }

    public void copyAttr(de.jreality.scene.Appearance src, RemoteAppearance dst) {
        copyAttr((de.jreality.scene.SceneGraphNode) src,
                (RemoteSceneGraphNode) dst);
        Set lst = src.getStoredAttributes();
        for (Iterator i = lst.iterator(); i.hasNext(); ) {
          String aName = (String) i.next();
            dst.setAttribute(aName, src.getAppearanceAttribute(aName));
        }
    }

    public void copyAttr(de.jreality.scene.Transformation src,
            RemoteTransformation dst) {
        copyAttr((de.jreality.scene.SceneGraphNode) src,
                (RemoteSceneGraphNode) dst);
        dst.setMatrix(src.getMatrix());
    }

    public void copyAttr(de.jreality.scene.Light src, RemoteLight dst) {
        copyAttr((de.jreality.scene.SceneGraphNode) src,
                (RemoteSceneGraphNode) dst);
        dst.setColor(src.getColor());
        dst.setIntensity(src.getIntensity());
    }

    public void copyAttr(de.jreality.scene.DirectionalLight src,
            RemoteDirectionalLight dst) {
        copyAttr((de.jreality.scene.Light) src, (RemoteLight) dst);
    }

    public void copyAttr(de.jreality.scene.PointLight src, RemotePointLight dst) {
      
      copyAttr((de.jreality.scene.Light) src, (RemoteLight) dst);

      dst.setFalloffA0(src.getFalloffA0());
      dst.setFalloffA1(src.getFalloffA1());
      dst.setFalloffA2(src.getFalloffA2());
      dst.setUseShadowMap(src.isUseShadowMap());
      dst.setShadowMapX(src.getShadowMapX());
      dst.setShadowMapY(src.getShadowMapY());
      dst.setShadowMap(src.getShadowMap());
    }
    
    public void copyAttr(de.jreality.scene.SpotLight src, RemoteSpotLight dst) {
        
      copyAttr((de.jreality.scene.PointLight) src, (RemotePointLight) dst);
        
      dst.setConeAngle(src.getConeAngle());
      dst.setConeDeltaAngle(src.getConeDeltaAngle());
      dst.setDistribution(src.getDistribution());
    }

    public void copyAttr(de.jreality.scene.Geometry src, RemoteGeometry dst) {
        copyAttr((de.jreality.scene.SceneGraphNode) src,
                (RemoteSceneGraphNode) dst);
        dst.setGeometryAttributes(src.getGeometryAttributes());
    }

    public void copyAttr(de.jreality.scene.Sphere src, RemoteSphere dst) {
        copyAttr((de.jreality.scene.Geometry) src, (RemoteGeometry) dst);
    }

    public void copyAttr(de.jreality.scene.Cylinder src, RemoteCylinder dst) {
        copyAttr((de.jreality.scene.Geometry) src, (RemoteGeometry) dst);
    }

    public void copyAttr(de.jreality.scene.PointSet src, RemotePointSet dst) {
        copyAttr((de.jreality.scene.Geometry) src, (RemoteGeometry) dst);
        DataListSet dls = ByteBufferList.prepareDataListSet(src.getVertexAttributes());
        dst.setVertexCountAndAttributes(dls);
        ByteBufferList.releaseDataListSet(dls);
    }

    public void copyAttr(de.jreality.scene.IndexedLineSet src,
            RemoteIndexedLineSet dst) {
        copyAttr((de.jreality.scene.PointSet) src, (RemotePointSet) dst);
        DataListSet dls = ByteBufferList.prepareDataListSet(src.getEdgeAttributes());
        dst.setEdgeCountAndAttributes(dls);
        ByteBufferList.releaseDataListSet(dls);
    }

    public void copyAttr(de.jreality.scene.IndexedFaceSet src,
            RemoteIndexedFaceSet dst) {
        copyAttr((de.jreality.scene.IndexedLineSet) src,
                (RemoteIndexedLineSet) dst);
        DataListSet dls = ByteBufferList.prepareDataListSet(src.getFaceAttributes());
        dst.setFaceCountAndAttributes(dls);
        ByteBufferList.releaseDataListSet(dls);
    }

    public void copyAttr(de.jreality.scene.Camera src, RemoteCamera dst) {
        copyAttr((de.jreality.scene.SceneGraphNode) src,
                (RemoteSceneGraphNode) dst);
//        dst.setAspectRatio(src.getAspectRatio());
        dst.setEyeSeparation(src.getEyeSeparation());
        dst.setFar(src.getFar());
        dst.setFieldOfView(src.getFieldOfView());
        dst.setFocus(src.getFocus());
        dst.setNear(src.getNear());
        dst.setOnAxis(src.isOnAxis());
        dst.setOrientationMatrix(src.getOrientationMatrix());
        dst.setPerspective(src.isPerspective());
//        dst.setSignature(src.getSignature());
        dst.setStereo(src.isStereo());
        dst.setViewPort(src.getViewPort().getX(), src.getViewPort().getY(),
            src.getViewPort().getWidth(), src.getViewPort().getHeight());
    }

}
