
package de.jreality.scene.proxy.treeview;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.jreality.scene.SceneGraphNode;

/**
 * Render a node by showing the simple name and an (optional) icon.
 * @author holger
 */
public class SimpleSGCellRenderer
  extends DefaultTreeCellRenderer
  //implements TreeCellRenderer, TableCellRenderer, ListCellRenderer
{
  final StringBuffer buffer=new StringBuffer(30);
  /**
   * Constructor for SimpleSGCellRenderer.
   */
  public SimpleSGCellRenderer()
  {
    super();
  }

  /**
   * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(JTree, Object, boolean, boolean, boolean, int, boolean)
   */
  public Component getTreeCellRendererComponent(JTree tree, Object value,
    boolean selected, boolean expanded, boolean leaf, int row, boolean focus)
  {
    SceneGraphNode m=((SceneTreeModel.Node)value).target;
    buffer.append(m.getName());
    buffer.append(" : ");
    final int ix1=buffer.length();
    String clName=m.getClass().getName();
    
    // TODO: check how to deal with that without appearance attributes
    /*
    if(clName.intern()=="de.jreality.scene.AppearanceAttribute")
    {
      Object aa=(AppearanceAttribute)m;
      buffer.append(aa.getAttributeName()).append(" = ").append(aa.getValue());
    }
    
    else
    */
    {
      buffer.append(clName);
      buffer.delete(ix1, clName.lastIndexOf('.')+ix1+1);
    }
    final Component c=super.getTreeCellRendererComponent(
      tree, buffer.toString(), selected, expanded, leaf, row, focus);
    buffer.setLength(0);
    return c;
  }
}
