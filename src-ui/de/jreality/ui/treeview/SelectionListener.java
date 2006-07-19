package de.jreality.ui.treeview;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

public abstract class SelectionListener implements TreeSelectionListener {

  public abstract void selectionChanged(SelectionEvent e);
  
  public void valueChanged(TreeSelectionEvent e) {
    
    boolean[] areNew = new boolean[e.getPaths().length];
    for (int i = 0; i < areNew.length; i++)
      areNew[i] = e.isAddedPath(i);
    
    SelectionEvent se = new SelectionEvent(e.getSource(), e.getPaths(), 
        areNew, e.getOldLeadSelectionPath(), e.getNewLeadSelectionPath()); 
      
    selectionChanged(se);
  }

  
}
