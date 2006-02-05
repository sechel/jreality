package de.jreality.scene.tool;

import java.util.Iterator;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.event.ToolEvent;
import de.jreality.scene.event.ToolListener;
import de.jreality.scene.proxy.tree.EntityFactory;
import de.jreality.scene.proxy.tree.SceneGraphNodeEntity;
import de.jreality.scene.proxy.tree.SceneTreeNode;
import de.jreality.scene.proxy.tree.UpToDateSceneProxyBuilder;
import de.jreality.util.LoggingSystem;

public class ToolUpdateProxy {

  private ToolSystem toolSystem;

  private Builder builder;

  ToolUpdateProxy(ToolSystem ts) {
    toolSystem = ts;
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
    builder = new Builder(comp);
    builder.createProxyTree();
  }

  private class Builder extends UpToDateSceneProxyBuilder {
    Builder(SceneGraphComponent root) {
      super(root);
      setEntityFactory(new EntityFactory() {
        public SceneGraphNodeEntity produceSceneGraphNodeEntity(
            SceneGraphNode node) {
          if (node instanceof SceneGraphComponent) {
            return new SceneGraphComponentEntity((SceneGraphComponent) node);
          } else
            return super.produceSceneGraphNodeEntity(node);
        }
      });
    }
  }

  private class SceneGraphComponentEntity extends SceneGraphNodeEntity
      implements ToolListener {

    private final SceneGraphComponent comp;

    protected SceneGraphComponentEntity(SceneGraphComponent node) {
      super(node);
      this.comp = node;
      comp.addToolListener(this);
    }

    protected void addTreeNode(SceneTreeNode tn) {
      super.addTreeNode(tn);
      for (Iterator i = comp.getTools().iterator(); i.hasNext();)
        toolSystem.addTool((Tool) i.next(), tn.toPath());
    }

    protected void removeTreeNode(SceneTreeNode tn) {
      super.removeTreeNode(tn);
      for (Iterator i = comp.getTools().iterator(); i.hasNext();)
        toolSystem.removeTool((Tool) i.next(), tn.toPath());
    }

    public void toolAdded(ToolEvent ev) {
      for (Iterator i = getTreeNodes(); i.hasNext();) {
        toolSystem.addTool(ev.getTool(), ((SceneTreeNode) i.next()).toPath());
      }
    }

    public void toolRemoved(ToolEvent ev) {
      for (Iterator i = getTreeNodes(); i.hasNext();) {
        toolSystem.removeTool(ev.getTool(), ((SceneTreeNode) i.next()).toPath());
      }
    }

    protected void dispose() {
      comp.removeToolListener(this);
    }

  }

}