
package de.jreality.scene.event;

import java.util.EventListener;

/**
 * Implemented by event listeners that wish to get informed when a direct
 * child of a component has been added or removed.
 * <img src="{@docRoot}/de/jreality/scene/doc-files/hierarchy-events.gif">
 * @author holger
 */
public interface SceneGraphComponentListener extends EventListener
{
  public void childAdded(  SceneGraphComponentEvent ev);
  public void childRemoved(SceneGraphComponentEvent ev);
  public void childReplaced(SceneGraphComponentEvent ev);
  public void visibilityChanged(SceneGraphComponentEvent ev);
}
