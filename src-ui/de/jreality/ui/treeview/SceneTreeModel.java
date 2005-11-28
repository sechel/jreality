package de.jreality.ui.treeview;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.proxy.tree.ProxyTreeFactory;
import de.jreality.scene.proxy.tree.SceneTreeNode;
import de.jreality.scene.proxy.tree.UpToDateSceneProxyBuilder;

public class SceneTreeModel extends AbstractTreeModel {

  private UpToDateSceneProxyBuilder builder;

  public SceneTreeModel(SceneGraphComponent root) {
    super(null);
    setSceneRoot(root);
  }

  void dispose() {
    builder.dispose();
    builder = null;
  }
  
  void setSceneRoot(SceneGraphComponent comp) {
    if (builder != null) {
      throw new IllegalStateException("twice called");
    }
    if (comp == null) return;
    builder = new UpToDateSceneProxyBuilder(comp);
    builder.setProxyTreeFactory(new ProxyTreeFactory() {
      public SceneTreeNode createProxyTreeNode(SceneGraphNode n) {
        return new SceneTreeNode(n) {
          public int addChild(SceneTreeNode child) {
            int ret = super.addChild(child);
            fireNodesAdded(this, new Object[]{child});
            return ret;
          }
          protected int removeChild(SceneTreeNode prevChild) {
            int ret = super.removeChild(prevChild);
            fireNodesRemoved(this, new int[]{ret}, new Object[]{prevChild});
            return ret;
          }
        };
      }
    });
    super.root = builder.createProxyTree();
  }

  public Object getChild(Object parent, int index) {
    return ((SceneTreeNode)parent).getChildren().get(index);
  }

  public int getChildCount(Object parent) {
    return ((SceneTreeNode)parent).getChildren().size();
  }

  public Object getParent(Object o) {
    return ((SceneTreeNode)o).getParent();
  }

}