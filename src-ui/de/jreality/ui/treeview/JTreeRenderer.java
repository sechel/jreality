
package de.jreality.ui.treeview;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.Geometry;
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.AttributeEntity;
import de.jreality.scene.proxy.tree.SceneTreeNode;
import de.jreality.ui.treeview.SceneTreeModel.TreeTool;

/**
 * Render a node by showing the simple name and an (optional) icon.
 * @author holger
 */
public class JTreeRenderer extends DefaultTreeCellRenderer
  //implements TreeCellRenderer, TableCellRenderer, ListCellRenderer
{

  protected static ImageIcon createImageIcon(String path) {
    java.net.URL imgURL = JTreeRenderer.class.getResource(path);
    if (imgURL != null) {
        return new ImageIcon(imgURL);
    } else {
        System.err.println("Couldn't find file: " + path);
        return null;
    }
}


  static final ImageIcon trafoIcon = createImageIcon("icons/TrafoIcon.jpg");
  static final ImageIcon camIcon = createImageIcon("icons/CamIcon.jpg");
  static final ImageIcon geomIcon = createImageIcon("icons/GeometryIcon.jpg");
  static final ImageIcon sgcIcon = createImageIcon("icons/SceneGraphComponentIcon.jpg");
  static final ImageIcon appIcon = createImageIcon("icons/AppearanceIcon.jpg");
  static final ImageIcon lightIcon = createImageIcon("icons/LightIcon.jpg");
  static final ImageIcon shaderIcon = createImageIcon("icons/ShaderIcon.jpg");
  static final ImageIcon toolIcon = createImageIcon("icons/ToolIcon.jpg");

  final SceneGraphVisitor iconSelector = new SceneGraphVisitor() {
    public void visit(Appearance a) {
      setIcon(appIcon);
    }
    public void visit(Geometry g) {
      setIcon(geomIcon);
    }
    public void visit(SceneGraphComponent c) {
      setIcon(sgcIcon);
    }
    public void visit(Transformation t) {
      setIcon(trafoIcon);
    }
    public void visit(Camera c) {
      setIcon(camIcon);
    }
    public void visit(Light l) {
      setIcon(lightIcon);
    }
  };
  
  final StringBuffer buffer=new StringBuffer(30);
  /**
   * Constructor for SimpleSGCellRenderer.
   */
  public JTreeRenderer()
  {
    super();
  }

  /**
   * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(JTree, Object, boolean, boolean, boolean, int, boolean)
   */
  public Component getTreeCellRendererComponent(JTree tree, Object value,
    boolean selected, boolean expanded, boolean leaf, int row, boolean focus)
  {
    if (value instanceof SceneTreeNode) {
      SceneGraphNode m=((SceneTreeNode)value).getNode();
      buffer.append(m.getName());
    } else if (value instanceof AttributeEntity){
      String ifName = value.getClass().getInterfaces()[0].getName();
      buffer.append(ifName.substring(ifName.lastIndexOf(".")+1));
    }
    else if (value instanceof TreeTool){
      String ifName = ((TreeTool)value).getTool().getClass().getName();
      buffer.append(ifName.substring(ifName.lastIndexOf(".")+1));
    }
    final Component c=super.getTreeCellRendererComponent(
        tree, buffer.toString(), selected, expanded, leaf, row, focus);
    buffer.setLength(0);
    if (value instanceof SceneTreeNode) {
      ((SceneTreeNode)value).getNode().accept(iconSelector);
    } else if (value instanceof AttributeEntity) {
      setIcon(shaderIcon);
    } else if (value instanceof TreeTool) {
      setIcon(toolIcon);
    }
    return c;
  }
}
