
package de.jreality.scene.proxy;

import java.util.Iterator;
import java.util.Set;

import de.jreality.geometry.QuadMeshShape;
import de.jreality.scene.*;

/**
 * 
 */
public class CopyFactory extends ProxyFactory {

    Object created;

    public Object getProxy() {
        return created;
    }

    public void visit(Appearance a) {
        Appearance newApp= new Appearance();
        copyAttr(a, newApp);
        created=newApp;
    }

    public void visit(Camera c) {
        Camera newCamera= new Camera();
        copyAttr(c, newCamera);
        created=newCamera;
    }

    public void visit(Cylinder c) {
        Cylinder newCyl= new Cylinder();
        copyAttr(c, newCyl);
        created=newCyl;
    }

    public void visit(DirectionalLight l) {
        DirectionalLight newLight=new DirectionalLight();
        copyAttr(l, newLight);
        created=newLight;
    }

    public void visit(IndexedFaceSet i) {
        IndexedFaceSet newIFS=new IndexedFaceSet();
        copyAttr(i, newIFS);
        created=newIFS;
    }

    public void visit(IndexedLineSet g) {
        IndexedLineSet newILS=new IndexedLineSet();
        copyAttr(g, newILS);
        created=newILS;
    }

    public void visit(PointSet p) {
        PointSet newPS=new PointSet();
        copyAttr(p, newPS);
        created=newPS;
    }

    public void visit(QuadMeshShape q) {
        QuadMeshShape newQMS=new QuadMeshShape();
        copyAttr(q, newQMS);
        created=newQMS;
    }

    public void visit(SceneGraphComponent c) {
        SceneGraphComponent newSGC=new SceneGraphComponent();
        copyAttr(c, newSGC);//Note: attributes does not include children
        created=newSGC;
    }

    public void visit(Sphere s) {
        Sphere newSphere= new Sphere();
        copyAttr(s, newSphere);
        created=newSphere;
    }

    public void visit(SpotLight l) {
        SpotLight newLight=new SpotLight();
        copyAttr(l, newLight);
        created=newLight;
    }

    public void visit(ClippingPlane c)
    {
        ClippingPlane newCP=new ClippingPlane();
        copyAttr(c, newCP);
        created=newCP;
    }

    public void visit(PointLight l)
    {
        PointLight newLight=new PointLight();
        copyAttr(l, newLight);
        created=newLight;
    }

    public void visit(Transformation t) {
        Transformation newTrans=new Transformation();
        copyAttr(t, newTrans);
        created=newTrans;
    }

  public void copyAttr(SceneGraphNode src, SceneGraphNode dst) {
    dst.setName(src.getName());
  }

  public void copyAttr(SceneGraphComponent src, SceneGraphComponent dst) {
      copyAttr((SceneGraphNode)src, (SceneGraphNode)dst);
  }

  public void copyAttr(Appearance src, Appearance dst) {
      Set lst = src.getStoredAttributes();
      for (Iterator i = lst.iterator(); i.hasNext(); ) {
        String aName = (String) i.next();
        dst.setAttribute(aName, src.getAppearanceAttribute(aName));
      }
      copyAttr((SceneGraphNode) src, (SceneGraphNode) dst);
  }

  public void copyAttr(Transformation src, Transformation dst) {
      dst.setMatrix(src.getMatrix());
      copyAttr((SceneGraphNode)src, (SceneGraphNode)dst);
  }

  public void copyAttr(Light src, Light dst) {
      dst.setColor(src.getColor());
      dst.setIntensity(src.getIntensity());
      copyAttr((SceneGraphNode)src, (SceneGraphNode)dst);
  }

  public void copyAttr(DirectionalLight src, DirectionalLight dst) {
      copyAttr((Light)src, (Light)dst);
  }

  public void copyAttr(SpotLight src, SpotLight dst) {
      dst.setConeAngle(src.getConeAngle());
      dst.setConeDeltaAngle(src.getConeDeltaAngle());
      dst.setFalloffA0(src.getFalloffA0());
      dst.setFalloffA1(src.getFalloffA1());
      dst.setFalloffA2(src.getFalloffA2());
      dst.setDistribution(src.getDistribution());
      dst.setUseShadowMap(src.isUseShadowMap());
      dst.setShadowMapX(src.getShadowMapX());
      dst.setShadowMapY(src.getShadowMapY());
      dst.setShadowMap(src.getShadowMap());
      copyAttr((Light)src, (Light)dst);
  }

  public void copyAttr(Geometry src, Geometry dst) {
  	  dst.setGeometryAttributes(src.getGeometryAttributes());
      copyAttr((SceneGraphNode)src, (SceneGraphNode)dst);
  }

  public void copyAttr(Sphere src, Sphere dst) {
      copyAttr((Geometry)src, (Geometry)dst);
  }
  
  public void copyAttr(Cylinder src, Cylinder dst) {
      copyAttr((Geometry)src, (Geometry)dst);
  }

  public void copyAttr(PointSet src, PointSet dst) {
    dst.setVertexCountAndAttributes(src.getVertexAttributes());
      copyAttr((Geometry)src, (Geometry)dst);
  }

  public void copyAttr(IndexedLineSet src, IndexedLineSet dst) {
    dst.setEdgeCountAndAttributes(src.getEdgeAttributes());
      copyAttr((PointSet)src, (PointSet)dst);
  }

  public void copyAttr(IndexedFaceSet src, IndexedFaceSet dst) {
    dst.setFaceCountAndAttributes(src.getFaceAttributes());
      copyAttr((PointSet)src, (PointSet)dst);
  }

//  public void copyAttr(QuadMeshShape src, QuadMeshShape dst) {
//      copyAttr((IndexedFaceSet)src, (IndexedFaceSet)dst);
//  }

  public void copyAttr(Camera src, Camera dst) {
//  	dst.setAspectRatio(src.getAspectRatio());
  	dst.setEyeSeparation(src.getEyeSeparation());
  	dst.setFar(src.getFar());
  	dst.setFieldOfView(src.getFieldOfView());
  	dst.setFocus(src.getFocus());
  	dst.setNear(src.getNear());
  	dst.setOnAxis(src.isOnAxis());
  	dst.setOrientationMatrix(src.getOrientationMatrix());
  	dst.setPerspective(src.isPerspective());
//  	dst.setSignature(src.getSignature());
  	dst.setStereo(src.isStereo());
  	dst.setViewPort(src.getViewPort());
    copyAttr((SceneGraphNode)src, (SceneGraphNode)dst);
  }

    public void visit(SceneGraphNode m) {
        throw new IllegalStateException(
          m.getClass()+" not handled by "+getClass().getName());
    }

}
