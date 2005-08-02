/*
 * Created on Mar 16, 2005
 *
 */
package de.jreality.jogl;


import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.proxy.treeview.SceneTreeModel.Node;

/**
 * @author Holger
 */
public class HandleTreeSelection implements TreeSelectionListener
{
	SceneGraphPath selectionPath = null;
   private InteractiveViewer       viewer;

  public HandleTreeSelection(InteractiveViewer v) {
    viewer=v;
  }

  public void valueChanged(TreeSelectionEvent evt) {
  	TreePath tp = evt.getNewLeadSelectionPath();
  	if (tp == null) return;
  	Object[] list = tp.getPath();
  	int n = list.length;
  	selectionPath = new SceneGraphPath(); 	
  	for (int i = 0; i<n; ++i)		{
  		Node node = (Node) list[i];
  		Object sel = node.target;
  		if (sel instanceof SceneGraphNode)	{
  			selectionPath.push((SceneGraphNode) sel);
  		}
  	}
  	viewer.getSelectionManager().setSelection(selectionPath);
 
   }

  }