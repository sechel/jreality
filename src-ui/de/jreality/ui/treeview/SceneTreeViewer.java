
package de.jreality.ui.treeview;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JTree;

import de.jreality.scene.SceneGraphComponent;

/**
 * @author holger
 */
public class SceneTreeViewer extends JTree
{

  Component viewingComponent;

  public SceneTreeViewer() {
    super();
    setCellRenderer(new SimpleSGCellRenderer());
  }
  
  public Component getViewingComponent() {
		if (viewingComponent == null) viewingComponent = new JScrollPane(this);
		return viewingComponent;
	}

  public void setRoot(SceneGraphComponent root) {
    TreeUpdateProxy model = new TreeUpdateProxy();
    model.setSceneRoot(root);
    setModel(model);
  }

}
