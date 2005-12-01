package de.jreality.io.jrs;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphNode;

public class SceneGraphNodeConverter implements Converter {

  Mapper mapper;
//  NodeStarter nodeStarter = new NodeStarter();
  NodeWriter nodeWriter = new NodeWriter();
  NodeReader nodeReader = new NodeReader();
  
  public SceneGraphNodeConverter(Mapper mapper) {
    this.mapper = mapper;
  }

  public boolean canConvert(Class type) {
    return SceneGraphNode.class.isAssignableFrom(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    SceneGraphNode n = (SceneGraphNode) source;
    nodeWriter.setUp(writer, context, mapper);
    n.accept(nodeWriter);
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    SceneGraphNode node = null;
    Class type = reader.getAttribute("type") == null ? context.getRequiredType() : mapper.realClass(reader.getAttribute("type"));
    if (!reader.hasMoreChildren()) return null;
    try {
      node = (SceneGraphNode) type.newInstance();
    } catch (Exception e) {
      return null;
    }
    nodeReader.setUp(reader, context, mapper);
    final SceneGraphNode fn = node;
    Scene.executeWriter(node, new Runnable() {
      public void run() {
        fn.accept(nodeReader);
      }
    });
    return node;
  }
  
}
