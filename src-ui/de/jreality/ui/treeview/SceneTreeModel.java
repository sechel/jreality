package de.jreality.ui.treeview;

import java.awt.EventQueue;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.swing.tree.TreeNode;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.data.AttributeEntity;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.event.ToolEvent;
import de.jreality.scene.event.ToolListener;
import de.jreality.scene.proxy.tree.ProxyTreeFactory;
import de.jreality.scene.proxy.tree.SceneTreeNode;
import de.jreality.scene.proxy.tree.UpToDateSceneProxyBuilder;
import de.jreality.scene.tool.Tool;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.RootAppearance;
import de.jreality.shader.ShaderUtility;

public class SceneTreeModel extends AbstractTreeModel {

  private UpToDateSceneProxyBuilder builder;
  
  WeakHashMap entities = new WeakHashMap();
  WeakHashMap parents = new WeakHashMap();

  public SceneTreeModel(SceneGraphComponent root) {
    super(null);
    setSceneRoot(root);
  }

  public void dispose() {
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
        return new SceneTreeNodeWithToolListener(n);
      }
      
    });
    super.root = builder.createProxyTree();
  }

  public Object getChild(Object parent, int index) {
    if (parent instanceof SceneTreeNode) {
      SceneTreeNode sn = (SceneTreeNode) parent;
      if (sn.getNode() instanceof SceneGraphComponent) {
        if (index < sn.getChildren().size()) return sn.getChildren().get(index);
        int newInd = index-sn.getChildren().size();
        SceneGraphComponent comp = (SceneGraphComponent) sn.getNode();
        Tool t = (Tool) comp.getTools().get(newInd);
        return TreeTool.getInstance(sn, t);
      }
    }
    Object[] childEntities = (Object[]) entities.get(parent);
    return childEntities[index];
  }

  public int getChildCount(Object parent) {
    if (parent instanceof TreeTool) return 0;
    if (parent instanceof SceneTreeNode) {
      SceneTreeNode sn = (SceneTreeNode)parent;
      if ((sn.getNode() instanceof Appearance)) {
        Object[] ents = (Object[]) entities.get(sn);
        if (ents == null) {
          Object o1 = ShaderUtility.createDefaultGeometryShader((Appearance) sn.getNode(), false);
          Object o11 = ShaderUtility.createDefaultRenderingHintsShader((Appearance) sn.getNode(), false);
          Object o2 = null;
          if (AttributeEntityUtility.hasAttributeEntity(RootAppearance.class, "", (Appearance)sn.getNode()))
            o2 = ShaderUtility.createRootAppearance((Appearance) sn.getNode());
          ents = new Object[o2 == null ? 2 : 3];
          ents[0] = o1; ents[1] = o11;
          if (o2 != null) ents[2] = o2;
          entities.put(sn, ents);
          for (int i = 0; i < ents.length; i++)
            parents.put(ents[i], sn);
        }
        return ents.length;
      }
	int ret = sn.getChildren().size(); 
	if (sn.getNode() instanceof SceneGraphComponent) {
	  ret += ((SceneGraphComponent)sn.getNode()).getTools().size();
	}
	return ret;
    }
	// entity
      Object[] ents = (Object[]) entities.get(parent);
      if (ents == null) {
        BeanInfo bi=null;
        try {
          bi = Introspector.getBeanInfo(parent.getClass());
        } catch (IntrospectionException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        PropertyDescriptor[] pd=bi.getPropertyDescriptors();
        List childEntities = new LinkedList();
        for (int i = 0; i < pd.length; i++) {
          if (!AttributeEntity.class.isAssignableFrom(pd[i].getPropertyType())) continue;
          try {
            AttributeEntity ae = (AttributeEntity) pd[i].getReadMethod().invoke(parent, null);
            if (ae != null) {
              childEntities.add(ae);
              parents.put(ae, parent);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        ents = childEntities.toArray();
        entities.put(parent, ents);
      }
      return ents.length;
  }

  public Object getParent(Object o) {
    if (o instanceof SceneTreeNode )
      return ((SceneTreeNode)o).getParent();
    if (o instanceof TreeTool) return ((TreeTool)o).getTreeNode();
    else return parents.get(o);
  }
  
  public static class TreeTool {
    
    static WeakHashMap map = new WeakHashMap();
    
    private final SceneTreeNode treeNode;
    private final Tool tool;

    static TreeTool getInstance(SceneTreeNode n, Tool t) {
      HashMap m = (HashMap) map.get(n);
      if (m == null) {
        m = new HashMap();
        map.put(n, m);
      }
      HashMap m2 = (HashMap) m.get(t);
      if (m2 == null) {
        m2 = new HashMap();
        m2.put(t, new TreeTool(n, t));
      }
      return (TreeTool) m2.get(t);
    }
    
    private TreeTool(SceneTreeNode n, Tool t) {
      this.treeNode = n;
      this.tool = t;
    }

    public Tool getTool() {
      return tool;
    }

    public SceneTreeNode getTreeNode() {
      return treeNode;
    }
  }
  
  private class SceneTreeNodeWithToolListener extends SceneTreeNode implements ToolListener {

    SceneGraphComponent cmp;
    List tools=new LinkedList();
    
    protected SceneTreeNodeWithToolListener(SceneGraphNode node) {
      super(node);
      if (isComponent) {
        cmp = (SceneGraphComponent) node;
        cmp.addToolListener(this);
        tools.addAll(cmp.getTools());
      }
    }
    
    // addChild and removeChild is not really correct!
    // both methods would cause a deadlock when calling super.... in the runnable
    // and executed by invokeAndWait(..)
    
    public int addChild(final SceneTreeNode child) {
      final int[] ret = new int[1];
      Runnable runner = new Runnable(){
        public void run() {
          ret[0] = SceneTreeNodeWithToolListener.super.addChild(child);
          fireNodesAdded(SceneTreeNodeWithToolListener.this, new Object[]{child});
        }
      };
      if (EventQueue.isDispatchThread()) runner.run();
      else try {
        EventQueue.invokeAndWait(runner);
//        EventQueue.invokeLater(runner);
      } catch (Exception e) {
        throw new Error(";-(");
      }
      return ret[0];
    }

    protected int removeChild(final SceneTreeNode prevChild) {
      final int[] ret = new int[1];
      Runnable runner = new Runnable(){
        public void run() {
          ret[0] = SceneTreeNodeWithToolListener.super.removeChild(prevChild);
          fireNodesRemoved(SceneTreeNodeWithToolListener.this, new int[]{ret[0]}, new Object[]{prevChild});
        }
      };
      if (EventQueue.isDispatchThread()) runner.run();
      else try {
        EventQueue.invokeAndWait(runner);
//        EventQueue.invokeLater(runner);
      } catch (Exception e) {
        throw new Error(";-(");
      }
      return ret[0];
    }

    public void toolAdded(final ToolEvent ev) {
      Runnable runner = new Runnable(){
        public void run() {
          int idx = getChildren().size()+tools.size();
          tools.add(ev.getTool());
          fireNodesAdded(SceneTreeNodeWithToolListener.this, new int[]{idx}, new Object[]{TreeTool.getInstance(SceneTreeNodeWithToolListener.this, ev.getTool())});
        }
      };
      if (EventQueue.isDispatchThread()) runner.run();
      else try {
        EventQueue.invokeAndWait(runner);
//          EventQueue.invokeLater(runner);
      } catch (Exception e) {
        throw new Error(";-(");
      }
    }
    
    public void toolRemoved(final ToolEvent ev) {
      Runnable runner = new Runnable(){
        public void run() {
          int idx = getChildren().size();
          int tind = tools.indexOf(ev.getTool());
          tools.remove(tind);
          fireNodesRemoved(SceneTreeNodeWithToolListener.this, new int[]{idx+tind}, new Object[]{TreeTool.getInstance(SceneTreeNodeWithToolListener.this, ev.getTool())});
        }
      };
      if (EventQueue.isDispatchThread()) runner.run();
      else try {
        EventQueue.invokeAndWait(runner);
//        EventQueue.invokeLater(runner);
      } catch (Exception e) {
        throw new Error(";-(");
      }
    }
    
    protected void dispose(ArrayList disposedEntities) {
      super.dispose(disposedEntities);
      if (isComponent) {
        cmp.removeToolListener(this);
      }
    }
    
  }
  
}