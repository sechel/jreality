/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.ui.viewerapp;

import java.util.Vector;

import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.proxy.tree.SceneTreeNode;
import de.jreality.ui.treeview.SceneTreeModel;
import de.jreality.util.SceneGraphUtility;


public class SelectionManager {
  
  private SceneGraphPath defaultSelection;
  private SceneGraphPath selection;

  private Vector<SelectionListener> listeners;
  private SelectionListener smListener = null;
  

  public SelectionManager(SceneGraphPath defaultSelection) {
    setDefaultSelection(defaultSelection);
    setSelection(defaultSelection);
  }
  
  
  /**
   * Add communication between the viewerApps navigator and the SelectionManager.
   * @param navigator the navigator
   */
  public void attachNavigator(final Navigator navigator) {
    
    final TreeSelectionModel tsm = navigator.getTreeSelectionModel();
    
    //add listener to Navigator
    final Navigator.SelectionListener navigatorListener = new Navigator.SelectionListener(){
      public void selectionChanged(Navigator.SelectionEvent e) {
        removeSelectionListener(smListener);  //avoid listener cycle
        setSelection(e.getSGPath());
        addSelectionListener(smListener);
      }
    };
    tsm.addTreeSelectionListener(navigatorListener);
    
    //add listener to SelectionManager
    smListener = new SelectionListener() {
      public void selectionChanged(SelectionEvent e) {
        //convert selection into TreePath
        TreePath path = getTreePath((SceneTreeModel) navigator.getSceneTree().getModel());
        tsm.removeTreeSelectionListener(navigatorListener);  //avoid listener cycle
        tsm.setSelectionPath(path);
        tsm.addTreeSelectionListener(navigatorListener);
      }
    };
    addSelectionListener(smListener);
    
    setSelection(defaultSelection);
  }
  
  
  /**
   * Remove communication between the viewerApps navigator and the SelectionManager.
   */
  public void detachNavigator() {
    removeSelectionListener(smListener);
  }
  
  
  public SceneGraphPath getDefaultSelection() {
    return defaultSelection;
  }
  
  
  public void setDefaultSelection(SceneGraphPath defaultSelection) {
    this.defaultSelection = defaultSelection;
  }
  
  
  public SceneGraphPath getSelection() {
    return selection;
  }
  
  
  public void setSelection(SceneGraphPath selection) {
    if (selection == null)
      this.selection = defaultSelection;
    else this.selection = selection;
    
    selectionChanged();
  }


  /**
   * converts the path of the current selection into the 
   * corresponding treepath in the scene tree model
   * (model of the viewerApp's navigator)
   */
  private TreePath getTreePath(SceneTreeModel model) {
    Object[] path = selection.toList().toArray();
    Object[] newPath = new Object[path.length];
    newPath[0] = model.getRoot();
    for (int i = 1; i < newPath.length; i++) {
      final int index = SceneGraphUtility.getIndexOfChild(
          (SceneGraphComponent) path[i-1], (SceneGraphComponent) path[i]);
      //get SceneTreeNodes which are no components
      int offset = 0;
      while(!(((SceneTreeNode)model.getChild(newPath[i-1], offset)).getNode() 
          instanceof SceneGraphComponent))
        offset++;
      
      newPath[i] = model.getChild(newPath[i-1], index+offset);
    }
    return new TreePath(newPath);
  }
  
  
  
  public static class SelectionEvent extends java.util.EventObject {
    
    private static final long serialVersionUID = 1L;
    
    private SceneGraphPath selection;
    
    public SelectionEvent(Object source, SceneGraphPath selection) {
      super(source);
      this.selection = selection;
    }
    
    public SceneGraphPath getSelection() {
      return selection;
    }
  }
 
  
  public interface SelectionListener extends java.util.EventListener {
    
    public void selectionChanged(SelectionManager.SelectionEvent e);
  }
  
  
  public void addSelectionListener(SelectionManager.SelectionListener listener)  {
    if (listeners == null)  listeners = new Vector<SelectionListener>();
    if (listeners.contains(listener)) return;
    listeners.add(listener);
  }

  
  public void removeSelectionListener(SelectionManager.SelectionListener listener) {
    if (listeners == null) return;
    listeners.remove(listener);
  }
  
  
  public void selectionChanged() {
    if (listeners == null) return;
    if (!listeners.isEmpty()) {
      for (int i = 0; i<listeners.size(); i++)  {
        SelectionListener l = listeners.get(i);
        l.selectionChanged(new SelectionEvent(this, this.selection));
      }
    }
  }
  
}