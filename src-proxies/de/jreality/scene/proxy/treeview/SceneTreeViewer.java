
package de.jreality.scene.proxy.treeview;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.ui.treeview.JTreeRenderer;
import de.jreality.ui.treeview.SceneTreeModel;
import de.jreality.util.LoggingSystem;

/**
 * @author holger
 */
public class SceneTreeViewer extends JTree implements de.jreality.scene.Viewer
{
	SceneGraphPath camPath;
	SceneGraphComponent root;
	int signature;
	Component viewingComponent;
	/* (non-Javadoc)
	 * @see de.jreality.scene.Viewer#getCameraPath()
	 */
	public SceneGraphPath getCameraPath() {
		
		return camPath;
	}

	/* (non-Javadoc)
	 * @see de.jreality.scene.Viewer#getSceneRoot()
	 */
	public SceneGraphComponent getSceneRoot() {
		return root;
	}
	/* (non-Javadoc)
	 * @see de.jreality.scene.Viewer#getSignature()
	 */
	public int getSignature() {
		return signature;
	}
	/* (non-Javadoc)
	 * @see de.jreality.scene.Viewer#getViewingComponent()
	 */
	public Component getViewingComponent() {
		if (viewingComponent == null) viewingComponent = new JScrollPane(this);
		return viewingComponent;
	}

	/* (non-Javadoc)
	 * @see de.jreality.scene.Viewer#hasViewingComponent()
	 */
	public boolean hasViewingComponent() {
		return true;
	}
	/* (non-Javadoc)
	 * @see de.jreality.scene.Viewer#initializeFrom(de.jreality.scene.Viewer)
	 */
	public void initializeFrom(Viewer v) {
		root = v.getSceneRoot();
		camPath = v.getCameraPath();
		signature = v.getSignature();
	}
	/* (non-Javadoc)
	 * @see de.jreality.scene.Viewer#render()
	 */
	public void render() {
		viewingComponent.paint(viewingComponent.getGraphics());
	}
	/* (non-Javadoc)
	 * @see de.jreality.scene.Viewer#setCameraPath(de.jreality.scene.SceneGraphPath)
	 */
	public void setCameraPath(SceneGraphPath p) {
		camPath = p;
	}
	/* (non-Javadoc)
	 * @see de.jreality.scene.Viewer#setSceneRoot(de.jreality.scene.SceneGraphComponent)
	 */
	public void setSceneRoot(SceneGraphComponent r) {
		root = r;
	}
	/* (non-Javadoc)
	 * @see de.jreality.scene.Viewer#setSignature(int)
	 */
	public void setSignature(int sig) {
		signature = sig;
	}
	public static SceneTreeViewer sceneTreeViewerFactory(Viewer v)	{
		SceneTreeViewer stv = new SceneTreeViewer(v.getSceneRoot());
		stv.initializeFrom(v);
		return stv;
	}
  /**
   * Constructor for SceneTreeViewer.
   * @param root {@link SceneGraphComponent} whose subtree should be displayed
   */
  public SceneTreeViewer(SceneGraphComponent root)
  {
   super(new SceneTreeModel(root));
    setCellRenderer(new SimpleSGCellRenderer());
    SimpleSGCellRenderer cr = (SimpleSGCellRenderer) getCellRenderer();
    Font f = cr.getFont();
    f = new Font("Helvetica",Font.PLAIN, 10);
    cr.setFont(f);
    LoggingSystem.getLogger(this).fine("Tree has font: "+((f == null) ? "null" : cr.getFont().toString()));
  }

  public static JComponent getViewerComponent(SceneGraphComponent root)
  {
    return new JScrollPane(new SceneTreeViewer(root));
  }

  public static JComponent getViewerComponent(SceneGraphComponent root,
    Viewer v)
  {
    final SceneTreeViewer viewer= new SceneTreeViewer(root);
    return new JScrollPane(viewer);
  }

  /**
   * Overridden because the JTree calls this before we have any chance to
   * set the CellRenderer.
   * @see javax.swing.JTree#convertValueToText(Object, boolean, boolean, boolean, int, boolean)
   */
  public String convertValueToText(Object value, boolean selected,
    boolean expanded, boolean leaf, int row, boolean hasFocus)
  {
    try
    {
      return (String)value;
    }
    catch(ClassCastException ex) // happens only inside the constructor
    {
      return "";//value.toString();
    }
  }
public SceneGraphComponent getAuxiliaryRoot() {
	// TODO Auto-generated method stub
	return null;
}
public void setAuxiliaryRoot(SceneGraphComponent ar) {
	// TODO Auto-generated method stub
	
}

}
