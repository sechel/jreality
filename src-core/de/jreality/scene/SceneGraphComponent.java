package de.jreality.scene;

import java.util.*;

import de.jreality.scene.event.*;
import de.jreality.scene.tool.Tool;

/**
 * A component of the scene graph: the only node that can have another
 * SceneGraphComponent instances as its children.
 */
public class SceneGraphComponent extends SceneGraphNode {
  
  private Transformation transformation;
  private Appearance appearance;
  private Camera camera;
  private Light light;
  private Geometry geometry;
  private boolean visible = true;
  protected List children= Collections.EMPTY_LIST;
  protected List tools = Collections.EMPTY_LIST;

  private transient ToolListener toolListener;
  private transient SceneGraphComponentListener containerListener;
  
  private transient List cachedEvents=new LinkedList();

  public List getChildNodes() {
    startReader();
    try {
      ArrayList list=new ArrayList();
      if(transformation!=null) list.add(transformation);
      if(appearance!=null) list.add(appearance);
      if(camera!=null) list.add(camera);
      if(light!=null) list.add(light);
      if(geometry!=null) list.add(geometry);
      list.addAll(children);
      return list.isEmpty()? Collections.EMPTY_LIST: list;
    } finally {
      finishReader();
    }
  }
  
  public void addChild(SceneGraphComponent sgc) {
    //new Exception("addChild").printStackTrace();
    class CheckLoop extends SceneGraphVisitor {
      final HashSet encountered=new HashSet();
      public void visit(SceneGraphComponent c)
      {
        if(c==SceneGraphComponent.this)
          throw new SceneGraphLoopException();
        if(encountered.add(c)) c.childrenAccept(this);
      }
    }
    checkReadOnly();
    if(sgc==this) throw new SceneGraphLoopException();
    startWriter();
    try {
      sgc.childrenAccept(new CheckLoop());
    } catch(SceneGraphLoopException ex) {
      finishWriter();
      ex.fillInStackTrace(); throw ex;
    }
    if (children == Collections.EMPTY_LIST) children= new ArrayList();
    int index=children.size();
    children.add(sgc);
    fireSceneGraphElementAdded(sgc, SceneGraphComponentEvent.CHILD_TYPE_COMPONENT, index);
    finishWriter();
  }

  /**
  * Returns a child component node.
  * @return SceneGraphComponent
  */
  public SceneGraphComponent getChildComponent(int index) {
    startReader();
    try {
      return (SceneGraphComponent) children.get(index);
    } finally {
      finishReader();
    }
  }

  /**
   * Returns the number of child component nodes.
   * @return int 
   */
  public int getChildComponentCount() {
    startReader();
    try {
      return children.size();
    } finally {
      finishReader();
    }
  }

  public void removeChild(SceneGraphComponent sgc) {
  	checkReadOnly();
    startWriter();
	  int index=children.indexOf(sgc);
 	  if(index == -1) {
 	    finishWriter();
      throw new IllegalStateException("sgc not a child of this node");
    }
  	children.remove(index);
  	fireSceneGraphElementRemoved(sgc, SceneGraphComponentEvent.CHILD_TYPE_COMPONENT, index);
    finishWriter();
  }

  public void setAppearance(Appearance newApp) {
    checkReadOnly();
    startWriter();
    final Appearance old=appearance;
    appearance=newApp;
    fireSceneGraphElementSet(old, newApp, SceneGraphComponentEvent.CHILD_TYPE_APPEARANCE);
    finishWriter();
  }

  /**
   * Returns the appearance node.
   * @return Appearance
   */
  public Appearance getAppearance() {
    startReader();
    try {
      return appearance;
    } finally {
      finishReader();
    }
  }
  /**
   * Returns the camera child if any.
   * @return Camera
   */
  public Camera getCamera() {
    startReader();
    try {
      return camera;
    } finally {
      finishReader();
    }
  }
  /**
   * Adds a camera, replacing any previously added camera.
   * @param camera The camera to set
   */
  public void setCamera(Camera newCamera) {
    checkReadOnly();
    startWriter();
    final Camera old= camera;
    camera= newCamera;
    fireSceneGraphElementSet(old, newCamera, SceneGraphComponentEvent.CHILD_TYPE_CAMERA);
    finishWriter();
  }

