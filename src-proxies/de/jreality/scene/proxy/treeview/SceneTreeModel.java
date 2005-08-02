
package de.jreality.scene.proxy.treeview;

import java.util.List;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.event.SceneContainerEvent;
import de.jreality.scene.event.SceneHierarchyEvent;
import de.jreality.scene.event.SceneTreeListener;

/**
 * @author holger
 */
public class SceneTreeModel extends AbstractTreeModel
  implements SceneTreeListener
{

  public SceneTreeModel(SceneGraphNode root)
  {
    super(mirror(root));
    root.addSceneTreeListener(this);
  }

  private static Node mirror(SceneGraphNode node) {
    Node reflected=new Node();
    reflected.target=node;
    List l;
    if(node instanceof SceneGraphComponent) {
      SceneGraphComponent comp=(SceneGraphComponent)node;
      l=comp.getChildNodes();
    } /*else if(node instanceof Appearance) {
      Appearance comp=(Appearance)node;
      l=comp.getChildNodes();
    } */else return reflected;
    final int num=l.size();
    reflected.children=new Node[num];
    for(int i=0; i<num; i++) {
      Node n=mirror((SceneGraphNode)l.get(i));
      n.parent=reflected;
      reflected.children[i]=n;
    }
    return reflected;
  }

  /**
   * @see javax.swing.tree.TreeModel#getChild(Object, int)
   */
  public Object getChild(Object parent, int index)
  {
    return ((Node)parent).children[index];
  }

  /**
   * @see javax.swing.tree.TreeModel#getChildCount(Object)
   */
  public int getChildCount(Object parent)
  {
    return ((Node)parent).children.length;
  }

  /**
   * @see de.jreality.scene.proxy.treeview.AbstractTreeModel#getParent(Object)
   */
  public Object getParent(Object o)
  {
    return ((Node)o).parent;
  }

  public void childAdded(SceneHierarchyEvent ev)
  {
    fireNodesAdded(ev.getParentElement(),
      new int[]{ ev.getChildIndex() }, new Object[] { ev.getNewChildElement() });
  }

  public void childRemoved(SceneHierarchyEvent ev)
  {
    fireNodesRemoved(ev.getParentElement(),
      new int[]{ ev.getChildIndex() }, new Object[] { ev.getOldChildElement() });
  }

  public void childReplaced(SceneHierarchyEvent ev)
  {
    fireNodesChanged(ev.getParentElement(),
      new int[]{ ev.getChildIndex() }, new Object[] { ev.getNewChildElement() });
  }

  private int ix(SceneContainerEvent ev)
  {
    SceneGraphComponent p=(SceneGraphComponent)ev.getParentElement();
    int index=0;
    switch(ev.getChildType())
    {
      case SceneContainerEvent.CHILD_TYPE_COMPONENT: index=ev.getChildIndex();
//      case CHILD_TYPE_TOOL           = 6;
        if(p.getGeometry()!=null) index++;
      case SceneContainerEvent.CHILD_TYPE_GEOMETRY:
        if(p.getLight()!=null) index++;
      case SceneContainerEvent.CHILD_TYPE_LIGHT:
        if(p.getCamera()!=null) index++;
      case SceneContainerEvent.CHILD_TYPE_CAMERA:
        if(p.getAppearance()!=null) index++;
      case SceneContainerEvent.CHILD_TYPE_APPEARANCE:
        if(p.getTransformation()!=null) index++;
      case SceneContainerEvent.CHILD_TYPE_TRANSFORMATION:
    }
    return 0;
  }

  public static class Node {
    public Node parent;
    public SceneGraphNode target;
    public Node[] children;
    public String toString() {
      return target.toString();
    }
  }

  public boolean isLeaf(Object node) {
    return ((Node)node).children==null;
  }
}
