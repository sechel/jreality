package de.jreality.io.jrs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import de.jreality.scene.*;
import de.jreality.scene.tool.Tool;

/**
 * this class should work like the inherited copy factory but copying objects on remote places
 * 
 * @author weissman
 */
class NodeWriter extends SceneGraphVisitor {

  private HierarchicalStreamWriter writer;
  private MarshallingContext context;
  private Mapper mapper;

  public void setUp(HierarchicalStreamWriter writer, MarshallingContext context, Mapper mapper) {
    this.writer = writer;
    this.context = context;
    this.mapper = mapper;
  }

  public void visit(Appearance a) {
    copyAttr(a);
  }

  public void visit(Camera c) {
    copyAttr(c);
  }

  public void visit(Cylinder c) {
    copyAttr(c);
  }

  public void visit(DirectionalLight l) {
    writer.addAttribute("type", mapper.serializedClass(DirectionalLight.class));
    copyAttr(l);
  }

  public void visit(IndexedFaceSet i) {
    writer.addAttribute("type", mapper.serializedClass(IndexedFaceSet.class));
    copyAttr(i);
  }

  public void visit(IndexedLineSet ils) {
    writer.addAttribute("type", mapper.serializedClass(DirectionalLight.class));
    copyAttr(ils);
  }

  public void visit(PointSet p) {
    writer.addAttribute("type", mapper.serializedClass(PointSet.class));
    copyAttr(p);
  }

  public void visit(SceneGraphComponent c) {
    copyAttr(c);
  }

  public void visit(Sphere s) {
    writer.addAttribute("type", mapper.serializedClass(Sphere.class));
    copyAttr(s);
  }

  public void visit(SpotLight l) {
    writer.addAttribute("type", mapper.serializedClass(DirectionalLight.class));
    copyAttr(l);
  }

  public void visit(ClippingPlane c) {
    writer.addAttribute("type", mapper.serializedClass(ClippingPlane.class));
    copyAttr(c);
  }

  public void visit(PointLight l) {
    writer.addAttribute("type", mapper.serializedClass(PointLight.class));
    copyAttr(l);
  }

  public void visit(Transformation t) {
    copyAttr(t);
  }

  public void visit(SceneGraphNode m) {
    throw new IllegalStateException(m.getClass() + " not handled by "
        + getClass().getName());
  }

  public void copyAttr(SceneGraphNode src) {
    write("name", src.getName());
  }

  public void copyAttr(SceneGraphComponent src) {
    copyAttr((SceneGraphNode) src);
    write("visible", src.isVisible());
    write("transformation", src.getTransformation());
    write("appearance", src.getAppearance());
    write("camera", src.getCamera());
    write("light", src.getLight());
    write("geometry", src.getGeometry());
    writer.startNode("children");
    for (int i = 0; i < src.getChildComponentCount(); i++) {
      write("child", src.getChildComponent(i));
    }
    writer.endNode();
    writer.startNode("tools");
    for (Iterator i = src.getTools().iterator(); i.hasNext(); ) {
      Tool t = (Tool) i.next();
      writeUnknown(t);
    }
    writer.endNode();
  }

  public void copyAttr(Appearance src) {
    copyAttr((SceneGraphNode) src);
    Set lst = src.getStoredAttributes();
    for (Iterator i = lst.iterator(); i.hasNext();) {
      writer.startNode("attribute");
      String aName = (String) i.next();
      Object val = src.getAttribute(aName);
      writer.addAttribute("name", aName);
      writeUnknown(val);
      writer.endNode();
    }
  }

  public void copyAttr(Transformation src) {
    copyAttr((SceneGraphNode) src);
    write("matrix", src.getMatrix());
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
    write("eyeSeparation", src.getEyeSeparation());
    write("far", src.getFar());
    write("fieldOfView", src.getFieldOfView());
    write("focus", src.getFocus());
    write("near", src.getNear());
    write("onAxis", src.isOnAxis());
    write("orientationMatrix", src.getOrientationMatrix());
    write("perspective", src.isPerspective());
    write("stereo", src.isStereo());
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
    if (src != null) context.convertAnother(src);
    writer.endNode();
  }

  private void writeUnknown(Object src) {
    writer.startNode(mapper.serializedClass(src.getClass()));
    if (src != null) context.convertAnother(src);
    writer.endNode();
  }

}
