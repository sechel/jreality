package de.jreality.io.jrs;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import de.jreality.io.JrScene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.util.LoggingSystem;

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
    writer.startNode("sceneRoot");
    context.convertAnother(scene.getSceneRoot());
    writer.endNode();
    writer.startNode("scenePaths");
    for (Iterator it = scene.getScenePaths().entrySet().iterator(); it.hasNext(); ) {
      Map.Entry e = (Entry) it.next();
      writer.startNode("path");
      writer.addAttribute("name", (String) e.getKey());
      context.convertAnother(e.getValue());
      writer.endNode();
    }
    writer.endNode();
    writer.startNode("sceneAttributes");
    for (Iterator it = scene.getSceneAttributes().entrySet().iterator(); it.hasNext(); ) {
      Map.Entry e = (Entry) it.next();
      if (XStreamFactory.canWrite(e.getValue())) {
        writer.startNode("attribute");
        writer.addAttribute("name", (String) e.getKey());
        XStreamFactory.writeUnknown(e.getValue(), writer, context, mapper);
        writer.endNode();
      } else {
        LoggingSystem.getLogger(this).warning("cannot write scene attribute="+e.getKey()+" ["+e.getValue().getClass()+"] not supported.");
      }
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
      Object obj = XStreamFactory.readUnknown(reader, context, mapper);
      ret.addAttribute(attrName, obj);
    }
    reader.moveUp();
    
    return ret;
  }

}
