
package de.jreality.scene.event;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;

/**
 * @author holger
 */
public class SceneGraphComponentEvent extends SceneEvent
{
  public static final int CHILD_TYPE_APPEARANCE     = 1;
  public static final int CHILD_TYPE_CAMERA         = 2;
  public static final int CHILD_TYPE_COMPONENT      = 3;
  public static final int CHILD_TYPE_GEOMETRY       = 4;
  public static final int CHILD_TYPE_LIGHT          = 5;
  public static final int CHILD_TYPE_TRANSFORMATION = 7;
  public static final int CHILD_TYPE_NONE           = 8;

  public static final int EVENT_TYPE_ADDED              = 21;
  public static final int EVENT_TYPE_REMOVED            = 22;
  public static final int EVENT_TYPE_REPLACED           = 23;
  public static final int EVENT_TYPE_VISIBILITY_CHANGED = 24;
  
  private final SceneGraphComponent component;
  private final SceneGraphNode oldChildElement, newChildElement;
  private final int            childIndex;
  private final int            childType;
  private final int            eventType;
  
  public SceneGraphComponentEvent(SceneGraphComponent source,
    int evType, int childNodeType,
    SceneGraphNode oldChild, SceneGraphNode newChild, int index)
  {
    super(source);
    component=source;
    oldChildElement=oldChild;
    newChildElement=newChild;
    childIndex=index;
    switch(evType) {
      case EVENT_TYPE_ADDED: case EVENT_TYPE_REMOVED:
      case EVENT_TYPE_REPLACED: eventType=evType; break;
      default: throw new IllegalArgumentException("evType "+evType);
    }
    childType=nodeType(childNodeType);
  }

  public SceneGraphComponentEvent(SceneGraphComponent source,
    int childNodeType, SceneGraphNode oldChild, SceneGraphNode newChild)
  {
    super(source);
    component=source;
    oldChildElement=oldChild;
    newChildElement=newChild;
    childIndex=0;
    eventType=(oldChild==null)? EVENT_TYPE_ADDED: (newChild==null)?
      EVENT_TYPE_REMOVED: EVENT_TYPE_REPLACED;
    childType=nodeType(childNodeType);
  }

  public SceneGraphComponentEvent(SceneGraphComponent source)
    {
      super(source);
      component=source;
      oldChildElement=null;
      newChildElement=null;
      childIndex=0;
      eventType=EVENT_TYPE_VISIBILITY_CHANGED;
      childType=CHILD_TYPE_NONE;
    }

  private int nodeType(int childNodeType)
  {
    switch(childNodeType) {
      case CHILD_TYPE_APPEARANCE:
      case CHILD_TYPE_CAMERA:
      case CHILD_TYPE_COMPONENT:
      case CHILD_TYPE_GEOMETRY:
      case CHILD_TYPE_LIGHT:
      case CHILD_TYPE_TRANSFORMATION:
        return childNodeType;
      default: throw
        new IllegalArgumentException("childNodeType "+childNodeType);
    }
  }

  /**
   * Returns the childIndex or <code>0</code> for singleton child types.
   */
  public int getChildIndex()
  {
    return childIndex;
  }

  public SceneGraphComponent getSceneGraphComponent()
  {
    return component;
  }
  /**
   * Get the CHILD_TYPE_xxx constant reflecting the current modification.
   */
  public int getChildType()
  {
    return childType;
  }

  /**
   * Get the event type either added, removed or replaced.
   */
  public int getEventType()
  {
    return eventType;
  }

  /**
   * Get the new child for added or replaced or <code>null</code> for removed.
   */
  public SceneGraphNode getNewChildElement()
  {
    return newChildElement;
  }

  /**
   * Get the new child for removed or replaced or <code>null</code> for added.
   */
  public SceneGraphNode getOldChildElement()
  {
    return oldChildElement;
  }

}
