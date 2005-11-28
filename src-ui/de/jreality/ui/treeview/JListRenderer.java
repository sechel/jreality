
package de.jreality.ui.treeview;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
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
import de.jreality.scene.proxy.tree.SceneTreeNode;

/**
 * Render a node by showing the simple name and an (optional) icon.
 * @author holger
 */
public class JListRenderer extends DefaultListCellRenderer
  //implements TreeCellRenderer, TableCellRenderer, ListCellRenderer
{

  protected static ImageIcon createImageIcon(String path) {
    java.net.URL imgURL = JListRenderer.class.getResource(path);
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
  public JListRenderer()
  {
    super();
  }

  /**
   * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(JTree, Object, boolean, boolean, boolean, int, boolean)
   */
  public Component getListCellRendererComponent(JList list, Object value,
    int index, boolean selected, boolean focus)
  {
    SceneGraphNode m=(SceneGraphNode)value;
    buffer.append(m.getName());
    final Component c=super.getListCellRendererComponent(
      list, buffer.toString(), index, selected, focus);
    buffer.setLength(0);
    m.accept(iconSelector);
    return c;
  }
}
