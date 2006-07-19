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

import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.beans.InspectorPanel;
import de.jreality.ui.treeview.JTreeRenderer;
import de.jreality.ui.treeview.SceneTreeModel;
import de.jreality.ui.treeview.SelectionEvent;
import de.jreality.ui.treeview.SelectionListener;


public class Navigator {

  private InspectorPanel inspector;
  private JTree sceneTree;
  private TreeSelectionModel tsm;
  private BeanShell beanShell;
  
  private SceneGraphComponent sceneRoot;  //the scene root
  
  
  public Navigator(SceneGraphComponent sceneRoot) {
  
    inspector = new InspectorPanel();

    sceneTree = new JTree();
    SceneTreeModel model = new SceneTreeModel(sceneRoot);
    sceneTree.setModel(model);
    sceneTree.setCellRenderer(new JTreeRenderer());
    
    tsm = sceneTree.getSelectionModel();
    tsm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    
    tsm.addTreeSelectionListener(new SelectionListener(){

      public void selectionChanged(SelectionEvent e) {

        Object selection = null;
        
        if (e.selectionIsSGNode()) selection = e.selectionAsSGNode();
        else if (e.selectionIsTool()) selection = e.selectionAsTool();
        else selection = e.getSelection();  //e.g. shader
        
        inspector.setObject(selection);
        if (beanShell != null) beanShell.setSelf(selection);
      }
    });
    
    tsm.setSelectionPath(model.getPathTo(model.getRoot()));  //select sceneRoot in tree by default
    
    this.sceneRoot = sceneRoot;
  }
  
  
  public InspectorPanel getInspector() {
    return inspector;
  }
  
  
  public JTree getSceneTree() {
    return sceneTree;
  }
  
  
  public TreeSelectionModel getTreeSelectionModel() {
    return tsm;
  }
  

  public void setBeanShell(BeanShell beanShell) {
    this.beanShell = beanShell;
  }
  
  
  public SceneGraphComponent getRoot() {
    return sceneRoot;
  }

}