  public Geometry getGeometry() {
    startReader();
    try {
      return geometry;
    } finally {
      finishReader();
    }
  }

  public void setGeometry(Geometry g) {
    checkReadOnly();
    startWriter();
    final Geometry old=geometry;
    geometry=g;
    fireSceneGraphElementSet(old, g, SceneGraphComponentEvent.CHILD_TYPE_GEOMETRY);
    finishWriter();
  }

  /**
   * Returns the light child if any.
   * @return Light
   */
  public Light getLight() {
    startReader();
    try {
      return light;
    } finally {
      finishReader();
    }
  }
  /**
  * Adds a light, replacing any previously added light.
  * @param light The light to set
  */
  public void setLight(Light newLight) {
    checkReadOnly();
    startWriter();
    final Light old=light;
    light= newLight;
    fireSceneGraphElementSet(old, newLight, SceneGraphComponentEvent.CHILD_TYPE_LIGHT);
    finishWriter();
  }

  /**
   * Returns the transformation node.
   * @return Transformation
   */
  public Transformation getTransformation() {
    startReader();
    try {
      return transformation;
    } finally {
      finishReader();
    }
  }

  public void setTransformation(Transformation newTrans) {
    checkReadOnly();
    startWriter();
    final Transformation oldTrans=transformation;
    transformation= newTrans;
    fireSceneGraphElementSet(oldTrans, newTrans, SceneGraphComponentEvent.CHILD_TYPE_TRANSFORMATION);
    finishWriter();
  }
  
  public boolean isDirectAncestor(SceneGraphNode child) {
    startReader();
    try {
      return transformation==child||appearance==child||
        camera==child||light==child||geometry==child||
        children.contains(child);
    } finally {
      finishReader();
    }
  }
  
//  public boolean isAncestor(final SceneGraphNode child) {
//    final boolean[] result={ false };
//    SceneGraphVisitor v=new SceneGraphVisitor()
//    {
//      public void visit(SceneGraphComponent c) {
//        if(!result[0]) c.childrenAccept(this);
//      }
//      public void visit(SceneGraphNode n) {
//        if(n==child) result[0]=true;
//      }
//    };
//    childrenAccept(v);
//    return result[0];
//  }
  
  public void accept(SceneGraphVisitor v) {
    startReader();
    try {
      v.visit(this);
    } finally {
      finishReader();
    }
  }
  static void superAccept(SceneGraphComponent c, SceneGraphVisitor v) {
    c.superAccept(v);
  }
  private void superAccept(SceneGraphVisitor v) {
    super.accept(v);
  }
  /**
   * This method calls the accept method on all childMembers in the following order
   * <ul>
   * <li> transformation
   * <li> appearance
   * <li> camera
   * <li> light
   * <li> geometry
   * <li> all child SceneGraphComponents
   * </ul>
   * The default use would be a rendering system, that implements the SceneGraphVisitor interface and 
   * calls <code>childrenAccept(this)</code> in its visit implementations.
   */
  public void childrenAccept(SceneGraphVisitor v) {
    startReader();
    try {
      if(transformation != null) transformation.accept(v);
      if(appearance != null)     appearance.accept(v);
      if(camera != null)         camera.accept(v);
      if(light != null)          light.accept(v);
      if(geometry!=null)         geometry.accept(v);
      if (children.size() > 0) {		// save time?
    		for(Iterator iter=children.iterator(); iter.hasNext();) {
    		  SceneGraphComponent c=(SceneGraphComponent)iter.next();
    		  c.accept(v);
    		}
      }
    } finally {
      finishReader();
    }
  }

