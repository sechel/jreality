package de.jreality.io.jrs;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import de.jreality.io.JrScene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.IntArrayArray;

class JrSceneConverter implements Converter {

  Mapper mapper;
  
  public JrSceneConverter(Mapper mapper) {
    this.mapper = mapper;
  }

  public boolean canConvert(Class type) {
    return type == JrScene.class;
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    JrScene scene = (JrScene) source;
    writer.startNode("SceneRoot");
    context.convertAnother(scene.getSceneRoot());
    writer.endNode();
    writer.startNode("ScenePaths");
    for (Iterator it = scene.getScenePaths().entrySet().iterator(); it.hasNext(); ) {
      Map.Entry e = (Entry) it.next();
      writer.startNode("path");
      writer.addAttribute("name", (String) e.getKey());
      context.convertAnother(e.getValue());
      writer.endNode();
    }
    writer.endNode();
    writer.startNode("SceneAttributes");
    for (Iterator it = scene.getSceneAttributes().entrySet().iterator(); it.hasNext(); ) {
      Map.Entry e = (Entry) it.next();
      writer.startNode("attribute");
      writer.addAttribute("name", (String) e.getKey());
      writer.startNode(mapper.serializedClass(e.getValue().getClass()));
      context.convertAnother(e.getValue());
      writer.endNode();
      writer.endNode();
    }
    writer.endNode();
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    JrScene ret = new JrScene();
    
    reader.moveDown();
    SceneGraphComponent root = (SceneGraphComponent) context.convertAnother(null, SceneGraphComponent.class);
    reader.moveUp();
    
    ret.setSceneRoot(root);
    
    // paths
    reader.moveDown();
    while(reader.hasMoreChildren()) {
      reader.moveDown();
      String pathName = reader.getAttribute("name");
      SceneGraphPath path = (SceneGraphPath) context.convertAnother(null, SceneGraphPath.class);
      reader.moveUp();

      ret.addPath(pathName, path);
    }
    reader.moveUp();

    // attributes
    reader.moveDown();
    while(reader.hasMoreChildren()) {
      reader.moveDown();
      String attrName = reader.getAttribute("name");
      reader.moveDown();
      Class type = mapper.realClass(reader.getNodeName());
      Object obj = context.convertAnother(null, type);
      reader.moveUp();
      reader.moveUp();
      ret.addAttribute(attrName, obj);
    }
    reader.moveUp();
    
    return ret;
  }

}
