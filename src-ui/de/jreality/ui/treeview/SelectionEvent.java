package de.jreality.ui.treeview;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreePath;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.proxy.tree.SceneTreeNode;
import de.jreality.scene.tool.Tool;
import de.jreality.ui.treeview.SceneTreeModel.TreeTool;

public class SelectionEvent extends TreeSelectionEvent{

  private static final long serialVersionUID = 1L;

  private Object selection;
  

  /** calls TreeSelectionEvent(...) */
  public SelectionEvent(Object source, TreePath[] paths, boolean[] areNew, TreePath oldLeadSelectionPath, TreePath newLeadSelectionPath) {
    super(source, paths, areNew, oldLeadSelectionPath, newLeadSelectionPath);
    
    TreePath path = getNewLeadSelectionPath();
    if (path != null) selection = path.getLastPathComponent();
  }
  
  
  public Object getSelection() {
    return selection;
  }
  

  public boolean selectionIsSGNode() {
    return (selection instanceof SceneTreeNode);
  }
  
  
  public SceneGraphNode selectionAsSGNode() {
    if (selectionIsSGNode())
      return ((SceneTreeNode) selection).getNode();
    else return null;
  }
  
  
  public boolean selectionIsSGComp() {
    return (selectionAsSGNode() instanceof SceneGraphComponent);
  }
  
  
  public SceneGraphComponent selectionAsSGComp() {
    if (selectionIsSGComp())
      return (SceneGraphComponent) selectionAsSGNode();
    else return null;
  }
  
  
  public boolean selectionIsTool() {
    return (selection instanceof TreeTool);
  }
  
  
  public Tool selectionAsTool() {
    if (selectionIsTool())
      return ((TreeTool) selection).getTool();
    else return null;
  }

  
}
