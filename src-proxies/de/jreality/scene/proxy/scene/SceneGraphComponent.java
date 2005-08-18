package de.jreality.scene.proxy.scene;

import de.jreality.scene.proxy.SgAdd;
import de.jreality.scene.proxy.SgRemove;

/**
 * Note: all remote references passed in must be hosted on the same machine as
 * this implementation.
 */
public class SceneGraphComponent extends de.jreality.scene.SceneGraphComponent implements RemoteSceneGraphComponent {

  public void add(RemoteSceneGraphNode newChild) {
    new SgAdd().add(this, (de.jreality.scene.SceneGraphNode) newChild);
  }

  public void remove(RemoteSceneGraphNode newChild) {
    new SgRemove().remove(this, (de.jreality.scene.SceneGraphNode) newChild);
  }
}
