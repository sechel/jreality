
package de.jreality.scene.proxy.treeview;

import java.util.List;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
//import de.jreality.scene.event.SceneGraphComponentEvent;

/**
 * @author holger
 */
public class SceneTreeModel extends AbstractTreeModel {

  public SceneTreeModel(SceneGraphNode root)
  {
    super(mirror(root));
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

//  public void childAdded(SceneGraphComponentEvent ev)
//  {
//    fireNodesAdded(ev.getSceneGraphComponent(),
//      new int[]{ ev.getChildIndex() }, new Object[] { ev.getNewChildElement() });
//  }
//
//  public void childRemoved(SceneGraphComponentEvent ev)
//  {
//    fireNodesRemoved(ev.getSceneGraphComponent(),
//      new int[]{ ev.getChildIndex() }, new Object[] { ev.getOldChildElement() });
//  }
//
//  public void childReplaced(SceneGraphComponentEvent ev)
//  {
//    fireNodesChanged(ev.getSceneGraphComponent(),
//      new int[]{ ev.getChildIndex() }, new Object[] { ev.getNewChildElement() });
//  }
//
//  public void visibilityChanged(SceneGraphComponentEvent ev) {
//    fireNodesChanged(ev.getSceneGraphComponent(),
//        null, null); //new int[]{ }, new Object[] { });
//  }
//
//  private int ix(SceneGraphComponentEvent ev)
//  {
//    SceneGraphComponent p=(SceneGraphComponent)ev.getSceneGraphComponent();
//    int index=0;
//    switch(ev.getChildType())
//    {
//      case SceneGraphComponentEvent.CHILD_TYPE_COMPONENT: index=ev.getChildIndex();
////      case CHILD_TYPE_TOOL           = 6;
//        if(p.getGeometry()!=null) index++;
//      case SceneGraphComponentEvent.CHILD_TYPE_GEOMETRY:
//        if(p.getLight()!=null) index++;
//      case SceneGraphComponentEvent.CHILD_TYPE_LIGHT:
//        if(p.getCamera()!=null) index++;
//      case SceneGraphComponentEvent.CHILD_TYPE_CAMERA:
//        if(p.getAppearance()!=null) index++;
//      case SceneGraphComponentEvent.CHILD_TYPE_APPEARANCE:
//        if(p.getTransformation()!=null) index++;
//      case SceneGraphComponentEvent.CHILD_TYPE_TRANSFORMATION:
//    }
//    return 0;
//  }

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
