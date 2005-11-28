
package de.jreality.scene.proxy.treeview;

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

/**
 * Render a node by showing the simple name and an (optional) icon.
 * @author holger
 */
public class SimpleSGCellRenderer extends DefaultTreeCellRenderer
  //implements TreeCellRenderer, TableCellRenderer, ListCellRenderer
{

  protected static ImageIcon createImageIcon(String path) {
    java.net.URL imgURL = SimpleSGCellRenderer.class.getResource(path);
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
    //buffer.append(" : ");
    //final int ix1=buffer.length();
    //String clName=m.getClass().getName();
    
    // TODO: check how to deal with that without appearance attributes
    /*
    if(clName.intern()=="de.jreality.scene.AppearanceAttribute")
    {
      Object aa=(AppearanceAttribute)m;
      buffer.append(aa.getAttributeName()).append(" = ").append(aa.getValue());
    }
    
    else
    */
    //{
      //buffer.append(clName);
      //buffer.delete(ix1, clName.lastIndexOf('.')+ix1+1);
    //}
    final Component c=super.getTreeCellRendererComponent(
      tree, buffer.toString(), selected, expanded, leaf, row, focus);
    buffer.setLength(0);
    m.accept(iconSelector);
    return c;
  }
}
