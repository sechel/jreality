package de.jreality.io.jrs;

import java.util.Iterator;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;

class SceneGraphPathConverter implements Converter {

  Mapper mapper;
  
  public SceneGraphPathConverter(Mapper mapper) {
    this.mapper = mapper;
  }

  public boolean canConvert(Class type) {
    return type == SceneGraphPath.class;
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    for (Iterator it = ((SceneGraphPath)source).iterator(); it.hasNext(); ) {
      writer.startNode("node");
      context.convertAnother((SceneGraphNode)it.next());
      writer.endNode();
    }
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    SceneGraphPath ret = new SceneGraphPath();
    while(reader.hasMoreChildren()) {
      reader.moveDown();
      ret.push((SceneGraphNode)context.convertAnother(null, SceneGraphNode.class));
      reader.moveUp();
    }
    return ret;
  }

}
