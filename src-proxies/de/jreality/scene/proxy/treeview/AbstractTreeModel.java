
package de.jreality.scene.proxy.treeview;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * Baseclass for TreeModel implementations. Manages listeners and
 * provides fireXXX methods for notifying them.<br>
 * The {@link #valueForPathChanged(TreePath,newValue) valueForPathChanged}
 * method is implemented as no-op which comes handy for immutable trees
 * and trees where the editor&lt;-&gt;node communication is already enough.
 */
public abstract class AbstractTreeModel implements TreeModel
{
  ArrayList listeners;
  Object root;
  /**
   * Construct a tree model with the given root object.
   * Normally there is no need for changing the root
   * afterwards. If you want to do so, you have to override
   * the {@link #getRoot() getRoot()} method to return the
   * dynamic root object. And don't forget to call the
   * {@link #fireNodesChanged fireNodesChanged} method each time
   * the root node is replaced.
   */
  protected AbstractTreeModel(Object root)
  {
    this.root=root;
    listeners=new ArrayList();
  }
  public void addTreeModelListener(TreeModelListener l)
  {
    listeners.add(l);
  }
  public void removeTreeModelListener(TreeModelListener l)
  {
    listeners.remove(l);
  }
  protected void fireNodesAdded(Object parent, Object[] child)
  {
    if(listeners.isEmpty()) return;
    fireNodesAdded(parent, getIndicesFor(parent, child), child);
  }
  protected void fireNodesAdded(Object parent, int[] indices, Object[] child)
  {
    if(listeners.isEmpty()) return;
    TreeModelEvent e= new TreeModelEvent(this,getPathTo(parent), indices,child);
    for(int i=listeners.size()-1; i>-1; --i)
    {
      TreeModelListener tml=(TreeModelListener)listeners.get(i);
      tml.treeNodesInserted(e);
    }
  }
  protected void fireNodesChanged(Object parent, Object[] child)
  {
    if(listeners.isEmpty()) return;
    fireNodesChanged(parent, getIndicesFor(parent, child), child);
  }
  protected void fireNodesChanged(Object parent, int[] indices, Object[] child)
  {
    if(listeners.isEmpty()) return;
    TreeModelEvent e=new TreeModelEvent(this,getPathTo(parent), indices, child);
  //System.out.println(getClass()+": firing "+e);
    for(int i=listeners.size()-1; i>-1; --i)
    {
      TreeModelListener tml=(TreeModelListener)listeners.get(i);
      tml.treeNodesChanged(e);
    }
  }
  protected void fireTreeStructureChanged(Object parent)
  {
    if(listeners.isEmpty()) return;
    TreeModelEvent e=new TreeModelEvent(this, getPathTo(parent));
  //System.out.println(getClass()+": firing "+e);
    for(int i=listeners.size()-1; i>-1; --i)
    {
      TreeModelListener tml=(TreeModelListener)listeners.get(i);
      tml.treeStructureChanged(e);
    }
  }
  protected void fireNodesRemoved(Object parent, int[] indices, Object[] child)
  {
    if(listeners.isEmpty()) return;
    TreeModelEvent e=new TreeModelEvent(this,getPathTo(parent),indices,child);
  //System.out.println(getClass()+": firing "+e);
    for(int i=listeners.size()-1; i>-1; --i)
    {
      TreeModelListener tml=(TreeModelListener)listeners.get(i);
      tml.treeNodesRemoved(e);
    }
  }
  /**
   * Used by event instance creation. Calls
   * {@link #getParent(Object) getParent} until the root object is reached.
   * Should be overridden if more efficient methods are available.
   */
  public TreePath getPathTo(Object o)
  {
    final Object root=getRoot(); // use get method since it can be overridden
    ArrayList path=new ArrayList();
    path.add(o);
    for(Object p=o; !root.equals(p); path.add(p=getParent(p)));
    Collections.reverse(path);
  //System.out.println("getPathTo("+o+"): "+path+", root="+root);
    return new TreePath(path.toArray());
  }
  /**
   * Used by event instance creation. Calls
   * {@link #getIndexOfChild(Object) getIndexOfChild} for every array entry.
   * Should be overridden if more efficient methods are available.
   */
  protected int[] getIndicesFor(Object parent, Object[] child)
  {
    int[] indices=new int[child.length];
    for(int i=0; i<indices.length; i++)
      indices[i]=getIndexOfChild(parent, child[i]);
    return indices;
  }
  /**
   * Must be implemented by subclasses, return the child of parent at index.
   * @see TreeModel#getChild(Object,int)
   */
  public abstract Object getChild(Object parent, int index);
  /**
   * Must be implemented by subclasses, return number of children.
   * @see TreeModel#getChildCount(Object)
   */
  public abstract int getChildCount(Object parent);
  /**
   * Defaults to a linear search for the child. Can be overridden
   * if there is a more efficient way of determining the index.
   */
  public int getIndexOfChild(Object parent, Object child)
  {
    final int l=getChildCount(parent);
    for(int i=0; i<l; i++)
      if(getChild(parent, i)==child) return i;
    return -1;
  }
  /**
   * Returns the root object specified in the constructor.
   */
  public Object getRoot()
  {
    return root;
  }
  /**
   * Returns <code>getChildCount(node)==0</code>, should be overridden
   * if the semantics of leaf nodes differs.
   */
  public boolean isLeaf(Object node)
  {
    return getChildCount(node)==0;
  }
  /**
   * Simply does nothing. Models that doe not support editing or do
   * not required conversion are not required to override this.
   */
  public void valueForPathChanged(TreePath path, Object newValue) {}
  /**
   * Must be implemented by subclasses, return the parent of
   * a node. This is only used by {@link #getPathTo(Object)}
   * and therefore does not require a valid implementation if
   * the <code>getPathTo()</code> method is overridden with an
   * implementation that does not call it's super impl.
   */
  public abstract Object getParent(Object o);
}