  public void childrenWriteAccept(SceneGraphVisitor v, boolean writeTransformation, boolean writeAppearance, boolean writeCamera, boolean writeLight, boolean writeGeometry, boolean writeChildren) {
    startReader();
    try {
      if(transformation != null) { 
        if (writeTransformation) transformation.startWriter();
        try {
          transformation.accept(v);
        } finally {
          if (writeTransformation) transformation.finishWriter();
        }
      }
      if(appearance != null) { 
        if (writeAppearance) appearance.startWriter();
        try {
          appearance.accept(v);
        } finally {
          if (writeAppearance) appearance.finishWriter();
        }
      }
      if(camera != null) { 
        if (writeCamera) camera.startWriter();
        try {
          camera.accept(v);
        } finally {
          if (writeCamera) camera.finishWriter();
        }
      }
      if(light != null) { 
        if (writeLight) light.startWriter();
        try {
          light.accept(v);
        } finally {
          if (writeLight) light.finishWriter();
        }
      }
      if(geometry != null) { 
        if (writeGeometry) geometry.startWriter();
        try {
          geometry.accept(v);
        } finally {
          if (writeGeometry) geometry.finishWriter();
        }
      }
      if (children.size() > 0) {    // save time?
        for(Iterator iter=children.iterator(); iter.hasNext();) {
          SceneGraphComponent c=(SceneGraphComponent)iter.next();
          if (writeChildren) c.startWriter();
          try {
            c.accept(v);
          } finally {
            if (writeChildren) c.finishWriter();
          }
        }
      }
    } finally {
      finishReader();
    }
  }

  public void addTool(Tool tool) {
    startWriter();
    if(tools == Collections.EMPTY_LIST)
    	tools= new ArrayList();
    if (tools.contains(tool)) throw new IllegalStateException("duplicate tool");
    tools.add(tool);
    fireToolAdded(tool);
    finishWriter();
  }

  public void removeTool(Tool tool) {
    startWriter();
    int toolIndex= tools.indexOf(tool);
    if(toolIndex == -1) {
      finishWriter();
      throw new IllegalStateException("not a tool of this node");
    }
    tools.remove(toolIndex);
    fireToolRemoved(tool);
    finishWriter();
  }
  
  private void fireToolAdded(Tool tool) {
    // cache event if there are listeners
    if (toolListener == null) return;
    final ToolEvent event = new ToolEvent(this, tool, ToolEvent.TOOL_ADDED);
    cachedEvents.add(event);
  }

  private void fireToolRemoved(Tool tool) {
    // cache event if there are listeners
    if (toolListener == null) return;
    final ToolEvent event = new ToolEvent(this, tool, ToolEvent.TOOL_REMOVED);
    cachedEvents.add(event);
  }

  /**
   * use from inside Scene.executeReader(..)
   * @return
   */
  public List getTools() {
    startReader();
    try {
      return Collections.unmodifiableList(tools);
    } finally {
      finishReader();
    }
  }

  public void addToolListener(ToolListener listener) {
    startReader();
    toolListener=ToolEventMulticaster.add(toolListener, listener);
    finishReader();
  }
  
  public void removeToolListener(ToolListener listener) {
    startReader();
    toolListener=ToolEventMulticaster.remove(toolListener, listener);
    finishReader();
  }
  
  /**
   * @return a boolean that indicates wether this SceneGraphNode 
   * and its children get their geometry rendered or not.
   */
  public boolean isVisible() {
    startReader();
    try {
      return visible;
    } finally {
      finishReader();
    }
  }
  /**
   * Sets the visibility of this SceneGraphComponent and its children. This
   * flag affects rendering and bounding box calculations only (i.e. lights
   * and cameras at or below this node are unaffected).
   * @param visible sets wether this barnch of the scene graph should be rendered 
   * or not.
   */
  public void setVisible(boolean newVisibleState) {
    checkReadOnly();
    startWriter();
    if (visible != newVisibleState) {
      visible=newVisibleState;
      fireVisibilityChanged();
    }
    finishWriter();
  }

