package de.jreality.io.jrs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Light;
import de.jreality.scene.PointLight;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.SpotLight;
import de.jreality.scene.Transformation;

/**
 * this class should work like the inherited copy factory but copying objects on remote places
 * 
 * @author weissman
 */
class NodeWriter extends SceneGraphVisitor {

  private HierarchicalStreamWriter writer;
  private MarshallingContext context;

  public void setUp(HierarchicalStreamWriter writer, MarshallingContext context) {
    this.writer = writer;
    this.context = context;
  }

  public void visit(de.jreality.scene.Appearance a) {
    copyAttr(a);
  }

  public void visit(de.jreality.scene.Camera c) {
    copyAttr(c);
  }

  public void visit(de.jreality.scene.Cylinder c) {
    copyAttr(c);
  }

  public void visit(de.jreality.scene.DirectionalLight l) {
    copyAttr(l);
  }

  public void visit(de.jreality.scene.IndexedFaceSet i) {
    copyAttr(i);
  }

  public void visit(de.jreality.scene.IndexedLineSet ils) {
    copyAttr(ils);
  }

  public void visit(de.jreality.scene.PointSet p) {
    copyAttr(p);
  }

  public void visit(de.jreality.scene.SceneGraphComponent c) {
    copyAttr(c);
  }

  public void visit(de.jreality.scene.Sphere s) {
    copyAttr(s);
  }

  public void visit(de.jreality.scene.SpotLight l) {
    copyAttr(l);
  }

  public void visit(de.jreality.scene.ClippingPlane c) {
    copyAttr(c);
  }

  public void visit(de.jreality.scene.PointLight l) {
    copyAttr(l);
  }

  public void visit(de.jreality.scene.Transformation t) {
    copyAttr(t);
  }

  public void visit(de.jreality.scene.SceneGraphNode m) {
    throw new IllegalStateException(m.getClass() + " not handled by "
        + getClass().getName());
  }

  public void copyAttr(SceneGraphNode src) {
    write("name", src.getName());
  }

  public void copyAttr(SceneGraphComponent src) {
    copyAttr((SceneGraphNode) src);
    write("visible", src.isVisible());
    writer.startNode("children");
    for (Iterator i = src.getChildNodes().iterator(); i.hasNext(); ) {
      context.convertAnother(i.next());
    }
    writer.endNode();
  }

  public void copyAttr(Appearance src) {
    copyAttr((SceneGraphNode) src);
    Set lst = src.getStoredAttributes();
    for (Iterator i = lst.iterator(); i.hasNext();) {
      writer.startNode("appearanceAttribute");
      String aName = (String) i.next();
      writer.addAttribute("name", aName);
      context.convertAnother(src.getAppearanceAttribute(aName));
      writer.endNode();
    }
  }

  public void copyAttr(Transformation src) {
    copyAttr((SceneGraphNode) src);
    context.convertAnother(src.getMatrix());
  }

  public void copyAttr(Light src) {
    copyAttr((SceneGraphNode) src);
    write("color", src.getColor());
    write("intensity", src.getIntensity());
  }

  public void copyAttr(PointLight src) {
    copyAttr((Light) src);
    write("falloffA0", src.getFalloffA0());
    write("falloffA1", src.getFalloffA1());
    write("falloffA2", src.getFalloffA2());
    write("useShadowMap", src.isUseShadowMap());
    write("shadowMapX", src.getShadowMapX());
    write("shadowMapY", src.getShadowMapY());
    write("shadowMap", src.getShadowMap());
  }

  public void copyAttr(SpotLight src) {
    copyAttr((PointLight) src);
    write("coneAngle", src.getConeAngle());
    write("coneDeltaAngle", src.getConeDeltaAngle());
    write("distribution", src.getDistribution());
  }

  public void copyAttr(Geometry src) {
    copyAttr((SceneGraphNode) src);
    HashMap serializableGeometryAttributes = new HashMap();
    for (Iterator i = src.getGeometryAttributes().keySet().iterator(); i
        .hasNext();) {
      Object key = i.next();
      Object attr = src.getGeometryAttributes().get(key);
      if (attr instanceof Serializable) {
        serializableGeometryAttributes.put(key, attr);
      }
    }
    write("geometryAttributes", serializableGeometryAttributes);
  }

  public void copyAttr(PointSet src) {
    copyAttr((Geometry) src);
    write("vertexAttributes", src.getVertexAttributes());
  }

  public void copyAttr(IndexedLineSet src) {
    copyAttr((PointSet) src);
    write("edgeAttributes", src.getEdgeAttributes());
  }

  public void copyAttr(IndexedFaceSet src) {
    copyAttr((IndexedLineSet) src);
    write("faceAttributes", src.getFaceAttributes());
  }

  public void copyAttr(Camera src) {
    copyAttr((SceneGraphNode) src);
    //        src.setAspectRatio(src.getAspectRatio());
    write("eyeSeparation", src.getEyeSeparation());
    write("far", src.getFar());
    write("fieldOfView", src.getFieldOfView());
    write("focus", src.getFocus());
    write("near", src.getNear());
    write("onAxis", src.isOnAxis());
    write("orientationMatrix", src.getOrientationMatrix());
    write("perspective", src.isPerspective());
    // src.setSignature(src.getSignature());
    write("stereo", src.isStereo());
// if (src.getViewPort() != null)
// dst.setViewPort(src.getViewPort().getX(), src.getViewPort().getY(), src
//          .getViewPort().getWidth(), src.getViewPort().getHeight());
  }

  private void write(String name, double d) {
    write(name, new Double(d));
  }

  private void write(String name, int i) {
    write(name, new Integer(i));
  }

  private void write(String name, boolean b) {
    write(name, Boolean.valueOf(b));
  }

  private void write(String name, Object src) {
    writer.startNode(name);
    context.convertAnother(src);
    writer.endNode();
  }

}
