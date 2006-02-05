
package de.jreality.scene.event;

import java.util.EventObject;

import de.jreality.scene.SceneGraphNode;

/**
 * @author holger
 */
public class SceneEvent extends EventObject
{
  private final SceneGraphNode sourceElement;

  /**
   * Constructor for SceneEvent.
   * @param source
   */
  public SceneEvent(SceneGraphNode source)
  {
    super(source);
    sourceElement=source;
  }

  /**
   * Returns the source {@link SceneGraphElement}.
   * @return the SceneGraphElement that sent this event
   */
  public SceneGraphNode getSourceNode()
  {
    return sourceElement;
  }
  
  /**
   * this is for event triggered write operations on the source node
   * 
   * @param runnable
   */
  public void enqueueWriter(Runnable runnable) {
    sourceElement.enqueueWriter(this, runnable);
  }
}