  public void addSceneGraphComponentListener(SceneGraphComponentListener listener) {
    startReader();
    containerListener=SceneGraphComponentEventMulticaster.add(containerListener, listener);
    finishReader();
  }

  public void removeSceneGraphComponentListener(SceneGraphComponentListener listener) {
    startReader();
    containerListener=SceneGraphComponentEventMulticaster.remove(containerListener, listener);
    finishReader();
  }

  void fireSceneGraphElementAdded(final SceneGraphNode child, final int type, final int index) {
    // we are in write lock
    if (containerListener == null) return;
    final SceneGraphComponentEvent event = new SceneGraphComponentEvent(
          this, SceneGraphComponentEvent.EVENT_TYPE_ADDED,
          type, null, child, index);
    cachedEvents.add(event);
  }

  void fireSceneGraphElementRemoved(final SceneGraphNode child, final int type, final int index) {
    // we are in write lock
    if (containerListener == null) return;
    final SceneGraphComponentEvent event = new SceneGraphComponentEvent(this, SceneGraphComponentEvent.EVENT_TYPE_REMOVED, type, child, null, index);
    cachedEvents.add(event);
  }

  void fireSceneGraphElementReplaced(final SceneGraphNode old, final SceneGraphNode _new, final int type, final int index) {
    // we are in write lock
    if (containerListener == null) return;
    final SceneGraphComponentEvent event = new SceneGraphComponentEvent(this, SceneGraphComponentEvent.EVENT_TYPE_REPLACED, type, old, _new, index);
    cachedEvents.add(event);
  }

  void fireSceneGraphElementSet(final SceneGraphNode old, final SceneGraphNode _new, final int type) {
    // we are in write lock
    if(old==_new) return;
    if(old==null) fireSceneGraphElementAdded(_new, type, 0);
    else if(_new==null) fireSceneGraphElementRemoved(old, type, 0);
    else fireSceneGraphElementReplaced(old, _new, type, 0);
}

  void fireVisibilityChanged() {
    // we are in write lock
    if (containerListener == null) return;
    final SceneGraphComponentEvent event = new SceneGraphComponentEvent(this);
    cachedEvents.add(event);
  }

  private void fire(SceneGraphComponentEvent event) {
    switch (event.getEventType()) {
    case SceneGraphComponentEvent.EVENT_TYPE_ADDED:
      containerListener.childAdded(event);
      return;
    case SceneGraphComponentEvent.EVENT_TYPE_REMOVED:
      containerListener.childRemoved(event);
      return;
    case SceneGraphComponentEvent.EVENT_TYPE_REPLACED:
      containerListener.childReplaced(event);
      return;
    case SceneGraphComponentEvent.EVENT_TYPE_VISIBILITY_CHANGED:
      containerListener.visibilityChanged(event);
    }
  }
  
  private void fire(ToolEvent event) {
    switch (event.getEventType()) {
    case ToolEvent.TOOL_ADDED:
      toolListener.toolAdded(event);
      return;
    case ToolEvent.TOOL_REMOVED:
      toolListener.toolRemoved(event);
      return;
    }
  }
  
  private void fire(SceneEvent event) {
    if (event instanceof ToolEvent) fire((ToolEvent) event);
    else fire((SceneGraphComponentEvent) event);
  }
  
  protected void writingFinished() {
    // we are in a readLock and broadcast all cached events - TODO: merge if possible
    try {
      for (Iterator it = cachedEvents.iterator(); it.hasNext(); ) {
        SceneEvent event = (SceneEvent) it.next();
        it.remove();
        // TODO: try catch ?
        fire(event);
      }
    } finally {
      cachedEvents.clear();
    }
  }
}
