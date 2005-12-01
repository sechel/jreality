package de.jreality.io.jrs;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.ClippingPlane;
import de.jreality.scene.Cylinder;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Light;
import de.jreality.scene.PointLight;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Sphere;
import de.jreality.scene.SpotLight;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;

public class SceneGraphNodeConverter implements Converter {

  Mapper mapper;
  NodeStarter nodeStarter = new NodeStarter();
  NodeWriter nodeWriter = new NodeWriter();
  
  public SceneGraphNodeConverter(Mapper mapper) {
    this.mapper = mapper;
  }

  public boolean canConvert(Class type) {
    return SceneGraphNode.class.isAssignableFrom(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    SceneGraphNode n = (SceneGraphNode) source;
    nodeStarter.setUp(writer, context);
    n.accept(nodeStarter);
    nodeWriter.setUp(writer, context);
    n.accept(nodeWriter);
    writer.endNode();
  }

  public Object unmarshal(HierarchicalStreamReader reader,
      UnmarshallingContext context) {
    String str = reader.getValue();
    StringTokenizer toki = new StringTokenizer(str, " ");
    int len = toki.countTokens();
    if (reader.getNodeName().equals("d")) {
      double[] data = new double[len];
      for (int i = 0; toki.hasMoreTokens(); i++) data[i]=Double.parseDouble(toki.nextToken());
      return data;
   } else if (reader.getNodeName().equals("i")) {
      int[] data = new int[len];
      for (int i = 0; toki.hasMoreTokens(); i++) data[i]=Integer.parseInt(toki.nextToken());
      return data;
    }
    throw new Error();
  }
  
  private class NodeStarter extends SceneGraphVisitor {

    private HierarchicalStreamWriter writer;
    private MarshallingContext context;

    public void setUp(HierarchicalStreamWriter writer, MarshallingContext context) {
      this.writer=writer;
      this.context=context;
    }
    public void visit(Appearance a) {
      writer.startNode(mapper.serializedClass(Appearance.class));
    }
    public void visit(Camera c) {
      writer.startNode(mapper.serializedClass(Camera.class));
    }
    public void visit(ClippingPlane c) {
      writer.startNode(mapper.serializedClass(ClippingPlane.class));
    }
    public void visit(Cylinder c) {
      writer.startNode(mapper.serializedClass(Cylinder.class));
    }
    public void visit(DirectionalLight l) {
      writer.startNode(mapper.serializedClass(DirectionalLight.class));
    }
    public void visit(IndexedFaceSet i) {
      writer.startNode(mapper.serializedClass(IndexedFaceSet.class));
    }
    public void visit(IndexedLineSet g) {
      writer.startNode(mapper.serializedClass(IndexedLineSet.class));
    }
    public void visit(PointLight l) {
      writer.startNode(mapper.serializedClass(PointLight.class));
    }
    public void visit(PointSet p) {
      writer.startNode(mapper.serializedClass(PointSet.class));
    }
    public void visit(SceneGraphComponent c) {
      writer.startNode(mapper.serializedClass(SceneGraphComponent.class));
    }
    public void visit(Sphere s) {
      writer.startNode(mapper.serializedClass(Sphere.class));
    }
    public void visit(SpotLight l) {
      writer.startNode(mapper.serializedClass(SpotLight.class));
    }
    public void visit(Transformation t) {
      writer.startNode(mapper.serializedClass(Transformation.class));
    }
  };
  
}
