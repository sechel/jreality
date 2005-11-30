package de.jreality.io;

import java.util.HashMap;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;

public class JrScene {
  
  private SceneGraphComponent sceneGraph;  
  private HashMap scenePaths;
  private HashMap sceneAttributes;
  
  public JrScene(SceneGraphComponent root) {
    this.sceneGraph=root;
  }
  
  public void addPath(String name, SceneGraphPath path) {
    if (!(path.getFirstElement() == sceneGraph) || !path.isValid())
      throw new IllegalArgumentException("invalid path!");
    if (scenePaths == null) scenePaths = new HashMap();
    scenePaths.put(name, path);
  }
  
  public void addAttribute(String name, Object attribute) {
    if (sceneAttributes == null) sceneAttributes = new HashMap();
    sceneAttributes.put(name, attribute);
  }

  public SceneGraphComponent getSceneRoot() {
    return sceneGraph;
  }
  
  public SceneGraphPath getPath(String name) {
    return (SceneGraphPath) scenePaths.get(name);
  }

  public Object getAttribute(String name) {
    return sceneAttributes.get(name);
  }
}
